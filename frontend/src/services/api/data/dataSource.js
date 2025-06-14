import axios from 'axios';

export default {
  // 기본 병원 데이터 가져오기
  async fetch_default() {
    this.pharmacyList = null;
    try {
      if (this.subs && this.subs.length != 0) {
        const res = await axios.get('http://56.155.87.205//hospitalsData', {
          params: {
            subs: this.subs.join(','),        // 진료과 전체 전달
            userLat: this.$store.getters.userLat,
            userLng: this.$store.getters.userLng,
            radius: this.radius,
            tags: this.subsTag.join(','),
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

  // 약국 데이터 가져오기
  async fetch_pharmacy() {
    try {
      if (this.subsTag && this.subsTag.length != 0) {
        const res = await axios.get('http://localhost:8889/hospital_main/pharmaciesData', {
          params: {
            userLat: this.$store.getters.userLat,
            userLng: this.$store.getters.userLng,
            radius: this.radius,
          }
        });
        this.pharmacyList = res.data;
        // this.pharmacyList.forEach(p => alert(p.pharmacyName));
        if (this.map) {
          this.loadMaker();
        }
      }
    } catch (err) {
      console.error('에러 발생 : ', err);
    }
  }
}
