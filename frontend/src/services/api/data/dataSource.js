import axios from 'axios';
export default {
  // 병원 데이터 가져오기
  async fetch() {
    try {
      if (this.tags.length != 0 && this.tags != null) {
        const res = await axios.get('/api/mapData', {  
          params: {
            sub: this.tags[0],
            userLat: this.$store.getters.userLat,
            userLng: this.$store.getters.userLng,
            radius: this.radius,
            tags: this.tags.slice(1).join(','),
          }
        });
        this.hospitalList = res.data;
        if (this.map) {
          this.loadMaker();
        }
      }
    } catch (err) {
      console.error('에러 발생 : ', err);
    }
  },
}
