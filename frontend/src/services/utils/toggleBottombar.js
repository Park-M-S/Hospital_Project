export default {
  // 토글 사비드바 버튼
  toggle_Bottombar() {
    if (this.tags.length != 0 && this.tags != null || this.isBottomBar == false) {
      this.isBlurred = !this.isBlurred;
      this.isBottomBar = !this.isBottomBar;
    } else {
      alert('과를 선택해주세요.')
    }

  },
}