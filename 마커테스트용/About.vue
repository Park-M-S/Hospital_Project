<template>
  <div class="root_container">
    <div class="search_container">
      <vue3-tags-input :tags="tags" placeholder="진료과를 검색 하세요." @on-tags-changed="handleChangeTag" />
    </div>

    <div id="map"></div>

    <div class="tag_container">
      <div class="tag" v-for="(tagItem, i) in tagList" :key="i" @click="handleAddTag(tagItem)"> {{ tagItem }}</div>
    </div>

    <div class="keyword_container">
      <div class="hospital_visible" v-if="!hospitalList || hospitalList.length === 0">
        정보가 없습니다.
      </div>
      <div v-else>
        <div class="hospital_info" v-for="(hospitals, i) in hospitalList" :key="i">
          <div class="hospital_flex" @click="showRoute(hospitals)">
            <div class="hospital_container">
              <div class="hospital_name"> {{ hospitals.hospitalName }} </div>
              <div class="hospital_department"> {{ hospitals.subject }} </div>
              <div class="hospital_address"> {{ hospitals.hospitalAddress }} </div>
              <div class="hospital_content"> {{ hospitals.hospitalAddress }} </div>
            </div>
            <img class="hospital_img" :src="hospitalimg">
          </div>
          <div class="hr"></div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>

import axios from 'axios';
import hospitalList from '@/assets/hospitalData.js';
import hospitalimg from '@/assets/hospital.png';
import Vue3TagsInput from 'vue3-tags-input';

export default {
  name: 'About',
  components: {
    Vue3TagsInput
  },
  data() {
    return {
      map: null,
      tagList: ['응급실', '전문의', '진료가능', '주차가능'],
      hospitalList: hospitalList,
      hospitalimg: hospitalimg,
      radius: 6.0,
      tags: [this.$store.getters.department],
      markers: [],
      routePolyline: null, // 경로 표시용 폴리라인
    }
  },
  mounted() {
    this.fetch();

    if (window.kakao && window.kakao.maps) {
      this.loadMap();
    } else {
      this.loadScript();
    }
  },
  watch: {
    tags: {
      handler(tags) {
        // 태그 유효성 검사
        if (this.tagList.includes(tags[0])) {
          alert(`진료과를 먼저 선택해주세요.`);
          this.tags.splice(0, 1); // 0 번째 값 제거
          return;
        }

        this.fetch();
      },
      deep: true
    }
  },
  methods: {
    handleChangeTag(tags) {
      this.tags = tags;
    },

    handleAddTag(tags) {
      if(this.tags == null || this.tags.length === 0) {
        this.$store.dispatch('updateDepartment', { department: tags });
      }

      this.tags.push(tags);
    },

    async fetch() {
      try {
        const res = await axios.get('http://localhost:8888/hospital_main/mapData', {
          params: {
            sub: this.tags[0],
            userLat: this.$store.getters.userLat,
            userLng: this.$store.getters.userLng,
            radius: this.radius,
            tags: this.tags.slice(1).join(','),
          }
        });

        this.hospitalList = res.data;
        if (this.map) {
          this.loadMaker();
        }
      } catch (err) {
        console.error('에러 발생 : ', err);
      }
    },

    // api 불러오기 - 경로 서비스도 함께 로드
    loadScript() {
      const script = document.createElement("script");
      script.src = "//dapi.kakao.com/v2/maps/sdk.js?appkey=JS 앱키&autoload=false&libraries=services";
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
      const imageSrc = 'https://park-m-s.github.io/Spring-study/hospital_marker.png';
      const imageSize = new window.kakao.maps.Size(32, 32);
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

      this.hospitalList.forEach(hospital => {
        // 병원 전용 커스텀 마커 이미지
        const imageSrc = 'data:image/svg+xml;base64,' + btoa(`
          <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 40 40">
          <circle cx="20" cy="20" r="18" fill="#FF4444" stroke="#FFFFFF" stroke-width="2"/>
          <rect x="17" y="10" width="6" height="20" fill="white"/>
          <rect x="10" y="17" width="20" height="6" fill="white"/>
          </svg>
        `);
        
        const imageSize = new window.kakao.maps.Size(40, 40);
        const imageOption = { offset: new window.kakao.maps.Point(20, 40) };
        const markerImage = new window.kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);
        const markerPosition = new window.kakao.maps.LatLng(hospital.coordinateY, hospital.coordinateX);

        const marker = new window.kakao.maps.Marker({
          position: markerPosition,
          title: hospital.hospitalName,
          image: markerImage,
        });

        marker.setMap(this.map);
        const customOverlay = this.loadCustomOverlay(hospital.coordinateY, hospital.coordinateX, hospital.hospitalName);

        window.kakao.maps.event.addListener(marker, 'click', () => {
          customOverlay.setMap(this.map);
          this.showRoute(hospital); // 마커 클릭 시에도 경로 표시
        });

        this.markers.push(marker);
      });
    },

    // 커스텀 오버레이
    loadCustomOverlay(y, x, name) {
      var content = '<div class="customoverlay">' +
        '  <a href="http://www.kidshealth.co.kr/" target="_blank">' +
        '    <span class="title">' + name + '</span>' +
        '  </a>' +
        '</div>';

      var position = new window.kakao.maps.LatLng(y, x);
      var customOverlay = new window.kakao.maps.CustomOverlay({
        map: null,
        position: position,
        content: content,
        yAnchor: 1
      });

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
            strokeWeight: 6,
            strokeColor: '#FF4081',
            strokeOpacity: 0.8,
            strokeStyle: 'solid'
          });

          this.routePolyline.setMap(this.map);

          // 지도 범위 조정
          const bounds = new window.kakao.maps.LatLngBounds();
          path.forEach(point => bounds.extend(point));
          this.map.setBounds(bounds);

          // 실제 이동 거리와 시간 표시
          const distance = (route.summary.distance / 1000).toFixed(1); // km 변환
          const duration = Math.round(route.summary.duration / 60); // 분 변환
          
          alert(`${hospital.hospitalName}까지\n거리: ${distance}km\n예상 시간: ${duration}분`);
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
            'Authorization': 'KakaoAK REST API키',
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

      alert(`${hospital.hospitalName}까지의 직선거리: ${distance.toFixed(2)}km\n(실제 경로를 찾을 수 없어 직선거리로 표시)`);
    },

    // 두 지점 간의 거리 계산 (하버사인 공식)
    calculateDistance(lat1, lng1, lat2, lng2) {
      const R = 6371; // 지구의 반지름 (km)
      const dLat = this.deg2rad(lat2 - lat1);
      const dLng = this.deg2rad(lng2 - lng1);
      const a = 
        Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(this.deg2rad(lat1)) * Math.cos(this.deg2rad(lat2)) * 
        Math.sin(dLng/2) * Math.sin(dLng/2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
      const distance = R * c;
      return distance;
    },

    deg2rad(deg) {
      return deg * (Math.PI/180);
    },

    link() {
      window.location.href = 'http://www.kidshealth.co.kr/';
    },
  },
}
</script>

<style scoped>
.search_container {
  padding: 30px 60px;
}

.search_input {
  position: relative;
}

/* .search {
  width: 100%;
} */

.search_tag {
  position: absolute;
  border-radius: 25px;
  padding: 5px 30px;
  cursor: pointer;
  background-color: #11BF7F;
  color: white;
  top: 50%;
  transform: translateY(-50%);
  box-shadow: rgba(50, 50, 93, 0.25) 0px 6px 12px -2px, rgba(0, 0, 0, 0.3) 0px 3px 7px -3px;
}

.search_tag:hover {
  color: white;
  background: #11BF7F;
}

#map {
  height: 40vh;
}

.tag_container {
  padding: 15px 0;
  margin: 0 10px;
  display: flex;
  gap: 15px;
  overflow-x: auto;
  white-space: nowrap;
  /* justify-content: center; */
}

.tag {
  margin-left: 4px;
  display: flex;
  flex: 0 0 auto;
  background: #F5F6F7;
  /* color: white; */
  padding: 10px 20px;
  border-radius: 25px;
  cursor: pointer;
  box-shadow: rgba(50, 50, 93, 0.25) 0px 6px 12px -2px, rgba(0, 0, 0, 0.3) 0px 3px 7px -3px;
  /* box-shadow: rgba(99, 99, 99, 0.2) 0px 2px 8px 0px; */
  /* box-shadow: rgba(0, 0, 0, 0.24) 0px 3px 8px; */
}

.tag:hover {
  color: white;
  background: #11BF7F;
}

.keyword_container {
  padding: 0 10px;
  padding-bottom: 15px;
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  grid-auto-rows: 160px;
  gap: 15px;
  background: white;
}

.hospital_info {
  display: flex;
  flex-direction: column;
}

.hospital_container {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.hospital_name {
  /* color: #298A08; */
  font-size: 20px;
  /* background-color: aqua; */
}

.hospital_department {
  color: gray;
  /* background-color: beige; */
}

.hospital_address {
  color: gray;
  /* background-color: tomato; */
}

/* .hospital_content {
  color: #298A08;
  background-color: yellow;
} */

.hospital_flex {
  display: flex;
  justify-content: space-between;
  cursor: pointer;
}

.hospital_img {
  width: 200px;
  height: 100%;
  border-radius: 10px;
}

.hr {
  width: 100%;
  height: 1px;
  margin-top: auto;
  background-color: #E6E6E6;
}

/* 커스텀 오버레이 스타일 */
.customoverlay {
  position: relative;
  bottom: 85px;
  border-radius: 6px;
  border: 1px solid #ccc;
  border-bottom: 2px solid #ddd;
  float: left;
}

.customoverlay:nth-of-type(n) {
  border: 0;
  box-shadow: 0px 1px 2px #888;
}

.customoverlay a {
  display: block;
  text-decoration: none;
  color: #000;
  text-align: center;
  border-radius: 6px;
  font-size: 14px;
  font-weight: bold;
  overflow: hidden;
  background: #d95050;
  background: #d95050 url(https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/arrow_white.png) no-repeat right 14px center;
}

.customoverlay .title {
  display: block;
  text-align: center;
  background: #fff;
  margin-right: 35px;
  padding: 10px 15px;
  font-size: 14px;
  font-weight: bold;
}

.customoverlay:after {
  content: '';
  position: absolute;
  margin-left: -12px;
  left: 50%;
  bottom: -12px;
  width: 22px;
  height: 12px;
  background: url('https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/vertex_white.png')
}
</style>
