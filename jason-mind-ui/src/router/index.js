import {createRouter, createWebHistory} from 'vue-router'
import Main from "@/views/main/index.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/mind',
      name: 'Main',
      component: Main,
    },
  ],
})

export default router
