const LEAGUE_FLOW_DEFINITIONS = [
  { id: 'league-1', name: '提交入团申请书', expectedTime: '2025-09', description: '向团组织提交入团申请，完成基础信息登记。' },
  { id: 'league-2', name: '确定为入团积极分子', expectedTime: '2025-10', description: '经支部大会讨论确定为入团积极分子，进入培养考察期。' },
  { id: 'league-3', name: '参加团课培训', expectedTime: '2025-11', description: '参加团课培训并完成结业考核。' },
  { id: 'league-4', name: '政治审查', expectedTime: '2025-12', description: '由老师发起政治审查并整理审核材料。' },
  { id: 'league-5', name: '支部大会讨论', expectedTime: '2026-01', description: '支部大会讨论是否接收为共青团员。' },
  { id: 'league-6', name: '上级团组织审批', expectedTime: '2026-02', description: '上级团组织审批通过后，正式成为共青团员。' }
]

const PARTY_FLOW_DEFINITIONS = [
  { id: 'party-1', name: '提交入党申请书', expectedTime: '2026-03', description: '向党组织提交入党申请，表明入党意愿和认识。' },
  { id: 'party-2', name: '确定为入党积极分子', expectedTime: '2026-06', description: '经支部大会讨论确定为入党积极分子，进入培养考察期。' },
  { id: 'party-3', name: '参加培训考核', expectedTime: '2026-09', description: '完成党校培训课程并通过考核。' },
  { id: 'party-4', name: '确定为发展对象', expectedTime: '2027-03', description: '由管理端教师审核培养情况并确认发展对象资格。' },
  { id: 'party-5', name: '政治审查', expectedTime: '2027-04', description: '由教师发起政治审查并形成政审材料。' },
  { id: 'party-6', name: '支部大会讨论', expectedTime: '2027-05', description: '支部大会讨论接收预备党员并形成决议。' },
  { id: 'party-7', name: '上级党委审批', expectedTime: '2027-06', description: '上级党委审批通过后，进入预备党员阶段。' },
  { id: 'party-8', name: '预备期考察', expectedTime: '2028-06', description: '预备期内接受党组织教育、考察与阶段性审核。' },
  { id: 'party-9', name: '转为正式党员', expectedTime: '2028-07', description: '预备期满后由教师提交转正审核，完成正式党员审批。' }
]

const MOCK_PARTY_FLOW_SCENARIOS = {
  '2023100001': {
    leagueStatuses: ['completed', 'completed', 'in_progress', 'pending', 'pending', 'pending'],
    partyStatuses: ['pending', 'pending', 'pending', 'pending', 'pending', 'pending', 'pending', 'pending', 'pending']
  },
  '2023100002': {
    leagueStatuses: ['completed', 'completed', 'completed', 'completed', 'completed', 'completed'],
    partyStatuses: ['completed', 'completed', 'completed', 'in_progress', 'pending', 'pending', 'pending', 'pending', 'pending']
  }
}

const EMPTY_STATE = {
  title: '您暂未开启任何党团流程',
  description: '请完成前置入团申请后等待老师审核激活'
}

const PARTY_GATE_STATE = {
  title: '入党流程暂未开放',
  description: '请先完成入团流程全部节点，待管理端教师审核通过后再查看入党流程'
}

const READONLY_TIP = '学生端仅支持查看，所有入党流程状态均由管理端教师审核更新。'

function getScenario(studentId = '') {
  return MOCK_PARTY_FLOW_SCENARIOS[String(studentId || '').trim()] || null
}

function getStatusText(status = '') {
  if (status === 'completed') return '已完成'
  if (status === 'in_progress') return '进行中'
  return '未完成'
}

function buildStages(definitions = [], statuses = []) {
  return definitions.map((item, index) => {
    const status = statuses[index] || 'pending'
    const completed = status === 'completed'
    return {
      id: item.id,
      name: item.name,
      description: item.description,
      expectedTime: item.expectedTime,
      time: item.expectedTime,
      status,
      statusText: getStatusText(status),
      statusVariant: completed ? 'completed' : 'incomplete',
      completed
    }
  })
}

function getCurrentStage(stages = []) {
  const current = stages.find((item) => !item.completed)
  if (current) return current.name
  return stages.length ? stages[stages.length - 1].name : '暂未开始'
}

function getProgressPercent(stages = []) {
  if (!stages.length) return 0
  const completedCount = stages.filter((item) => item.completed).length
  return Math.round((completedCount / stages.length) * 100)
}

function buildFlowSummary(type, stages = []) {
  const meta = type === 'party'
    ? {
        title: '入党进程',
        subtitle: '申请人 -> 入党积极分子 -> 发展对象 -> 预备党员 -> 正式党员'
      }
    : {
        title: '入团进程',
        subtitle: '提交申请 -> 入团积极分子 -> 共青团员'
      }

  return {
    ...meta,
    currentStage: getCurrentStage(stages),
    progressPercent: getProgressPercent(stages),
    stages,
    canApply: false,
    applyBtnText: type === 'party' ? '入党' : '入团',
    readonlyTip: READONLY_TIP
  }
}

function buildFlow(type, definitions, statuses) {
  const stages = buildStages(definitions, statuses)
  return {
    type,
    summary: buildFlowSummary(type, stages),
    flow: stages.map((item, index) => ({
      id: item.id,
      index: index + 1,
      name: item.name,
      description: item.description,
      expectedTime: item.expectedTime,
      status: item.status,
      statusText: item.statusText,
      statusVariant: item.statusVariant,
      completed: item.completed
    })),
    allCompleted: stages.length > 0 && stages.every((item) => item.completed)
  }
}

function buildHomePartyCard(partyFlow) {
  if (!partyFlow) return null
  return {
    id: 2,
    title: `入党进度 · ${partyFlow.summary.currentStage}`,
    status: `${partyFlow.summary.progressPercent}%`,
    statusClass: 'primary',
    showRightStatus: true,
    typeText: '党',
    typeClass: 'party',
    desc: '由管理端教师审核更新，学生端仅支持查看',
    time: ''
  }
}

function getPartyFlowState(studentId = '') {
  const scenario = getScenario(studentId)
  if (!scenario) {
    return {
      hasAnyFlow: false,
      hasLeagueFlow: false,
      canShowParty: false,
      emptyState: EMPTY_STATE,
      partyGateState: PARTY_GATE_STATE,
      teacherAuditTip: READONLY_TIP,
      league: null,
      party: null,
      homePartyCard: null
    }
  }

  const league = buildFlow('league', LEAGUE_FLOW_DEFINITIONS, scenario.leagueStatuses)
  const canShowParty = league.allCompleted
  const party = canShowParty
    ? buildFlow('party', PARTY_FLOW_DEFINITIONS, scenario.partyStatuses)
    : null

  return {
    hasAnyFlow: true,
    hasLeagueFlow: true,
    canShowParty,
    emptyState: EMPTY_STATE,
    partyGateState: PARTY_GATE_STATE,
    teacherAuditTip: READONLY_TIP,
    league,
    party,
    homePartyCard: canShowParty ? buildHomePartyCard(party) : null
  }
}

module.exports = {
  getPartyFlowState,
  READONLY_TIP,
  EMPTY_STATE,
  PARTY_GATE_STATE
}
