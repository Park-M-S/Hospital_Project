let koreanVoice = null;

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

export async function speakText(text) {
  if (!('speechSynthesis' in window)) {
    console.warn("이 브라우저는 Web Speech API를 지원하지 않습니다.");
    return;
  }
  await loadVoices();

  if (!koreanVoice) {
    console.warn("한국어 음성을 찾지 못했습니다.");
  }

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
}

export function stopSpeech() {
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel();
  }
}
