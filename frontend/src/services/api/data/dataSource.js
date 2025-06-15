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
      // 기존 WebSocket 연결이 있다면 종료
      if (this.socket && this.socket.readyState !== WebSocket.CLOSED) {
        this.socket.close();
      }

      // ✅ 올바른 WebSocket URL (// 추가됨)
      this.socket = new WebSocket("wss://hospitalmap.duckdns.org/emergency-websocket");
      
      // WebSocket 연결 성공 이벤트
      this.socket.onopen = (event) => {
        console.log('✅ WebSocket 연결 성공!');
      };
      
      // WebSocket 연결 실패 이벤트
      this.socket.onerror = (error) => {
        console.error('❌ WebSocket 연결 오류:', error);
      };
      
      // WebSocket 연결 종료 이벤트
      this.socket.onclose = (event) => {
        console.log('🔴 WebSocket 연결 종료:', event.code, event.reason);
      };

      // 응급실 데이터 시작 API 호출
      if (this.subs && this.subs.length != 0) {
        await axios.get('https://hospitalmap.duckdns.org/api/emergency/start');
      }

      // WebSocket 메시지 수신 처리
      this.socket.onmessage = (event) => {
        try {
          const receivedData = JSON.parse(event.data);
          
          if (receivedData.body && receivedData.body.items) {
            console.log("웹소켓 초기 데이터 수신");
            return;
          } else if (Array.isArray(receivedData)) {
            console.log("웹소켓 실시간 업데이트 수신");
            // 이전 데이터와 비교하여 변경사항이 있는지 확인
            const hasChanges = JSON.stringify(this.emergencyList) !== JSON.stringify(receivedData);
            if (hasChanges) {
              this.emergencyList = receivedData;
              console.log("응급실 데이터 업데이트됨:", receivedData.length, "건");
              if (this.map) {
                this.loadMaker();
              }
            }
          } else {
            console.error("웹소켓 알 수 없는 형식의 데이터 수신:", receivedData);
          }
        } catch (parseError) {
          console.error("웹소켓 데이터 파싱 오류:", parseError, "원본 데이터:", event.data);
        }
      };
      
    } catch (err) {
      console.error('응급실 데이터 시작 오류:', err);
    }
  },

  // 응급실 실시간 데이터 종료
  async fetch_emergency_stop() {
    console.log('응급실 데이터 종료');
    try {
      // WebSocket 연결 종료
      if (this.socket && this.socket.readyState !== WebSocket.CLOSED) {
        this.socket.close();
        this.socket = null;
      }

      // 응급실 데이터 종료 API 호출
      await axios.get('https://hospitalmap.duckdns.org/api/emergency/stop');
      
      this.emergencyList = [];
      if (this.map) {
        this.loadMaker();
      }
      
      console.log('응급실 실시간 데이터 종료 완료');
    } catch (err) {
      console.error('응급실 데이터 종료 오류:', err);
    }
  }
}
