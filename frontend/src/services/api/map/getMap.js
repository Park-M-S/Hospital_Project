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

    const markerTypes = [
      { list: this.emergencyList, method: this.createEmergencyMarker },
      { list: this.hospitalList, method: this.createHospitalMarker },
      { list: this.pharmacyList, method: this.createPharmacyMarker },
    ];

    console.log('emergencyList : ' + this.emergencyList + '\n\n' + 'hospitalList: ' + this.hospitalList + '\n\n' + 'pharmacyList : ' + this.pharmacyList);

    markerTypes.forEach(({ list, method }) => {
      if (list?.length) list.forEach(item => method.call(this, item));
    });
  },


  // 응급실 마커 생성
  createEmergencyMarker(emergency) {
    const imageSrc = 'https://i.imgur.com/xvsaZUe.png';
    const imageSize = new window.kakao.maps.Size(34, 47);
    const imageOption = { offset: new window.kakao.maps.Point(20, 40) };
    const markerImage = new window.kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);
    const markerPosition = new window.kakao.maps.LatLng(emergency.coordinateY, emergency.coordinateX);

    const marker = new window.kakao.maps.Marker({
      position: markerPosition,
      title: emergency.dutyName,
      image: markerImage,
      zIndex: 1,
    });
    marker.setMap(this.map);

    window.kakao.maps.event.addListener(marker, 'click', () => {
      const emergencyId = emergency.hpid || emergency.dutyName;

      // 1. 이미 해당 마커의 오버레이가 열려있는지 확인
      if (this.openOverlays[emergencyId]) {
        // 2. 열려있다면 닫아줍니다 (토글 기능)
        const existingOverlay = this.openOverlays[emergencyId];
        existingOverlay.setMap(null);

        // openOverlays 객체에서도 제거합니다.
        delete this.openOverlays[emergencyId];

      } else {
        // 3. 열려있지 않다면 새로 생성해서 보여줍니다.
        // 기존의 "다른 오버레이 닫는 로직"은 삭제합니다.
        const newOverlay = this.emergencyOverlay(emergency);
        newOverlay.setMap(this.map);

        // 새로 연 오버레이를 openOverlays 객체에 추가하여 관리합니다.
        this.openOverlays[emergencyId] = newOverlay;
      }
    });
    this.markers.push(marker);
  },

  // === [수정됨] 응급실 오버레이 ===
  emergencyOverlay(emergency) {
    const emergencyId = emergency.hpid || emergency.dutyName;
    const startY = this.$store.getters.userLat;
    const startX = this.$store.getters.userLng;
    const googleDirectionsUrl = `https://www.google.com/maps/dir/?api=1&origin=${startY},${startX}&destination=${emergency.coordinateY},${emergency.coordinateX}&travelmode=transit`;

    const wrapper = document.createElement('div');
    wrapper.className = 'custom-overlay-wrap';

    // hvidate (정보 업데이트 시간) 포맷 변경
    const updateTime = emergency.hvidate.replace(/(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/, '$4 : $5 : $6');

    wrapper.innerHTML = `
    <div class="info-container">
      <div class="info-title-emergency"> 
        <span>${emergency.dutyName}</span>
        <div class="header-controls">
          <button class="toggle-btn open" title="상세 정보 접기/펼치기">▲</button>
          <div class="close-btn" title="닫기">×</div>
        </div>
      </div>

      <div class="info-body"> 
        <div class="update-time">
          <span class="emergency_default">
            <div class="subject-list-container">
              <div class="subject-tags-wrapper">
                ${emergency.hvctayn ? `<span class="subject-tag-emergency"> CT </span>` : ''}
                ${emergency.hvmriayn ? `<span class="subject-tag-emergency"> MRI </span>` : ''}
                ${emergency.hvventiayn ? `<span class="subject-tag-emergency"> 인공호흡기 </span>` : ''}
              </div>
            </div>
            업데이트 : ${updateTime}
          </span> 
        </div>

        <div class="collapsible-content open">
          <ul class="details-list">
            ${emergency.emergencyAddress ? `<li class="available">🏠&nbsp ${emergency.emergencyAddress}</li>` : ''}
            ${emergency.hvec > 0 ? `<li class="available">🚑 &nbsp <span class="emergency_green"> 응급실 일반 병상 : ${emergency.hvec}석</span></li>` : `<li class="unavailable">🚑 <span class="emergency_red"> &nbsp 응급실 일반 병상 부족 : ${emergency.hvec}석</span> </li>`}
            ${emergency.hvoc > 0 ? `<li class="available">🩺 &nbsp <span class="emergency_green"> 응급실 수술실 병상 : ${emergency.hvoc}석</span></li>` : `<li class="unavailable">🩺 <span class="emergency_red"> &nbsp 수술실 병상 부족 : ${emergency.hvoc}석</span> </li>`}
            ${emergency.hvgc > 0 ? `<li class="available">🛏️ &nbsp <span class="emergency_green"> 일반 입원실 병상 : ${emergency.hvgc}석</span></li>` : `<li class="unavailable">🛏️ <span class="emergency_red"> &nbsp 입원실 병상 부족 : ${emergency.hvgc}석</span> </li>`}
            ${emergency.dutyTel3 ? `<li class="available">📞&nbsp 전화번호 : ${emergency.dutyTel3}</li>` : ''}
          </ul>
        </div>
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
      yAnchor: 1.12,
      xAnchor: 0.5,
      zIndex: 3,
    });

    const toggleBtn = wrapper.querySelector('.toggle-btn');
    const collapsibleContent = wrapper.querySelector('.collapsible-content');


    toggleBtn.addEventListener('click', (e) => {
      e.stopPropagation(); // 이벤트 버블링 방지
      collapsibleContent.classList.toggle('open');
      toggleBtn.classList.toggle('open');
      toggleBtn.innerHTML = toggleBtn.classList.contains('open') ? '▲' : '▼';
    });

    // 닫기 버튼 클릭 시 activeEmergencyId도 초기화
    wrapper.querySelector('.close-btn').onclick = () => {
      customOverlay.setMap(null); // 오버레이를 지도에서 제거
      // openOverlays 객체에서도 해당 ID의 오버레이를 삭제합니다.
      delete this.openOverlays[emergencyId];
    };

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
        ? `<li class="available">📞&nbsp 전화번호 : ${pharmacyTel}</li>`
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
      yAnchor: 1.12,
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

        ? `<li class="available"> 🏠&nbsp ${hospitalAddress}</li>`

        : ``

      }

          ${hospitalTel != 0 && hospitalTel != null

        ? `<li class="available"> 📞&nbsp 전화번호 : ${hospitalTel}  </li>`

        : `<li class="unavailable">전화번호 없음</li>`

      }


          ${todayOpen != null && todayClose != null

        ? `<li class="available"> 🕰️&nbsp 진료시간 : ${todayOpen} ~ ${todayClose}</li>`

        : ``

      }

          ${weekdayLunch != 0 && weekdayLunch != null

        ? `<li class="available">🕰️&nbsp 점심시간 : ${weekdayLunch}</li>`

        : ``

      }  

          ${parkingCapacity != 0 && parkingCapacity != null
        ? `<li class="available">
          🚗&nbsp 주차 가능 : ${parkingCapacity}대
          <span> ${parkingFee === 'Y' ? '&nbsp&nbsp유료 주차' : '&nbsp&nbsp무료 주차'} </span>
        </li>`
        : `<li class="unavailable">주차장 없음</li>`
      }

          ${doctorNum != 0 && doctorNum != null

        ? `<li class="available">🧑‍⚕️&nbsp 일반의 : ${doctorNum}명</li>`

        : `<li class="unavailable">의사 없음</li>`

      }

          ${professionalDoctors != 0 && professionalDoctors != null

        ? `<li class="available">👩🏽‍⚕️&nbsp 전문의 : ${professionalDoctors}명</li>`

        : `<li class="unavailable">전문의 없음</li>`

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

      yAnchor: 1.12,

      xAnchor: 0.5

    });


    // 닫기 버튼 클릭 이벤트는 그대로 유지

    wrapper.querySelector('.close-btn').onclick = () => {

      customOverlay.setMap(null);

    };


    return customOverlay;

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