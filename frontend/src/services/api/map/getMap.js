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
    this.map = null;
    const container = document.getElementById("map");
    const options = {
      center: new window.kakao.maps.LatLng(this.$store.getters.userLat, this.$store.getters.userLng),
      level: 2
    }

    this.map = new window.kakao.maps.Map(container, options);
    this.loadUserMaker();
    this.loadMaker();
    this.loadCircle();
  },

  // 사용자 마커
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

  // 마커 모음
  loadMaker() {
    this.markers.forEach(marker => marker.setMap(null));
    this.markers = [];
    // 응급실 마커
    if (this.emergencyList && this.emergencyList.length) {
      this.emergencyList.forEach(emergency => this.createEmergencyMarker(emergency));
    }

    // 병원 마커
    if (this.hospitalList && this.hospitalList.length) {
      this.hospitalList.forEach(hospital => this.createHospitalMarker(hospital));
    }

    // 약국 마커
    if (this.pharmacyList && this.pharmacyList.length) {
      this.pharmacyList.forEach(pharmacy => this.createPharmacyMarker(pharmacy));
    }


  },

  // 응급실 마커 생성
  createEmergencyMarker(emergency) {
    const imageSrc = 'https://i.imgur.com/xvsaZUe.png'; // 응급실 마커 아이콘
    const imageSize = new window.kakao.maps.Size(34, 47);
    const imageOption = { offset: new window.kakao.maps.Point(20, 40) };
    const markerImage = new window.kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);
    const markerPosition = new window.kakao.maps.LatLng(emergency.coordinateY, emergency.coordinateX);

    const marker = new window.kakao.maps.Marker({
      position: markerPosition,
      title: emergency.dutyName,
      image: markerImage,
    });
    marker.setMap(this.map);

    window.kakao.maps.event.addListener(marker, 'click', () => {
      if (this.activeOverlay) {
        this.activeOverlay.setMap(null);
      }
      const newOverlay = this.emergencyOverlay(emergency); // 객체 전체를 전달
      newOverlay.setMap(this.map);
      this.activeOverlay = newOverlay;
      this.showRoute(emergency);
    });
    this.markers.push(marker);
  },

  // === [수정됨] 응급실 오버레이 ===
  emergencyOverlay(emergency) {
    const startY = this.$store.getters.userLat;
    const startX = this.$store.getters.userLng;
    const googleDirectionsUrl = `https://www.google.com/maps/dir/?api=1&origin=${startY},${startX}&destination=${emergency.coordinateY},${emergency.coordinateX}&travelmode=transit`;

    const wrapper = document.createElement('div');
    wrapper.className = 'custom-overlay-wrap';

    // hvidate (정보 업데이트 시간) 포맷 변경
    const updateTime = emergency.hvidate.replace(/(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/, '$4:$5:$6');

    wrapper.innerHTML = `
      <div class="info-container">
        <div class="info-title-emergency"> ${emergency.dutyName}
          <div class="close-btn" title="닫기">×</div>
        </div>
        <div class="info-body">
          <div class="update-time">${updateTime}</div>
          <ul class="details-list">
            <li class="${emergency.hospitalAddress != null ? 'available' : 'unavailable'}">🏠 ${emergency.hospitalAddress} </li>
            <li class="${emergency.hvec > 0 ? 'available' : 'unavailable'}">🚑 응급실 일반병상 : ${emergency.hvec} 석</li>
            <li class="${emergency.hvoc > 0 ? 'available' : 'unavailable'}">🩺 응글실 수술실 병상 : ${emergency.hvoc} 석</li>
            <li class="${emergency.hvgc > 0 ? 'available' : 'unavailable'}">🛏️ 일반 입원실 병상 : ${emergency.hvgc} 석</li>
            <li class="${emergency.hvctayn === 'Y' ? 'available' : 'unavailable'}">🧠 CT : ${emergency.hvctayn === 'Y' ? '가능' : '불가'}</li>
            <li class="${emergency.hvmriayn === 'Y' ? 'available' : 'unavailable'}">🔬 MRI : ${emergency.hvmriayn === 'Y' ? '가능' : '불가'}</li>
            <li class="${emergency.hvventiayn === 'Y' ? 'available' : 'unavailable'}">😮 인공호흡기 : ${emergency.hvventiayn === 'Y' ? '가능' : '불가'}</li>
            ${emergency.dutyTel3 ? `<li class="available">📞 전화번호 : ${emergency.dutyTel3}</li>` : ''}
          </ul>
        </div>
        <div class="info-footer">
          <a href="${googleDirectionsUrl}" target="_blank" class="emergency-google-btn" rel="noopener noreferrer">
            길찾기
          </a>
        </div>
      </div>
    `;

    const position = new window.kakao.maps.LatLng(emergency.coordinateY, emergency.coordinateX);
    const customOverlay = new window.kakao.maps.CustomOverlay({
      map: null,
      position: position,
      content: wrapper,
      yAnchor: 1.15,
      xAnchor: 0.5
    });

    wrapper.querySelector('.close-btn').onclick = () => customOverlay.setMap(null);
    return customOverlay;
  },

  // 약국 마커
  createPharmacyMarker(pharmacy) {
    const imageSrc = 'https://i.imgur.com/z4BFIhQ.png'; // 약국 아이콘
    const imageSize = new window.kakao.maps.Size(34, 47);
    const imageOption = { offset: new window.kakao.maps.Point(20, 40) };

    const markerImage = new window.kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);
    const markerPosition = new window.kakao.maps.LatLng(pharmacy.coordinateY, pharmacy.coordinateX);

    const marker = new window.kakao.maps.Marker({
      position: markerPosition,
      title: pharmacy.pharmacyName,
      image: markerImage,
    });

    marker.setMap(this.map);

    // 클릭 이벤트 추가
    window.kakao.maps.event.addListener(marker, 'click', () => {
      if (this.activeOverlay) {
        this.activeOverlay.setMap(null);
      }

      const newOverlay = this.pharmacyOverlay(pharmacy.coordinateY, pharmacy.coordinateX, pharmacy.pharmacyName, pharmacy.pharmacyAddress, pharmacy.pharmacyTel);
      newOverlay.setMap(this.map);
      this.activeOverlay = newOverlay;

      this.showRoute(pharmacy);
    });

    this.markers.push(marker);
  },

  // 약국 오버레이
  pharmacyOverlay(y, x, name, address, pharmacyTel) {
    // 출발지: 현재 사용자 위치
    const startY = this.$store.getters.userLat;
    const startX = this.$store.getters.userLng;

    // 도착지: 클릭한 병원
    const endY = y;
    const endX = x;

    // Google Maps 길찾기 URL
    const googleDirectionsUrl = `https://www.google.com/maps/dir/?api=1&origin=${startY},${startX}&destination=${endY},${endX}&travelmode=transit`;

    // wrapper div 요소 생성
    const wrapper = document.createElement('div');
    wrapper.className = 'custom-overlay-wrap';

    // 버튼 HTML
    wrapper.innerHTML = `
    <div class="info-container">
      <div class="info-title-pharmacy">
        ${name}
        <div class="close-btn" title="닫기">×</div>
      </div>
      <div class="info-body">
        <div class="address">${address}</div>
        <ul class="details-list">
          ${pharmacyTel != null
        ? `<li class="available">📞 전화번호 : ${pharmacyTel}</li>`
        : `<li class="unavailable">전화번호 없음</li>`
      }
     
        </ul>
      </div>
      <div class="info-footer">
        <a href="${googleDirectionsUrl}" target="_blank" class="pharmacy-google-btn" rel="noopener noreferrer">
          길찾기
        </a>
      </div>
    </div>
  `;

    const position = new window.kakao.maps.LatLng(y, x);

    // [수정된 부분] 오타 수정: window.kakaomaps -> window.kakao.maps
    const customOverlay = new window.kakao.maps.CustomOverlay({
      map: null,
      position: position,
      content: wrapper,
      yAnchor: 1.15,
      xAnchor: 0.5
    });

    // 닫기 버튼 클릭 이벤트는 그대로 유지
    wrapper.querySelector('.close-btn').onclick = () => {
      customOverlay.setMap(null);
    };

    return customOverlay;
  },


  // 병원 마커
  createHospitalMarker(hospital) {

    // 일반 병원 마커
    const imageSrc = 'https://park-m-s.github.io/Spring-study/병원.png';
    const imageSize = new window.kakao.maps.Size(34, 47);
    const imageOption = { offset: new window.kakao.maps.Point(20, 40) };

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

      const newOverlay = this.loadCustomOverlay(hospital.hospitalName, hospital.todayOpen, hospital.todayClose, hospital.hospitalAddress, hospital.hospitalTel, hospital.doctorNum, hospital.professionalDoctors, hospital.coordinateX, hospital.coordinateY, hospital.weekdayLunch, hospital.parkingCapacity, hospital.parkingFee, hospital.medicalSubject);
      newOverlay.setMap(this.map);
      this.activeOverlay = newOverlay;

      this.showRoute(hospital);
    });

    this.markers.push(marker);
  },

  // 병원 오버레이
  loadCustomOverlay(hospitalName, todayOpen, todayClose, hospitalAddress, hospitalTel, doctorNum, professionalDoctors, coordinateX, coordinateY, weekdayLunch, parkingCapacity, parkingFee, medicalSubject) {

    // 출발지: 현재 사용자 위치

    const startY = this.$store.getters.userLat;

    const startX = this.$store.getters.userLng;


    // 도착지: 클릭한 병원

    const endY = coordinateY;

    const endX = coordinateX;


    // Google Maps 길찾기 URL

    const googleDirectionsUrl = `https://www.google.com/maps/dir/?api=1&origin=${startY},${startX}&destination=${endY},${endX}&travelmode=transit`;


    // wrapper div 요소 생성

    const wrapper = document.createElement('div');

    wrapper.className = 'custom-overlay-wrap';


    // 버튼 HTML

    wrapper.innerHTML = `

    <div class="info-container">

      <div class="info-title">

        ${hospitalName}

        <div class="close-btn" title="닫기">×</div>

      </div>

      <div class="info-body">

      ${medicalSubject != 0 && medicalSubject != null

        ? `<div class="address">
             <div class="subject-list-container">
               <div class="subject-tags-wrapper">
                 ${medicalSubject.split(',').map(subject => `<span class="subject-tag">${subject.trim()}</span>`).join('')}
               </div>
             </div>
           </div>`
        : ''

      }
        <ul class="details-list">

          ${hospitalAddress != null

        ? `<li class="available"> 🏠 ${hospitalAddress}</li>`

        : ``

      }

          ${hospitalTel != 0 && hospitalTel != null

        ? `<li class="available"> 📞 전화번호 : ${hospitalTel}  </li>`

        : `<li class="unavailable">전화번호 없음</li>`

      }


          ${todayOpen != null && todayClose != null

        ? `<li class="available"> 🕰️ 진료시간 : ${todayOpen} ~ ${todayClose}</li>`

        : ``

      }

          ${weekdayLunch != 0 && weekdayLunch != null

        ? `<li class="available">🕰️ 점심시간 : ${weekdayLunch}</li>`

        : ``

      }  

          ${parkingCapacity != 0 && parkingCapacity != null
        ? `<li class="available">
          🚗 주차 가능 : ${parkingCapacity}대
          <span> ${parkingFee === 'Y' ? '&nbsp&nbsp유료 주차' : '&nbsp&nbsp무료 주차'} </span>
        </li>`
        : `<li class="unavailable">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;주차장 없음</li>`
      }

          ${doctorNum != 0 && doctorNum != null

        ? `<li class="available">🧑‍⚕️ 일반의 : ${doctorNum}명</li>`

        : `<li class="unavailable">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;의사 없음</li>`

      }

          ${professionalDoctors != 0 && professionalDoctors != null

        ? `<li class="available">👩🏽‍⚕️ 전문의 : ${professionalDoctors}명</li>`

        : `<li class="unavailable">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;전문의 없음</li>`

      }
        </ul>

      </div>

      <div class="info-footer">

        <a href="${googleDirectionsUrl}" target="_blank" class="google-btn" rel="noopener noreferrer">

          길찾기

        </a>

      </div>

    </div>

  `;


    const position = new window.kakao.maps.LatLng(coordinateY, coordinateX);



    const customOverlay = new window.kakao.maps.CustomOverlay({

      map: null,

      position: position,

      content: wrapper,

      yAnchor: 1.15,

      xAnchor: 0.5

    });


    // 닫기 버튼 클릭 이벤트는 그대로 유지

    wrapper.querySelector('.close-btn').onclick = () => {

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
        // const bounds = new window.kakao.maps.LatLngBounds();
        // path.forEach(point => bounds.extend(point));
        // this.map.setBounds(bounds);

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
      // this.showStraightRoute(hospital);
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