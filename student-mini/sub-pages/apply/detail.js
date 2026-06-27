// sub-pages/apply/detail.js
const app = getApp()
const applyApi = require('../../api/apply')
const { getApplyDisplayTitle } = require('../../api/apply-business')
const { getApplyStatusMeta, normalizeApplyStatus } = require('../../api/apply-status')

function formatApplyTime(value = '') {
  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day, hour = 0, minute = 0] = value
    return `${year}-${String(month || 1).padStart(2, '0')}-${String(day || 1).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
  }

  const text = String(value || '').trim()
  if (!text) return ''
  const normalized = text.includes('T') ? text.replace('T', ' ') : text
  return normalized.length >= 16 ? normalized.slice(0, 16) : normalized
}

function normalizeDetail(payload = {}) {
  const statusRaw = payload.statusCode || payload.status || payload.statusText || ''
  const statusCode = normalizeApplyStatus(statusRaw)
  const meta = getApplyStatusMeta(statusCode)
  const rawTypeName = payload.typeName || payload.certificateType || payload.typeTitle || payload.title || '申请详情'
  const rawCreateTime = payload.submittedAt || payload.createTime || payload.createdAt || payload.createAt || payload.time || ''

  return {
    ...payload,
    typeName: getApplyDisplayTitle(rawTypeName, payload.typeKey),
    status: statusCode,
    statusText: payload.statusText || meta.detailLabel || meta.listLabel,
    statusTitle: payload.statusTitle || meta.title,
    statusDescription: payload.statusDescription || meta.description,
    createTime: formatApplyTime(rawCreateTime),
    canCancel: typeof payload.canWithdraw === 'boolean'
      ? payload.canWithdraw
      : (typeof payload.canCancel === 'boolean' ? payload.canCancel : meta.canCancel)
  }
}

Page({
  data: {
    id: '',
    detail: null,
    loading: true,
    canceling: false
  },
  
  onLoad(options) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    
    if (options.id) {
      this.setData({ id: options.id })
      this.loadDetail()
    }
  },
  
  async loadDetail() {
    this.setData({ loading: true })
    
    try {
      const res = await applyApi.getApplyDetail(this.data.id)
      this.setData({ detail: normalizeDetail(res.data) })
    } catch (e) {
      console.error('加载详情失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 撤回申请
  handleCancel() {
    wx.showModal({
      title: '提示',
      content: '确定要撤回此申请吗？',
      success: async (res) => {
        if (res.confirm) {
          await this.cancelApply()
        }
      }
    })
  },
  
  async cancelApply() {
    this.setData({ canceling: true })
    
    try {
      await applyApi.cancelApply(this.data.id)
      wx.showToast({ title: '已撤回', icon: 'success' })
      this.loadDetail()
    } catch (e) {
      wx.showToast({ title: '撤回失败', icon: 'none' })
    } finally {
      this.setData({ canceling: false })
    }
  },

  showStatusHelp() {
    const detail = this.data.detail || {}
    wx.showModal({
      title: detail.statusTitle || '状态说明',
      content: detail.statusDescription || '暂无状态说明',
      showCancel: false,
      confirmText: '我知道了'
    })
  },
  
  // 下载附件
  downloadFile(e) {
    const { url, name } = e.currentTarget.dataset
    wx.showLoading({ title: '下载中...' })
    
    wx.downloadFile({
      url,
      success: (res) => {
        wx.hideLoading()
        wx.openDocument({
          filePath: res.tempFilePath,
          success: () => console.log('打开成功'),
          fail: () => wx.showToast({ title: '打开失败', icon: 'none' })
        })
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '下载失败', icon: 'none' })
      }
    })
  }
})
