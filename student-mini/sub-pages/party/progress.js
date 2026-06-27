const app = getApp()
const { getPartyFlowState } = require('../../api/party-flow')

function resolveFlowKey(flow = {}, index = 0) {
  const raw = `${flow.flowCode || ''} ${flow.flowType || ''} ${flow.flowName || ''}`.toLowerCase()
  if (raw.includes('league') || raw.includes('入团')) return 'league'
  if (raw.includes('party') || raw.includes('入党')) return 'party'
  return index === 0 ? 'party' : index === 1 ? 'league' : `flow-${index + 1}`
}

function formatExpectedTime(expectedDays) {
  if (expectedDays === null || expectedDays === undefined || expectedDays === '') {
    return '-'
  }
  return `${expectedDays}天`
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
      statusText: stage.statusText || (status === 'completed' ? '已完成' : status === 'in_progress' ? '进行中' : '未完成'),
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
    title: flow.flowName || flow.flowCode || (flowKey === 'party' ? '入党流程' : '入团流程'),
    subtitle: flow.flowType || '',
    currentStage: flow.currentStage || '未开启',
    progressPercent: Number(flow.progressPercent || 0),
    stages
  }
}

Page({
  data: {
    type: 'party',
    detail: null,
    canView: false,
    emptyState: { title: '', description: '' },
    teacherAuditTip: '',
    loading: true
  },

  onLoad(options) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }

    if (options.type) {
      this.setData({ type: options.type })
    }
    this.loadProgress()
  },

  async loadProgress() {
    this.setData({ loading: true })

    try {
      const res = await getPartyFlowState()
      const data = res?.data || {}
      const flows = Array.isArray(data.flows) ? data.flows : []
      const normalized = flows.map((flow, index) => normalizeFlow(flow, index))
      const selected = normalized.find((flow) => flow.flowKey === this.data.type) || normalized[0] || null

      this.setData({
        detail: selected,
        canView: !!selected && selected.stages.length > 0,
        emptyState: {
          title: data.emptyTitle || '您暂未开启任何党团流程',
          description: data.emptyDescription || '当前账号尚未关联到任何党团流程记录'
        },
        teacherAuditTip: data.teacherAuditTip || '学生端仅支持查看，流程状态由管理端教师审核更新。',
        loading: false
      })
    } catch (error) {
      this.setData({
        detail: null,
        canView: false,
        emptyState: {
          title: '流程加载失败',
          description: '请稍后重试，或联系管理员检查学生党团流程数据。'
        },
        teacherAuditTip: '学生端仅支持查看，流程状态由管理端教师审核更新。',
        loading: false
      })
      console.error('加载流程详情失败:', error)
    }
  }
})
