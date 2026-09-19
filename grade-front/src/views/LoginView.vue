<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseModal from '@/components/BaseModal.vue'
import { useToast } from '@/composables/useToast'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { validateAccount, validatePassword } from '@/utils/validate'

/** P1 学生登录 —— 对应 FR-P1-01 ~ FR-P1-07 */

const router = useRouter()
const user = useUserStore()
const data = useScoreStore()
const toast = useToast()

const REMEMBER_KEY = 'grade-system:remember'

const form = reactive({ account: '', password: '', remember: false })
const errors = reactive({ account: '', password: '' })
const loginError = ref('')
const forgotVisible = ref(false)
const submitting = ref(false)

onMounted(() => {
  const remembered = localStorage.getItem(REMEMBER_KEY)
  if (remembered) {
    form.account = remembered
    form.remember = true
  }
})

function clearError(key) {
  errors[key] = ''
  loginError.value = ''
}

/** FR-P1-02：空值 / 格式校验，不通过则不提交 */
function validate() {
  errors.account = validateAccount(form.account)
  errors.password = validatePassword(form.password)
  return !errors.account && !errors.password
}

function onSubmit() {
  loginError.value = ''
  if (!validate()) return

  submitting.value = true
  const result = user.login(form.account, form.password)
  submitting.value = false

  // FR-P1-03：账号或密码错误
  if (!result.ok) {
    loginError.value = result.message
    form.password = ''
    return
  }

  if (form.remember) localStorage.setItem(REMEMBER_KEY, form.account)
  else localStorage.removeItem(REMEMBER_KEY)

  toast.success(`欢迎回来，${user.name}`)
  // FR-P1-06：按角色分流
  router.push({ name: user.homeRoute })
}

function onResetDemo() {
  data.resetDemo()
  toast.info('演示数据已恢复为初始状态')
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <span class="logo">▤</span>
        <span class="brand-name">成绩管理系统</span>
      </div>
      <p class="subtitle">学生 / 班主任 登录</p>
      <div class="divider"></div>

      <div v-if="loginError" class="alert alert-danger login-error">⚠ {{ loginError }}</div>

      <div class="field">
        <label class="label" for="account">账号 <span class="req">*</span></label>
        <input
          id="account"
          v-model.trim="form.account"
          class="input"
          :class="{ 'is-error': errors.account }"
          type="text"
          maxlength="12"
          placeholder="请输入学号或工号（4~12 位）"
          autocomplete="username"
          @input="clearError('account')"
        />
        <span v-if="errors.account" class="err">{{ errors.account }}</span>
      </div>

      <div class="field">
        <label class="label" for="password">密码 <span class="req">*</span></label>
        <input
          id="password"
          v-model="form.password"
          class="input"
          :class="{ 'is-error': errors.password }"
          type="password"
          placeholder="请输入密码"
          autocomplete="current-password"
          @input="clearError('password')"
          @keyup.enter="onSubmit"
        />
        <span v-if="errors.password" class="err">{{ errors.password }}</span>
      </div>

      <div class="row-between options">
        <label class="checkbox">
          <input v-model="form.remember" type="checkbox" />
          <span>记住账号</span>
        </label>
        <button type="button" class="link-btn" @click="forgotVisible = true">忘记密码？</button>
      </div>

      <button type="button" class="btn btn-primary btn-block" :disabled="submitting" @click="onSubmit">
        {{ submitting ? '登录中…' : '登 录' }}
      </button>
    </div>

    <div class="page-foot">
      <p>测试账号：学生 <b>2021001</b> / 123456　·　班主任 <b>T001</b> / 123456</p>
      <button type="button" class="link-btn" @click="onResetDemo">重置演示数据</button>
    </div>

    <BaseModal v-model="forgotVisible" title="忘记密码" :width="420" confirm-text="知道了" :show-footer="true" @confirm="forgotVisible = false">
      <p>请联系班主任或教务处重置密码。</p>
      <p class="hint mt-8">本原型未实现密码重置流程，列为后续扩展功能。</p>
    </BaseModal>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #eaeff6 0%, #f7fafc 60%, #ffffff 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
}

.login-card {
  width: 400px;
  max-width: 100%;
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 28px 32px 32px;
  box-shadow: 0 6px 24px rgba(15, 23, 42, 0.08);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 18px;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
}

.subtitle {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.divider {
  height: 1px;
  background: var(--color-border);
  margin: 16px 0 20px;
}

.login-error {
  margin-bottom: 16px;
}

.options {
  margin: 4px 0 20px;
  font-size: 13px;
}

.checkbox {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #4a5568;
  cursor: pointer;
}

.link-btn {
  border: none;
  background: none;
  padding: 0;
  color: var(--color-primary);
  font-family: inherit;
  font-size: 13px;
  cursor: pointer;
}

.link-btn:hover {
  text-decoration: underline;
}

.page-foot {
  margin-top: 20px;
  text-align: center;
  font-size: var(--font-size-hint);
  color: var(--color-text-secondary);
  line-height: 2;
}
</style>
