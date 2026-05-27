<template>
  <div class="home">
    <div class="hero">
      <div class="hero-title">综合管理平台</div>
      <div class="search" @click="router.push('/student/qa/search')">
        <div class="search-icon">
          <el-icon><Search /></el-icon>
        </div>
        <div class="search-text">搜索政策/通知/模板</div>
      </div>
    </div>

    <div class="content">
      <div class="banner" @click="router.push('/student/dashboard')">
        <div class="banner-text">
          <div class="banner-line">一键触达</div>
          <div class="banner-line">轻松办理</div>
          <div class="banner-sub">一站式办理</div>
        </div>
        <div class="banner-illu" aria-hidden="true">
          <svg viewBox="0 0 360 220" width="180" height="110" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="g1" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0" stop-color="#e6f7ff" />
                <stop offset="1" stop-color="#ffffff" />
              </linearGradient>
            </defs>
            <rect x="16" y="36" width="220" height="144" rx="18" fill="url(#g1)" opacity="0.9"/>
            <rect x="34" y="52" width="120" height="16" rx="8" fill="#1677ff" opacity="0.35"/>
            <rect x="34" y="76" width="168" height="12" rx="6" fill="#1677ff" opacity="0.18"/>
            <rect x="34" y="96" width="150" height="12" rx="6" fill="#1677ff" opacity="0.18"/>
            <circle cx="270" cy="128" r="64" fill="#ffffff" opacity="0.85"/>
            <rect x="232" y="84" width="92" height="18" rx="9" fill="#1677ff" opacity="0.18"/>
            <rect x="232" y="110" width="76" height="12" rx="6" fill="#1677ff" opacity="0.14"/>
            <rect x="232" y="130" width="62" height="12" rx="6" fill="#1677ff" opacity="0.14"/>
            <rect x="60" y="150" width="96" height="12" rx="6" fill="#1677ff" opacity="0.2"/>
          </svg>
        </div>
      </div>

      <div class="feature-card">
        <div class="feature-grid">
          <div v-for="item in quickActions" :key="item.label" class="feature-item" @click="router.push(item.path)">
            <div class="feature-icon">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="feature-name">{{ item.label }}</div>
          </div>
        </div>
      </div>

      <div class="section">
        <div class="section-head">
          <div class="section-title">待办进度</div>
          <div class="section-more" @click="router.push('/student/affairs')">更多 ›</div>
        </div>
        <div class="list">
          <div v-for="todo in todos.slice(0, 3)" :key="todo.id" class="list-item" @click="handleTodoClick(todo)">
            <div class="list-icon">{{ (todo.title || '').slice(0, 1) }}</div>
            <div class="list-main">
              <div class="list-title">{{ todo.title }}</div>
              <div class="list-sub">{{ todo.deadline ? `截止：${todo.deadline}` : '请尽快处理' }}</div>
            </div>
            <div class="list-side">
              <div class="status" :class="`status-${todo.status}`">{{ getStatusText(todo.status) }}</div>
            </div>
          </div>
          <div v-if="todos.length === 0" class="empty">暂无待办</div>
        </div>
      </div>

      <div class="section">
        <div class="section-head">
          <div class="section-title">精选通知</div>
          <div class="section-more" @click="router.push('/student/notices')">更多 ›</div>
        </div>
        <div class="notice-list">
          <div
            v-for="notice in notices.slice(0, 3)"
            :key="notice.id"
            class="notice-item"
            @click="router.push(`/student/notices/${notice.id}`)"
          >
            <div class="notice-title">{{ notice.title }}</div>
            <div class="notice-meta">
              <div class="notice-source">来源：{{ notice.department || '系统通知' }}</div>
              <div class="notice-time">发布时间：{{ notice.publishTime }}</div>
            </div>
          </div>
          <div v-if="notices.length === 0" class="empty">暂无通知</div>
        </div>
      </div>

      <div class="footer">
        <div class="footer-text">学院学生综合服务平台</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  Document,
  Flag,
  Bell,
  Tickets,
  Medal,
  TrendCharts,
  ChatDotRound
} from '@element-plus/icons-vue'
import { useStudentStore } from '@/stores/student'
import { getStudentDashboard } from '@/api/student'

const router = useRouter()
const studentStore = useStudentStore()

// 用户信息（默认显示）
const userInfo = reactive({
  fullName: '张三',
  studentNo: '2023100001',
  major: '计算机科学与技术'
})

// 快捷入口
const quickActions = [
  { path: '/student/qa/search', label: '智能检索', icon: Search },
  { path: '/student/policies', label: '政策库', icon: Document },
  { path: '/student/policies', label: '模板下载', icon: Document },
  { path: '/student/certificates/apply', label: '证明申请', icon: Medal },
  { path: '/student/party', label: '党团流程', icon: Flag },
  { path: '/student/academic', label: '学业分析', icon: TrendCharts },
  { path: '/student/notices', label: '通知聚合', icon: Bell },
  { path: '/student/qa/tickets', label: '常见问题', icon: ChatDotRound }
]

// 待办事项
const todos = ref([])

// 最新通知
const notices = ref([])

// 党团进度
const partyProgress = ref(null)

// 学业统计
const academicStats = reactive({
  gpa: '--',
  credits: '--/--',
  rank: '--'
})

// 申请记录
const affairs = ref([])

// 加载状态
const loading = ref(false)

function getStatusType(status) {
  const map = { pending: 'warning', reviewing: 'primary', completed: 'success' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { pending: '待处理', reviewing: '审核中', completed: '已完成' }
  return map[status] || status
}

function mapPriorityToStatus(priority) {
  const map = {
    HIGH: 'pending',
    MEDIUM: 'reviewing',
    LOW: 'completed'
  }
  return map[priority] || 'reviewing'
}

function mapCertificateStatus(status) {
  const normalized = String(status || '').toUpperCase()
  const map = {
    PENDING: 'pending',
    SUBMITTED: 'pending',
    IN_REVIEW: 'reviewing',
    APPROVED: 'completed'
  }
  return map[normalized] || 'reviewing'
}

function getAffairStatusType(status) {
  const map = { 
    draft: 'info', 
    submitted: 'warning', 
    in_review: 'primary', 
    approved: 'success', 
    rejected: 'danger',
    canceled: 'info'
  }
  return map[status] || 'info'
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return time
}

function handleTodoClick(todo) {
  if (todo.actionPath) {
    router.push(todo.actionPath)
    return
  }
  if (todo.type === 'certificate') {
    router.push('/student/affairs')
    return
  }
  console.log('点击待办:', todo)
}

// 加载首页数据
async function loadDashboardData() {
  loading.value = true
  
  try {
    const res = await getStudentDashboard()
    const data = res?.data || {}

    if (data.profile) {
      Object.assign(userInfo, {
        fullName: data.profile.name || studentStore.fullName || userInfo.fullName,
        studentNo: data.profile.studentNo || studentStore.studentNo || userInfo.studentNo,
        major: data.profile.major || userInfo.major
      })
    }

    todos.value = (data.focusItems || []).map((item, index) => ({
      id: `${item.type || 'focus'}-${index}`,
      title: item.title,
      deadline: '',
      status: mapPriorityToStatus(item.priority),
      actionPath: item.actionPath,
      type: item.type
    }))

    if (todos.value.length === 0) {
      todos.value = (data.certificateRequests || []).map((item) => ({
        id: `certificate-${item.id}`,
        title: item.certificateType,
        deadline: '',
        status: mapCertificateStatus(item.status),
        actionPath: '/student/affairs',
        type: 'certificate'
      }))
    }

    notices.value = (data.notices || []).map((item) => ({
      id: item.id,
      title: item.title,
      publishTime: item.publishTime,
      department: Array.isArray(item.tags) && item.tags.length > 0 ? item.tags[0] : item.targetDescription,
      important: item.priority === 'HIGH',
      read: false
    }))

    partyProgress.value = data.partyProgress || null

    affairs.value = (data.certificateRequests || []).map((item) => ({
      id: item.id,
      title: item.certificateType,
      status: String(item.status || '').toLowerCase(),
      statusText: item.status
    }))
  } catch (error) {
    todos.value = [
      { id: 1, title: '团费缴纳', deadline: '2026-05-01', status: 'pending' },
      { id: 2, title: '积极分子培训心得', deadline: '2026-05-15', status: 'pending' },
      { id: 3, title: '在学证明申请', deadline: '-', status: 'reviewing' }
    ]

    notices.value = [
      { id: 1, title: '2026春季双选会即将开始！', publishTime: '2026-03-25', department: '人大就业', important: true, read: false },
      { id: 2, title: '关于组织申报2026年中国人民大学“大学生创业训练计划”创业训练项目的通知', publishTime: '2026-03-25', department: '教务处', important: false, read: true }
    ]

    partyProgress.value = {
      flowName: '入党积极分子培训班',
      progress: 40,
      currentNode: '理论学习阶段'
    }

    affairs.value = [
      { id: 1, title: '在学证明申请', status: 'approved', statusText: '已通过' },
      { id: 2, title: '缓考申请-线性代数', status: 'in_review', statusText: '审核中' }
    ]

    ElMessage.error('加载数据失败，已切换为本地演示数据')
    console.error('加载首页数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: #f4f7fb;
}

.hero {
  background: linear-gradient(180deg, #1677ff 0%, #4096ff 100%);
  padding: 18px 16px 56px;
}

.hero-title {
  text-align: center;
  font-size: 20px;
  font-weight: 800;
  color: rgba(31, 45, 61, 0.92);
  letter-spacing: 0.2px;
}

.search {
  margin-top: 14px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.92);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  box-shadow: 0 12px 32px rgba(21, 42, 72, 0.1);
  cursor: pointer;
}

.search-icon {
  color: #cbd5e1;
  font-size: 18px;
}

.search-text {
  font-size: 14px;
  color: #cbd5e1;
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.content {
  max-width: 1200px;
  margin: -36px auto 0;
  padding: 0 16px 28px;
}

.banner {
  border-radius: 16px;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  padding: 18px 18px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  box-shadow: 0 12px 32px rgba(21, 42, 72, 0.1);
  overflow: hidden;
  cursor: pointer;
}

.banner-text {
  color: #ffffff;
}

.banner-line {
  font-size: 22px;
  font-weight: 800;
  line-height: 1.1;
}

.banner-sub {
  margin-top: 10px;
  font-size: 14px;
  opacity: 0.92;
}

.banner-illu {
  flex-shrink: 0;
  opacity: 0.95;
}

.feature-card {
  margin-top: 14px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  padding: 16px 12px;
  box-shadow: 0 12px 32px rgba(21, 42, 72, 0.07);
  backdrop-filter: blur(6px);
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 10px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.feature-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #e6f7ff;
  border: 1px solid #edf1f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1677ff;
  box-shadow: 0 12px 32px rgba(22, 119, 255, 0.12);
  font-size: 22px;
}

.feature-name {
  font-size: 14px;
  color: #1f2d3d;
  font-weight: 600;
}

.section {
  margin-top: 14px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(21, 42, 72, 0.07);
  overflow: hidden;
}

.section-head {
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #edf1f6;
}

.section-title {
  font-size: 18px;
  font-weight: 800;
  color: #1f2d3d;
}

.section-more {
  font-size: 14px;
  color: #8a98aa;
  cursor: pointer;
  user-select: none;
}

.list {
  padding: 8px 16px 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 12px;
  border-radius: 14px;
  background: #f8f9fb;
  border: 1px solid #edf1f6;
  cursor: pointer;
}

.list-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #e6f7ff;
  color: #1677ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 18px;
  flex-shrink: 0;
}

.list-main {
  flex: 1;
  min-width: 0;
}

.list-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2d3d;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.list-sub {
  margin-top: 6px;
  font-size: 13px;
  color: #8a98aa;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.list-side {
  flex-shrink: 0;
}

.status {
  font-size: 13px;
  font-weight: 700;
  padding: 6px 10px;
  border-radius: 999px;
}

.status-pending {
  color: #faad14;
  background: #fff7e6;
}

.status-reviewing {
  color: #1677ff;
  background: #e6f7ff;
}

.status-completed {
  color: #52c41a;
  background: #f6ffed;
}

.notice-list {
  padding: 6px 16px 14px;
  display: flex;
  flex-direction: column;
}

.notice-item {
  padding: 14px 0;
  border-bottom: 1px solid #edf1f6;
  cursor: pointer;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2d3d;
  line-height: 1.4;
}

.notice-meta {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  color: #8a98aa;
}

.notice-source,
.notice-time {
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty {
  padding: 18px 0 6px;
  text-align: center;
  color: #8a98aa;
  font-size: 14px;
}

.footer {
  margin-top: 14px;
  padding: 16px 0 10px;
  text-align: center;
  color: #8a98aa;
  font-size: 12px;
}

@media (max-width: 520px) {
  .feature-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 14px 6px;
  }

  .feature-name {
    font-size: 13px;
  }

  .banner-line {
    font-size: 20px;
  }
}

@media (min-width: 900px) {
  .hero {
    padding: 24px 20px 72px;
  }

  .hero-title {
    font-size: 22px;
  }

  .content {
    margin-top: -44px;
    padding: 0 20px 32px;
  }

  .banner {
    padding: 22px 22px 18px;
  }

  .banner-line {
    font-size: 26px;
  }

  .feature-card {
    padding: 18px 14px;
  }

  .feature-icon {
    width: 58px;
    height: 58px;
  }
}
</style>
