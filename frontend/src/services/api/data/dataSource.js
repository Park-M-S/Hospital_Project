import axios from 'axios';
export default {
  // 기본 병원 데이터 가져오기
  async fetch_default() {
    console.log('일반병원 데이터 집어넣기');
    this.emergencyList = null;
    this.pharmacyList = null;
    try {
      if (this.subs && this.subs.length != 0) {
        const res = await axios.get('https://hospitalmap.duckdns.org/hospitalsData', {
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
    console.log('약국 데이터 집어넣기');
    try {
      if (this.subsTag && this.subsTag.length != 0) {
        const res = await axios.get('https://hospitalmap.duckdns.org/pharmaciesData', {
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
    this.hospitalList = null;
    this.pharmacyList = null;
    console.log('응급실 데이터 집어넣기');
    try {
      if (this.subs && this.subs.length != 0) {
        console.log('start');
        await axios.get('https://hospitalmap.duckdns.org/api/emergency/start');
      }
      if (!this.socket || this.socket.readyState === WebSocket.CLOSED) {
        console.log('웹소켓 연결 시도');
        this.socket = new WebSocket('wss://hospitalmap.duckdns.org/emergency-websocket');
        /* 연결 이벤트는 처음 한 번만 등록 */
        this.socket.addEventListener('open', () => console.log('WebSocket 연결됨'));
        this.socket.addEventListener('close', () => console.log('WebSocket 연결 종료됨'));
        this.socket.addEventListener('error', (e) => console.error('WebSocket 에러:', e));
      }
      this.socket.onmessage = (event) => {
        const receivedData = JSON.parse(event.data);
        if (receivedData.body && receivedData.body.items) {
          console.log("웹소켓 초기 데이터 업데이트");
          this.emergencyList = receivedData.body.items.item;
          console.log(this.emergencyList);
          return;
        } else if (Array.isArray(receivedData)) {
          console.log("웹소켓 실시간 업데이트");
          // 이전 데이터와 비교하여 변경사항이 있는지 확인
          const hasChanges = JSON.stringify(this.emergencyList) !== JSON.stringify(receivedData);
          if (hasChanges) {
            this.emergencyList = receivedData;
            console.log("응급실 데이터 업데이트됨");
            console.log(this.emergencyList);
          }
        } else {
          console.error("웹소켓 알 수 없는 형식의 데이터 수신:", receivedData);
        }
      };
      if (this.map) {
        this.loadMaker();
      }
    } catch (err) {
      console.error('에러 발생 : ', err);
    }
  },
  // 응급실 실시간 데이터 종료
  async fetch_emergency_stop() {
    console.log('응급실 데이터 없애기');
    try {
      // console.log("응급실 실시간 데이터 종료");
      await axios.get('https://hospitalmap.duckdns.org/api/emergency/stop');
      this.emergencyList = [];
      if (this.map) {
        this.loadMaker();
      }
    } catch (err) {
      console.error('에러 발생 : ', err);
    }
  }
}
