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
    {
      path: '/mind/agents/:agent',
      name: 'Agent',
      component: Main,
      props: true,
    },
  ],
})

export default router
