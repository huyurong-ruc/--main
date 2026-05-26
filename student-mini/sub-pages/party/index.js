const app = getApp()
const { get } = require('../../api/request')

const TAB_CONFIG = [
  { label: '入党流程', type: 'party' },
  { label: '入团流程', type: 'league' }
]

function normalizeStages(stages = []) {
  return stages.map((s) => ({
    ...s,
    status: s.status || 'pending',
    statusText: s.statusText || (s.status === 'completed' ? '已完成' : s.status === 'in_progress' ? '进行中' : '待开始')
  }))
}

function normalizeFlow(flow = []) {
  return flow.map((f, index) => ({
    ...f,
    index: index + 1
  }))
}

Page({
  data: {
    activeTab: 0,
    tabs: TAB_CONFIG.map((t) => t.label),
    partyProgress: null,
    partyFlow: [],
    leagueProgress: null,
    leagueFlow: [],
    loading: false,
    dataLoaded: false
  },

  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadData()
  },

  onShow() {
    if (!this.data.dataLoaded && app.isLoggedIn()) {
      this.loadData()
    }
  },

  onTabChange(e) {
    const index = parseInt(e.currentTarget.dataset.index, 10)
    if (index === this.data.activeTab) return
    this.setData({ activeTab: index })
  },

  async loadData() {
    this.setData({ loading: true })

    try {
      const [progressRes, remindersRes] = await Promise.all([
        get('/student/party-progress', {}, { showLoading: false }),
        get('/student/party-progress/reminders', {}, { showLoading: false })
      ])

      const progressData = progressRes.data || {}
      const remindersData = remindersRes.data || {}

      // 入党数据（当前接口返回的是个人入党进度）
      const partyStages = normalizeStages(progressData.stages || [])
      const partyFlow = normalizeFlow(remindersData.flow || progressData.stages || [])

      // 入团数据（如果后端暂未提供独立入团接口，复用结构但标注为待接入）
      const leagueStages = normalizeStages(remindersData.leagueStages || [])
      const leagueFlow = normalizeFlow(remindersData.leagueFlow || [])

      this.setData({
        partyProgress: { ...progressData, stages: partyStages },
        partyFlow,
        leagueProgress: leagueStages.length ? { stages: leagueStages } : null,
        leagueFlow,
        loading: false,
        dataLoaded: true
      })
    } catch (e) {
      console.error('加载党团数据失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
      this.setData({
        partyProgress: null,
        partyFlow: [],
        leagueProgress: null,
        leagueFlow: [],
        loading: false,
        dataLoaded: true
      })
    }
  },

  goToProgress(e) {
    const { type } = e.currentTarget.dataset
    wx.navigateTo({ url: `/sub-pages/party/progress?type=${type}` })
  }
})
