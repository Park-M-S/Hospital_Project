import { speakText, stopSpeech, loadVoices } from '@/services/api/tts/tts.js';

export default {
  toggleSymptom(item, departments) {
    const index = this.selectedSymptoms.indexOf(item.id);
    if (index > -1) {
      // 이미 선택된 항목이면 배열에서 제거 (선택 해제)
      this.selectedSymptoms.splice(index, 1);
    } else {
      // 선택되지 않은 항목이면 배열에 추가 (선택)
      this.selectedSymptoms.push(item.id);
    }
  },
  // 해당 항목이 선택되었는지 확인하는 함수 (클래스 바인딩에 사용)
  isSelected(item) {
    return this.selectedSymptoms.includes(item.id);
  },
  // '선택 완료' 버튼을 눌렀을 때 실행될 함수
  // '선택 완료' 버튼을 눌렀을 때 실행될 함수 (핵심 로직 수정)
  submitSymptoms() {
    if (this.selectedSymptoms.length === 0) {
      alert("증상을 하나 이상 선택해주세요.");
      return;
    }

    // --- 추천 진료과 계산 로직 시작 ---

    // 1. 모든 추천 진료과를 담을 임시 배열 생성
    let allDepartments = [];

    // 2. 선택된 증상 ID들을 하나씩 순회
    this.selectedSymptoms.forEach(symptomId => {
      // 3. ID에 해당하는 증상 데이터를 전체 목록에서 찾기
      const symptomData = this.diagnosis.find(item => item.id === symptomId);

      // 4. 찾은 데이터의 departments 배열을 임시 배열에 추가
      if (symptomData && symptomData.departments) {
        allDepartments = allDepartments.concat(symptomData.departments);
      }
    });

    // 5. 중복된 진료과 이름을 제거하여 유니크한 목록 생성
    // 예: ["내과", "신경과", "정형외과", "신경과"] -> ["내과", "신경과", "정형외과"]
    const uniqueDepartments = [...new Set(allDepartments)];

    // 6. 최종 결과를 사용자에게 보여주기
    if (uniqueDepartments.length > 0) {
      const resultText = `추천 진료과는 ${uniqueDepartments.join(', ')} 입니다.`;
      speakText(resultText);
      alert(resultText);
    } else {
      const failText = "추천 드릴 진료과 정보를 찾지 못했습니다.";
      speakText(failText);
      alert(failText);
    }
  }
}