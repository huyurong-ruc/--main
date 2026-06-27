// sub-pages/faq/submit.js
const {
  getTicketErrorMessage,
  submitTicket
} = require('../../api/faq')

let submitTimer = null

Page({
  data: {
    content: '',
    contact: '',
    submitting: false
  },
  
  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },
  
  onContactInput(e) {
    this.setData({ contact: e.detail.value })
  },
  
  handleSubmit() {
    if (submitTimer) {
      wx.showToast({ title: '请勿重复提交', icon: 'none' })
      return
    }
    
    if (!this.data.content.trim()) {
      wx.showToast({ title: '请填写问题内容', icon: 'none' })
      return
    }
    
    this.setData({ submitting: true })
    submitTimer = setTimeout(() => { submitTimer = null }, 2000)
    
    this.submitTicket()
  },
  
  async submitTicket() {
    try {
      const res = await submitTicket({
        questionText: this.data.content.trim(),
        contact: this.data.contact.trim()
      })
      const createdId = res?.data?.id ? String(res.data.id) : ''
      wx.showToast({ title: '提交成功', icon: 'success' })
      setTimeout(() => {
        wx.redirectTo({
          url: `/sub-pages/faq/ticket${createdId ? `?createdId=${createdId}` : ''}`
        })
      }, 300)
    } catch (e) {
      wx.showToast({
        title: getTicketErrorMessage(e, '提交失败，请稍后重试'),
        icon: 'none'
      })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
