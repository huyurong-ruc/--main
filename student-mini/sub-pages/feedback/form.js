const app = getApp()
const { submitFeedback } = require('../../api/feedback')

const MIN_CONTENT_LENGTH = 5
const DEBOUNCE_MS = 2000

Page({
  data: {
    type: 1,
    types: [
      { id: 1, name: '功能异常' },
      { id: 2, name: '产品建议' },
      { id: 3, name: '其他问题' }
    ],
    content: '',
    contact: '',
    attachments: [],
    submitting: false
  },

  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
  },

  onTypeChange(e) {
    this.setData({ type: this.data.types[e.detail.value].id })
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  onContactInput(e) {
    this.setData({ contact: e.detail.value })
  },

  chooseImage() {
    const remain = 3 - this.data.attachments.length
    if (remain <= 0) {
      wx.showToast({ title: '最多3张图片', icon: 'none' })
      return
    }
    wx.chooseImage({
      count: remain,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.setData({
          attachments: this.data.attachments.concat(res.tempFilePaths)
        })
      }
    })
  },

  removeImage(e) {
    const { index } = e.currentTarget.dataset
    const attachments = [...this.data.attachments]
    attachments.splice(index, 1)
    this.setData({ attachments })
  },

  handleSubmit() {
    if (this.data.submitting) {
      wx.showToast({ title: '请勿重复提交', icon: 'none' })
      return
    }

    const content = this.data.content.trim()
    if (!content) {
      wx.showToast({ title: '请填写反馈内容', icon: 'none' })
      return
    }
    if (content.length < MIN_CONTENT_LENGTH) {
      wx.showToast({ title: `反馈内容至少${MIN_CONTENT_LENGTH}个字`, icon: 'none' })
      return
    }

    // 管理端反馈处理模块尚未建设，暂时拦截提交并友好提示
    wx.showModal({
      title: '提示',
      content: '意见反馈功能正在持续开发中，预计后续版本上线',
      showCancel: false
    })
    return
  },

  async submitFeedback() {
    wx.showLoading({ title: '提交中...', mask: true })
    try {
      await submitFeedback({
        type: this.data.type,
        content: this.data.content.trim(),
        contact: this.data.contact.trim(),
        images: this.data.attachments
      })
      wx.showToast({ title: '提交成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1200)
    } catch (e) {
      console.error('提交反馈失败', e)
      wx.showToast({ title: '提交失败，请重试', icon: 'none' })
      this.setData({ submitting: false })
    } finally {
      wx.hideLoading()
    }
  }
})
