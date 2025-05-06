<template>
  <div class="root_container">
    <div class="search_container">
      <div class="search_comment_first">의료기관 검색</div>
      <div class="search_comment_second">현재 위치에서 가까운 병원을 찾아보세요</div>
      <div class="search_input">
        <input class="search" placeholder="진료과를 검색 하세요."></input>
        <div class="search_tag"></div>
      </div>
    </div>

    <div class="keyword_container">
      <router-link :to="`about/${nameItem.title}`" v-for="(nameItem, i) in name" :key="i" class="keyword" @click="currentDepartment(nameItem.title)">
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
  </div>
</template>
  
<script>

import axios from 'axios';
import data from '@/assets/departmentData.js';

export default {
  name : 'Home',
  data() {
    return {
      name : data,
      userLat: null,
      userLng: null,
      error: null,
    }
  },
  mounted() {
    this.currentLocation();
  },
  methods : {
    currentLocation() {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            this.userLat = position.coords.latitude;
            this.userLng = position.coords.longitude;
            // alert(`${this.userLat} + ${this.userLng}`);
            this.$store.dispatch('updateLocation', { userLat: 37.44101664410122, userLng: 127.14762419695022 });
            // alert(`${this.$store.getters.userLat} + ${this.$store.getters.userLng}`);
            alert('함수 실행됨');
          },
          (err) => {
            this.error = err.message;
          }
        );
      } else {
        this.error = "Geolocation is not supported by this browser.";
      }
    },

    currentDepartment(department) {
      this.$store.dispatch('updateDepartment', { department: department });
    }

  }
}
</script>

<style>
.root_container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 2vh);
  width: calc(100vw - 40vw);
  margin-top: 1vh;
  margin-bottom: 1vh;    
  background: white;
  border-radius: 30px 30px 0 0;
  box-shadow: rgba(0, 0, 0, 0.34) 2.8px 2.8px 7.7px;
}

/* search section */
.search_container{
  display: flex;
  flex-direction: column;
  gap: 30px;
  border-radius: 30px 30px 0 0;
  padding: 60px;
  background: linear-gradient(87deg, rgba(20,184,166,1) 0%, rgba(16,185,129,1) 100%);
}

.search_comment_first {
  font-size: 60px;
  color: white;
}

.search_comment_second {
  font-size: 30px;
  color: white;
}

.search_input {
  display: flex;
  align-items: center;
  height: 50px;
  padding:10px;
  padding-left: 30px;
  font-size: 30px;
  box-shadow: 0 3px 3px rgba(0,0,0,0.2);
  border-radius: 30px;
  background-color: white;
}

/* keyword section */
.keyword_container {
  flex: 1;
  overflow-y: auto;
  padding: 60px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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
  width: 60px;
  height: 60px;
  border-radius: 10px;
  background: #F2F2F2;
}

.keyword_content {
  color: gray;
  font-size: 15px;
}

.keyword_title {
  flex: 1;
}
</style>