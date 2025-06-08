<template>
  <div class="header">
    <div class="search_comment_first">의료기관 검색</div>
    <div class="search_comment_second">현재 위치에서 가까운 병원을 찾아보세요</div>
    <vue3-tags-input :tags="tags" placeholder="진료과를 검색 하세요." @on-tags-changed="handleChangeTag" />
  </div>
    <div class="keyword_container">
      <router-link :to="`about/${nameItem.title}`" v-for="(nameItem, i) in name" :key="i" class="keyword"
        @click="currentDepartment(nameItem.title)"
        @mouseenter="speakText(nameItem.title + '입니다 해당 과목에서 담당하는 주요 병명은 ' + nameItem.content + '입니다.')" @mouseleave="stopSpeech">
        <div class="keyword_icon_container">
          <div class="keyword_icon">
            <i :class="nameItem.image"></i>
          </div>
        </div>
        <div class="keyword_info_container">
          <div class="keyword_title">{{ nameItem.title }}</div>
          <div class="keyword_content">{{ nameItem.content }}</div>
        </div>
      </router-link>
    </div>
  </template>

  <script>
import data from '@/assets/departmentData.js';
import { setUserLocation } from '@/services/utils/setUserLocation';
import Vue3TagsInput from 'vue3-tags-input';
import '@/styles/header.css';
import { speakText, stopSpeech, loadVoices } from '@/services/api/tts/tts.js';

export default {
  name: 'Home',
  components: {
    Vue3TagsInput
  },
  data() {
    return {
      name: data,
      tags: [],
    };
  },
  async mounted() {
    await loadVoices();
    if (this.$store.getters.userLat == null) {
      setUserLocation(this);
    }
  },
  methods: {
    currentDepartment(department) {
      this.$store.dispatch('updateDepartment', { department });
    },
    handleChangeTag(tags) {
      this.tags = tags;
    },
    speakText,
    stopSpeech,
  }
};
</script>


  <style scoped>
    .root_container {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      background: white;
      border-radius: 30px 30px 0 0;
      box-shadow: rgba(0, 0, 0, 0.34) 2.8px 2.8px 7.7px;
    }

    /* header_custom */
    .header {
      padding-top: 30px;
      padding-bottom: 30px;
      background: linear-gradient(87deg, rgba(20, 184, 166, 1) 0%, rgba(16, 185, 129, 1) 100%);
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      gap: 60px;
    }

    .search_comment_first {
      font-size: 60px;
      color: white;
    }

    .search_comment_second {
      font-size: 30px;
      color: white;
    }



    /* keyword section */
    .keyword_container {
      padding: 30px;
      margin: auto;
      width: 87%;
      height: 100%;
      overflow-y: auto;
      display: grid;
      justify-content: center;
      align-items: center;
      grid-template-columns: repeat(4, 1fr);
      grid-auto-rows: 100px;
      gap: 40px;
      background: white;
    }


    .keyword {
      display: flex;
      align-items: center;
      padding: 10px;
      cursor: pointer;
      font-size: 30px;
      border-radius: 10px;
      gap: 20px;
      border: 1px solid #F2F2F2;
      box-shadow: rgba(0, 0, 0, 0.24) 0px 3px 8px;
    }

    .keyword:hover {
      background: #F2F2F2;

      .keyword_icon {
        background: #D8D8D8;
      }
    }

    .keyword_info_container {
      display: flex;
      flex-direction: column;
      height: 60px;
    }

    .keyword_icon {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 65px;
      height: 65px;
      border-radius: 10px;
      background: #F2F2F2;
    }

    .keyword_content {
      color: gray;
      font-size: 17px;
    }

    .keyword_title {
      flex: 1;
    }

    /* About 페이지 커스텀 레이아웃 */
    .customoverlay {
      position: relative;
      bottom: 50px;
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
      /* font-weight: bold; */
      overflow: hidden;
      background: #11BF7F;
      background: #11BF7F url(https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/arrow_white.png) no-repeat right 14px center;
    }

    .customoverlay .title {
      display: block;
      text-align: center;
      background: #fff;
      margin-right: 35px;
      padding: 10px 15px;
      font-size: 14px;
      /* font-weight: bold; */
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
