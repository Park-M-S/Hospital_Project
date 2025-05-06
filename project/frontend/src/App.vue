<!-- src/App.vue -->
<template>
    <router-view />
</template>
  
<style>
@font-face {
    font-family: 'GmarketSansMedium';
    src: url('https://fastly.jsdelivr.net/gh/projectnoonnu/noonfonts_2001@1.1/GmarketSansMedium.woff') format('woff');
    font-weight: normal;
    font-style: normal;
}

body {
  font-family: 'GmarketSansMedium';
  display: flex;
  justify-content: center;
  background: #EFFBEF;
  margin: 0;
  padding: 0;
  color: #253140;
}

a {
  text-decoration: none !important; /* 밑줄 제거 */
  color: inherit;  
}

div {
  line-height: 1.4;
}

input {
  all: unset;
}
</style>