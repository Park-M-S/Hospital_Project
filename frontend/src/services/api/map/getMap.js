import axios from 'axios';
import { speakText, stopSpeech } from '@/services/api/tts/tts.js';

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
    
    // TTS 전역 함수 설정
    this.setupGlobalTTSFunctions();
  },

  // TTS 전역 함수 설정 (오버레이에서 사용하기 위해)
  setupGlobalTTSFunctions() {
    // 기본 TTS 함수를 전역으로 설정
    window.speakText = speakText;
    window.stopSpeech = stopSpeech;
    
    // 응급실 전체 정보 읽기
    window.speakEmergencyInfo = (emergency) => {
      let message = `${emergency.dutyName || '응급실'} 정보를 안내드립니다. `;
      
      if (emergency.emergencyAddress) {
        message += `주소는 ${emergency.emergencyAddress}입니다. `;
      }
      
      const equipment = [];
      if (emergency.hvctayn) equipment.push('CT');
      if (emergency.hvmriayn) equipment.push('MRI');
      if (emergency.hvventiayn) equipment.push('인공호흡기');
      
      if (equipment.length > 0) {
        message += `보유 장비는 ${equipment.join(', ')}입니다. `;
      }
      
      if (emergency.hvec !== undefined) {
        if (emergency.hvec > 0) {
          message += `응급실 일반 병상이 ${emergency.hvec}석 사용 가능합니다. `;
        } else {
          message += `응급실 일반 병상이 부족합니다. `;
        }
      }
      
      if (emergency.hvoc !== undefined) {
        if (emergency.hvoc > 0) {
          message += `응급실 수술실 병상이 ${emergency.hvoc}석 사용 가능합니다. `;
        } else {
          message += `응급실 수술실 병상이 부족합니다. `;
        }
      }
      
      if (emergency.hvgc !== undefined) {
        if (emergency.hvgc > 0) {
          message += `일반 입원실 병상이 ${emergency.hvgc}석 사용 가능합니다. `;
        } else {
          message += `일반 입원실 병상이 부족합니다. `;
        }
      }
      
      if (emergency.dutyTel3) {
        const formattedNumber = emergency.dutyTel3.replace(/-/g, ' ');
        message += `전화번호는 ${formattedNumber}입니다.`;
      }
      
      speakText(message);
    };

    // 개별 항목 읽기 함수들
    window.speakEmergencyAddress = (address) => {
      speakText(`주소: ${address}`);
    };

    window.speakEmergencyBedInfo = (type, count) => {
      const bedTypes = {
        'hvec': '응급실 일반 병상',
        'hvoc': '응급실 수술실 병상', 
        'hvgc': '일반 입원실 병상'
      };
      
      const bedName = bedTypes[type] || '병상';
      const status = count > 0 ? `${count}석 사용 가능` : '부족';
      const message = `${bedName}이 ${status}합니다.`;
      speakText(message);
    };

    window.speakEmergencyPhone = (phoneNumber) => {
      // 전화번호를 자연스럽게 읽기 위해 처리
      let formattedNumber = phoneNumber.replace(/-/g, '');
      
      // 지역번호와 나머지 번호를 분리하여 자연스럽게 읽기
      if (formattedNumber.length === 10 || formattedNumber.length === 11) {
        // 02-1234-5678 형태나 031-123-4567 형태 처리
        if (formattedNumber.startsWith('02')) {
          // 서울 지역번호 (02)
          const areaCode = '공이';
          const middle = formattedNumber.slice(2, -4).split('').join(' ');
          const last = formattedNumber.slice(-4).split('').join(' ');
          formattedNumber = `${areaCode} ${middle} ${last}`;
        } else if (formattedNumber.startsWith('031')) {
          // 경기도 지역번호 (031)
          const areaCode = '공삼일';
          const middle = formattedNumber.slice(3, 6).split('').join(' ');
          const last = formattedNumber.slice(6).split('').join(' ');
          formattedNumber = `${areaCode} ${middle} ${last}`;
        } else if (formattedNumber.length === 11) {
          // 010-1234-5678 형태
          const areaCode = formattedNumber.slice(0, 3).split('').join(' ');
          const middle = formattedNumber.slice(3, 7).split('').join(' ');
          const last = formattedNumber.slice(7).split('').join(' ');
          formattedNumber = `${areaCode} ${middle} ${last}`;
        } else {
          // 기타 지역번호
          const areaCode = formattedNumber.slice(0, 3).split('').join(' ');
          const middle = formattedNumber.slice(3, 6).split('').join(' ');
          const last = formattedNumber.slice(6).split('').join(' ');
          formattedNumber = `${areaCode} ${middle} ${last}`;
        }
      } else {
        // 기본적으로 모든 숫자를 하나씩 읽기
        formattedNumber = phoneNumber.replace(/-/g, '').split('').join(' ');
      }
      
      speakText(`전화번호: ${formattedNumber}`);
    };


    window.speakEmergencyEquipment = (equipment) => {
      speakText(`${equipment} 장비를 보유하고 있습니다.`);
    };

    // 병원 정보 읽기
    window.speakHospitalInfo = (hospital) => {
      let message = `${hospital.hospitalName} 병원 정보입니다. `;
      
      if (hospital.hospitalAddress) {
        message += `주소는 ${hospital.hospitalAddress}입니다. `;
      }
      
      if (hospital.medicalSubject) {
        const subjects = hospital.medicalSubject.split(',').map(s => s.trim()).join(', ');
        message += `진료과목은 ${subjects}입니다. `;
      }
      
      if (hospital.todayOpen && hospital.todayClose) {
        message += `진료시간은 ${hospital.todayOpen}부터 ${hospital.todayClose}까지입니다. `;
      }
      
      if (hospital.hospitalTel) {
        const formattedNumber = hospital.hospitalTel.replace(/-/g, ' ');
        message += `전화번호는 ${formattedNumber}입니다.`;
      }
      
      speakText(message);
    };

    // 약국 정보 읽기
    window.speakPharmacyInfo = (pharmacy) => {
      let message = `${pharmacy.pharmacyName} 약국 정보입니다. `;
      
      if (pharmacy.pharmacyAddress) {
        message += `주소는 ${pharmacy.pharmacyAddress}입니다. `;
      }
      
      if (pharmacy.pharmacyTel) {
        const formattedNumber = pharmacy.pharmacyTel.replace(/-/g, ' ');
        message += `전화번호는 ${formattedNumber}입니다.`;
      }
      
      speakText(message);
    };
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

      if (this.openOverlays[emergencyId]) {
        const existingOverlay = this.openOverlays[emergencyId];
        existingOverlay.setMap(null);
        delete this.openOverlays[emergencyId];
      } else {
        const newOverlay = this.emergencyOverlay(emergency);
        newOverlay.setMap(this.map);
        this.openOverlays[emergencyId] = newOverlay;
      }
    });
    this.markers.push(marker);
  },

  // 응급실 오버레이 (TTS 기능 추가)
  emergencyOverlay(emergency) {
    const emergencyId = emergency.hpid || emergency.dutyName;
    const startY = this.$store.getters.userLat;
    const startX = this.$store.getters.userLng;
    const googleDirectionsUrl = `https://www.google.com/maps/dir/?api=1&origin=${startY},${startX}&destination=${emergency.coordinateY},${emergency.coordinateX}&travelmode=transit`;

    const wrapper = document.createElement('div');
    wrapper.className = 'custom-overlay-wrap';

    const updateTime = emergency.hvidate.replace(/(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/, '$4 : $5 : $6');

    wrapper.innerHTML = `
    <style>
      .tts-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
      }
      .tts-item .content {
        flex: 1;
      }
      .tts-item .mini-speaker {
        flex-shrink: 0;
        margin-left: 8px;
      }
    </style>
    <div class="info-container">
      <div class="info-title-emergency"> 
        <span>${emergency.dutyName}</span>
        <div class="header-controls">
          <button class="tts-main-btn" onclick="speakEmergencyInfo(${JSON.stringify(emergency).replace(/"/g, '&quot;')})" title="전체 정보 듣기">
            <i class="fa-solid fa-volume-up"></i>
          </button>
          <button class="toggle-btn open" title="상세 정보 접기/펼치기">▲</button>
          <div class="close-btn" title="닫기">×</div>
        </div>
      </div>

      <div class="info-body" ondblclick="speakEmergencyInfo(${JSON.stringify(emergency).replace(/"/g, '&quot;')})" title="더블클릭하면 전체 정보를 읽어드립니다"> 
        <div class="update-time">
          <span class="emergency_default">
            <div class="subject-list-container">
              <div class="subject-tags-wrapper">
                ${emergency.hvctayn ? `<span class="subject-tag-emergency" onclick="speakEmergencyEquipment('CT');" style="cursor: pointer;" title="클릭하여 듣기"> CT </span>` : ''}
                ${emergency.hvmriayn ? `<span class="subject-tag-emergency" onclick="speakEmergencyEquipment('MRI');" style="cursor: pointer;" title="클릭하여 듣기"> MRI </span>` : ''}
                ${emergency.hvventiayn ? `<span class="subject-tag-emergency" onclick="speakEmergencyEquipment('인공호흡기');" style="cursor: pointer;" title="클릭하여 듣기"> 인공호흡기 </span>` : ''}
              </div>
            </div>
            업데이트 : ${updateTime}
          </span> 
        </div>

        <div class="collapsible-content open">
          <ul class="details-list">
            ${emergency.emergencyAddress ? `<li class="available emergency-tts-item tts-item" onclick="speakEmergencyAddress('${emergency.emergencyAddress.replace(/'/g, "\\'")}');" style="cursor: pointer;" title="클릭하여 듣기"><span class="content">🏠&nbsp ${emergency.emergencyAddress}</span></li>` : ''}
            ${emergency.hvec !== undefined ? `<li class="${emergency.hvec > 0 ? 'available' : 'unavailable'} emergency-tts-item tts-item" onclick="speakEmergencyBedInfo('hvec', ${emergency.hvec});" style="cursor: pointer;" title="클릭하여 듣기"><span class="content">🚑 &nbsp <span class="${emergency.hvec > 0 ? 'emergency_green' : 'emergency_red'}"> ${emergency.hvec > 0 ? `응급실 일반 병상 : ${emergency.hvec}석` : `응급실 일반 병상 부족 : ${emergency.hvec}석`}</span></span></li>` : ''}
            ${emergency.hvoc !== undefined ? `<li class="${emergency.hvoc > 0 ? 'available' : 'unavailable'} emergency-tts-item tts-item" onclick="speakEmergencyBedInfo('hvoc', ${emergency.hvoc});" style="cursor: pointer;" title="클릭하여 듣기"><span class="content">🩺 &nbsp <span class="${emergency.hvoc > 0 ? 'emergency_green' : 'emergency_red'}"> ${emergency.hvoc > 0 ? `응급실 수술실 병상 : ${emergency.hvoc}석` : `수술실 병상 부족 : ${emergency.hvoc}석`}</span></span></li>` : ''}
            ${emergency.hvgc !== undefined ? `<li class="${emergency.hvgc > 0 ? 'available' : 'unavailable'} emergency-tts-item tts-item" onclick="speakEmergencyBedInfo('hvgc', ${emergency.hvgc});" style="cursor: pointer;" title="클릭하여 듣기"><span class="content">🛏️ &nbsp <span class="${emergency.hvgc > 0 ? 'emergency_green' : 'emergency_red'}"> ${emergency.hvgc > 0 ? `일반 입원실 병상 : ${emergency.hvgc}석` : `입원실 병상 부족 : ${emergency.hvgc}석`}</span></span></li>` : ''}
            ${emergency.dutyTel3 ? `<li class="available emergency-tts-item tts-item" onclick="speakEmergencyPhone('${emergency.dutyTel3}');" style="cursor: pointer;" title="클릭하여 듣기"><span class="content">📞&nbsp 전화번호 : ${emergency.dutyTel3}</span></li>` : ''}
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
      e.stopPropagation();
      collapsibleContent.classList.toggle('open');
      toggleBtn.classList.toggle('open');
      toggleBtn.innerHTML = toggleBtn.classList.contains('open') ? '▲' : '▼';
    });

    wrapper.querySelector('.close-btn').onclick = () => {
      customOverlay.setMap(null);
      delete this.openOverlays[emergencyId];
    };

    return customOverlay;
  },

  // 약국 마커
  createPharmacyMarker(pharmacy) {
    const imageSrc = 'https://i.imgur.com/z4BFIhQ.png';
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

  // 약국 오버레이 (TTS 기능 추가)
  pharmacyOverlay(y, x, name, address, pharmacyTel) {
    const startY = this.$store.getters.userLat;
    const startX = this.$store.getters.userLng;
    const googleDirectionsUrl = `https://www.google.com/maps/dir/?api=1&origin=${startY},${startX}&destination=${y},${x}&travelmode=transit`;

    const wrapper = document.createElement('div');
    wrapper.className = 'custom-overlay-wrap';

    const pharmacyData = { pharmacyName: name, pharmacyAddress: address, pharmacyTel: pharmacyTel };

    wrapper.innerHTML = `
    <style>
      .tts-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
      }
      .tts-item .content {
        flex: 1;
      }
      .tts-item .mini-speaker {
        flex-shrink: 0;
        margin-left: 8px;
      }
    </style>
    <div class="info-container">
      <div class="info-title-pharmacy">
        ${name}
        <button class="tts-main-btn" onclick="speakPharmacyInfo(${JSON.stringify(pharmacyData).replace(/"/g, '&quot;')})" title="약국 정보 듣기">
          <i class="fa-solid fa-volume-up"></i>
        </button>
        <div class="close-btn" title="닫기">×</div>
      </div>
      <div class="info-body" ondblclick="speakPharmacyInfo(${JSON.stringify(pharmacyData).replace(/"/g, '&quot;')})" title="더블클릭하면 전체 정보를 읽어드립니다">
        <div class="address">${address}</div>
        <ul class="details-list">
          ${pharmacyTel != null
        ? `<li class="available emergency-tts-item tts-item" onclick="speakEmergencyPhone('${pharmacyTel}');" style="cursor: pointer;" title="클릭하여 듣기"><span class="content">📞&nbsp 전화번호 : ${pharmacyTel}</span></li>`
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
    const customOverlay = new window.kakao.maps.CustomOverlay({
      map: null,
      position: position,
      content: wrapper,
      yAnchor: 1.12,
      xAnchor: 0.5
    });

    wrapper.querySelector('.close-btn').onclick = () => {
      customOverlay.setMap(null);
    };

    return customOverlay;
  },

  // 병원 마커
  createHospitalMarker(hospital) {
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

  // 병원 오버레이 (TTS 기능 추가)
  loadCustomOverlay(hospitalName, todayOpen, todayClose, hospitalAddress, hospitalTel, doctorNum, professionalDoctors, coordinateX, coordinateY, weekdayLunch, parkingCapacity, parkingFee, medicalSubject) {
    const startY = this.$store.getters.userLat;
    const startX = this.$store.getters.userLng;
    const endY = coordinateY;
    const endX = coordinateX;
    const googleDirectionsUrl = `https://www.google.com/maps/dir/?api=1&origin=${startY},${startX}&destination=${endY},${endX}&travelmode=transit`;

    const wrapper = document.createElement('div');
    wrapper.className = 'custom-overlay-wrap';

    const hospitalData = {
      hospitalName,
      hospitalAddress,
      medicalSubject,
      todayOpen,
      todayClose,
      hospitalTel
    };

    wrapper.innerHTML = `
    <style>
      .tts-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
      }
      .tts-item .content {
        flex: 1;
      }
      .tts-item .mini-speaker {
        flex-shrink: 0;
        margin-left: 8px;
      }
    </style>
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
                 ${medicalSubject.split(',').map(subject => `<span class="subject-tag" onclick="speakText('${subject.trim()} 진료과');" style="cursor: pointer;" title="클릭하여 듣기">${subject.trim()}</span>`).join('')}
               </div>
             </div>
           </div>`
        : ''
      }
        <ul class="details-list">
          ${hospitalAddress != null
        ? `<li class="available emergency-tts-item tts-item" onclick="speakText('주소: ${hospitalAddress.replace(/'/g, "\\'")}');" style="cursor: pointer;" title="클릭하여 듣기"><span class="content">🏠&nbsp ${hospitalAddress}</span></li>`
        : ``
      }
          ${hospitalTel != 0 && hospitalTel != null
        ? `<li class="available emergency-tts-item tts-item" onclick="speakEmergencyPhone('${hospitalTel}');" style="cursor: pointer;" title="클릭하여 듣기"><span class="content">📞&nbsp 전화번호 : ${hospitalTel}</span></li>`
        : `<li class="unavailable">전화번호 없음</li>`
      }
          ${todayOpen != null && todayClose != null
        ? `<li class="available emergency-tts-item tts-item" onclick="speakText('진료시간: ${todayOpen}부터 ${todayClose}까지');" style="cursor: pointer;" title="클릭하여 듣기"><span class="content">🕰️&nbsp 진료시간 : ${todayOpen} ~ ${todayClose}</span></li>`
        : ``
      }
          ${weekdayLunch != 0 && weekdayLunch != null
        ? `<li class="available emergency-tts-item tts-item" onclick="speakText('점심시간: ${weekdayLunch}');" style="cursor: pointer;" title="클릭하여 듣기"><span class="content">🕰️&nbsp 점심시간 : ${weekdayLunch}</span></li>`
        : ``
      }  
          ${parkingCapacity != 0 && parkingCapacity != null
        ? `<li class="available emergency-tts-item tts-item" onclick="speakText('주차 가능: ${parkingCapacity}대, ${parkingFee === 'Y' ? '유료 주차' : '무료 주차'}');" style="cursor: pointer;" title="클릭하여 듣기">
          <span class="content">🚗&nbsp 주차 가능 : ${parkingCapacity}대
          <span> ${parkingFee === 'Y' ? '&nbsp&nbsp유료 주차' : '&nbsp&nbsp무료 주차'} </span>
          </span>
        </li>`
        : `<li class="unavailable">주차장 없음</li>`
      }
          ${doctorNum != 0 && doctorNum != null
        ? `<li class="available emergency-tts-item tts-item" onclick="speakText('일반의: ${doctorNum}명');" style="cursor: pointer;" title="클릭하여 듣기"><span class="content">🧑‍⚕️&nbsp 일반의 : ${doctorNum}명</span></li>`
        : `<li class="unavailable">의사 없음</li>`
      }
          ${professionalDoctors != 0 && professionalDoctors != null
        ? `<li class="available emergency-tts-item tts-item" onclick="speakText('전문의: ${professionalDoctors}명');" style="cursor: pointer;" title="클릭하여 듣기"><span class="content">👩🏽‍⚕️&nbsp 전문의 : ${professionalDoctors}명</span></li>`
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

    wrapper.querySelector('.close-btn').onclick = () => {
      customOverlay.setMap(null);
    };

    return customOverlay;
  },

  // 반경 표시
  loadCircle() {
    var circle = new window.kakao.maps.Circle({
      center: new window.kakao.maps.LatLng(this.$store.getters.userLat, this.$store.getters.userLng),
      radius: this.radius * 1000,
      strokeWeight: 5,
      strokeColor: '#75B8FA',
      strokeOpacity: 1,
      strokeStyle: 'dashed',
    })
    circle.setMap(this.map);
  },

  // 실제 도로 경로 표시 기능
  async showRoute(hospital) {
    if (this.routePolyline) {
      this.routePolyline.setMap(null);
    }

    const userPosition = new window.kakao.maps.LatLng(this.$store.getters.userLat, this.$store.getters.userLng);
    const hospitalPosition = new window.kakao.maps.LatLng(hospital.coordinateY, hospital.coordinateX);

    try {
      const routeData = await this.getDirections(
        this.$store.getters.userLng, this.$store.getters.userLat,
        hospital.coordinateX, hospital.coordinateY
      );

      if (routeData && routeData.routes && routeData.routes.length > 0) {
        const route = routeData.routes[0];
        const path = [];

        route.sections.forEach(section => {
          section.roads.forEach(road => {
            road.vertexes.forEach((vertex, index) => {
              if (index % 2 === 0) {
                const lng = road.vertexes[index];
                const lat = road.vertexes[index + 1];
                if (lng && lat) {
                  path.push(new window.kakao.maps.LatLng(lat, lng));
                }
              }
            });
          });
        });

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

        const distance = (route.summary.distance / 1000).toFixed(1);
        const duration = Math.round(route.summary.duration / 60);
      } else {
        this.showStraightRoute(hospital);
      }
    } catch (error) {
      console.error('경로 검색 오류:', error);
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
          priority: 'RECOMMEND',
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
  },

  // 두 지점 간의 거리 계산 (하버사인 공식)
  calculateDistance(lat1, lng1, lat2, lng2) {
    const R = 6371;
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
