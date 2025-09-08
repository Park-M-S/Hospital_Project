export default {
  // 태그 감지
  handleChangeTag(tags) {
    this.tags = tags;

    const departmentTitles = this.name.map(item => item.title);

    // 진료과만 필터
    this.subs = tags.filter(tag => departmentTitles.includes(tag) &&
      tag !== '주변 약국' &&
      tag !== '응급실');

    // 진료과 외 태그 중에서 '주변 약국'과 '응급실' 제외
    this.subsTag = tags.filter(tag =>
      !departmentTitles.includes(tag) &&
      tag !== '주변 약국' &&
      tag !== '응급실'
    );
  },



  // 태그 추가
  handleAddTag(tag) {
    // 이미 있으면 추가하지 않음
    if (this.tags.includes(tag)) {
      return;
    }

    // '주변 약국' 또는 '응급실' 태그는 조건 없이 바로 추가
    if (tag === '주변 약국' || tag === '응급실') {
      this.tags.push(tag);
      return;
    }

    // 진료과(subs)가 하나도 선택되지 않은 경우 경고
    if (this.subs.length === 0) {
      alert('진료과를 먼저 선택해주세요.');
      return;
    }

    // 일반 태그 추가
    this.tags.push(tag);
    this.subsTag.push(tag);
  }

}