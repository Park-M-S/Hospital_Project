import axios from 'axios';

let koreanVoice = null;
let currentAudio = null;
let voicesLoaded = false;

// Google Cloud TTS 설정 (Vite 환경변수)
const GOOGLE_TTS_CONFIG = {
  apiKey: import.meta.env.VITE_GOOGLE_TTS_API_KEY,
  apiUrl: 'https://texttospeech.googleapis.com/v1/text:synthesize',
  defaultOptions: {
    languageCode: 'ko-KR',
    voiceName: 'ko-KR-Chirp3-HD-Leda',
    audioEncoding: 'MP3',
    speakingRate: 1.0,
    pitch: 0.0
  }
};

// TTS 설정 옵션
const ttsConfig = {
  useGoogleTts: true,
  fallbackToWebSpeech: true,
  googleTtsOptions: { ...GOOGLE_TTS_CONFIG.defaultOptions },
  webSpeechOptions: {
    rate: 1,
    pitch: 1,
    volume: 1
  }
};

// 개선된 loadVoices 함수 - 더 안정적인 음성 로딩
export function loadVoices() {
  return new Promise((resolve) => {
    if (voicesLoaded) {
      resolve();
      return;
    }

    function setVoices() {
      const voices = window.speechSynthesis.getVoices();
      // console.log('사용 가능한 음성 개수:', voices.length);
      
      if (voices.length > 0) {
        // 한국어 음성 찾기 (우선순위: ko-KR > ko)
        koreanVoice = voices.find(voice => voice.lang === 'ko-KR') || 
                     voices.find(voice => voice.lang.startsWith('ko')) ||
                     voices.find(voice => voice.lang.includes('ko'));
        
        if (koreanVoice) {
          // console.log('선택된 한국어 음성:', koreanVoice.name, koreanVoice.lang);
        } else {
          // console.warn('한국어 음성을 찾지 못했습니다. 기본 음성을 사용합니다.');
          koreanVoice = voices[0]; // 첫 번째 음성을 기본으로 사용
        }
        
        voicesLoaded = true;
        resolve();
      }
    }

    // 즉시 시도
    setVoices();
    
    // 음성이 로드되지 않았다면 이벤트 리스너 설정
    if (!voicesLoaded) {
      let attempts = 0;
      const maxAttempts = 10;
      
      const checkVoices = () => {
        attempts++;
        setVoices();
        
        if (!voicesLoaded && attempts < maxAttempts) {
          setTimeout(checkVoices, 200);
        } else if (!voicesLoaded) {
          // console.warn('음성 로딩 시간 초과. 기본 설정으로 진행합니다.');
          voicesLoaded = true;
          resolve();
        }
      };

      window.speechSynthesis.onvoiceschanged = checkVoices;
      
      // 추가 체크를 위한 타이머
      setTimeout(checkVoices, 100);
    }
  });
}

// Google TTS API 키 확인
function checkGoogleTtsApiKey() {
  const hasKey = !!GOOGLE_TTS_CONFIG.apiKey;
  if (!hasKey) {
    // console.log('Google TTS API 키가 설정되지 않음. Web Speech API 사용.');
  }
  return hasKey;
}

// Google Cloud TTS로 음성 생성
async function speakWithGoogleTts(text, options = {}) {
  try {
    if (!checkGoogleTtsApiKey()) {
      throw new Error('Google TTS API 키가 설정되지 않았습니다.');
    }

    // console.log('Google TTS로 음성 생성 시도:', text);

    const requestBody = {
      input: { text: text },
      voice: {
        languageCode: options.languageCode || ttsConfig.googleTtsOptions.languageCode,
        name: options.voiceName || ttsConfig.googleTtsOptions.voiceName
      },
      audioConfig: {
        audioEncoding: options.audioEncoding || ttsConfig.googleTtsOptions.audioEncoding,
        speakingRate: options.speakingRate || ttsConfig.googleTtsOptions.speakingRate,
        pitch: options.pitch || ttsConfig.googleTtsOptions.pitch
      }
    };

    const response = await axios.post(
      `${GOOGLE_TTS_CONFIG.apiUrl}?key=${GOOGLE_TTS_CONFIG.apiKey}`,
      requestBody,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
    
    const audioData = response.data.audioContent;
    const audioBlob = base64ToBlob(audioData, 'audio/mp3');
    const audioUrl = URL.createObjectURL(audioBlob);
    
    if (currentAudio) {
      currentAudio.pause();
      currentAudio = null;
    }
    
    currentAudio = new Audio(audioUrl);
    
    return new Promise((resolve, reject) => {
      currentAudio.onended = () => {
        URL.revokeObjectURL(audioUrl);
        currentAudio = null;
        // console.log('Google TTS 재생 완료');
        resolve();
      };
      
      currentAudio.onerror = (error) => {
        URL.revokeObjectURL(audioUrl);
        currentAudio = null;
        console.error('Google TTS 재생 오류:', error);
        reject(new Error('오디오 재생 실패'));
      };
      
      currentAudio.play().then(() => {
        // console.log('Google TTS 재생 시작');
      }).catch(reject);
    });
    
  } catch (error) {
    console.error('Google TTS 오류:', error);
    throw error;
  }
}

// 개선된 Web Speech API 함수
async function speakWithWebSpeech(text, options = {}) {
  if (!('speechSynthesis' in window)) {
    throw new Error("이 브라우저는 Web Speech API를 지원하지 않습니다.");
  }

  // console.log('Web Speech API로 음성 생성 시도:', text);

  // 음성 중지
  window.speechSynthesis.cancel();
  
  // 음성 로딩 대기
  await loadVoices();

  const utterance = new SpeechSynthesisUtterance(text);
  
  // 언어 및 음성 설정
  utterance.lang = 'ko-KR';
  if (koreanVoice) {
    utterance.voice = koreanVoice;
    // console.log('사용 음성:', koreanVoice.name);
  } else {
    // console.log('기본 음성 사용');
  }
  
  // 음성 옵션 설정
  utterance.rate = options.rate || ttsConfig.webSpeechOptions.rate;
  utterance.pitch = options.pitch || ttsConfig.webSpeechOptions.pitch;
  utterance.volume = options.volume || ttsConfig.webSpeechOptions.volume;

  return new Promise((resolve, reject) => {
    let hasEnded = false;
    
    utterance.onstart = () => {
      // console.log('Web Speech API 재생 시작');
    };
    
    utterance.onend = () => {
      if (!hasEnded) {
        hasEnded = true;
        // console.log('Web Speech API 재생 완료');
        resolve();
      }
    };
    
    utterance.onerror = (event) => {
      if (!hasEnded) {
        hasEnded = true;
        console.error('Web Speech API 오류:', event.error);
        reject(new Error(`Speech synthesis error: ${event.error}`));
      }
    };

    // 재생 시작 전 잠시 대기 (브라우저 호환성)
    setTimeout(() => {
      try {
        window.speechSynthesis.speak(utterance);
        // console.log('speechSynthesis.speak() 호출됨');
        
        // 재생이 시작되지 않는 경우를 대비한 타임아웃
        setTimeout(() => {
          if (!hasEnded && !window.speechSynthesis.speaking) {
            console.warn('재생이 시작되지 않음. 다시 시도...');
            window.speechSynthesis.cancel();
            window.speechSynthesis.speak(utterance);
          }
        }, 1000);
        
      } catch (error) {
        if (!hasEnded) {
          hasEnded = true;
          reject(error);
        }
      }
    }, 200);

    // 최대 10초 타임아웃
    setTimeout(() => {
      if (!hasEnded) {
        hasEnded = true;
        console.warn('Web Speech API 타임아웃');
        window.speechSynthesis.cancel();
        reject(new Error('Speech synthesis timeout'));
      }
    }, 10000);
  });
}

// 메인 speakText 함수 - 대폭 개선
export async function speakText(text, options = {}) {
  if (!text || !text.trim()) {
    console.warn("읽을 텍스트가 없습니다.");
    return;
  }

  // console.log('TTS 시작:', text);

  // 기존 재생 중지
  stopSpeech();

  // 설정 병합
  const config = { ...ttsConfig, ...options };
  
  try {
    // Google TTS 시도 (API 키가 있는 경우)
    if (config.useGoogleTts && checkGoogleTtsApiKey()) {
      try {
        await speakWithGoogleTts(text, config.googleTtsOptions);
        return;
      } catch (error) {
        console.warn('Google TTS 실패:', error.message);
        
        if (!config.fallbackToWebSpeech) {
          throw error;
        }
      }
    }
    
    // Web Speech API 사용
    await speakWithWebSpeech(text, config.webSpeechOptions);
    
  } catch (error) {
    console.error('TTS 실행 실패:', error);
    
    // 최후의 수단: 매우 간단한 방식
    try {
      // console.log('최후의 fallback 시도...');
      
      await loadVoices();
      
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'ko-KR';
      utterance.rate = 1;
      utterance.pitch = 1;
      utterance.volume = 1;
      
      window.speechSynthesis.cancel();
      
      setTimeout(() => {
        window.speechSynthesis.speak(utterance);
        // console.log('최후 fallback 실행됨');
      }, 300);
      
    } catch (fallbackError) {
      console.error('최종 fallback도 실패:', fallbackError);
      // 사용자에게 알림
      alert('음성 재생에 실패했습니다. 브라우저가 음성 기능을 지원하지 않거나 오류가 발생했습니다.');
    }
  }
}

// 개선된 stopSpeech 함수
export function stopSpeech() {
  // Google TTS 오디오 중지
  if (currentAudio) {
    currentAudio.pause();
    currentAudio.currentTime = 0;
    currentAudio = null;
    // console.log('Google TTS 오디오 중지됨');
  }
  
  // Web Speech API 중지
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel();
    // console.log('Web Speech API 중지됨');
  }
}

// TTS 설정 업데이트
export function updateTtsConfig(newConfig) {
  Object.assign(ttsConfig, newConfig);
}

// 현재 TTS 설정 조회
export function getTtsConfig() {
  return { ...ttsConfig };
}

// Google TTS 음성 목록 조회
export async function getGoogleTtsVoices(languageCode = 'ko-KR') {
  try {
    if (!checkGoogleTtsApiKey()) {
      return [];
    }

    const response = await axios.get(
      `https://texttospeech.googleapis.com/v1/voices?key=${GOOGLE_TTS_CONFIG.apiKey}&languageCode=${languageCode}`,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
    
    return response.data.voices || [];
  } catch (error) {
    console.error('Google TTS 음성 목록 조회 실패:', error);
    return [];
  }
}

// Web Speech API 음성 목록 조회
export async function getWebSpeechVoices() {
  await loadVoices();
  return window.speechSynthesis.getVoices().filter(voice => 
    voice.lang.startsWith('ko') || voice.lang.includes('ko')
  );
}

// TTS 상태 확인
export function getTtsStatus() {
  return {
    isGoogleTtsAvailable: checkGoogleTtsApiKey(),
    isWebSpeechAvailable: 'speechSynthesis' in window,
    isCurrentlyPlaying: !!(currentAudio && !currentAudio.paused) || 
                       ('speechSynthesis' in window && window.speechSynthesis.speaking),
    currentConfig: { ...ttsConfig },
    voicesLoaded: voicesLoaded,
    koreanVoice: koreanVoice ? koreanVoice.name : null
  };
}

// Base64를 Blob으로 변환하는 헬퍼 함수
function base64ToBlob(base64, mimeType) {
  const byteCharacters = atob(base64);
  const byteNumbers = new Array(byteCharacters.length);
  
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  
  const byteArray = new Uint8Array(byteNumbers);
  return new Blob([byteArray], { type: mimeType });
}

// 간단한 사용을 위한 헬퍼 함수들

// Google TTS 활성화/비활성화
export function enableGoogleTts(enable = true) {
  updateTtsConfig({ useGoogleTts: enable });
}

// 음성 속도 설정
export function setSpeakingRate(rate) {
  updateTtsConfig({
    googleTtsOptions: { ...ttsConfig.googleTtsOptions, speakingRate: rate },
    webSpeechOptions: { ...ttsConfig.webSpeechOptions, rate: rate }
  });
}

// 음성 높이 설정
export function setPitch(pitch) {
  updateTtsConfig({
    googleTtsOptions: { ...ttsConfig.googleTtsOptions, pitch: pitch },
    webSpeechOptions: { ...ttsConfig.webSpeechOptions, pitch: pitch / 10 + 1 }
  });
}

// 음성 변경
export function setVoice(voiceName) {
  updateTtsConfig({
    googleTtsOptions: { ...ttsConfig.googleTtsOptions, voiceName: voiceName }
  });
}

// 개선된 초기화 함수
export async function initializeTts() {
  // console.log('TTS 모듈 초기화 중...');
  
  // Google TTS API 키 확인
  const hasApiKey = checkGoogleTtsApiKey();
  
  // Web Speech API 음성 로드
  if ('speechSynthesis' in window) {
    await loadVoices();
    // console.log('Web Speech API 초기화 완료');
  } else {
    console.warn('Web Speech API를 지원하지 않는 브라우저입니다.');
  }
  
  const status = getTtsStatus();
  // console.log('TTS 모듈 초기화 완료');
  // console.log('TTS 상태:', status);
  
  if (!hasApiKey) {
    // console.log('💡 Google TTS를 사용하려면 .env 파일에 VITE_GOOGLE_TTS_API_KEY를 설정하세요.');
  }
  
  return status;
}

// 테스트 함수 추가
export function testTts() {
  // console.log('TTS 테스트 시작...');
  speakText('안녕하세요. TTS 테스트입니다.')
    .then(() => console.log('TTS 테스트 성공'))
    .catch(error => console.error('TTS 테스트 실패:', error));
}

// 자동 초기화
initializeTts();