// sub-pages/search/result.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    keyword: '',
    results: [],
    loading: false,
    empty: false
  },
  
  onLoad(options) {
    if (options.keyword) {
      this.setData({ keyword: options.keyword })
      this.doSearch()
    }
  },
  
  onShareAppMessage() {
    return {
      title: `搜索: ${this.data.keyword}`,
      path: `/sub-pages/search/result?keyword=${this.data.keyword}`
    }
  },
  
  async doSearch() {
    if (!this.data.keyword) return
    
    this.setData({ loading: true, empty: false })
    
    try {
      const res = await get('/search', { keyword: this.data.keyword })
      const results = res.data || []
      this.setData({ 
        results, 
        empty: results.length === 0,
        loading: false 
      })
    } catch (e) {
      console.error('搜索失败', e)
      this.setData({ loading: false })
    }
  },
  
  // 跳转详情
  goToDetail(e) {
    const { type, id } = e.currentTarget.dataset
    if (type === 'policy') {
      wx.navigateTo({ url: `/sub-pages/policy/detail?id=${id}` })
    } else if (type === 'notice') {
      wx.navigateTo({ url: `/pages/message/detail?id=${id}` })
    } else if (type === 'template') {
      wx.navigateTo({ url: `/sub-pages/policy/template?id=${id}` })
    } else if (type === 'apply') {
      wx.navigateTo({ url: `/sub-pages/apply/detail?id=${id}` })
    }
  }
})
