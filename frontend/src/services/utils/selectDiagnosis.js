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
    return this.subs.includes(item.id);
  },
  // '선택 완료' 버튼을 눌렀을 때 실행될 함수 (핵심 로직 수정)
  submitSymptoms() {
    this.isBottomBar = !this.isBottomBar;
    this.isBlurred = !this.isBlurred;
  }
}