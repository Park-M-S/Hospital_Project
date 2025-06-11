import axios from 'axios';

const KAKAO_MAP_KEY = import.meta.env.VITE_KAKAO_MAP_KEY;
const KAKAO_REST_API_KEY = import.meta.env.VITE_KAKAO_REST_API_KEY;

export default {
  // api 불러오기
  loadScript() {
    const script = document.createElement("script");
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_MAP_KEY}&autoload=false`;
    script.onload = () => window.kakao.maps.load(this.loadMap);

    document.head.appendChild(script);
  },

  // 맵 출력
  loadMap() {
    const container = document.getElementById("map");
    const options = {
      center: new window.kakao.maps.LatLng(this.$store.getters.userLat, this.$store.getters.userLng),
      level: 3
    }

    this.map = new window.kakao.maps.Map(container, options);
    this.loadUserMaker();
    this.loadMaker();
    this.loadCircle();
  },

  // 사용자 마커 불러오기 - 커스텀 마커로 변경
  loadUserMaker() {
    const imageSrc = 'https://park-m-s.github.io/Spring-study/사용자위치.png';
    const imageSize = new window.kakao.maps.Size(34, 47);
    const imageOption = { offset: new window.kakao.maps.Point(16, 32) };
    const markerImage = new window.kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);
    const markerPosition = new window.kakao.maps.LatLng(this.$store.getters.userLat, this.$store.getters.userLng);

    const marker = new window.kakao.maps.Marker({
      position: markerPosition,
      title: '현재 위치',
      image: markerImage,
    });

    marker.setMap(this.map);
  },

  // 병원 마커 불러오기 - 병원 전용 커스텀 마커
  loadMaker() {
    this.markers.forEach(marker => marker.setMap(null));
    this.markers = [];

    // 1단계: 일반 병원과 응급실 병원을 분리
    const regularHospitals = this.hospitalList.filter(hospital => hospital.emergency_available !== 'Y');
    const emergencyHospitals = this.hospitalList.filter(hospital => hospital.emergency_available === 'Y');

    // 2단계: 일반 병원 마커들을 먼저 추가 (아래쪽에 표시됨)
    regularHospitals.forEach(hospital => {
      this.createHospitalMarker(hospital, false);
    });

    // 3단계: 응급실 병원 마커를 나중에 추가 (위쪽에 표시됨)
    emergencyHospitals.forEach(hospital => {
      this.createHospitalMarker(hospital, true);
    });
  },

  // 마커 생성 로직을 별도 함수로 분리
  createHospitalMarker(hospital, isEmergency) {
    let imageSrc, imageSize, imageOption;
    
    if (isEmergency) {
      // 응급실 가능한 병원 - 빨간색 십자가 마커
      imageSrc = 'https://park-m-s.github.io/Spring-study/응급실.png';
      imageSize = new window.kakao.maps.Size(34, 47);
      imageOption = { offset: new window.kakao.maps.Point(25, 51) };
    } else {
      // 일반 병원 마커
      imageSrc = 'https://park-m-s.github.io/Spring-study/병원.png';
      imageSize = new window.kakao.maps.Size(34, 47);
      imageOption = { offset: new window.kakao.maps.Point(20, 40) };
    }

    const markerImage = new window.kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);
    const markerPosition = new window.kakao.maps.LatLng(hospital.coordinateY, hospital.coordinateX);

    const marker = new window.kakao.maps.Marker({
      position: markerPosition,
      title: hospital.hospitalName,
      image: markerImage,
    });

    marker.setMap(this.map);

    // 클릭 이벤트 추가
    window.kakao.maps.event.addListener(marker, 'click', () => {
      if (this.activeOverlay) {
        this.activeOverlay.setMap(null);
      }

      const newOverlay = this.loadCustomOverlay(hospital.coordinateY, hospital.coordinateX, hospital.hospitalName, hospital.hospitalAddress, hospital.pro_doc, hospital.emergency_available, hospital.park_available);
      newOverlay.setMap(this.map);
      this.activeOverlay = newOverlay;

      this.showRoute(hospital);
    });

    this.markers.push(marker);
  },

  // 커스텀 오버레이
  loadCustomOverlay(y, x, name, address, prodoc, emergency, parking) {
    // 1. div 요소 생성
    const wrapper = document.createElement('div');
    wrapper.className = 'wrap';

    // 응급실 상태에 따른 스타일 및 텍스트 (추후에 응급실 실시간 여부시 수정필요)
    const emergencyStatus = emergency === 'Y' 
      ? `<div class="emergency-available">🚨 응급실 운영중</div>` 
      : `<div class="emergency-unavailable">응급실 정보 없음</div>`;

    // 2. innerHTML로 내부 HTML 구조 추가
    wrapper.innerHTML = `
    <div class="info">
      <div class="title">
        ${name}
        <div class="close" title="닫기"></div>
      </div>
      <div class="body">
        <div class="img">
          <img src="https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/place_thumb.png" width="73" height="70">
        </div>
        <div class="desc">
          <div class="ellipsis">${address}</div>
          <div class="jibun ellipsis">(우) 63309 (지번) 영평동 2181</div> 
          <div> ${prodoc} </div>
          ${emergencyStatus}
          <div> ${parking} </div>
        </div>
      </div>
    </div>
  `;

    const position = new window.kakao.maps.LatLng(y, x);

    const customOverlay = new window.kakao.maps.CustomOverlay({
      map: null,
      position: position,
      content: wrapper,
      yAnchor: 1,
    });

    // 3. 닫기 버튼 클릭 이벤트 바인딩
    wrapper.querySelector('.close').onclick = () => {
      customOverlay.setMap(null);
    };

    return customOverlay;
  },

  // 닫기 버튼을 눌렀을 때 오버레이 닫기
  closeOverlay() {
    alert('닫기');
    customOverlay.setMap(null);
  },

  // 반경 표시
  loadCircle() {
    var circle = new window.kakao.maps.Circle({
      center: new window.kakao.maps.LatLng(this.$store.getters.userLat, this.$store.getters.userLng),
      radius: this.radius * 1000, // 미터 단위의 원의 반지름 
      strokeWeight: 5, // 선 두께 
      strokeColor: '#75B8FA', // 선 색깔
      strokeOpacity: 1, // 선의 불투명도, 1에서 0 사이의 값이며 0에 가까울수록 투명
      strokeStyle: 'dashed', // 선 스타일
      // fillColor: '#CFE7FF', // 채우기 색깔
      // fillOpacity: 0.1  // 채우기 불투명도   
    })
    circle.setMap(this.map);
  },

  // 실제 도로 경로 표시 기능
  async showRoute(hospital) {
    // 기존 경로가 있다면 제거
    if (this.routePolyline) {
      this.routePolyline.setMap(null);
    }

    const userPosition = new window.kakao.maps.LatLng(this.$store.getters.userLat, this.$store.getters.userLng);
    const hospitalPosition = new window.kakao.maps.LatLng(hospital.coordinateY, hospital.coordinateX);

    try {
      // 카카오 길찾기 API 호출
      const routeData = await this.getDirections(
        this.$store.getters.userLng, this.$store.getters.userLat,
        hospital.coordinateX, hospital.coordinateY
      );

      if (routeData && routeData.routes && routeData.routes.length > 0) {
        // 실제 도로 경로 표시
        const route = routeData.routes[0];
        const path = [];

        // 경로의 모든 좌표를 추출
        route.sections.forEach(section => {
          section.roads.forEach(road => {
            road.vertexes.forEach((vertex, index) => {
              if (index % 2 === 0) { // x, y 좌표 쌍으로 처리
                const lng = road.vertexes[index];
                const lat = road.vertexes[index + 1];
                if (lng && lat) {
                  path.push(new window.kakao.maps.LatLng(lat, lng));
                }
              }
            });
          });
        });

        // 경로를 지도에 표시
        this.routePolyline = new window.kakao.maps.Polyline({
          path: path,
          strokeWeight: 10,
          strokeColor: '#3396ff',
          strokeOpacity: 1,
          strokeStyle: 'solid'
        });

        this.dashedLine = new window.kakao.maps.Polyline({
          path: path,
          strokeWeight: 2,
          strokeColor: '#ffffff',
          strokeOpacity: 1,
          strokeStyle: 'dash'
        });

        this.routePolyline.setMap(this.map);
        this.dashedLine.setMap(this.map);

        // 지도 범위 조정
        const bounds = new window.kakao.maps.LatLngBounds();
        path.forEach(point => bounds.extend(point));
        this.map.setBounds(bounds);

        // 실제 이동 거리와 시간 표시
        const distance = (route.summary.distance / 1000).toFixed(1); // km 변환
        const duration = Math.round(route.summary.duration / 60); // 분 변환

        // alert(`${hospital.hospitalName}까지\n거리: ${distance}km\n예상 시간: ${duration}분`);
      } else {
        // 길찾기 실패 시 직선거리로 표시
        this.showStraightRoute(hospital);
      }
    } catch (error) {
      console.error('경로 검색 오류:', error);
      // 오류 발생 시 직선거리로 표시
      this.showStraightRoute(hospital);
    }
  },

  // 카카오 길찾기 API 호출
  async getDirections(startX, startY, endX, endY) {
    try {
      const response = await axios.get('https://apis-navi.kakaomobility.com/v1/directions', {
        params: {
          origin: `${startX},${startY}`,
          destination: `${endX},${endY}`,
          waypoints: '',
          priority: 'RECOMMEND', // 추천 경로
          car_fuel: 'GASOLINE',
          car_hipass: false,
          alternatives: false,
          road_details: false
        },
        headers: {
          'Authorization': `KakaoAK ${KAKAO_REST_API_KEY}`,
          'Content-Type': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      console.error('길찾기 API 오류:', error);
      throw error;
    }
  },

  // 직선 경로 표시 (백업용)
  showStraightRoute(hospital) {
    const userPosition = new window.kakao.maps.LatLng(this.$store.getters.userLat, this.$store.getters.userLng);
    const hospitalPosition = new window.kakao.maps.LatLng(hospital.coordinateY, hospital.coordinateX);

    const linePath = [userPosition, hospitalPosition];

    this.routePolyline = new window.kakao.maps.Polyline({
      path: linePath,
      strokeWeight: 5,
      strokeColor: '#FF6B6B',
      strokeOpacity: 0.7,
      strokeStyle: 'dashed'
    });

    this.routePolyline.setMap(this.map);

    const bounds = new window.kakao.maps.LatLngBounds();
    bounds.extend(userPosition);
    bounds.extend(hospitalPosition);
    this.map.setBounds(bounds);

    const distance = this.calculateDistance(
      this.$store.getters.userLat,
      this.$store.getters.userLng,
      hospital.coordinateY,
      hospital.coordinateX
    );

    // alert(`${hospital.hospitalName}까지의 직선거리: ${distance.toFixed(2)}km\n(실제 경로를 찾을 수 없어 직선거리로 표시)`);
  },
  // 두 지점 간의 거리 계산 (하버사인 공식)
  calculateDistance(lat1, lng1, lat2, lng2) {
    const R = 6371; // 지구의 반지름 (km)
    const dLat = this.deg2rad(lat2 - lat1);
    const dLng = this.deg2rad(lng2 - lng1);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.deg2rad(lat1)) * Math.cos(this.deg2rad(lat2)) *
      Math.sin(dLng / 2) * Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c;
    return distance;
  },

  deg2rad(deg) {
    return deg * (Math.PI / 180);
  },
}