// services/utils/selectDiagnosis.js

export default {
  // 해당 항목이 선택되었는지 확인하는 함수 (클래스 바인딩에 사용)
  isSelected(items) {
    return items.every(item => this.subs.includes(item));
  },


  // '선택 완료' 버튼을 눌렀을 때 실행될 함수 (핵심 로직 수정)
  submitSymptoms() {
    this.isBottomBar = !this.isBottomBar;
    this.isBlurred = !this.isBlurred;
  }
}