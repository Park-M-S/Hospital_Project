<!-- src/App.vue -->
<template>

  <div class="container" :class="{ 'blurred': isBlurred }">
    <div class="header">
      <vue3-tags-input :tags="tags" placeholder="진료과를 검색 하세요." @on-tags-changed="handleChangeTag" />
    </div>
    <div class="sidebar">
      <div class="sidebar_container">
        <div class="hospital_visible" v-if="!hospitalList || hospitalList.length === 0">
          정보가 없습니다.
        </div>
        <div v-else>
          <div class="test_flex">
            <ul class="test">
              <li> <button>구현예정</button> </li>
              <li> <button>구현예정</button> </li>
              <li> <button>구현예정</button> </li>
            </ul>
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
              <div class="hospital_flex">
                <div class="hospital_container">
                  <div class="hospital_name"> {{ hospitals.hospitalName }} </div>
                  <div class="hospital_department"> {{ hospitals.subject }} </div>
                  <div class="hospital_address"> 09 : 00 ~ 16 : 00 </div>
                  <div class="hospital_content"> {{ hospitals.hospitalAddress }} </div>
                </div>
                <img class="hospital_img" :src="hospitalimg">
              </div>
            </div>
          </div>

        </div>
      </div>
      <div class="tag_container">
        <div class="tag" v-for="(tagItem, i) in tagButton" :key="i" @click="handleAddTag(tagItem.title)">
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
          <div v-for="(nameItem, i) in name" :key="i" @click="toggleDepartmentSelection(nameItem.title)">
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
          <li v-for="item in diagnosis" :key="item.id" @click="toggleDepartmentSelection(item.departments)"
            :class="{ selected: isSelected(item) }">

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
import axios from 'axios';

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
      // tagList: ['응급실', '전문의', '주차가능', '약국'],
      hospitalList: [],
      pharmacyList: [],

      hospitalimg: hospitalimg,
      radius: 6.0,

      // 전체 태그
      tags: [],

      // 진료과 태그
      subs: [],

      // 진료과 세부 태그
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
      emergencyList: [],
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

    this.socket = new WebSocket("ws://localhost:8889/hospital_main/emergency-websocket");

    this.socket.onmessage = (event) => {
      // 1. 서버가 보낸 JSON 문자열을 항상 자바스크립트 객체/배열로 먼저 변환합니다.
      const receivedData = JSON.parse(event.data);

      // 2. 데이터의 구조를 확인하여 초기 데이터인지, 업데이트 데이터인지 판별합니다.
      if (receivedData.body && receivedData.body.items) {
        // [판단 근거] 'body'와 'items' 속성이 있다면, 이것은 '초기 데이터'입니다.
        console.log("초기 상세 데이터를 수신했습니다.");

        return

      } else if (Array.isArray(receivedData)) {
        // [판단 근거] 수신된 데이터가 배열이라면, 이것은 '업데이트 데이터'입니다.
        console.log("실시간 업데이트 데이터를 수신했습니다.");

        // 이미 원하는 형태의 배열이므로 그대로 할당하거나, 기존 목록과 비교하여 업데이트합니다.
        // 여기서는 간단히 전체를 교체하는 것으로 가정합니다.
        this.emergencyList = receivedData;

      } else {
        console.error("알 수 없는 형식의 데이터 수신:", receivedData);
      }

      console.log("갱신된 emergencyList:", this.emergencyList);
    };

    // 연결 성공
    this.socket.onopen = () => {
      console.log("WebSocket 연결됨");
    };
    // 연결 종료
    this.socket.onclose = () => {
      console.log("WebSocket 연결 종료됨");
    };
    // 에러 처리
    this.socket.onerror = (error) => {
      console.error("WebSocket 에러:", error);
    };
  },
  beforeDestroy() {
    if (this.socket) {
      this.socket.close(); // 컴포넌트 제거 시 연결 닫기
    }
  },
  watch: {
    tags: {
      handler(tags) {
        // alert('전체 태그 : ' + this.tags + '\n\n' + '진료과 태그: ' + this.subs + '\n\n' + '진료과 세부 태그 : ' + this.subsTag);
        if (this.subs == null || this.subs.length === 0) {
          alert('진료과를 선택해주세요.');
          return;
        } else if (this.subsTag.includes('주변 약국')) {
          this.fetch_pharmacy();
        } else if (this.subsTag.includes('응급실')) {
          this.fetch_emergency_start();
        } else {
          this.fetch_default();
        }
      },
      deep: true
    },
    isSideBar(newVal) {
      const sidebarWidth = newVal ? 'calc(100vw - 70vw)' : 'calc(100vw - 100vw)';
      document.documentElement.style.setProperty('--sidebar-width', sidebarWidth);
    },
    emergencyList(tags) {
      console.log('마커 업데이트');
      // this.loadMaker();
    }
  },
  methods: {
    currentDepartment(department) {
      this.$store.dispatch('updateDepartment', { department: department });
    },
    isSelectedOptions(item) {
      // this.isBottombarOptions = !this.isBottombarOptions;
      return item.id === this.selectedItemId;
    },

    // 진료과 토글 선택
    toggleDepartmentSelection(departmentTitle) {
      // 현재 진료과 태그 리스트에 값이 있다면
      if (this.subs.includes(departmentTitle)) {
        // 리스트에서 해당 과목 제거
        this.subs = this.subs.filter(item => item !== departmentTitle);
        this.tags = this.tags.filter(item => item !== departmentTitle);
      } else {
        // 리스트에 해당 과목 푸쉬
        this.tags.push(departmentTitle);
        this.subs.push(departmentTitle);
      }
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
