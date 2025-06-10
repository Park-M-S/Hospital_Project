import axios from 'axios';

let koreanVoice = null;
let currentAudio = null;

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
  useGoogleTts: true, // true: Google TTS 우선, false: Web Speech API 우선
  fallbackToWebSpeech: true, // Google TTS 실패 시 Web Speech API로 fallback
  googleTtsOptions: { ...GOOGLE_TTS_CONFIG.defaultOptions },
  webSpeechOptions: {
    rate: 1,
    pitch: 1,
    volume: 1
  }
};

// 기존 loadVoices 함수 유지
export function loadVoices() {
  return new Promise((resolve) => {
    const voices = window.speechSynthesis.getVoices();
    if (voices.length) {
      koreanVoice = voices.find(voice => voice.lang === 'ko-KR');
      resolve();
    } else {
      window.speechSynthesis.onvoiceschanged = () => {
        const updatedVoices = window.speechSynthesis.getVoices();
        koreanVoice = updatedVoices.find(voice => voice.lang === 'ko-KR');
        resolve();
      };
    }
  });
}

// Google TTS API 키 확인
function checkGoogleTtsApiKey() {
  if (!GOOGLE_TTS_CONFIG.apiKey) {
    console.warn('Google TTS API 키가 설정되지 않았습니다. Web Speech API만 사용됩니다.');
    return false;
  }
  return true;
}

// Google Cloud TTS로 음성 생성
async function speakWithGoogleTts(text, options = {}) {
  try {
    if (!checkGoogleTtsApiKey()) {
      throw new Error('Google TTS API 키가 설정되지 않았습니다.');
    }

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
    
    // Base64 오디오 데이터를 Blob으로 변환
    const audioData = response.data.audioContent;
    const audioBlob = base64ToBlob(audioData, 'audio/mp3');
    const audioUrl = URL.createObjectURL(audioBlob);
    
    // 기존 오디오 정리
    if (currentAudio) {
      currentAudio.pause();
      currentAudio = null;
    }
    
    // 새 오디오 재생
    currentAudio = new Audio(audioUrl);
    
    return new Promise((resolve, reject) => {
      currentAudio.onended = () => {
        URL.revokeObjectURL(audioUrl);
        currentAudio = null;
        resolve();
      };
      
      currentAudio.onerror = (error) => {
        URL.revokeObjectURL(audioUrl);
        currentAudio = null;
        reject(new Error('오디오 재생 실패'));
      };
      
      currentAudio.play().catch(reject);
    });
    
  } catch (error) {
    console.error('Google TTS 오류:', error);
    throw error;
  }
}

// 기존 Web Speech API 함수를 기반으로 개선
async function speakWithWebSpeech(text, options = {}) {
  if (!('speechSynthesis' in window)) {
    throw new Error("이 브라우저는 Web Speech API를 지원하지 않습니다.");
  }

  await loadVoices();

  if (!koreanVoice) {
    console.warn("한국어 음성을 찾지 못했습니다.");
  }

  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = 'ko-KR';
  utterance.voice = koreanVoice;
  utterance.rate = options.rate || ttsConfig.webSpeechOptions.rate;
  utterance.pitch = options.pitch || ttsConfig.webSpeechOptions.pitch;
  utterance.volume = options.volume || ttsConfig.webSpeechOptions.volume;

  window.speechSynthesis.cancel();
  
  return new Promise((resolve, reject) => {
    utterance.onend = resolve;
    utterance.onerror = reject;
    
    setTimeout(() => {
      window.speechSynthesis.speak(utterance);
    }, 100);
  });
}

// 기존 speakText 함수를 확장 (기존 코드와 호환성 유지)
export async function speakText(text, options = {}) {
  if (!text || !text.trim()) {
    console.warn("읽을 텍스트가 없습니다.");
    return;
  }

  // 기존 방식 지원: 단순히 텍스트만 전달하는 경우
  if (typeof options !== 'object') {
    options = {};
  }

  // 설정 병합
  const config = { ...ttsConfig, ...options };
  
  try {
    // Google TTS를 먼저 시도 (설정이 활성화된 경우)
    if (config.useGoogleTts && checkGoogleTtsApiKey()) {
      try {
        await speakWithGoogleTts(text, config.googleTtsOptions);
        return; // 성공하면 종료
      } catch (error) {
        console.warn('Google TTS 실패:', error.message);
        
        // fallback이 비활성화되어 있으면 에러 throw
        if (!config.fallbackToWebSpeech) {
          throw error;
        }
      }
    }
    
    // Web Speech API로 fallback 또는 기본 사용 (기존 방식 유지)
    await speakWithWebSpeech(text, config.webSpeechOptions);
    
  } catch (error) {
    console.error('TTS 실행 실패:', error);
    // 기존 코드와의 호환성을 위해 에러를 throw하지 않고 콘솔에만 출력
    console.warn("음성 재생에 실패했습니다. 기존 방식으로 재시도합니다.");
    
    // 최후의 수단: 기존 방식 그대로 실행
    try {
      await loadVoices();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'ko-KR';
      utterance.voice = koreanVoice;
      utterance.rate = 1;
      utterance.pitch = 1;
      utterance.volume = 1;
      
      window.speechSynthesis.cancel();
      setTimeout(() => {
        window.speechSynthesis.speak(utterance);
      }, 100);
    } catch (fallbackError) {
      console.error('최종 fallback도 실패:', fallbackError);
    }
  }
}

// 기존 stopSpeech 함수 확장
export function stopSpeech() {
  // Google TTS 오디오 중지
  if (currentAudio) {
    currentAudio.pause();
    currentAudio.currentTime = 0;
    currentAudio = null;
  }
  
  // Web Speech API 중지 (기존 방식 유지)
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel();
  }
}

// 새로운 고급 기능들 (기존 코드를 확장하는 선택적 기능)

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
  return window.speechSynthesis.getVoices().filter(voice => voice.lang.startsWith('ko'));
}

// TTS 상태 확인
export function getTtsStatus() {
  return {
    isGoogleTtsAvailable: checkGoogleTtsApiKey(),
    isWebSpeechAvailable: 'speechSynthesis' in window,
    isCurrentlyPlaying: !!(currentAudio && !currentAudio.paused) || window.speechSynthesis.speaking,
    currentConfig: { ...ttsConfig }
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

// 초기화 함수
export async function initializeTts() {
  console.log('TTS 모듈 초기화 중...');
  
  // Google TTS API 키 확인
  const hasApiKey = checkGoogleTtsApiKey();
  
  // Web Speech API 음성 로드
  if ('speechSynthesis' in window) {
    await loadVoices();
  }
  
  console.log('TTS 모듈 초기화 완료');
  console.log('TTS 상태:', getTtsStatus());
  
  if (!hasApiKey) {
    console.log('💡 Google TTS를 사용하려면 .env 파일에 VITE_GOOGLE_TTS_API_KEY를 설정하세요.');
  }
}

// 자동 초기화
initializeTts();