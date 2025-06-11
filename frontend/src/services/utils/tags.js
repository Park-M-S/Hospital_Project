export default {
	// 태그 감지
	handleChangeTag(tags) {
		this.tags = tags;
	},

	// 태그 추가
	handleAddTag(tags) {
		if (this.tags == null || this.tags.length === 0) {
			this.$store.dispatch('updateDepartment', { department: tags });
		}
		if (this.tags.includes(tags)) {
			this.fetch();
		} else {
			this.tags.push(tags);
		}

	},
}