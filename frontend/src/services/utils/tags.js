export default {
  // 태그 감지
  handleChangeTag(tags) {
    this.tags = tags;
    
    // 진료과 이름 리스트
    const departmentTitles = this.name.map(item => item.title);  

    // 진료과만 필터
    this.subs = tags.filter(tag => departmentTitles.includes(tag));

    // 진료과 외 나머지 태그만 필터
    this.subsTag = tags.filter(tag => !departmentTitles.includes(tag));
  },


  // 태그 추가
  handleAddTag(tags) {
    // if (this.tags == null || this.tags.length === 0) {
    // 	this.$store.dispatch('updateDepartment', { department: tags });
    // }'
    if (this.tags.includes(tags)) {
      return
    }

    if (this.subs.length == null || this.subs.length === 0) {
      alert('진료과를 선택해주세요.');
      return
    }
    this.tags.push(tags);
    this.subsTag.push(tags);
  },
}