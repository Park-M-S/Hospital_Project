// src/router.js
import { createRouter, createWebHistory } from 'vue-router'
import Home from './components/Home.vue'
import About from './components/About.vue'
import Directions from './components/Directions.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/about/:title', component: About },
  { path: '/about/:title/directions', component: Directions }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router