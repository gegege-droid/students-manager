import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 路由表 —— 高中成绩管理系统（新高考 3+1+2）
 * 学生端：月考成绩查询 / 高考成绩预测 / 申请查分
 * 教师端：受理查分申请 / 录入月考成绩 / 修改成绩
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
    path: '/student/exams',
    name: 'StudentExamQuery',
    component: () => import('@/views/student/ExamQueryView.vue'),
    meta: { role: 'STUDENT', title: '月考成绩查询' }
  },
  {
    path: '/student/prediction',
    name: 'StudentPrediction',
    component: () => import('@/views/student/PredictionView.vue'),
    meta: { role: 'STUDENT', title: '高考成绩预测' }
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
    component: () => import('@/views/teacher/ExamScoreAddView.vue'),
    meta: { role: 'TEACHER', title: '录入月考成绩' }
  },
  {
    path: '/teacher/scores/edit',
    name: 'TeacherScoreEdit',
    component: () => import('@/views/teacher/ScoreEditView.vue'),
    meta: { role: 'TEACHER', title: '修改成绩' }
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

/** 登录守卫：未登录去登录页；角色不符回本角色首页 */
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
  document.title = to.meta.title ? `${to.meta.title} · 高中成绩管理系统` : '高中成绩管理系统'
})

export default router
