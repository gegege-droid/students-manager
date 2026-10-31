import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useUserStore } from '@/stores/user'
import { useScoreStore } from '@/stores/score'
import './styles/variables.css'
import './styles/main.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

// 后端返回 401 时统一清理会话并回到登录页
window.addEventListener('grade-auth-expired', () => {
  const user = useUserStore(pinia)
  const score = useScoreStore(pinia)
  user.logout()
  score.reset()
  router.push({ name: 'Login' })
})

app.mount('#app')
