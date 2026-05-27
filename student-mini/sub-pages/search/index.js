// sub-pages/search/index.js
const { getModuleSearchIndex } = require('../../api/search-index')

const TAB_TYPE_MAP = {
  0: 'all',
  1: 'policy',
  2: 'notice',
  3: 'qa',
  4: 'template',
  5: 'service'
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

function countOccurrences(text = '', keyword = '') {
  if (!text || !keyword) return 0
  const matches = String(text).match(new RegExp(escapeRegExp(keyword), 'ig'))
  return matches ? matches.length : 0
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

  const titleHits = tokens.reduce((sum, token) => sum + countOccurrences(lowerTitle, token.toLowerCase()), 0)
  const bodyHits = tokens.reduce((sum, token) => sum + countOccurrences(lowerBody, token.toLowerCase()), 0)
  const aliasHits = tokens.reduce((sum, token) => sum + countOccurrences(lowerAlias, token.toLowerCase()), 0)
  const titleFullMatch = lowerKeyword ? countOccurrences(lowerTitle, lowerKeyword) : 0
  const bodyFullMatch = lowerKeyword ? countOccurrences(lowerBody, lowerKeyword) : 0
  const aliasFullMatch = lowerKeyword ? countOccurrences(lowerAlias, lowerKeyword) : 0
  const exactTitleMatch = lowerKeyword && lowerTitle === lowerKeyword
  const exactBodyMatch = lowerKeyword && lowerBody === lowerKeyword
  const exactAliasMatch = lowerKeyword && lowerAlias.split('|').some((item) => item === lowerKeyword)
  const matched = !normalizedKeyword || titleHits > 0 || bodyHits > 0 || aliasHits > 0 || titleFullMatch > 0 || bodyFullMatch > 0 || aliasFullMatch > 0

  const score = (
    (exactTitleMatch ? 500 : 0) +
    (exactBodyMatch ? 300 : 0) +
    (exactAliasMatch ? 420 : 0) +
    (titleFullMatch * 180) +
    (bodyFullMatch * 110) +
    (aliasFullMatch * 150) +
    (titleHits * 45) +
    (aliasHits * 38) +
    (bodyHits * 18) +
    (normalizedKeyword ? normalizedKeyword.length : 0)
  )

  return {
    matched,
    score,
    titleHits,
    bodyHits,
    aliasHits,
    titleFullMatch,
    bodyFullMatch,
    aliasFullMatch
  }
}

function buildDisplayItem(item = {}) {
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
    .filter((item) => item.type === 'service')
    .sort((a, b) => {
      if (b.score !== a.score) return b.score - a.score
      if (b.aliasHits !== a.aliasHits) return b.aliasHits - a.aliasHits
      return 0
    })
    .slice(0, 4)
    .map((item) => ({
      id: item.id,
      title: item.title,
      desc: item.guideText || item.body,
      entryLabel: item.entryLabel || '进入模块',
      route: item.route,
      routeType: item.routeType,
      typeClass: item.typeClass
    }))
}

function filterByTab(list = [], activeTab = 0) {
  const type = TAB_TYPE_MAP[activeTab] || 'all'
  if (type === 'all') return list
  return list.filter((item) => item.type === type)
}

Page({
  data: {
    keyword: '',
    hasResult: false,
    activeTab: 0,
    tabs: ['全部', '政策', '通知', '问答', '模板', '功能'],
    historyList: ['证明申请', '学业分析', '办事指南', '成绩单', '消息中心', '放假通知'],
    hotList: ['入党流程', '教师资格证申请', '学生证补办', '个人档案', '通知聚合'],
    suggestionList: [],
    resultList: [
      {
        id: 'p1',
        type: 'policy',
        title: '比选资助学生评审条件',
        department: '校人大',
        publishDate: '2025-03-28',
        content: '面向家庭经济困难且综合表现优秀的学生，需提交成绩单、资助申请表和相关佐证材料。'
      },
      {
        id: 'p2',
        type: 'policy',
        title: '学生管理条例（节选）',
        department: '学生处',
        publishDate: '2026-03-15',
        content: '针对学籍异动、证明开具、请销假等学生事务办理流程进行统一说明。'
      },
      {
        id: 'n1',
        type: 'notice',
        title: '2026春季双选会即将开始',
        department: '人大就业',
        publishTime: '2025-03-25',
        content: '双选会将于本周五在世纪馆举办，现场提供简历诊断、岗位咨询与面试指导。'
      },
      {
        id: 'n2',
        type: 'notice',
        title: '关于2026年春季学期选课安排的通知',
        department: '教务处',
        publishTime: '2026-04-10',
        content: '本次选课分为预选、正选和补退选三个阶段，请同学按时完成课程确认。'
      },
      {
        id: 'q1',
        type: 'qa',
        category: '奖助学金',
        question: '奖助学金常见问题（FAQ）',
        answer: '可在“奖助信息”模块查看评审时间与材料要求。',
        updatedAt: '2025-03-25'
      },
      {
        id: 'q2',
        type: 'qa',
        category: '学籍事务',
        question: '如何办理在读证明？',
        answer: '在“证明申请”模块提交用途后即可申请。',
        updatedAt: '2026-04-01'
      },
      {
        id: 't1',
        type: 'template',
        fileName: '活动预算表',
        fileType: 'XLSX',
        updatedAt: '2025-03-25',
        description: '适用于学生活动经费预算申报，可填写预算科目、金额及审批说明。'
      },
      {
        id: 't2',
        type: 'template',
        fileName: '学生证补办申请表',
        fileType: 'DOCX',
        updatedAt: '2026-03-20',
        description: '用于学生证遗失补办申请，需填写个人信息、遗失原因和辅导员确认意见。'
      }
    ].concat(getModuleSearchIndex()),
    filteredList: []
  },

  onLoad() {
    this.applyFilter()
  },
  
  onSearchInput(e) {
    const keyword = e.detail.value
    const suggestionList = this.buildSuggestions(keyword)
    this.setData({ keyword, suggestionList })
    if (this.data.hasResult) {
      this.applyFilter(keyword)
    }
  },
  
  handleSearch() {
    const keyword = String(this.data.keyword || '').trim()
    if (!keyword) return
    this.setData({ keyword, hasResult: true, activeTab: 0 })
    this.applyFilter(keyword)
  },
  
  clearSearch() {
    this.setData({ keyword: '', hasResult: false, activeTab: 0, suggestionList: [] })
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
      wx.navigateTo({ url: '/sub-pages/faq/list' })
      return
    }

    if (item.type === 'service') {
      if (item.routeType === 'switchTab') {
        wx.switchTab({ url: item.route })
      } else {
        wx.navigateTo({ url: item.route })
      }
    }
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
      .sort((a, b) => {
        if (b.score !== a.score) return b.score - a.score
        if (b.type === 'service' && a.type !== 'service') return 1
        if (a.type === 'service' && b.type !== 'service') return -1
        if (b.aliasHits !== a.aliasHits) return b.aliasHits - a.aliasHits
        if (b.titleHits !== a.titleHits) return b.titleHits - a.titleHits
        if (b.bodyHits !== a.bodyHits) return b.bodyHits - a.bodyHits
        return String(a.title || '').length - String(b.title || '').length
      })

    const filteredList = filterByTab(list, this.data.activeTab)
    this.setData({
      filteredList,
      keyword: normalizedKeyword,
      suggestionList: this.buildSuggestions(normalizedKeyword)
    })
  }
})
