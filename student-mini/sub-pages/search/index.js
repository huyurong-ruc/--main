// sub-pages/search/index.js
const TAB_TYPE_MAP = {
  0: 'all',
  1: 'policy',
  2: 'notice',
  3: 'qa',
  4: 'template'
}

function buildDisplayItem(item = {}) {
  const type = item.type
  if (type === 'policy') {
    return {
      ...item,
      typeIcon: '文',
      typeClass: 'policy',
      title: item.title,
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
      metaLeft: `类型：${item.fileType || '-'}`,
      metaRight: `更新：${item.updatedAt || '-'}`
    }
  }
  return item
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
    tabs: ['全部', '政策', '通知', '问答', '模板'],
    historyList: ['奖学金', '创新训练计划', '办事指南', '校历', '校车下行', '放假通知'],
    hotList: ['入党流程', '学业分析', '春季双选会', '学生证补办', '党组织关系'],
    resultList: [
      {
        id: 'p1',
        type: 'policy',
        title: '比选资助学生评审条件',
        department: '校人大',
        publishDate: '2025-03-28'
      },
      {
        id: 'p2',
        type: 'policy',
        title: '学生管理条例（节选）',
        department: '学生处',
        publishDate: '2026-03-15'
      },
      {
        id: 'n1',
        type: 'notice',
        title: '2026春季双选会即将开始',
        department: '人大就业',
        publishTime: '2025-03-25'
      },
      {
        id: 'n2',
        type: 'notice',
        title: '关于2026年春季学期选课安排的通知',
        department: '教务处',
        publishTime: '2026-04-10'
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
        updatedAt: '2025-03-25'
      },
      {
        id: 't2',
        type: 'template',
        fileName: '学生证补办申请表',
        fileType: 'DOCX',
        updatedAt: '2026-03-20'
      }
    ],
    filteredList: []
  },

  onLoad() {
    this.applyFilter()
  },
  
  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },
  
  handleSearch() {
    if (!this.data.keyword) return
    this.setData({ hasResult: true, activeTab: 0 })
    this.applyFilter()
  },
  
  clearSearch() {
    this.setData({ keyword: '', hasResult: false, activeTab: 0 })
    this.applyFilter()
  },
  
  goBack() {
    wx.navigateBack()
  },
  
  onTabChange(e) {
    this.setData({ activeTab: e.currentTarget.dataset.index })
    this.applyFilter()
  },
  
  onTagTap(e) {
    this.setData({ keyword: e.currentTarget.dataset.word, hasResult: true, activeTab: 0 })
    this.applyFilter()
  },
  
  clearHistory() {
    this.setData({ historyList: [] })
  },
  
  goToDetail(e) {
    const { item } = e.currentTarget.dataset
    // 根据类型跳转到对应详情页
  },

  applyFilter() {
    const list = (this.data.resultList || []).map(buildDisplayItem)
    const filteredList = filterByTab(list, this.data.activeTab)
    this.setData({ filteredList })
  }
})
