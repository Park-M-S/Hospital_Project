import axios from 'axios';

export default {
  // 기본 병원 데이터 가져오기
  async fetch_default() {
    this.pharmacyList = null;
    try {
      if (this.subs && this.subs.length != 0) {
        const res = await axios.get('/api/hospitalsData', {  // 수정: 상대경로 사용
          params: {
            subs: this.subs,                    // 수정: 배열 그대로 전달
            userLat: this.$store.getters.userLat,
            userLng: this.$store.getters.userLng,
            radius: this.radius,
            tags: this.subsTag,                 // 수정: 배열 그대로 전달
          }
        });
        
        console.log('병원 API 응답:', res.data);
        this.hospitalList = res.data;
        
        if (this.map && Array.isArray(this.hospitalList)) {
          this.loadMaker();
        }
      }
    } catch (err) {
      console.error('병원 데이터 에러:', err);
    }
  },
  
  // 약국 데이터 가져오기
  async fetch_pharmacy() {
    try {
      if (this.subsTag && this.subsTag.length != 0) {
        const res = await axios.get('/api/pharmaciesData', {  // 수정: 상대경로 사용
          params: {
            userLat: this.$store.getters.userLat,
            userLng: this.$store.getters.userLng,
            radius: this.radius,
          }
        });
        
        console.log('약국 API 응답:', res.data);
        this.pharmacyList = res.data;
        
        if (this.map && Array.isArray(this.pharmacyList)) {
          this.loadMaker();
        }
      }
    } catch (err) {
      console.error('약국 데이터 에러:', err);
    }
  }
}
