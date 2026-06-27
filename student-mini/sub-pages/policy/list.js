// sub-pages/policy/list.js
const app = getApp()
const policyApi = require('../../api/policy')

function safeArray(value) {
  return Array.isArray(value) ? value : []
}

function isQaCategory(category = '') {
  const raw = String(category || '')
  const lower = raw.toLowerCase()
  return lower.includes('faq') || raw.includes('问答') || raw.includes('FAQ管理') || raw.includes('faq管理')
}

function normalizeTime(value = '') {
  if (!value) return ''
  const text = String(value)
  return text.includes('T') ? text.replace('T', ' ').slice(0, 16) : text.slice(0, 16)
}

function normalizePolicyItem(item = {}) {
  return {
    id: String(item.id || ''),
    title: item.title || '未命名政策',
    category: item.category || '政策',
    summary: `${item.category || '政策'}相关文件，可进入详情查看完整内容`,
    publishTime: normalizeTime(item.updatedAt || item.publishTime || ''),
    officialUrl: item.officialUrl || ''
  }
}

function normalizeNoticeItem(item = {}) {
  return {
    id: String(item.id || ''),
    title: item.title || '未命名通知',
    summary: item.summary || '暂无摘要',
    publishTime: normalizeTime(item.publishTime || ''),
    tags: safeArray(item.tags)
  }
}

function normalizeTemplateItem(item = {}) {
  return {
    id: String(item.id || ''),
    title: item.title || item.templateName || '未命名模板',
    description: item.description || `用于${item.certificateType || '证明'}模板下载`,
    fileSize: item.fileSize || item.outputFormat || '-',
    department: item.department || '学院服务平台',
    updatedAt: normalizeTime(item.updatedAt || ''),
    fileUrl: item.fileUrl || item.templateFilePath || ''
  }
}

function buildPolicyErrorMessage(error = {}) {
  const rawMessage = String(
    error?.message || error?.data?.message || error?.msg || error?.errMsg || ''
  ).trim()

  if (!rawMessage) return '当前分类内容加载失败，请稍后重试'
  if (/404|不存在/.test(rawMessage)) return '接口不存在，请联系管理员检查配置'
  if (/timeout|超时/i.test(rawMessage)) return '请求超时，请点击重试'
  if (/500|内部错误|server/i.test(rawMessage)) return '服务异常，请稍后重试'
  return rawMessage
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
    hasMore: true,
    errorMessage: ''
  },
  
  onLoad(options = {}) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    const initialTab = Number(options.tab)
    const activeTab = Number.isInteger(initialTab) && initialTab >= 0 && initialTab <= 2 ? initialTab : 0
    this.tabStores = { 0: [], 1: [], 2: [] }
    this.setData({ activeTab })
    this.refreshCurrentTab()
  },
  
  onShow() {
    if (app.isLoggedIn() && this.tabStores && this._loadedOnce) {
      this.refreshCurrentTab()
    }
  },

  refreshCurrentTab() {
    this.setData({
      page: 1,
      hasMore: true,
      errorMessage: '',
      policyList: [],
      noticeList: [],
      templateList: []
    })
    return this.loadList(true)
  },
  
  // 切换Tab
  onTabChange(e) {
    const nextTab = Number(e.currentTarget.dataset.index)
    if (nextTab === this.data.activeTab) return
    this.setData({ 
      activeTab: nextTab,
      page: 1,
      hasMore: true,
      errorMessage: '',
      policyList: [],
      noticeList: [],
      templateList: []
    })
    this.loadList(true)
  },
  
  // 加载列表
  async loadList(reset = false) {
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return

    this.setData({ loading: true })

    const { activeTab, page, pageSize } = this.data

    try {
      if (activeTab === 0) {
        let backendPage = Math.max(page - 1, 0)
        let totalPages = 0
        const incomingList = []

        do {
          const res = await policyApi.getPolicyPage({
            page: backendPage,
            size: pageSize
          })
          const pageData = res?.data || {}
          totalPages = Number(pageData.totalPages || 0)
          incomingList.push(
            ...safeArray(pageData.content)
              .filter((item) => !isQaCategory(item.category))
              .map(normalizePolicyItem)
          )
          backendPage += 1
        } while (incomingList.length < pageSize && backendPage < totalPages)

        this.setData({
          policyList: reset ? incomingList : this.data.policyList.concat(incomingList),
          hasMore: backendPage < totalPages,
          page: backendPage + 1,
          errorMessage: ''
        })
      } else {
        if (reset || !safeArray(this.tabStores[activeTab]).length) {
          const res = activeTab === 1
            ? await policyApi.getNotices()
            : await policyApi.getTemplates()
          const mappedList = activeTab === 1
            ? safeArray(res?.data).map(normalizeNoticeItem)
            : safeArray(res?.data).map(normalizeTemplateItem)
          this.tabStores[activeTab] = mappedList
        }

        const fullList = safeArray(this.tabStores[activeTab])
        const nextLength = page * pageSize
        const visibleList = fullList.slice(0, nextLength)
        const payload = {
          hasMore: nextLength < fullList.length,
          page: page + 1,
          errorMessage: ''
        }

        if (activeTab === 1) {
          payload.noticeList = visibleList
        } else {
          payload.templateList = visibleList
        }

        this.setData(payload)
      }
      this._loadedOnce = true
    } catch (e) {
      console.error('加载列表失败', e)
      this.setData({
        errorMessage: buildPolicyErrorMessage(e),
        hasMore: false
      })
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 跳转详情
  goToDetail(e) {
    const { id, type } = e.currentTarget.dataset
    if (type === 'template') {
      wx.navigateTo({ url: `/sub-pages/policy/template?id=${id}` })
    } else if (type === 'notice') {
      wx.navigateTo({ url: `/pages/message/detail?id=${id}` })
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
    this.refreshCurrentTab().finally(() => wx.stopPullDownRefresh())
  },
  
  // 上拉加载
  onReachBottom() {
    if (this.data.hasMore) this.loadList(false)
  },

  retryLoad() {
    this.refreshCurrentTab()
  }
})
