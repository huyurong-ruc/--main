// sub-pages/academic/report.js
const app = getApp()
const { getAcademicReport, getAcademicTranscriptStatus } = require('../../api/academic')

Page({
  data: {
    report: null,
    loading: true,
    transcriptStatus: {
      hasCurrentTranscript: false,
      hasHistoryTranscript: false,
      canLoadReport: false
    }
  },
  
  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadPageData()
  },
  
  onShow() {
    if (app.isLoggedIn()) {
      this.loadPageData()
    }
  },

  async loadPageData() {
    this.setData({ loading: true })
    
    try {
      const studentId = app.globalData.userInfo?.studentId || app.globalData.userInfo?.id || 1
      const statusRes = await getAcademicTranscriptStatus(studentId)
      const transcriptStatus = statusRes.data || {}

      if (!transcriptStatus.canLoadReport) {
        this.setData({ transcriptStatus, report: null })
        return
      }

      const res = await getAcademicReport(studentId)
      this.setData({ report: res.data, transcriptStatus })
    } catch (e) {
      console.error('加载报告失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },

  goToUpload() {
    wx.navigateTo({ url: '/sub-pages/academic/upload' })
  },
  
  // 分享
  onShareAppMessage() {
    return {
      title: '我的学业分析报告',
      path: '/sub-pages/academic/report'
    }
  }
})
