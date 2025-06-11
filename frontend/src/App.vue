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
          <!-- <div class="test_flex">
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
          </div> -->


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
      <div class="options">
        <ul>
          <li v-for="(optionsItem, i) in options" :key="i" :class="{ optionsSelected: isSelectedOptions(optionsItem) }"
            @click="selectedItemId = optionsItem.id; isBottombarOptions = !isBottombarOptions"> {{ optionsItem.title
            }}
          </li>
        </ul>

      </div>

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
      <div v-else class="bottombar_content">
        <ul class="check_list">
          <li v-for="item in diagnosis" :key="item.id" @click="toggleSymptom(item)"
            :class="{ selected: isSelected(item) }">

            <i :class="item.image"></i>
            <span>{{ item.content }}</span>
          </li>
        </ul>
      </div>


      <button class="submit-button" :disabled="selectedSymptoms.length === 0" @click="submitSymptoms">
        증상 선택 완료
      </button>
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

// assets
import hospitalList from '@/assets/hospitalData.js';
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


export default {
  name: 'About',
  components: {
    Vue3TagsInput
  },
  data() {
    return {
      name: data,
      map: null,
      tagList: ['응급실', '전문의', '주차가능', '약국'],
      hospitalList: hospitalList,

      hospitalimg: hospitalimg,
      radius: 1.0,
      tags: [],
      markers: [],
      isSideBar: false,
      isBottomBar: true,
      tagButton: tagButton,

      diagnosis: diagnosis,
      selectedSymptoms: [],
      isBlurred: true,

      isBottombarOptions: false,
      options: options,
      selectedItemId: 1,
    }
  },
  mounted() {
    if (this.$store.getters.userLat == null) {
      alert('사용자 위치에 접근할 수 있도록 허용해주시기 바랍니다.')
      setUserLocation(this);
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
    },
    isSideBar(newVal) {
      const sidebarWidth = newVal ? 'calc(100vw - 70vw)' : 'calc(100vw - 100vw)';
      document.documentElement.style.setProperty('--sidebar-width', sidebarWidth);
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
    toggleDepartmentSelection(departmentTitle) {
      const index = this.selectedSymptoms.indexOf(departmentTitle);
      if (index > -1) {
        // 이미 선택된 항목이면 배열에서 제거 (선택 해제)
        this.selectedSymptoms.splice(index, 1);
      } else {
        // 선택되지 않은 항목이면 배열에 추가 (선택)
        this.selectedSymptoms.push(departmentTitle);
      }
    },

    // 진료과 이름이 selectedSymptoms 배열에 있는지 확인하는 함수 (클래스 바인딩용)
    isDepartmentSelected(departmentTitle) {
      return this.selectedSymptoms.includes(departmentTitle);
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