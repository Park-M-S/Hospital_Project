<template>
  <div class="root_container">
    <div class="search_container">
      <div class="search_input">
        <input class="search"></input>
        <div class="search_tag"> {{ $store.getters.department }} </div>
      </div>
      
    </div>

    <div id="map"></div>

    <div class="tag_container">
        <div class="tag" v-for="(tagItem, i) in tagList" :key="i"> {{ tagItem }}</div>
    </div>

    <div class="keyword_container">
      <div class="hospital_info" v-for="(hospitals, i) in hospitalList" :key="i">
        <div class="hospital_flex" @click="link">
          <div class="hospital_container">
            <div class="hospital_name"> {{ hospitals.hospitalName }} </div>
            <div class="hospital_department"> {{ hospitals.subject }} </div>
            <div class="hospital_address"> {{ hospitals.hospitalAddress }} </div>
            <div class="hospital_content"> {{ hospitals.hospitalAddress }} </div>
          </div>
          <img class="hospital_img" :src="hospitals.img">
        </div>
        <div class="hr"></div>
      </div>
    </div>
  </div>
</template>

<script>

import axios from 'axios';
import hospitalList from '@/assets/hospitalData.js';

export default {
  name : 'About',
  data() {
    return {
      map: null,
      tagList: ['응급실', '전문의', '24시간', '야간진료', '주차 가능', '응급실', '전문의', '24시간', '야간진료', '전문의', '전문의', '전문의', '24시간', '야간진료',],
      hospitalList: hospitalList,
      hospitalId: this.$route.params.title,
      radius: 1.0,
    }
  },
  mounted() {
    this.fetch();

    if(window.kakao && window.kakao.maps) {
      this.loadMap();
    }else {
      this.loadScript();
    }
  },
  // watch : {
  //   $route() {
  //     this.fetch();

  //     if(window.kakao && window.kakao.maps) {
  //       this.loadMap();
  //     }else {
  //       this.loadScript();
  //     }
  //   }
  // },
  methods : {
    async fetch() {
      try{
        const res = await axios.get(`http://localhost:8080/hospital_main/mapData?sub=${this.hospitalId}&userLat=${this.$store.getters.userLat}&userLng=${this.$store.getters.userLng}&radius=${this.radius}`);
        this.hospitalList = res.data;
        if (this.map) {
          this.loadMaker();
        }
      }catch(err) {
        console.error('에러 발생 : ', err);
      }
    },

    // api 불러오기
    loadScript() {
      const script = document.createElement("script");
      script.src = "";
      script.onload = () => window.kakao.maps.load(this.loadMap);

      document.head.appendChild(script);
    },

    // 맵 출력
    loadMap() {
      const container = document.getElementById("map");
      const options = {
        center: new window.kakao.maps.LatLng(this.$store.getters.userLat, this.$store.getters.userLng),
        level: 4
      }

      this.map = new window.kakao.maps.Map(container, options);
      this.loadMaker();
    },

    // 마커 불러오기
    loadMaker() {
      this.hospitalList.forEach(hospital => {
        const markerPosition = new window.kakao.maps.LatLng(hospital.coordinateY, hospital.coordinateX);

        const marker = new window.kakao.maps.Marker({
          position: markerPosition,
          title: hospital.hospitalName,
        });

        marker.setMap(this.map);
      });
    },

    link() {
      window.location.href = 'http://www.kidshealth.co.kr/';
    },
  },
}
</script>

<style scoped>
.search_container{
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

</style>