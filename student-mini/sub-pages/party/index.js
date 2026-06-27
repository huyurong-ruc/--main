const app = getApp()
const { getPartyFlowState } = require('../../api/party-flow')

function formatExpectedTime(expectedDays) {
  if (expectedDays === null || expectedDays === undefined || expectedDays === '') {
    return '-'
  }
  return `${expectedDays}天`
}

function getStatusText(status = '') {
  if (status === 'completed') return '已完成'
  if (status === 'in_progress') return '进行中'
  return '未完成'
}

function resolveFlowKey(flow = {}, index = 0) {
  const raw = `${flow.flowCode || ''} ${flow.flowType || ''} ${flow.flowName || ''}`.toLowerCase()
  if (raw.includes('league') || raw.includes('入团')) return 'league'
  if (raw.includes('party') || raw.includes('入党')) return 'party'
  return index === 0 ? 'party' : index === 1 ? 'league' : `flow-${index + 1}`
}

function normalizeStages(stages = []) {
  return stages.map((stage, index) => {
    const status = stage.status || (stage.completed ? 'completed' : 'pending')
    return {
      id: stage.id || `${index}`,
      name: stage.name || stage.nodeName || `节点${index + 1}`,
      description: stage.description || '',
      expectedTime: stage.expectedTime || formatExpectedTime(stage.expectedDays),
      time: stage.time || formatExpectedTime(stage.expectedDays),
      status,
      statusText: stage.statusText || getStatusText(status),
      statusVariant: stage.statusVariant || (status === 'completed' ? 'completed' : 'incomplete'),
      completed: stage.completed !== undefined ? !!stage.completed : status === 'completed',
      current: !!stage.current
    }
  })
}

function normalizeFlow(flow = {}, index = 0) {
  const flowKey = resolveFlowKey(flow, index)
  const stages = normalizeStages(flow.stages || [])
  return {
    flowKey,
    flowId: flow.flowId,
    label: flow.flowName || flow.flowCode || (flowKey === 'party' ? '入党流程' : '入团流程'),
    progress: flow.hasProgress ? {
      id: flow.flowId,
      title: flow.flowName || '',
      subtitle: flow.flowType || '',
      currentStage: flow.currentStage || '未开启',
      progressPercent: Number(flow.progressPercent || 0),
      stages
    } : null,
    flowList: stages.map((stage, stageIndex) => ({
      ...stage,
      index: stageIndex + 1
    })),
    hasProgress: !!flow.hasProgress,
    currentStage: flow.currentStage || '未开启'
  }
}

function sortFlows(a, b) {
  const order = { party: 0, league: 1 }
  const rankA = Object.prototype.hasOwnProperty.call(order, a.flowKey) ? order[a.flowKey] : 2
  const rankB = Object.prototype.hasOwnProperty.call(order, b.flowKey) ? order[b.flowKey] : 2
  if (rankA !== rankB) return rankA - rankB
  return String(a.label || '').localeCompare(String(b.label || ''), 'zh-Hans-CN')
}

Page({
  data: {
    activeTab: 0,
    tabs: [],
    tabKeys: [],
    activeFlowKey: '',
    activeFlowLabel: '',
    partyProgress: null,
    partyFlow: [],
    leagueProgress: null,
    leagueFlow: [],
    hasAnyFlow: false,
    emptyState: { title: '', description: '' },
    teacherAuditTip: '',
    loading: false,
    dataLoaded: false
  },

  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadData()
  },

  onShow() {
    if (!this.data.dataLoaded && app.isLoggedIn()) {
      this.loadData()
    }
  },

  onTabChange(e) {
    const index = parseInt(e.currentTarget.dataset.index, 10)
    if (Number.isNaN(index) || index === this.data.activeTab) return
    const flowKey = this.data.tabKeys[index] || ''
    this.setData({
      activeTab: index,
      activeFlowKey: flowKey,
      activeFlowLabel: this.data.tabs[index] || ''
    })
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const res = await getPartyFlowState()
      const data = res?.data || {}
      const flows = Array.isArray(data.flows) ? data.flows : []
      const normalized = flows.map((flow, index) => normalizeFlow(flow, index)).sort(sortFlows)
      const tabKeys = normalized.map((item) => item.flowKey)
      const tabs = normalized.map((item) => item.label)
      const partyItem = normalized.find((item) => item.flowKey === 'party') || null
      const leagueItem = normalized.find((item) => item.flowKey === 'league') || null
      const defaultIndex = partyItem ? tabKeys.indexOf('party') : (tabKeys.length > 0 ? 0 : -1)
      const activeTab = defaultIndex >= 0 ? defaultIndex : 0
      const activeFlowKey = tabKeys[activeTab] || ''
      const activeFlowLabel = tabs[activeTab] || ''

      this.setData({
        tabs,
        tabKeys,
        activeTab,
        activeFlowKey,
        activeFlowLabel,
        partyProgress: partyItem ? partyItem.progress : null,
        partyFlow: partyItem ? partyItem.flowList : [],
        leagueProgress: leagueItem ? leagueItem.progress : null,
        leagueFlow: leagueItem ? leagueItem.flowList : [],
        hasAnyFlow: normalized.length > 0,
        emptyState: {
          title: data.emptyTitle || '您暂未开启任何党团流程',
          description: data.emptyDescription || '当前账号尚未关联到任何党团流程记录'
        },
        teacherAuditTip: data.teacherAuditTip || '学生端仅支持查看，流程状态由管理端教师审核更新。',
        loading: false,
        dataLoaded: true
      })
    } catch (error) {
      this.setData({
        tabs: [],
        tabKeys: [],
        activeTab: 0,
        activeFlowKey: '',
        activeFlowLabel: '',
        partyProgress: null,
        partyFlow: [],
        leagueProgress: null,
        leagueFlow: [],
        hasAnyFlow: false,
        emptyState: {
          title: '流程加载失败',
          description: '请稍后重试，或联系管理员检查学生党团流程数据。'
        },
        teacherAuditTip: '学生端仅支持查看，流程状态由管理端教师审核更新。',
        loading: false,
        dataLoaded: true
      })
      console.error('加载党团流程失败:', error)
    }
  },

  goToProgress(e) {
    const { type } = e.currentTarget.dataset
    if (!type) return
    wx.navigateTo({ url: `/sub-pages/party/progress?type=${type}` })
  }
})
