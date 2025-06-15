// config/api.js 또는 utils/api.js 파일 생성
const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? 'https://hospitalmap.duckdns.org'  // 프로덕션
  : 'http://localhost:8888';           // 개발

export default {
  // 기본 병원 데이터 가져오기
  async fetch_default() {
    this.pharmacyList = null;
    try {
      if (this.subs && this.subs.length != 0) {
        const res = await axios.get(`${API_BASE_URL}/hospitalsData`, {
          params: {
            subs: this.subs.join(','),
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
        const res = await axios.get(`${API_BASE_URL}/pharmaciesData`, {
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
      await axios.get(`${API_BASE_URL}/api/emergency/start`);
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
        await axios.get(`${API_BASE_URL}/api/emergency/stop`);
        this.emergencyList = [];
      }
    } catch (err) {
      console.error('에러 발생 : ', err);
    }
  }
}
