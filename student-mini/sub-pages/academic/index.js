const app = getApp()
const { getAcademicOverview, getAcademicTranscriptStatus } = require('../../api/academic')

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
    loading: false,
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
    this.loadAcademicData()
  },

  onShow() {
    if (app.isLoggedIn()) {
      this.loadAcademicData()
    }
  },

  async loadAcademicData() {
    this.setData({ loading: true })
    try {
      const studentId = app.globalData.userInfo?.studentId || app.globalData.userInfo?.id
      const statusRes = await getAcademicTranscriptStatus(studentId)
      const transcriptStatus = statusRes.data || {}

      if (!transcriptStatus.canLoadReport) {
        this.setData({
          transcriptStatus,
          report: null
        })
        return
      }

      const res = await getAcademicOverview(studentId)
      this.setData({
        transcriptStatus,
        report: res.data ? normalizeReport(res.data) : null
      })
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
    if (!this.data.transcriptStatus.canLoadReport) {
      wx.showToast({ title: '请先上传成绩单', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/sub-pages/academic/report' })
  }
})
