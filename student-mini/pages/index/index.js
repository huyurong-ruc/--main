// pages/index/index.js
const app = getApp()
const { get } = require('../../api/request')
const { getPartyFlowState } = require('../../api/party-flow')

const BASE_PENDING_PROGRESS = [
  {
    id: 1,
    title: '盖章申请',
    status: '审核中',
    statusClass: 'warning',
    showRightStatus: true,
    typeText: '申',
    typeClass: 'apply',
    desc: '申请时间：2025-03-25',
    time: ''
  }
]

Page({
  data: {
    userInfo: null,
    banners: [
      { id: 1, image: '/static/images/banner2.png' }
    ],
    navs: [
      { name: '智能检索', iconText: '🔍', class: 'search', handler: 'goToSearch' },
      { name: '政策库', iconText: '📋', class: 'policy', handler: 'goToPolicy' },
      { name: '模板下载', iconText: '📄', class: 'template', handler: 'goToTemplate' },
      { name: '证明申请', iconText: '📝', class: 'apply', handler: 'goToApply' },
      { name: '党团流程', iconText: '🎗️', class: 'party', handler: 'goToParty' },
      { name: '学业分析', iconText: '📊', class: 'academic', handler: 'goToAcademic' },
      { name: '通知聚合', iconText: '🔔', class: 'notice', handler: 'goToMessage' },
      { name: '常见问题', iconText: '❓', class: 'faq', handler: 'goToFaq' }
    ],
    pendingProgress: BASE_PENDING_PROGRESS,
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

  syncPendingProgress() {
    const studentId = app.globalData.userInfo?.studentId || app.globalData.userInfo?.id
    const flowState = getPartyFlowState(studentId)
    const nextList = [...BASE_PENDING_PROGRESS]

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
