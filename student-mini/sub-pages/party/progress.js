// sub-pages/party/progress.js
const app = getApp()
const { getPartyFlowState } = require('../../api/party-flow')

Page({
  data: {
    type: 'party', // party / league
    detail: null,
    canView: false,
    emptyState: { title: '', description: '' },
    teacherAuditTip: '',
    loading: true
  },
  
  onLoad(options) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    
    if (options.type) {
      this.setData({ type: options.type })
    }
    this.loadProgress()
  },
  
  async loadProgress() {
    this.setData({ loading: true })

    const studentId = app.globalData.userInfo?.studentId || app.globalData.userInfo?.id
    const flowState = getPartyFlowState(studentId)
    const detail = this.data.type === 'party' ? flowState.party?.summary || null : flowState.league?.summary || null
    const emptyState = this.data.type === 'party' && !flowState.canShowParty
      ? flowState.partyGateState
      : flowState.emptyState

    this.setData({
      detail,
      canView: !!detail,
      emptyState,
      teacherAuditTip: flowState.teacherAuditTip,
      loading: false
    })
  }
})
