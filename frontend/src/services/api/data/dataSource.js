import axios from 'axios';

export default {
  async fetch_default() {
    this.pharmacyList = null;
    try {
      if (this.subs && this.subs.length != 0) {
        // 필수 파라미터 확인
        const userLat = this.$store.getters.userLat;
        const userLng = this.$store.getters.userLng;
        
        if (!userLat || !userLng) {
          console.error('사용자 위치 정보가 없습니다.');
          return;
        }
        
        const res = await axios.get('/api/hospitalsData', {
          params: {
            subs: this.subs[0],                 // 첫 번째 요소만 전달
            userLat: userLat,
            userLng: userLng,
            radius: this.radius,
            tags: this.subsTag,
          },
          paramsSerializer: function(params) {
            // Spring Boot가 기대하는 형태로 직렬화
            const parts = [];
            for (const key in params) {
              if (Array.isArray(params[key])) {
                params[key].forEach(value => {
                  parts.push(`${key}=${encodeURIComponent(value)}`);
                });
              } else if (params[key] !== null && params[key] !== undefined) {
                parts.push(`${key}=${encodeURIComponent(params[key])}`);
              }
            }
            return parts.join('&');
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
      console.error('요청 설정:', err.config);
    }
  },
  
  // 약국 데이터 가져오기
  async fetch_pharmacy() {
    try {
      const userLat = this.$store.getters.userLat;
      const userLng = this.$store.getters.userLng;
      
      if (!userLat || !userLng) {
        console.error('사용자 위치 정보가 없습니다.');
        return;
      }
      
      const res = await axios.get('/api/pharmaciesData', {
        params: {
          userLat: userLat,
          userLng: userLng,
          radius: this.radius,
        }
      });
      
      console.log('약국 API 응답:', res.data);
      this.pharmacyList = res.data;
      
      if (this.map && Array.isArray(this.pharmacyList)) {
        this.loadMaker();
      }
    } catch (err) {
      console.error('약국 데이터 에러:', err);
    }
  }
}
