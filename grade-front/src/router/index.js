import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 路由表 —— 与 Axure 原型 6 个页面一一对应
 * 用 hash 路由：部署到静态服务器或任意子目录都不需要额外的 URL 重写配置。
 */
const routes = [
  { path: '/', redirect: { name: 'Login' } },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/student/scores',
    name: 'StudentScoreQuery',
    component: () => import('@/views/student/ScoreQueryView.vue'),
    meta: { role: 'STUDENT', title: '成绩查询' }
  },
  {
    path: '/student/appeal',
    name: 'StudentAppealApply',
    component: () => import('@/views/student/AppealApplyView.vue'),
    meta: { role: 'STUDENT', title: '申请查分' }
  },
  {
    path: '/teacher/appeals',
    name: 'TeacherAppealHandle',
    component: () => import('@/views/teacher/AppealHandleView.vue'),
    meta: { role: 'TEACHER', title: '受理查分申请' }
  },
  {
    path: '/teacher/scores/add',
    name: 'TeacherScoreAdd',
    component: () => import('@/views/teacher/ScoreAddView.vue'),
    meta: { role: 'TEACHER', title: '添加学生成绩' }
  },
  {
    path: '/teacher/scores/edit',
    name: 'TeacherScoreEdit',
    component: () => import('@/views/teacher/ScoreEditView.vue'),
    meta: { role: 'TEACHER', title: '修改学生成绩' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { public: true, title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

/** 登录守卫 —— 对应 FR-P1-06 角色分流与权限矩阵 */
router.beforeEach((to) => {
  const user = useUserStore()

  if (to.meta.public) {
    if (to.name === 'Login' && user.isLoggedIn) {
      return { name: user.homeRoute }
    }
    return true
  }

  if (!user.isLoggedIn) {
    return { name: 'Login' }
  }

  if (to.meta.role && to.meta.role !== user.role) {
    return { name: user.homeRoute }
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 成绩管理系统` : '成绩管理系统'
})

export default router
