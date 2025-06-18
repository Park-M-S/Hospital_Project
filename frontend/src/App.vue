<!-- src/App.vue -->
<template>

  <div class="container" :class="{ 'blurred': isBlurred }">
    <div class="header">
      <vue3-tags-input :tags="tags" placeholder="진료과를 검색 하세요." @on-tags-changed="handleChangeTag" />
      <div v-if="subs.length" class="header_reset">
        <div v-on:click="reset"> <i class="fa-solid fa-trash-can"></i> 초기화 </div>
      </div>
    </div>
    <div class="sidebar">
      <div class="sidebar_container">
        <div class="hospital_visible" v-if="!hospitalList || hospitalList.length === 0">
          정보가 없습니다.
        </div>
        <div v-else>
          <div class="test_flex">
            병원 리스트
            <!-- <ul class="test">
              <li> <button>구현예정</button> </li>
              <li> <button>구현예정</button> </li>
              <li> <button>구현예정</button> </li>
            </ul> -->
            <div class="custom-select">
              <select id="custom-select">
                <option value="">정렬</option>
                <option value="kr">옵션1</option>
                <option value="us">옵션2</option>
                <option value="jp">옵션3</option>
                <option value="cn">옵션4</option>
                <option value="uk">옵션5</option>
              </select>
            </div>
          </div>

          <div class="hospital_visible" v-if="hospitalList">
            <div class="hospital_info" v-for="(hospitals, i) in hospitalList" :key="i">
              <div class="hr"></div>
              <a class="hospital_flex_info" :href="hospitals.hospitalHomepage ?? ''" target="_blank"
                rel="noopener noreferrer">
                <div class="hospital_container">
                  <div class="hospital_name"> {{ hospitals.hospitalName }} </div>
                  <div class="hospital_department"> {{ hospitals.subject }} </div>
                  <div v-if="hospitals.todayOpen" class="hospital_address">
                    {{ hospitals.todayOpen }} ~ {{ hospitals.todayClose || '' }}
                  </div>
                  <div v-else class="hospital_address">
                    진료시간 정보 없음
                  </div>
                  <div v-if="hospitals.hospitalHomepage" class="hospital_address">
                    <a :href="hospitals.hospitalHomepage">{{ hospitals.hospitalHomepage }} </a>
                  </div>
                  <div v-else class="hospital_address">
                    홈페이지 정보 없음
                  </div>
                  <div class="hospital_content"> {{ hospitals.hospitalAddress }} </div>
                </div>
                <div class="hospital_load">
                  <a :href="`https://www.google.com/maps/dir/?api=1&origin=${this.$store.getters.userLat},${this.$store.getters.userLng}&destination=${hospitals.coordinateY},${hospitals.coordinateX}&travelmode=transit`"
                    target="_blank" class="hospital_load-btn" rel="noopener noreferrer" @click="speakTagTitle('해당병원까지의 길찾기 페이지로 이동합니다.')">
                    길찾기
                  </a>
                </div>
                <!-- <img class="hospital_img" :src="hospitalimg"> -->

              </a>
            </div>
          </div>

        </div>
      </div>
      <div class="tag_container">
        <div class="tag" v-for="(tagItem, i) in tagButton" :key="i"
          @click="handleAddTag(tagItem.title); speakTagTitle(tagItem.title + '태그를 클릭하셨습니다.')">
          <i :class="tagItem.image"></i>
          <span> {{ tagItem.title }} </span>
        </div>
      </div>
      <button class="sidebar_button" @click="toggle_Sidebar">
        <div v-if="isSideBar">
          <i class="fa-solid fa-arrow-left"></i>
        </div>
        <div v-else>
          <i class="fa-solid fa-arrow-right"></i>
        </div>
      </button>
    </div>
    <div class="content">
      <div id="map">
      </div>
    </div>
  </div>
  <div class="bottombar" :class="{ active: isBottomBar }">
    <div class="bottombar-header" @click="toggle_Bottombar">
      <div class="handle-bar"></div>
    </div>

    <div class="bottombar_container">
      <span id="bottombar_container_title">진료과를 선택해주세요</span>
      <div class="title">
        <div v-if=isBottombarOptions> 진료과</div>
        <div v-else>
          증상 별 진료과 선택
        </div>

      </div>

      <!-- 옵션 메뉴 -->
      <div class="options">
        <ul>
          <li v-for="(optionsItem, i) in options" :key="i" :class="{ optionsSelected: isSelectedOptions(optionsItem) }"
            @click="selectedItemId = optionsItem.id; isBottombarOptions = !isBottombarOptions"> {{ optionsItem.title
            }}
          </li>
        </ul>
      </div>

      <!-- 진료과 선택 -->
      <div v-if=isBottombarOptions class="bottombar_content">
        <div class="department_info">
          <div v-for="(nameItem, i) in name" :key="i"
            @click="toggleDepartmentSelection(nameItem.title); speakTagTitle(nameItem.title + '를 클릭하셨습니다.')">
            <div class="hr"></div>
            <div class="hospital_flex" :class="{ selected: isDepartmentSelected(nameItem.title) }">
              <div class="hospital_container">
                <div class="department_name"> {{ nameItem.title }} </div>
                <div class="department_content"> {{ nameItem.content }} </div>
              </div>
              <img class="hospital_img" :src="nameItem.image">
            </div>
          </div>
        </div>
      </div>
      <!-- 증상 별 진료과 선택 -->
      <div v-else class="bottombar_content">
        <ul class="check_list">
          <li v-for="item in diagnosis" :key="item.id"
            @click="toggleDepartmentSelection(item.departments); speakTagTitle(item.content)"
            :class="{ selected: isSelected(item.departments) }">

            <i :class="item.image"></i>
            <span>{{ item.content }}</span>
          </li>
        </ul>
      </div>

      <!-- BottomBar-submit-button -->
      <div v-if=isBottombarOptions class="bottombar_submit"> <button class="submit-button" :disabled="subs.length === 0"
          @click="submitSymptoms">
          진료과 선택 완료
        </button> </div>
      <div v-else class="bottombar_submit">
        <button class="submit-button" :disabled="subs.length === 0" @click="submitSymptoms">
          증상 선택 완료
        </button>
      </div>


    </div>
  </div>

</template>

<script>
// css
import '@/styles/header.css';
import '@/styles/sidebar.css';
import '@/styles/content.css';
import '@/styles/bottombar.css';
import '@/styles/default.css'
import '@/styles/font.css'

// package
import Vue3TagsInput from 'vue3-tags-input';
import { speakText } from '@/services/api/tts/tts.js'

// assets
// import hospitalList from '@/assets/hospitalData.js';
import tagButton from '@/assets/tagButton';
import hospitalimg from '@/assets/hospital.png';
import diagnosis from '@/assets/diagnosis.js';
import data from '@/assets/departmentData.js';
import options from './assets/options';

// api
import getMap from '@/services/api/map/getMap.js'
import dataSource from '@/services/api/data/dataSource.js'

// utils
import toggleSidebar from '@/services/utils/toggleSidebar.js';
import toggleBottombar from '@/services/utils/toggleBottombar.js';
import tag from '@/services/utils/tags.js';
import selectDiagnosis from '@/services/utils/selectDiagnosis';
import setUserLocation from './services/utils/setUserLocation';


export default {
  name: 'App',
  components: {
    Vue3TagsInput
  },
  data() {
    return {
      name: data,
      map: null,

      hospitalList: [],
      pharmacyList: [],
      emergencyList: [],

      openOverlays: {},

      hospitalimg: hospitalimg,
      radius: 1.0,


      tags: [],
      subs: [],
      subsTag: [],

      markers: [],
      isSideBar: false,
      isBottomBar: true,
      tagButton: tagButton,

      diagnosis: diagnosis,
      isBlurred: true,

      isBottombarOptions: true,
      options: options,
      selectedItemId: 0,

      socket: null,

    }
  },
  mounted() {
    if (this.$store.getters.userLat == null) {
      alert('사용자 위치에 접근할 수 있도록 허용해주시기 바랍니다.')
      setUserLocation();
    }
    // this.fetch();

    const sidebarWidth = this.isSideBar ? 'calc(100vw - 70vw)' : 'calc(100vw - 100vw)';
    document.documentElement.style.setProperty('--sidebar-width', sidebarWidth);

    if (window.kakao && window.kakao.maps) {
      this.loadMap();
    } else {
      this.loadScript();
    }

  },
  beforeDestroy() {
    if (this.socket) {
      this.socket.close();
    }
    // 오버레이 정리
    if (this.activeOverlay) {
      this.activeOverlay.setMap(null);
    }
  },
  watch: {
    tags: {
      async handler(newTags, oldTags) {
        console.log('tags : ' + this.tags + '\n\n' + 'subs: ' + this.subs + '\n\n' + 'subsTag : ' + this.subsTag);

        // 실행할 Promise들을 미리 정의합니다.
        // 기본값은 빈 배열을 즉시 반환하는 해결된(resolved) Promise입니다.
        let hospitalPromise = Promise.resolve([]);
        let pharmacyPromise = Promise.resolve([]);
        let emergencyPromise = Promise.resolve([]);

        if (this.subs.length) {
          // fetch 함수가 반환하는 Promise 자체를 변수에 할당합니다.
          hospitalPromise = this.fetch_default();
        }

        // --- 주변 약국 데이터 관리 ---
        if (this.tags.includes('주변 약국')) {
          pharmacyPromise = this.fetch_pharmacy();
        }

        // --- 응급실 데이터 관리 (웹소켓은 비동기 대기 없이 즉시 실행) ---
        if (this.tags.includes('응급실')) {
          emergencyPromise = this.fetch_emergency();
        } else {
          if (this.socket && this.socket.readyState === WebSocket.OPEN) {
            this.socket.close();
          }
          this.emergencyList = [];
        }

        try {
          // Promise.all로 두 요청이 모두 끝나기를 기다리고, '반환된' 데이터를 각각 변수에 받습니다.
          const [hospitalData, pharmacyData, emergencyData] = await Promise.all([
            hospitalPromise,
            pharmacyPromise,
            emergencyPromise
          ]);

          // 받아온 데이터로 컴포넌트의 상태를 '직접' 업데이트합니다.
          // 이 시점에 데이터 상태가 확정됩니다.
          this.hospitalList = hospitalData || [];
          this.pharmacyList = pharmacyData || [];
          this.emergencyList = emergencyData || [];

          // 모든 상태가 완벽하게 준비된 후, 마커를 그립니다.
          this.loadMaker();

        } catch (error) {
          console.error("데이터를 가져오는 중 오류 발생:", error);
          // 오류 발생 시 모든 리스트를 비워 안전하게 만듭니다.
          this.hospitalList = [];
          this.pharmacyList = [];
          this.emergencyList = [];
          this.loadMaker();
        }
      },
      deep: true
    },
    isSideBar(newVal) {
      const sidebarWidth = newVal ? 'calc(100vw - 70vw)' : 'calc(100vw - 100vw)';
      document.documentElement.style.setProperty('--sidebar-width', sidebarWidth);
    },
    emergencyList: {
      handler(newList) {
        // 1. this.emergencyList가 null 또는 undefined가 아닌지 확인
        //    그리고 배열인지 확인 (선택적이지만 안전성을 높여줌)
        if (this.emergencyList && Array.isArray(this.emergencyList)) {
          // 2.newList가 유효한 배열인지도 확인 (handler의 인자로 넘어오는 값)
          if (newList && Array.isArray(newList)) {
            // 현재 열려있는 모든 오버레이에 대해 반복 실행
            for (const emergencyId in this.openOverlays) {
              // 최신 목록에서 해당 응급실의 업데이트된 정보를 찾습니다.
              const updatedEmergency = newList.find(e => (e.hpid || e.dutyName) === emergencyId);
              const oldOverlay = this.openOverlays[emergencyId];

              if (oldOverlay) { // oldOverlay가 존재하는지 확인
                if (updatedEmergency) {
                  // 정보가 있다면, 기존 오버레이를 닫고 새 정보로 다시 엽니다.
                  oldOverlay.setMap(null);
                  const newOverlay = this.emergencyOverlay(updatedEmergency);
                  newOverlay.setMap(this.map);
                  // 관리 객체에 새로 만든 오버레이로 교체해줍니다.
                  this.openOverlays[emergencyId] = newOverlay;
                } else {
                  // 최신 목록에 해당 응급실이 없다면, 그냥 닫아버립니다.
                  oldOverlay.setMap(null);
                  delete this.openOverlays[emergencyId];
                }
              }
            }
          }
        }

        // 마커 업데이트 로직은 그대로 유지
        if (this.map) {
          this.loadMaker();
        }
      },
      deep: true
    },
  },

  methods: {
    currentDepartment(department) {
      this.$store.dispatch('updateDepartment', { department: department });
    },
    isSelectedOptions(item) {
      // this.isBottombarOptions = !this.isBottombarOptions;
      return item.id === this.selectedItemId;
    },

    speakTagTitle(title) {
      speakText(title);
    },

    // 진료과 토글 선택
    toggleDepartmentSelection(departmentTitle) {
      // 배열일 경우 (복수 항목)
      if (Array.isArray(departmentTitle)) {
        const isAllIncluded = departmentTitle.every(item => this.subs.includes(item));

        if (isAllIncluded) {
          // 전부 포함되어 있다면 제거
          this.subs = this.subs.filter(item => !departmentTitle.includes(item));
          this.tags = this.tags.filter(item => !departmentTitle.includes(item));
        } else {
          // 일부라도 없으면 추가
          departmentTitle.forEach(item => {
            if (!this.subs.includes(item)) this.subs.push(item);
            if (!this.tags.includes(item)) this.tags.push(item);
          });
        }
      }
      // 문자열일 경우 (단일 항목)
      else {
        if (this.subs.includes(departmentTitle)) {
          this.subs = this.subs.filter(item => item !== departmentTitle);
          this.tags = this.tags.filter(item => item !== departmentTitle);
        } else {
          this.subs.push(departmentTitle);
          this.tags.push(departmentTitle);
        }
      }
    },


    // 검색 태그 삭제
    // 검색 태그 삭제 및 초기화
    reset() {
      this.subs = [];
      this.tags = [];
      this.subsTag = [];
      // this.map = null; // 이 줄을 제거하거나 주석 처리합니다.
      this.loadMaker(); // 이 함수가 마커를 다시 로드합니다.
      // 추가적으로, 모든 오버레이를 닫는 로직이 필요할 수 있습니다.
      for (const emergencyId in this.openOverlays) {
        if (this.openOverlays[emergencyId]) {
          this.openOverlays[emergencyId].setMap(null);
          delete this.openOverlays[emergencyId];
        }
      }
      // 만약 routePolyline이나 dashedLine이 있다면 제거
      if (this.routePolyline) {
        this.routePolyline.setMap(null);
        this.routePolyline = null;
      }
      if (this.dashedLine) {
        this.dashedLine.setMap(null);
        this.dashedLine = null;
      }

      // (선택 사항) 오버레이 겹침 방지 로직을 사용한다면 overlaysToManage 배열도 초기화
      this.overlaysToManage = [];
    },

    // 진료과 이름이 selectedSymptoms 배열에 있는지 확인하는 함수 (클래스 바인딩용)
    isDepartmentSelected(departmentTitle) {
      return this.subs.includes(departmentTitle);
    },
    ...dataSource,
    ...getMap,
    ...toggleSidebar,
    ...toggleBottombar,
    ...tag,
    ...selectDiagnosis,


    // link() {
    //   window.location.href = 'http://www.kidshealth.co.kr/';
    // },
  },
}
</script>

<style>
/* 루트 컨테이너 */
.container {
  filter: blur(0px);
  display: grid;
  grid-template-rows: 100px 1fr;
  grid-template-columns: var(--sidebar-width) 1fr;
  height: 100vh;
  transition: grid-template-columns 0.4s ease-in-out, filter 0.5s ease-in-out;
}

/* [추가] 블러 효과를 위한 클래스 */
.container.blurred {
  filter: blur(5px);
  /* 포인터 이벤트를 비활성화하여 블러 상태일 때 하위 요소 클릭 방지 (선택 사항) */
  pointer-events: none;
}
</style>
