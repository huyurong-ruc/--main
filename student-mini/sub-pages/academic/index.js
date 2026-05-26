const app = getApp()
const { getAcademicOverview } = require('../../api/academic')

function normalizeReport(raw = {}) {
  const modules = (raw.modules || []).map((m) => ({
    ...m,
    percent: m.total > 0 ? Math.round((m.current / m.total) * 100) : 0
  }))
  return {
    ...raw,
    percent: raw.percent ?? 0,
    currentCredits: raw.currentCredits ?? 0,
    totalCredits: raw.totalCredits ?? 0,
    modules,
    updateTime: raw.updateTime || '-'
  }
}

Page({
  data: {
    report: null,
    loading: false
  },

  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadReport()
  },

  onShow() {
    if (app.isLoggedIn()) {
      this.loadReport()
    }
  },

  async loadReport() {
    this.setData({ loading: true })
    try {
      const studentId = app.globalData.userInfo?.studentId || app.globalData.userInfo?.id
      const res = await getAcademicOverview(studentId)
      this.setData({ report: normalizeReport(res.data) })
    } catch (e) {
      console.error('加载学业报告失败', e)
      wx.showToast({ title: '加载失败，请重试', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  goToUpload() {
    wx.navigateTo({ url: '/sub-pages/academic/upload' })
  },

  goToReport() {
    wx.navigateTo({ url: '/sub-pages/academic/report' })
  }
})
