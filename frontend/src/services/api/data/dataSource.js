import axios from 'axios';

export default {
  // 기본 병원 데이터 가져오기
  async fetch_default() {
    this.pharmacyList = null;
    try {
      if (this.subs && this.subs.length != 0) {
        const res = await axios.get('http://localhost:8888/hospitalsData', {
          params: {
            subs: this.subs.join(','),
            userLat: this.$store.getters.userLat,
            userLng: this.$store.getters.userLng,
            radius: this.radius,
            tags: this.subsTag.join(','),
          }
        });
        this.hospitalList = res.data;
        // console.log(this.hospitalList);
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
        if (this.map) {
          this.loadMaker();
        }
      }
    } catch (err) {
      console.error('에러 발생 : ', err);
    }
  },

  // 응급실 실시간 데이터 시작
  async fetch_emergency_start() {
    try {
      await axios.get('http://localhost:8889/hospital_main/api/emergency/start');
      if (this.map) {
        this.loadMaker();
      }
    } catch (err) {
      console.error('에러 발생 : ', err);
    }
  },

  // 응급실 실시간 데이터 종료
  async fetch_emergency_stop() {
    try {
      if (this.subsTag && this.subsTag.length != 0) {
        await axios.get('http://localhost:8889/hospital_main/api/emergency/stop');
        this.emergencyList = [];
      }
    } catch (err) {
      console.error('에러 발생 : ', err);
    }
  }
}
