<template>
  <div class="dashboard-container">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-left">
        <h1>学生服务平台</h1>
        <span class="role-badge">{{ userRole }}</span>
      </div>
      <div class="header-right">
        <span>欢迎，{{ username }}</span>
        <button @click="handleLogout" class="logout-btn">退出登录</button>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <span class="logo">🏫</span>
          <span class="title">管理后台</span>
        </div>
        <nav class="nav-menu">
          <div class="nav-item" @click="navigateTo('/dashboard')">
            <span class="icon">📊</span>
            <span>首页概览</span>
          </div>
          <div class="nav-item" @click="navigateTo('/students')">
            <span class="icon">👥</span>
            <span>学生管理</span>
          </div>
          <div class="nav-item" @click="navigateTo('/policy')">
            <span class="icon">📜</span>
            <span>政策管理</span>
          </div>
          <div class="nav-item" @click="navigateTo('/knowledge')">
            <span class="icon">📚</span>
            <span>知识库</span>
          </div>
          <div class="nav-item active" @click="navigateTo('/qa-tickets')">
            <span class="icon">❓</span>
            <span>问答工单</span>
          </div>
          <div class="nav-item" @click="navigateTo('/keywords')">
            <span class="icon">🔑</span>
            <span>关键词库</span>
          </div>
          <div class="nav-item" @click="navigateTo('/notices')">
            <span class="icon">📢</span>
            <span>通知管理</span>
          </div>
          <div class="nav-item" @click="navigateTo('/scope')">
            <span class="icon">👨‍🏫</span>
            <span>负责范围</span>
          </div>
          <div class="nav-item" @click="navigateTo('/data-scope')">
            <span class="icon">🔐</span>
            <span>数据权限</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <div class="page-header">
          <h2>问答工单管理</h2>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">{{ stats.total }}</div>
            <div class="stat-label">工单总数</div>
          </div>
          <div class="stat-item open">
            <div class="stat-num">{{ stats.open }}</div>
            <div class="stat-label">待处理</div>
          </div>
          <div class="stat-item answered">
            <div class="stat-num">{{ stats.answered }}</div>
            <div class="stat-label">已回答</div>
          </div>
          <div class="stat-item closed">
            <div class="stat-num">{{ stats.closed }}</div>
            <div class="stat-label">已关闭</div>
          </div>
        </div>

        <!-- 标签页 -->
        <div class="tab-bar">
          <button 
            v-for="tab in tabs" 
            :key="tab.key"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            {{ tab.name }}
            <span class="tab-count">{{ getTabCount(tab.key) }}</span>
          </button>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="open">待处理</option>
            <option value="answered">已回答</option>
            <option value="closed">已关闭</option>
          </select>
          <input type="text" v-model="searchKeyword" placeholder="搜索问题内容..." class="search-input" />
          <button class="btn-reset" @click="resetFilter">重置</button>
        </div>

        <!-- 工单列表 -->
        <div v-if="activeTab === 'tickets' && errorMessage" class="empty-state">
          <div>{{ errorMessage }}</div>
          <button class="btn-action" @click="loadTickets">重新加载</button>
        </div>
        <div v-else-if="activeTab === 'tickets'" class="ticket-list">
          <div v-for="ticket in tickets" :key="ticket.id" class="ticket-card" :class="ticket.status">
            <div class="ticket-header">
              <div class="ticket-meta">
                <span class="ticket-id">#{{ ticket.id }}</span>
                <span class="asker">{{ ticket.askerName }}</span>
                <span class="time">{{ ticket.createdAt }}</span>
              </div>
              <span :class="['status-badge', ticket.status]">
                {{ getStatusText(ticket.status) }}
              </span>
            </div>
            <div class="ticket-body">
              <div class="question">
                <div class="q-icon">❓</div>
                <div class="q-content">{{ ticket.question }}</div>
              </div>
              <div v-if="ticket.answer" class="answer">
                <div class="a-icon">💬</div>
                <div class="a-content">{{ ticket.answer }}</div>
              </div>
              <div v-if="ticket.matchedFaq" class="matched-faq">
                <span>匹配FAQ：</span>
                <a href="#" @click.prevent="viewFaq(ticket.matchedFaq)">{{ ticket.matchedFaq }}</a>
              </div>
            </div>
            <div class="ticket-footer">
              <span v-if="ticket.handler">处理人：{{ ticket.handler }}</span>
              <span v-if="ticket.handledAt">处理时间：{{ ticket.handledAt }}</span>
              <div class="action-btns">
                <button v-if="ticket.status === 'open'" class="btn-action" @click="answerTicket(ticket)">回答</button>
                <button class="btn-action" @click="closeTicket(ticket)">关闭</button>
              </div>
            </div>
          </div>
          <div v-if="tickets.length === 0" class="empty-state">
            {{ loading ? '加载中...' : '暂无工单记录' }}
          </div>
        </div>

        <!-- 统计视图 -->
        <div v-if="activeTab === 'stats'" class="stats-section">
          <div class="chart-row">
            <div class="chart-card">
              <h3>工单趋势（近7天）</h3>
              <div class="trend-chart">
                <div v-for="day in trendData" :key="day.date" class="trend-item">
                  <div class="trend-bar" :style="{ height: day.count * 10 + 'px' }"></div>
                  <span class="trend-label">{{ day.date }}</span>
                  <span class="trend-value">{{ day.count }}</span>
                </div>
              </div>
            </div>
            <div class="chart-card">
              <h3>常见问题词云</h3>
              <div class="word-cloud">
                <span v-for="word in wordCloud" :key="word.text" class="word" :style="{ fontSize: word.size + 'px' }">
                  {{ word.text }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <span class="total">共 {{ total }} 条</span>
          <div class="page-buttons">
            <button :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
            <span class="page-info">第 {{ page }} / {{ totalPages }} 页</span>
            <button :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
          </div>
        </div>
      </main>
    </div>

    <!-- 回答对话框 -->
    <div v-if="showAnswerDialog" class="dialog-overlay" @click.self="closeAnswerDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>回答问题</h3>
          <button class="dialog-close" @click="closeAnswerDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="question-display">
            <label>问题：</label>
            <div class="q-text">{{ currentTicket.question }}</div>
          </div>
          <div class="form-group">
            <label>回答内容 *</label>
            <textarea v-model="answerContent" rows="6" placeholder="请输入回答内容..." class="form-textarea"></textarea>
          </div>
          <div class="form-group">
            <label>
              <input type="checkbox" v-model="createFaq" /> 同时添加到FAQ
            </label>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeAnswerDialog">取消</button>
          <button class="btn-primary" @click="submitAnswer">提交</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getQATickets,
  getQATicketDetail,
  answerQATicket,
  closeQATicket,
  createFaq as takeQATicket
} from '@/api'

const STATUS_TEXT_MAP = {
  OPEN: '待处理',
  IN_PROGRESS: '处理中',
  ANSWERED: '已回答',
  CLOSED: '已关闭'
}

function resolveErrorMessage(error) {
  const status = error?.response?.status
  if (status === 500) return 'FAQ接口返回500，请稍后重试'
  if (status === 404) return 'FAQ接口不存在，请核对后台配置'
  if (error?.code === 'ECONNABORTED') return 'FAQ接口请求超时，请重新加载'
  return error?.response?.data?.message || error?.message || 'FAQ加载失败，请稍后重试'
}

function pickLatestReply(messages = []) {
  if (!Array.isArray(messages) || messages.length === 0) {
    return {}
  }
  return [...messages].reverse().find((item) => String(item.actorRole || '').toUpperCase() !== 'STUDENT') || messages[messages.length - 1]
}

export default {
  name: 'QATicketPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      activeTab: 'tickets',
      filterStatus: '',
      searchKeyword: '',
      page: 1,
      total: 0,
      totalPages: 1,
      loading: false,
      errorMessage: '',
      allTickets: [],
      showAnswerDialog: false,
      currentTicket: {},
      answerContent: '',
      createFaq: false,
      tabs: [
        { key: 'tickets', name: '工单列表' },
        { key: 'stats', name: '统计分析' }
      ],
      stats: {
        total: 0,
        open: 0,
        answered: 0,
        closed: 0
      },
      tickets: [],
      trendData: [
        { date: '03-09', count: 12 },
        { date: '03-10', count: 18 },
        { date: '03-11', count: 15 },
        { date: '03-12', count: 20 },
        { date: '03-13', count: 8 },
        { date: '03-14', count: 25 },
        { date: '03-15', count: 10 }
      ],
      wordCloud: [
        { text: '奖学金', size: 24 },
        { text: '学分', size: 20 },
        { text: '勤工俭学', size: 18 },
        { text: '转专业', size: 22 },
        { text: '考试', size: 16 },
        { text: '请假', size: 14 },
        { text: '入党', size: 20 },
        { text: '就业', size: 18 }
      ]
    }
  },
  mounted() {
    this.loadTickets()
  },
  watch: {
    filterStatus() {
      this.page = 1
      this.loadTickets()
    },
    searchKeyword() {
      this.applyClientFilter()
    }
  },
  methods: {
    getStatusText(status) {
      return STATUS_TEXT_MAP[String(status || '').toUpperCase()] || status
    },
    getTabCount(tab) {
      if (tab === 'tickets') return this.stats.open
      return 0
    },
    answerTicket(ticket) {
      this.currentTicket = ticket
      this.answerContent = ''
      this.createFaq = false
      this.showAnswerDialog = true
    },
    closeAnswerDialog() {
      this.showAnswerDialog = false
    },
    async submitAnswer() {
      if (!this.answerContent) {
        ElMessage.warning('请输入回答内容')
        return
      }

      try {
        if (String(this.currentTicket.status || '').toUpperCase() === 'OPEN') {
          await takeQATicket(this.currentTicket.id)
        }
        await answerQATicket(this.currentTicket.id, {
          content: this.answerContent,
          publishAsFaq: this.createFaq,
          closeTicket: false
        })
        ElMessage.success('回复成功')
        this.closeAnswerDialog()
        this.loadTickets()
      } catch (error) {
        this.errorMessage = resolveErrorMessage(error)
      }
    },
    async closeTicket(ticket) {
      try {
        await ElMessageBox.confirm('确认关闭该工单吗？', '提示', { type: 'warning' })
        await closeQATicket(ticket.id)
        ElMessage.success('工单已关闭')
        this.loadTickets()
      } catch (error) {
        if (error !== 'cancel') {
          this.errorMessage = resolveErrorMessage(error)
        }
      }
    },
    viewFaq(faqTitle) {
      ElMessage.info(`关联FAQ：${faqTitle}`)
    },
    resetFilter() {
      this.filterStatus = ''
      this.searchKeyword = ''
      this.page = 1
      this.loadTickets()
    },
    changePage(newPage) {
      this.page = newPage
      this.loadTickets()
    },
    navigateTo(path) {
      this.$router.push(path)
    },
    handleLogout() {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      this.$router.push('/login')
    },
    normalizeTicket(item = {}, detail = {}) {
      const latestReply = pickLatestReply(detail.messages)
      const status = String(detail.status || item.status || '').toUpperCase()
      return {
        id: item.id,
        askerName: detail.studentName || item.studentName || '-',
        status,
        question: detail.questionText || item.summary || '',
        answer: latestReply.content || '',
        createdAt: detail.createdAt || item.createdAt || '',
        handledAt: latestReply.createdAt || '',
        handler: latestReply.actorName || '',
        matchedFaq: detail.matchedFaqId ? `FAQ #${detail.matchedFaqId}` : ''
      }
    },
    applyClientFilter(source = this.allTickets) {
      const keyword = String(this.searchKeyword || '').trim()
      this.tickets = keyword
        ? source.filter((item) => `${item.question}${item.answer}${item.askerName}`.includes(keyword))
        : source
    },
    updateStats(source = []) {
      this.stats = {
        total: this.total,
        open: source.filter((item) => item.status === 'OPEN').length,
        answered: source.filter((item) => item.status === 'ANSWERED' || item.status === 'IN_PROGRESS').length,
        closed: source.filter((item) => item.status === 'CLOSED').length
      }
    },
    async loadTickets() {
      this.loading = true
      this.errorMessage = ''

      try {
        const res = await getQATickets({
          page: Math.max(this.page - 1, 0),
          size: 10,
          status: this.filterStatus || undefined
        })
        const pageData = res?.data || {}
        const baseList = Array.isArray(pageData.content) ? pageData.content : []
        const detailEntries = await Promise.allSettled(baseList.map((item) => getQATicketDetail(item.id)))
        const normalized = detailEntries.map((result, index) => {
          if (result.status === 'fulfilled') {
            return this.normalizeTicket(baseList[index], result.value?.data || {})
          }
          return this.normalizeTicket(baseList[index], {})
        })

        this.allTickets = normalized
        this.total = Number(pageData.totalElements || normalized.length)
        this.totalPages = Math.max(Number(pageData.totalPages || 1), 1)
        this.applyClientFilter(normalized)
        this.updateStats(normalized)
      } catch (error) {
        this.allTickets = []
        this.tickets = []
        this.total = 0
        this.totalPages = 1
        this.updateStats([])
        this.errorMessage = resolveErrorMessage(error)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.dashboard-container { min-height: 100vh; background: #f0f2f5; }
.header { background: #1e3a5f; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 15px; }
.header h1 { font-size: 20px; color: white; margin: 0; }
.role-badge { background: #3d7bb8; color: white; padding: 4px 12px; border-radius: 12px; font-size: 12px; }
.header-right { display: flex; align-items: center; gap: 15px; color: rgba(255,255,255,0.9); }
.logout-btn { background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.3); padding: 8px 16px; border-radius: 5px; cursor: pointer; color: white; }
.main-content { display: flex; min-height: calc(100vh - 62px); }
.sidebar { width: 240px; background: white; }
.sidebar-header { padding: 20px 25px; background: #1e3a5f; color: white; display: flex; align-items: center; gap: 12px; }
.sidebar-header .logo { font-size: 24px; }
.sidebar-header .title { font-size: 16px; }
.nav-menu { list-style: none; padding: 15px 0; margin: 0; }
.nav-item { padding: 14px 25px; display: flex; align-items: center; gap: 12px; cursor: pointer; color: #666; transition: all 0.3s; }
.nav-item:hover { background: #f0f7ff; color: #1e3a5f; }
.nav-item.active { background: #e8f0fa; color: #1e3a5f; border-left: 3px solid #1e3a5f; font-weight: 500; }
.icon { font-size: 18px; }
.content { flex: 1; padding: 30px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 22px; color: #1e3a5f; margin: 0; }

/* 统计卡片 */
.stats-row { display: flex; gap: 20px; margin-bottom: 20px; }
.stat-item { flex: 1; background: white; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #e8e8e8; }
.stat-item.open { border-left: 3px solid #ff9800; }
.stat-item.answered { border-left: 3px solid #4caf50; }
.stat-item.closed { border-left: 3px solid #9e9e9e; }
.stat-num { font-size: 32px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 14px; margin-top: 5px; }

/* 标签页 */
.tab-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.tab-btn { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border: none; background: none; cursor: pointer; border-radius: 6px; color: #666; transition: all 0.3s; }
.tab-btn:hover { background: #f0f7ff; }
.tab-btn.active { background: #1e3a5f; color: white; }
.tab-count { background: rgba(0,0,0,0.1); padding: 2px 8px; border-radius: 10px; font-size: 12px; }

/* 筛选栏 */
.filter-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.filter-select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; min-width: 120px; }
.search-input { flex: 1; padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; }
.btn-reset { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 工单卡片 */
.ticket-list { display: grid; gap: 15px; }
.ticket-card { background: white; border-radius: 10px; padding: 20px; border-left: 4px solid #ff9800; }
.ticket-card.answered { border-left-color: #4caf50; }
.ticket-card.closed { border-left-color: #9e9e9e; opacity: 0.7; }
.ticket-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.ticket-meta { display: flex; align-items: center; gap: 15px; }
.ticket-id { font-weight: 500; color: #1e3a5f; }
.asker { font-weight: 500; color: #333; }
.time { color: #888; font-size: 12px; }
.status-badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; }
.status-badge.open { background: #fff3e0; color: #ef6c00; }
.status-badge.answered { background: #e8f5e9; color: #2e7d32; }
.status-badge.closed { background: #f5f5f5; color: #666; }
.ticket-body { margin-bottom: 15px; }
.question, .answer { display: flex; gap: 12px; margin-bottom: 12px; }
.q-icon, .a-icon { font-size: 18px; }
.q-content, .a-content { flex: 1; color: #333; line-height: 1.6; }
.answer { background: #f0f7ff; padding: 12px; border-radius: 8px; }
.matched-faq { font-size: 12px; color: #666; padding-left: 30px; }
.matched-faq a { color: #1e3a5f; text-decoration: underline; }
.ticket-footer { display: flex; align-items: center; gap: 20px; font-size: 12px; color: #888; }
.ticket-footer .action-btns { margin-left: auto; display: flex; gap: 10px; }
.btn-action { background: none; border: none; color: #1e3a5f; cursor: pointer; }
.btn-action:hover { text-decoration: underline; }
.empty-state { text-align: center; color: #999; padding: 60px; background: white; border-radius: 8px; }

/* 统计图表 */
.stats-section { display: grid; gap: 20px; }
.chart-row { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.chart-card { background: white; border-radius: 10px; padding: 20px; }
.chart-card h3 { margin: 0 0 20px 0; font-size: 16px; color: #333; }
.trend-chart { display: flex; justify-content: space-around; align-items: flex-end; height: 150px; }
.trend-item { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.trend-bar { width: 40px; background: linear-gradient(to top, #1e3a5f, #3d7bb8); border-radius: 4px 4px 0 0; }
.trend-label { font-size: 11px; color: #666; }
.trend-value { font-size: 12px; color: #1e3a5f; font-weight: 500; }
.word-cloud { display: flex; flex-wrap: wrap; justify-content: center; align-items: center; gap: 15px; padding: 20px; }
.word { color: #1e3a5f; font-weight: 500; }

/* 分页 */
.pagination { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; background: white; padding: 15px 20px; border-radius: 8px; }
.total { color: #666; }
.page-buttons { display: flex; align-items: center; gap: 15px; }
.page-buttons button { background: #f5f5f5; border: 1px solid #ddd; padding: 8px 15px; border-radius: 5px; cursor: pointer; }
.page-buttons button:disabled { opacity: 0.5; cursor: not-allowed; }
.page-info { color: #666; }

/* 对话框 */
.dialog-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.dialog { background: white; border-radius: 10px; width: 500px; max-height: 80vh; overflow: hidden; }
.dialog-header { background: #1e3a5f; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
.dialog-header h3 { margin: 0; font-size: 18px; }
.dialog-close { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }
.dialog-body { padding: 20px; max-height: 60vh; overflow-y: auto; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
.btn-primary { background: #1e3a5f; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
.question-display { background: #f8fafc; padding: 12px; border-radius: 8px; margin-bottom: 15px; }
.question-display label { color: #888; font-size: 14px; }
.q-text { color: #333; margin-top: 8px; line-height: 1.5; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; resize: vertical; box-sizing: border-box; }
</style>
