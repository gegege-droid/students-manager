<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { calcAvgGpa, calcAvgScore } from '@/utils/score'

/** 个人中心 —— 学生/教师共用，展示身份、学习/教学概览与账号安全 */

const user = useUserStore()
const data = useScoreStore()
const router = useRouter()

onMounted(async () => {
  try {
    await data.ensureLoaded()
  } catch (error) {
    console.warn('加载个人中心数据失败', error)
  }
})

const avatarText = computed(() => (user.name ? user.name.slice(0, 1) : '?'))
const accountLabel = computed(() => (user.isStudent ? '学号' : '工号'))

const myScores = computed(() =>
  user.isStudent ? data.scoreRowsOfStudent(user.loginId, {}) : []
)

const myAppeals = computed(() =>
  user.isStudent ? data.appealRowsOfStudent(user.loginId) : data.appeals
)

const stats = computed(() => {
  if (user.isStudent) {
    const scores = myScores.value
    const best = scores.reduce(
      (top, row) => (!top || Number(row.totalScore) > Number(top.totalScore) ? row : top),
      null
    )
    return {
      count: scores.length,
      credit: scores.reduce((sum, row) => sum + (row.credit || 0), 0),
      avgScore: scores.length ? calcAvgScore(scores) : 0,
      avgGpa: scores.length ? calcAvgGpa(scores) : 0,
      bestCourse: best ? `${best.courseName}（${best.totalScore} 分）` : '—',
      pending: myAppeals.value.filter((item) => item.status === 'PENDING').length
    }
  }

  const appeals = data.appeals
  return {
    pending: appeals.filter((item) => item.status === 'PENDING').length,
    handled: appeals.filter((item) => item.status !== 'PENDING').length,
    classes: user.manageClassIds.length,
    courses: data.courseList.length
  }
})

const suggestion = computed(() => {
  if (user.isStudent) {
    if (stats.value.pending > 0) {
      return `你有 ${stats.value.pending} 条查分申请正在等待受理，可在「申请查分」中查看处理进度。`
    }
    if (stats.value.avgScore >= 85) {
      return `当前平均分 ${stats.value.avgScore}，整体表现优秀，继续保持稳定的复习节奏。`
    }
    if (stats.value.avgScore >= 70) {
      return `当前平均分 ${stats.value.avgScore}，基础较稳；可重点巩固分数较低的课程。`
    }
    return '建议先梳理不及格课程的知识点，必要时向任课老师请教，并关注查分申请进度。'
  }
  if (stats.value.pending > 0) {
    return `还有 ${stats.value.pending} 条查分申请待受理，建议优先处理，避免学生等待过久。`
  }
  return '当前没有待受理的查分申请，可以在「添加成绩」中录入新成绩，或检查已录入成绩。'
})

function goHome() {
  router.push({ name: user.homeRoute })
}

function goAppealOrHandle() {
  router.push({ name: user.isStudent ? 'StudentAppealApply' : 'TeacherAppealHandle' })
}
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">个人中心</h2>
      <span class="breadcrumb">首页 / 个人中心</span>
    </div>

    <!-- 身份卡片 -->
    <div class="card profile-hero">
      <div class="avatar">{{ avatarText }}</div>
      <div class="hero-info">
        <h3 class="hero-name">{{ user.name }}</h3>
        <div class="hero-sub">{{ user.roleText }} · {{ accountLabel }} {{ user.loginId }}</div>
        <div class="hero-class">
          {{ user.isStudent ? `所在班级：${user.className || '—'}` : `管理班级：${user.manageClassIds.join('、') || '—'}` }}
        </div>
      </div>
      <div class="hero-actions">
        <button type="button" class="btn btn-primary btn-sm" @click="goHome">
          {{ user.isStudent ? '进入成绩查询' : '进入受理待办' }}
        </button>
        <button type="button" class="btn btn-sm" @click="goAppealOrHandle">
          {{ user.isStudent ? '申请查分' : '受理查分' }}
        </button>
      </div>
    </div>

    <div class="profile-grid mt-16">
      <!-- 基本信息 -->
      <div class="card">
        <div class="card-head">
          <span class="card-title">基本信息</span>
          <span class="status-dot">账号正常</span>
        </div>
        <dl class="info-list">
          <div><dt>姓名</dt><dd>{{ user.name }}</dd></div>
          <div><dt>{{ accountLabel }}</dt><dd>{{ user.loginId }}</dd></div>
          <div><dt>身份角色</dt><dd>{{ user.roleText }}</dd></div>
          <div v-if="user.isStudent"><dt>所在班级</dt><dd>{{ user.className || '—' }}</dd></div>
          <div v-else><dt>管理班级</dt><dd>{{ user.manageClassIds.join('、') || '—' }}</dd></div>
          <div><dt>系统版本</dt><dd>成绩管理系统 v1.0</dd></div>
        </dl>
      </div>

      <!-- 学习 / 教学概览 -->
      <div class="card">
        <div class="card-head">
          <span class="card-title">{{ user.isStudent ? '学习概览' : '教学概览' }}</span>
        </div>

        <template v-if="user.isStudent">
          <div class="mini-stats">
            <div class="mini-stat"><b>{{ stats.count }}</b><span>已修课程</span></div>
            <div class="mini-stat"><b>{{ stats.credit }}</b><span>总学分</span></div>
            <div class="mini-stat"><b>{{ stats.avgScore }}</b><span>平均分</span></div>
            <div class="mini-stat"><b>{{ stats.avgGpa }}</b><span>平均绩点</span></div>
          </div>
          <div class="info-line">最高分课程：<b>{{ stats.bestCourse }}</b></div>
          <div class="info-line">待受理查分申请：<b>{{ stats.pending }}</b> 条</div>
        </template>

        <template v-else>
          <div class="mini-stats">
            <div class="mini-stat"><b>{{ stats.classes }}</b><span>管理班级</span></div>
            <div class="mini-stat"><b>{{ stats.courses }}</b><span>课程数量</span></div>
            <div class="mini-stat"><b>{{ stats.pending }}</b><span>待受理</span></div>
            <div class="mini-stat"><b>{{ stats.handled }}</b><span>已处理</span></div>
          </div>
          <div class="info-line">管理班级：<b>{{ user.manageClassIds.join('、') || '—' }}</b></div>
          <div class="info-line">待办提醒：<b>{{ stats.pending > 0 ? '有新的查分申请待处理' : '暂无待办' }}</b></div>
        </template>
      </div>
    </div>

    <div class="profile-grid mt-16">
      <!-- 账号与安全 -->
      <div class="card">
        <div class="card-head">
          <span class="card-title">账号与安全</span>
        </div>
        <div class="security-row">
          <span>登录账号</span><b>{{ user.loginId }}</b>
        </div>
        <div class="security-row">
          <span>登录方式</span><b>账号密码 + JWT 令牌</b>
        </div>
        <div class="security-row">
          <span>密码</span><span class="hint">如需修改密码，请联系系统管理员</span>
        </div>
        <div class="security-row">
          <span>会话</span><span class="hint">令牌默认 30 分钟有效，过期需重新登录</span>
        </div>
      </div>

      <!-- 小贴士 -->
      <div class="card suggestion-card">
        <div class="card-head">
          <span class="card-title">小贴士</span>
          <span class="tip-badge">今日建议</span>
        </div>
        <p class="suggestion-text">{{ suggestion }}</p>
        <button type="button" class="btn btn-primary btn-sm mt-12" @click="goAppealOrHandle">
          {{ user.isStudent ? '去看看我的申请' : '去处理待办' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-hero {
  display: flex;
  align-items: center;
  gap: 18px;
  background: linear-gradient(135deg, #ebf4ff 0%, #ffffff 75%);
  border-color: #d5e0ee;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  font-size: 26px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 64px;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
}

.hero-info {
  flex: 1;
  min-width: 0;
}

.hero-name {
  font-size: 22px;
  margin-bottom: 4px;
}

.hero-sub {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.hero-class {
  color: #4a5568;
  font-size: 13px;
  margin-top: 2px;
}

.hero-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  align-items: stretch;
}

.profile-grid .card + .card {
  margin-top: 0;
}

.status-dot {
  font-size: 12px;
  color: var(--color-success);
}

.status-dot::before {
  content: "●";
  margin-right: 4px;
  font-size: 10px;
}

.info-list {
  margin: 0;
}

.info-list > div {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 9px 0;
  border-bottom: 1px dashed #edf0f4;
}

.info-list > div:last-child {
  border-bottom: none;
}

.info-list dt {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.info-list dd {
  margin: 0;
  font-weight: 600;
  text-align: right;
}

.mini-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.mini-stat {
  background: #f7fafc;
  border: 1px solid #edf0f4;
  border-radius: var(--radius-control);
  padding: 10px 8px;
  text-align: center;
}

.mini-stat b {
  display: block;
  font-size: 20px;
  color: var(--color-primary);
  line-height: 1.3;
}

.mini-stat span {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.info-line {
  font-size: 13px;
  color: #4a5568;
  padding: 4px 0;
}

.security-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 9px 0;
  border-bottom: 1px dashed #edf0f4;
  font-size: 13px;
}

.security-row:last-child {
  border-bottom: none;
}

.security-row > span:first-child {
  color: var(--color-text-secondary);
}

.suggestion-card {
  background: linear-gradient(135deg, #f0fff4 0%, #ffffff 75%);
  border-color: #cfe6d8;
}

.tip-badge {
  font-size: 11px;
  color: var(--color-success);
  background: #fff;
  border: 1px solid #cfe6d8;
  border-radius: 10px;
  padding: 0 9px;
  line-height: 20px;
}

.suggestion-text {
  color: #2f4d73;
  line-height: 1.8;
}

@media (max-width: 960px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-hero {
    flex-wrap: wrap;
  }

  .hero-actions {
    width: 100%;
  }

  .mini-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
