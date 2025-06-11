<template>

  <div class="container" :class="{ 'blurred': isBlurred }">
    <div class="header">
      <vue3-tags-input :tags="tags" placeholder="진료과를 검색 하세요." @on-tags-changed="handleChangeTag" />
    </div>
    <div class="sidebar">
      <div class="sidebar_container">
        <div>
          <div v-for="(nameItem, i) in name" :key="i" class="hospital_info"
            @click="currentDepartment(nameItem.title)">
            <div class="hr"></div>
            <div class="hospital_flex">
              <div class="hospital_container">
                <div class="hospital_name"> {{ nameItem.title }} </div>
                <div class="hospital_department"> {{ nameItem.content }} </div>
                <div class="hospital_address"> 09 : 00 ~ 16 : 00 </div>
                <div class="hospital_content"></div>
              </div>
              <img class="hospital_img" :src="nameItem.image">
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
      <div class="title">해당되는 증상을 모두 선택해주세요.</div>

      <ul class="check_list">
        <li v-for="item in diagnosis" :key="item.id" @click="toggleSymptom(item)"
          :class="{ selected: isSelected(item) }">

          <i :class="item.image"></i>
          <span>{{ item.content }}</span>
        </li>
      </ul>

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

// package
import Vue3TagsInput from 'vue3-tags-input';

// assets
import hospitalList from '@/assets/hospitalData.js';
import tagButton from '@/assets/tagButton';
import hospitalimg from '@/assets/hospital.png';
import diagnosis from '@/assets/diagnosis.js';
import data from '@/assets/departmentData.js';

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
    }
  },
  mounted() {
    if (this.$store.getters.userLat == null) {
      alert('사용자 위치에 접근할 수 있도록 허용해주시기 바랍니다.')
      setUserLocation(this);
    }
    // this.fetch();

    const sidebarWidth = this.isSideBar ? 'calc(100vw - 60vw)' : 'calc(100vw - 100vw)';
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
      const sidebarWidth = newVal ? 'calc(100vw - 60vw)' : 'calc(100vw - 100vw)';
      document.documentElement.style.setProperty('--sidebar-width', sidebarWidth);
    },
  },
  methods: {
    currentDepartment(department) {
      this.$store.dispatch('updateDepartment', { department: department });
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

<!-- <style>
/* 루트 컨테이너 */
.container {
  filter: blur(0px);
  display: grid;
  grid-template-rows: 100px 1fr;
  grid-template-columns: var(--sidebar-width) 1fr;
  height: 100vh;
  transition: grid-template-columns 0.4s ease-in-out, filter 0.5s ease-in-out !important;
}

/* [추가] 블러 효과를 위한 클래스 */
.container.blurred {
  filter: blur(5px);
  /* 포인터 이벤트를 비활성화하여 블러 상태일 때 하위 요소 클릭 방지 (선택 사항) */
  pointer-events: none; 
}
</style> -->