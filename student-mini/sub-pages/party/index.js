const app = getApp()
const { getPartyFlowState } = require('../../api/party-flow')

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
    hasAnyFlow: false,
    canShowParty: false,
    emptyState: { title: '', description: '' },
    partyGateState: { title: '', description: '' },
    teacherAuditTip: '',
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
    if (index === 0 && !this.data.canShowParty) {
      wx.showToast({ title: '请先完成全部入团节点', icon: 'none' })
      return
    }
    this.setData({ activeTab: index })
  },

  loadData() {
    this.setData({ loading: true })
    const studentId = app.globalData.userInfo?.studentId || app.globalData.userInfo?.id
    const flowState = getPartyFlowState(studentId)
    const partyStages = normalizeStages(flowState.party?.summary?.stages || [])
    const leagueStages = normalizeStages(flowState.league?.summary?.stages || [])
    const activeTab = flowState.canShowParty ? 0 : 1

    this.setData({
      activeTab,
      partyProgress: flowState.party ? { ...flowState.party.summary, stages: partyStages } : null,
      partyFlow: normalizeFlow(flowState.party?.flow || []),
      leagueProgress: flowState.league ? { ...flowState.league.summary, stages: leagueStages } : null,
      leagueFlow: normalizeFlow(flowState.league?.flow || []),
      hasAnyFlow: flowState.hasAnyFlow,
      canShowParty: flowState.canShowParty,
      emptyState: flowState.emptyState,
      partyGateState: flowState.partyGateState,
      teacherAuditTip: flowState.teacherAuditTip,
      loading: false,
      dataLoaded: true
    })
  },

  goToProgress(e) {
    const { type } = e.currentTarget.dataset
    if (type === 'party' && !this.data.canShowParty) {
      wx.showToast({ title: '请先完成全部入团节点', icon: 'none' })
      return
    }
    wx.navigateTo({ url: `/sub-pages/party/progress?type=${type}` })
  }
})
