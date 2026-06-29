// sub-pages/search/index.js
const policyApi = require('../../api/policy')

const TAB_TYPE_MAP = {
  0: 'all',
  1: 'policy',
  2: 'notice',
  3: 'qa',
  4: 'template'
}

function escapeRegExp(text = '') {
  return String(text).replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function tokenizeKeyword(keyword = '') {
  const normalized = String(keyword || '').trim()
  if (!normalized) return []
  const parts = normalized.split(/\s+/).filter(Boolean)
  return [...new Set(parts)]
}

function splitTextWithHighlight(text = '', keywords = []) {
  const content = String(text || '')
  const validKeywords = (keywords || []).filter(Boolean).sort((a, b) => b.length - a.length)
  if (!content || validKeywords.length === 0) {
    return [{ text: content, highlight: false }]
  }

  const pattern = validKeywords.map((item) => escapeRegExp(item)).join('|')
  if (!pattern) {
    return [{ text: content, highlight: false }]
  }

  const regex = new RegExp(`(${pattern})`, 'ig')
  const parts = content.split(regex).filter((item) => item !== '')
  return parts.map((part) => ({
    text: part,
    highlight: validKeywords.some((keyword) => part.toLowerCase() === keyword.toLowerCase())
  }))
}

function buildSearchMetrics(title = '', body = '', keyword = '', aliasText = '') {
  const normalizedKeyword = String(keyword || '').trim()
  const tokens = tokenizeKeyword(normalizedKeyword)
  const lowerTitle = String(title || '').toLowerCase()
  const lowerBody = String(body || '').toLowerCase()
  const lowerAlias = String(aliasText || '').toLowerCase()
  const lowerKeyword = normalizedKeyword.toLowerCase()

  const titleHits = tokens.reduce((sum, token) => sum + (lowerTitle.includes(token.toLowerCase()) ? 1 : 0), 0)
  const bodyHits = tokens.reduce((sum, token) => sum + (lowerBody.includes(token.toLowerCase()) ? 1 : 0), 0)
  const aliasHits = tokens.reduce((sum, token) => sum + (lowerAlias.includes(token.toLowerCase()) ? 1 : 0), 0)
  const titleFullMatch = lowerKeyword && lowerTitle.includes(lowerKeyword) ? 1 : 0
  const bodyFullMatch = lowerKeyword && lowerBody.includes(lowerKeyword) ? 1 : 0
  const aliasFullMatch = lowerKeyword && lowerAlias.includes(lowerKeyword) ? 1 : 0
  const exactTitleMatch = lowerKeyword && lowerTitle === lowerKeyword
  const exactBodyMatch = lowerKeyword && lowerBody === lowerKeyword
  const exactAliasMatch = lowerKeyword && lowerAlias.split('|').some((item) => item === lowerKeyword)
  const matched = !normalizedKeyword || titleHits > 0 || bodyHits > 0 || aliasHits > 0 || titleFullMatch > 0 || bodyFullMatch > 0 || aliasFullMatch > 0

  return {
    matched,
    titleHits,
    bodyHits,
    aliasHits,
    titleFullMatch,
    bodyFullMatch,
    aliasFullMatch
  }
}

function buildDisplayItem(item = {}) {
  if (item.title && item.body && item.metaLeft && item.metaRight) {
    const iconMap = {
      policy: ['文', 'policy'],
      notice: ['知', 'notice'],
      qa: ['问', 'faq'],
      template: ['模', 'template'],
      service: ['服', 'service']
    }
    const [typeIcon, typeClass] = iconMap[item.type] || ['文', 'policy']
    return {
      ...item,
      typeIcon,
      typeClass
    }
  }
  const type = item.type
  if (type === 'policy') {
    return {
      ...item,
      typeIcon: '文',
      typeClass: 'policy',
      title: item.title,
      body: item.summary || item.content || item.description || `${item.department || '发布部门'}相关政策文件`,
      metaLeft: `部门：${item.department || '-'}`,
      metaRight: `发布：${item.publishDate || '-'}`
    }
  }
  if (type === 'notice') {
    return {
      ...item,
      typeIcon: '知',
      typeClass: 'notice',
      title: item.title,
      body: item.summary || item.content || `${item.department || '发布单位'}通知`,
      metaLeft: `来源：${item.department || '-'}`,
      metaRight: `时间：${item.publishTime || '-'}`
    }
  }
  if (type === 'qa') {
    return {
      ...item,
      typeIcon: '问',
      typeClass: 'faq',
      title: item.question,
      body: item.answer || '暂无问答说明',
      metaLeft: `分类：${item.category || '问答'}`,
      metaRight: `更新：${item.updatedAt || '-'}`
    }
  }
  if (type === 'template') {
    return {
      ...item,
      typeIcon: '模',
      typeClass: 'template',
      title: item.fileName,
      body: item.description || `${item.fileType || '文件'} 模板，可下载后填写`,
      metaLeft: `类型：${item.fileType || '-'}`,
      metaRight: `更新：${item.updatedAt || '-'}`
    }
  }
  if (type === 'service') {
    return {
      ...item,
      typeIcon: '服',
      typeClass: 'service',
      title: item.name,
      body: `${item.description || ''} ${item.guide || ''}`.trim(),
      metaLeft: `分类：${item.category || '功能模块'}`,
      metaRight: item.entryLabel || '进入模块',
      aliasText: [...(item.aliases || []), ...(item.keywords || [])].join('|'),
      guideText: item.guide || '',
      route: item.route,
      routeType: item.routeType
    }
  }
  return item
}

function buildSuggestionList(keyword = '', list = []) {
  const normalizedKeyword = String(keyword || '').trim()
  if (!normalizedKeyword) return []

  return list
    .slice(0, 4)
    .map((item) => ({
      id: item.id,
      type: item.type,
      title: item.title,
      desc: item.body || '',
      entryLabel: item.type === 'notice' ? '查看通知' : (item.type === 'template' ? '查看模板' : '查看详情'),
      typeClass: item.typeClass,
      typeIcon: item.typeIcon
    }))
}

function filterByTab(list = [], activeTab = 0) {
  const type = TAB_TYPE_MAP[activeTab] || 'all'
  if (type === 'all') return list
  return list.filter((item) => item.type === type)
}

function isQaCategory(category = '') {
  const raw = String(category || '')
  const lower = raw.toLowerCase()
  return lower.includes('faq') || raw.includes('问答') || raw.includes('FAQ管理') || raw.includes('faq管理')
}

function safeArray(value) {
  return Array.isArray(value) ? value : []
}

function setDataAsync(ctx, payload) {
  return new Promise((resolve) => ctx.setData(payload, resolve))
}

Page({
  data: {
    keyword: '',
    hasResult: false,
    activeTab: 0,
    tabs: ['全部', '政策', '通知', '问答', '模板'],
    historyList: ['证明申请', '学业分析', '办事指南', '成绩单', '消息中心', '放假通知'],
    hotList: ['入党流程', '教师资格证申请', '学生证补办', '个人档案', '通知聚合'],
    suggestionList: [],
    resultList: [],
    filteredList: []
  },

  onLoad() {
    this.remoteCache = {}
    this.remoteSearchTimer = null
    this.applyFilter()
  },
  
  onSearchInput(e) {
    const keyword = e.detail.value
    this.setData({ keyword, suggestionList: [] })
    this.applyFilter(keyword)
    this.triggerRemoteSearch(keyword)
  },
  
  handleSearch() {
    const keyword = String(this.data.keyword || '').trim()
    if (!keyword) return
    this.setData({ keyword, hasResult: true, activeTab: 0 })
    this.triggerRemoteSearch(keyword, { immediate: true })
  },
  
  clearSearch() {
    this.setData({ keyword: '', hasResult: false, activeTab: 0, suggestionList: [] })
    if (this.remoteSearchTimer) {
      clearTimeout(this.remoteSearchTimer)
      this.remoteSearchTimer = null
    }
    this.remoteCache = {}
    this.setData({ resultList: [] })
    this.applyFilter()
  },
  
  goBack() {
    wx.navigateBack()
  },
  
  onTabChange(e) {
    this.setData({ activeTab: e.currentTarget.dataset.index })
    this.applyFilter(this.data.keyword)
  },
  
  onTagTap(e) {
    const keyword = e.currentTarget.dataset.word
    this.setData({ keyword, hasResult: true, activeTab: 0, suggestionList: [] })
    this.applyFilter(keyword)
  },
  
  clearHistory() {
    this.setData({ historyList: [] })
  },
  
  goToDetail(e) {
    const { item } = e.currentTarget.dataset
    this.openSearchItem(item)
  },

  onSuggestionTap(e) {
    const item = e.currentTarget.dataset.item
    this.openSearchItem(item)
  },

  triggerRemoteSearch(keyword = '', options = {}) {
    const normalized = String(keyword || '').trim()
    const immediate = options.immediate === true

    if (this.remoteSearchTimer) {
      clearTimeout(this.remoteSearchTimer)
      this.remoteSearchTimer = null
    }

    if (!normalized) {
      this.setData({ resultList: [] })
      this.applyFilter('')
      return
    }

    if (immediate) {
      this.performRemoteSearch(normalized)
      return
    }

    this.remoteSearchTimer = setTimeout(() => {
      this.remoteSearchTimer = null
      this.performRemoteSearch(normalized)
    }, 450)
  },

  async performRemoteSearch(keyword = '') {
    const normalizedKeyword = String(keyword || '').trim()
    if (!normalizedKeyword) return

    if (this.remoteCache && this.remoteCache[normalizedKeyword]) {
      await setDataAsync(this, { resultList: this.remoteCache[normalizedKeyword] || [] })
      this.applyFilter(normalizedKeyword)
      return
    }

    const res = await policyApi.searchAll({ keyword: normalizedKeyword })
    const mergedRemote = safeArray(res?.data).filter((item) => item && item.id)
    this.remoteCache[normalizedKeyword] = mergedRemote
    await setDataAsync(this, { resultList: mergedRemote })
    this.applyFilter(normalizedKeyword)
  },

  buildSuggestions(keyword = '') {
    const normalizedKeyword = String(keyword || '').trim()
    if (!normalizedKeyword) return []

    const preparedList = (this.data.resultList || [])
      .map(buildDisplayItem)
      .map((item) => ({
        ...item,
        ...buildSearchMetrics(item.title, item.body, normalizedKeyword, item.aliasText)
      }))
      .filter((item) => item.matched)

    return buildSuggestionList(normalizedKeyword, preparedList)
  },

  openSearchItem(item = {}) {
    if (!item) return

    if (item.type === 'policy') {
      wx.navigateTo({ url: `/sub-pages/policy/detail?id=${item.id}` })
      return
    }

    if (item.type === 'notice') {
      wx.navigateTo({ url: `/pages/message/detail?id=${item.id}` })
      return
    }

    if (item.type === 'template') {
      wx.navigateTo({ url: `/sub-pages/policy/template?id=${item.id}` })
      return
    }

    if (item.type === 'qa') {
      wx.navigateTo({ url: `/sub-pages/policy/detail?id=${item.id}` })
      return
    }

    return
  },

  applyFilter(keyword = this.data.keyword) {
    const normalizedKeyword = String(keyword || '').trim()
    const tokens = tokenizeKeyword(normalizedKeyword)
    const searchTerms = normalizedKeyword && tokens.length > 1
      ? [normalizedKeyword, ...tokens]
      : tokens

    const list = (this.data.resultList || [])
      .map(buildDisplayItem)
      .map((item) => {
        const metrics = buildSearchMetrics(item.title, item.body, normalizedKeyword, item.aliasText)
        return {
          ...item,
          ...metrics,
          titleSegments: splitTextWithHighlight(item.title, searchTerms),
          bodySegments: splitTextWithHighlight(item.body, searchTerms),
          entrySegments: splitTextWithHighlight(item.metaRight || '', searchTerms)
        }
      })
      .filter((item) => !normalizedKeyword || item.matched)

    const filteredList = filterByTab(list, this.data.activeTab)
    this.setData({
      filteredList,
      keyword: normalizedKeyword,
      suggestionList: this.buildSuggestions(normalizedKeyword)
    })
  }
})
