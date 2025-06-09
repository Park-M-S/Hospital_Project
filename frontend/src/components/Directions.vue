<template>
  <div class="container">
    <div class="header">
      <vue3-tags-input :tags="tags" placeholder="진료과를 검색 하세요." @on-tags-changed="handleChangeTag" />
    </div>
    <div class="sidebar">2</div>
    <div class="content">
      <div id="map"></div>
    </div>
  </div>
</template>

<script>
import Vue3TagsInput from 'vue3-tags-input';
import axios from 'axios';

export default {
  name: 'directions',
  components: {
    Vue3TagsInput
  },
  data() {
    return {
      tagList: ['응급실', '전문의', '주차가능'],
      tags: [this.$store.getters.department],
      path: null,
      destinationName: this.$route.params.title, // 목적지 이름
      map: null,
      tagList: ['응급실', '전문의', '주차가능'],
      hospitalList: hospitalList,
      hospitalimg: hospitalimg,
      radius: 6.0,
      tags: [this.$store.getters.department],
      markers: [],

    }
  },
  async mounted() {
    const API_KEY = 'LyAvekIoA6DjLIiAvqhB/w'
    const startX = 127.14155271363916; // 신흥역
    const startY = 37.440740251685675;
    const endX = this.$route.query.x;   // 신구대학교
    const endY = this.$route.query.y;

    const url = `https://api.odsay.com/v1/api/searchPubTransPathT?SX=${startX}&SY=${startY}&EX=${endX}&EY=${endY}&apiKey=${API_KEY}`;

    try {
      const response = await axios.get(url);
      const pathList = response.data.result.path;

      this.path = pathList[0]; // 첫 번째 경로 선택
    } catch (err) {
      console.error(err);
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
      },
      deep: true
    }
  },
  methods: {
    getWalkStartName(index) {
      // 이전 구간이 존재하면 그 구간의 도착지를 가져옴
      if (index > 0 && this.path.subPath[index - 1].endName) {
        return this.path.subPath[index - 1].endName
      }
      return "집"
    },

    handleChangeTag(tags) {
      this.tags = tags;
    },

    handleAddTag(tags) {
      if (this.tags == null || this.tags.length === 0) {
        this.$store.dispatch('updateDepartment', { department: tags });
      }

      this.tags.push(tags);
    },

  },
}

</script>

<style scoped>
.container {
  display: grid;
  grid-template-rows: 100px 1fr;
  grid-template-columns: 300px 1fr;
  height: 100vh;
  gap: 2px;
  border: 2px solid black;
}

.header {
  grid-column: 1 / 3;
  /* span both columns */
  background-color: #11BF7F;
  display: flex;
  justify-content: center;
  align-items: center;
}

.sidebar {
  background-color: #e0e0e0;
  border: 2px solid black;
  display: flex;
  justify-content: center;
  align-items: center;
}

.content {
  background-color: #ffffff;
  border: 2px solid black;
  display: flex;
  justify-content: center;
  align-items: center;
}

.v3ti {
  width: 80%;
  align-items: center;
  height: 50px;
  padding: 10px;
  /* padding-left: 30px; */
  font-size: 30px;
  box-shadow: 0 3px 3px rgba(0, 0, 0, 0.2);
  border: none !important;
  border-radius: 30px !important;
  flex-wrap: nowrap !important;
}

.v3ti-new-tag {
  height: 50px !important;
}

.v3ti-tag {
  gap: 10px !important;
  margin: 0 !important;
  padding: 1px 20px !important;
  border-radius: 25px !important;
  background: #11BF7F !important;
  height: 50px !important;
  box-shadow: rgba(50, 50, 93, 0.25) 0px 6px 12px -2px, rgba(0, 0, 0, 0.3) 0px 3px 7px -3px !important;
}

.v3ti-content {
  gap: 10px;
  align-items: center;
}

.search_input {
  display: flex;
  align-items: center;
  height: 50px;
  padding: 10px;
  padding-left: 30px;
  font-size: 30px;
  box-shadow: 0 3px 3px rgba(0, 0, 0, 0.2);
  border-radius: 30px !important;
  background-color: white;
}
</style>