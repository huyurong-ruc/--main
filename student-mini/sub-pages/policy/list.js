// sub-pages/policy/list.js
const app = getApp()
const { get } = require('../../api/request')

function normalizeTemplateItem(item = {}) {
  return {
    id: String(item.id || ''),
    title: item.title || item.templateName || '未命名模板',
    description: item.description || `用于${item.certificateType || '证明'}模板下载`,
    fileSize: item.fileSize || item.outputFormat || '-',
    department: item.department || '学院服务平台'
  }
}

Page({
  data: {
    activeTab: 0,
    tabs: ['政策文件', '通知通告', '模板下载'],
    policyList: [],
    noticeList: [],
    templateList: [],
    loading: false,
    page: 1,
    pageSize: 10,
    hasMore: true
  },
  
  onLoad(options = {}) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    const initialTab = Number(options.tab)
    const activeTab = Number.isInteger(initialTab) && initialTab >= 0 && initialTab <= 2 ? initialTab : 0
    this.setData({ activeTab })
    this.loadList()
  },
  
  onShow() {
    if (app.isLoggedIn()) {
      this.setData({ page: 1, hasMore: true })
      this.loadList()
    }
  },
  
  // 切换Tab
  onTabChange(e) {
    this.setData({ 
      activeTab: e.currentTarget.dataset.index,
      page: 1,
      hasMore: true,
      policyList: [],
      noticeList: [],
      templateList: []
    })
    this.loadList()
  },
  
  // 加载列表
  async loadList() {
    if (this.loading || !this.hasMore) return
    
    this.setData({ loading: true })
    
    const { activeTab, page, pageSize } = this.data
    let url = ''
    let params = { page, pageSize }
    
    // 根据 Tab 选择对应的后端接口
    if (activeTab === 0) {
      // 政策文件 - 学生侧分页（对接真实数据库 kb_policy）
      url = '/student/policies/page'
      params = { page: Math.max(page - 1, 0), size: pageSize }
    } else if (activeTab === 1) {
      // 通知通告 - 学生通知列表
      url = '/student/notices'
      params = {}
    } else {
      // 模板下载 - 知识模板
      url = '/certificate-templates/active'
      params = {}
    }
    
    try {
      const res = await get(url, params)
      // 处理不同接口的返回格式
      let list = []
      if (res.data?.content) {
        list = res.data.content
      } else if (res.data?.list) {
        list = res.data.list
      } else if (Array.isArray(res.data)) {
        list = res.data
      }
      
      if (activeTab === 0) {
        this.setData({
          policyList: page === 1 ? list : [...this.data.policyList, ...list]
        })
      } else if (activeTab === 1) {
        this.setData({
          noticeList: page === 1 ? list : [...this.data.noticeList, ...list]
        })
      } else {
        this.setData({
          templateList: page === 1
            ? list.map(normalizeTemplateItem)
            : [...this.data.templateList, ...list.map(normalizeTemplateItem)]
        })
      }
      
      this.setData({
        hasMore: list.length >= pageSize,
        page: page + 1
      })
    } catch (e) {
      console.error('加载列表失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 跳转详情
  goToDetail(e) {
    const { id, type } = e.currentTarget.dataset
    if (type === 'template') {
      wx.navigateTo({ url: `/sub-pages/policy/template?id=${id}` })
    } else {
      wx.navigateTo({ url: `/sub-pages/policy/detail?id=${id}` })
    }
  },

  downloadTemplate(e) {
    const { id } = e.currentTarget.dataset
    if (!id) {
      wx.showToast({ title: '模板信息缺失', icon: 'none' })
      return
    }
    wx.navigateTo({ url: `/sub-pages/policy/template?id=${id}` })
  },
  
  // 下拉刷新
  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true })
    this.loadList().finally(() => wx.stopPullDownRefresh())
  },
  
  // 上拉加载
  onReachBottom() {
    if (this.data.hasMore) this.loadList()
  }
})
