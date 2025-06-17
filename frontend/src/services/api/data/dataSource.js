import axios from 'axios';

export default {
  // 기본 병원 데이터 가져오기
  async fetch_default() {
    try {
      // subs 조건은 호출하는 쪽(App.vue)에서 이미 확인하므로 여기서 중복 확인할 필요가 없습니다.
      const res = await axios.get('http://localhost:8889/hospital_main/hospitalsData', {
        params: {
          subs: this.subs.join(','),
          userLat: this.$store.getters.userLat,
          userLng: this.$store.getters.userLng,
          radius: this.radius,
          tags: this.subsTag.join(','),
        }
      });
      return res.data; // 데이터를 '반환'합니다.
    } catch (err) {
      console.error('병원 데이터 로딩 에러 : ', err);
      return []; // 에러 발생 시 빈 배열을 반환합니다.
    }
  },

  // 약국 데이터 가져오기
  async fetch_pharmacy() {
    try {
      const res = await axios.get('http://localhost:8889/hospital_main/pharmaciesData', {
        params: {
          userLat: this.$store.getters.userLat,
          userLng: this.$store.getters.userLng,
          radius: this.radius,
        }
      });
      return res.data; // 데이터를 '반환'합니다.
    } catch (err) {
      console.error('약국 데이터 로딩 에러 : ', err);
      return []; // 에러 발생 시 빈 배열을 반환합니다.
    }
  },

  // 응급실 실시간 데이터 시작
  async fetch_emergency() {
    try {

      if (!this.socket || this.socket.readyState === WebSocket.CLOSED) {
        // console.log('웹소켓 연결 시도');
        this.socket = new WebSocket('ws://localhost:8889/hospital_main/emergency-websocket');

        /* 연결 이벤트는 처음 한 번만 등록 */
        this.socket.addEventListener('open', () => console.log('WebSocket 연결됨'));
        this.socket.addEventListener('close', () => console.log('WebSocket 연결 종료됨'));
        this.socket.addEventListener('error', (e) => console.error('WebSocket 에러:', e));

      }

      this.socket.onmessage = (event) => {
        const receivedData = JSON.parse(event.data);
        if (receivedData.body && receivedData.body.items) {
          // console.log("웹소켓 초기 데이터 업데이트");
          this.emergencyList = receivedData.body.items.item;
          console.log(this.emergencyList);
          return;
        } else if (Array.isArray(receivedData)) {
          // console.log("웹소켓 실시간 업데이트");

          // 이전 데이터와 비교하여 변경사항이 있는지 확인
          const hasChanges = JSON.stringify(this.emergencyList) !== JSON.stringify(receivedData);

          if (hasChanges) {
            this.emergencyList = receivedData;
            // console.log("응급실 데이터 업데이트됨");
            console.log(this.emergencyList);
          }
        } else {
          console.error("웹소켓 알 수 없는 형식의 데이터 수신:", receivedData);
        }
      };

    } catch (err) {
      console.error('에러 발생 : ', err);
    }
  },
}