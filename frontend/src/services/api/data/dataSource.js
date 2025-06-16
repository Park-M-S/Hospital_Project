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
    
    // 기존 WebSocket 연결이 있다면 정리
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    
    console.log('웹소켓 연결 시도');
    this.socket = new WebSocket('wss://hospitalmap.duckdns.org/emergency-websocket');
    
    // WebSocket 이벤트 핸들러 설정
    this.socket.onopen = () => {
      console.log('✅ WebSocket 연결 성공!');
    };
    
    this.socket.onclose = (event) => {
      console.log('WebSocket 연결 종료됨', event.code, event.reason);
      // 비정상 종료인 경우 재연결 시도
      if (event.code !== 1000) {
        console.log('3초 후 재연결 시도...');
        setTimeout(() => {
          if (this.emergencyList !== null) { // 응급실 모드가 활성화된 경우에만
            this.fetch_emergency_start();
          }
        }, 3000);
      }
    };
    
    this.socket.onerror = (error) => {
      console.error('❌ WebSocket 에러:', error);
    };
    
    this.socket.onmessage = (event) => {
      try {
        const receivedData = JSON.parse(event.data);
        console.log('📨 WebSocket 데이터 수신:', receivedData);
        
        if (receivedData.body && receivedData.body.items) {
          console.log("웹소켓 초기 데이터 업데이트");
          this.emergencyList = receivedData.body.items.item;
          console.log(this.emergencyList);
        } else if (Array.isArray(receivedData)) {
          console.log("웹소켓 실시간 업데이트");
          // 이전 데이터와 비교하여 변경사항이 있는지 확인
          const hasChanges = JSON.stringify(this.emergencyList) !== JSON.stringify(receivedData);
          if (hasChanges) {
            this.emergencyList = receivedData;
            console.log("응급실 데이터 업데이트됨");
            console.log(this.emergencyList);
            // 지도 업데이트
            if (this.map) {
              this.loadMaker();
            }
          }
        } else {
          console.error("웹소켓 알 수 없는 형식의 데이터 수신:", receivedData);
        }
      } catch (parseError) {
        console.error('WebSocket 메시지 파싱 에러:', parseError);
      }
    };
    
    if (this.map) {
      this.loadMaker();
    }
    
  } catch (err) {
    console.error('응급실 데이터 시작 에러:', err);
  }
},

// 응급실 실시간 데이터 종료
async fetch_emergency_stop() {
  console.log('응급실 데이터 없애기');
  try {
    // WebSocket 연결 정리
    if (this.socket) {
      this.socket.close(1000, 'User stopped emergency mode'); // 정상 종료 코드
      this.socket = null;
    }
    
    await axios.get('https://hospitalmap.duckdns.org/api/emergency/stop');
    this.emergencyList = [];
    
    if (this.map) {
      this.loadMaker();
    }
  } catch (err) {
    console.error('에러 발생 : ', err);
  }
}
