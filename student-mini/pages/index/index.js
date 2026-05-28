// pages/index/index.js
const app = getApp()
const { get } = require('../../api/request')
const { getPartyFlowState } = require('../../api/party-flow')
const applyApi = require('../../api/apply')
const { isCompletedStatus, getApplyStatusMeta } = require('../../api/apply-status')

function buildApplyPendingCard(item = {}) {
  const statusMeta = getApplyStatusMeta(item.statusCode || item.status)
  return {
    id: `apply-${item.id || 'latest'}`,
    title: item.certificateType || item.typeName || '证明申请',
    status: statusMeta.listLabel,
    statusClass: statusMeta.listClass,
    showRightStatus: true,
    typeText: '申',
    typeClass: 'apply',
    desc: item.pendingSummary?.pendingActionText || item.pendingActionText || '请前往证明申请查看处理进度',
    time: item.createdAt ? item.createdAt.slice(0, 10) : (item.createTime || '')
  }
}

Page({
  data: {
    userInfo: null,
    banners: [
      {
        id: 1,
        image: '/static/images/home-banner-final.png',
        showOverlay: false,
        title: '一键触达 轻松办理',
        subtitle: '一站式办理',
        actionText: '立即进入'
      }
    ],
    navs: [
      { name: '政策库', iconText: '📋', class: 'policy', handler: 'goToPolicy' },
      { name: '模板下载', iconText: '📄', class: 'template', handler: 'goToTemplate' },
      { name: '证明申请', iconText: '📝', class: 'apply', handler: 'goToApply' },
      { name: '党团流程', iconText: '🎗️', class: 'party', handler: 'goToParty' },
      { name: '学业分析', iconText: '📊', class: 'academic', handler: 'goToAcademic' },
      { name: '荣誉展示', iconText: '🏆', class: 'honor', handler: 'goToHonor' },
      { name: '通知聚合', iconText: '🔔', class: 'notice', handler: 'goToMessage' },
      { name: '常见问题', iconText: '❓', class: 'faq', handler: 'goToFaq' }
    ],
    pendingProgress: [],
    news: [
      { id: 'jobfair-2026-spring', title: '2026春季双选会即将开始！', source: '人大就业', time: '2026-03-25' },
      { id: 'innovation-2026', title: '关于组织申报2026年中国人民大学“大学生创新训练计划”创业训练项目的通知', source: '教务处', time: '2026-03-25' }
    ]
  },
  
  onLoad() {
    if (!app.isLoggedIn()) {
      wx.navigateTo({ url: '/sub-pages/login/index' })
      return
    }
    this.setData({ userInfo: app.globalData.userInfo })
    this.syncPendingProgress()
    this.loadHomeData()
  },

  onShow() {
    const tabBar = this.getTabBar && this.getTabBar()
    if (tabBar && tabBar.setData) {
      tabBar.setData({ selected: 0 })
    }
    this.syncPendingProgress()
  },

  async syncPendingProgress() {
    const studentId = Number(app.globalData.userInfo?.studentId || app.globalData.userInfo?.id || 0)
    const flowState = getPartyFlowState(studentId)
    const nextList = []

    try {
      if (!studentId || Number.isNaN(studentId)) {
        throw new Error('登录信息缺少学生ID')
      }

      const res = await applyApi.getApplyList(studentId)
      const applyList = Array.isArray(res.data)
        ? res.data
        : Array.isArray(res.data?.list)
          ? res.data.list
          : []
      const pendingApply = applyList
        .filter((item) => !isCompletedStatus(item.statusCode || item.status))
        .sort((a, b) => new Date((b.createdAt || '').replace(/-/g, '/')).getTime() - new Date((a.createdAt || '').replace(/-/g, '/')).getTime())[0]

      if (pendingApply) {
        nextList.push(buildApplyPendingCard(pendingApply))
      }
    } catch (e) {
      console.error('加载证明申请待办失败', e)
    }

    if (flowState.homePartyCard) {
      nextList.push(flowState.homePartyCard)
    }

    this.setData({ pendingProgress: nextList })
  },
  
  async loadHomeData() {
    try {
      const dashboardRes = await get('/student/dashboard')
      const dashboard = dashboardRes.data || {}
      
      if (dashboard.notices) {
        this.setData({
          news: dashboard.notices.map(n => ({
            id: n.id,
            title: n.title,
            source: n.source || '官方',
            time: n.publishTime || n.time || ''
          }))
        })
      }
    } catch (e) {
      console.error('加载首页数据失败', e)
    }
  },
  
  goToSearch() { wx.navigateTo({ url: '/sub-pages/search/index' }) },
  goToPolicy() { wx.navigateTo({ url: '/sub-pages/policy/list' }) },
  goToTemplate() { wx.navigateTo({ url: '/sub-pages/policy/template' }) },
  goToApply() { wx.navigateTo({ url: '/sub-pages/apply/list' }) },
  goToParty() { wx.navigateTo({ url: '/sub-pages/party/index' }) },
  goToAcademic() { wx.navigateTo({ url: '/sub-pages/academic/index' }) },
  goToHonor() { wx.navigateTo({ url: '/sub-pages/honor/index' }) },
  goToMessage() { wx.switchTab({ url: '/pages/message/index' }) },
  goToFaq() { wx.navigateTo({ url: '/sub-pages/faq/list' }) },
  
  goToNewsDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/message/detail?id=${id}` })
  },

  goToProgressDetail(e) {
    const { item } = e.currentTarget.dataset
    if (item.typeClass === 'apply') this.goToApply()
    else if (item.typeClass === 'party') this.goToParty()
  },

  goToProgressMore() {
    const list = this.data.pendingProgress || []
    const hasApply = list.some((item) => item.typeClass === 'apply')
    const hasParty = list.some((item) => item.typeClass === 'party')

    if (hasApply && !hasParty) {
      this.goToApply()
      return
    }

    if (!hasApply && hasParty) {
      this.goToParty()
      return
    }

    if (hasApply && hasParty) {
      wx.showActionSheet({
        itemList: ['我的证明/申请', '入党流程'],
        success: (res) => {
          if (res.tapIndex === 0) this.goToApply()
          if (res.tapIndex === 1) this.goToParty()
        }
      })
      return
    }

    wx.showToast({ title: '暂无待办事项', icon: 'none' })
  }
})
