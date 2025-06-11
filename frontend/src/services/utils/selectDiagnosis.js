// services/utils/selectDiagnosis.js

export default {
  toggleSymptom(item) {
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
  // '선택 완료' 버튼을 눌렀을 때 실행될 함수 (핵심 로직 수정)
  submitSymptoms() {
    if (this.selectedSymptoms.length === 0) {
      alert("증상을 하나 이상 선택해주세요.");
      return;
    }

    // --- 추천 진료과 계산 로직 시작 (수정됨) ---

    let allDepartments = [];

    // 1. 선택된 증상/진료과 배열을 순회
    this.selectedSymptoms.forEach(selection => {
      // 2. 타입에 따라 분기 처리
      if (typeof selection === 'number') {
        // 타입이 숫자이면 증상 ID로 간주
        const symptomData = this.diagnosis.find(item => item.id === selection);
        if (symptomData && symptomData.departments) {
          allDepartments = allDepartments.concat(symptomData.departments);
        }
      } else if (typeof selection === 'string') {
        // 타입이 문자열이면 진료과 이름으로 간주하고 바로 추가
        allDepartments.push(selection);
      }
    });

    // 3. 중복된 진료과 이름을 제거하여 유니크한 목록 생성
    const uniqueDepartments = [...new Set(allDepartments)];

    // 4. 최종 결과를 사용자에게 보여주기
    if (uniqueDepartments.length > 0) {
      this.isBottomBar = !this.isBottomBar;
      this.handleAddTag(uniqueDepartments.join(', '))
      this.isBlurred = !this.isBlurred;
    } else {
      alert("추천 드릴 진료과 정보를 찾지 못했습니다.");
    }
  }
}