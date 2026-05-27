const APPLY_STATUS_META = {
  ACTION_REQUIRED: {
    code: 'ACTION_REQUIRED',
    listLabel: '待处理',
    detailLabel: '待处理',
    listClass: 'warning',
    detailClass: 'action-required',
    completed: false,
    canCancel: true,
    title: '待处理',
    description: '表示申请流程中仍有需要学生本人完成的前置操作，如补充材料、确认信息或上传附件。完成这些节点后，申请才会进入老师审核队列。'
  },
  IN_REVIEW: {
    code: 'IN_REVIEW',
    listLabel: '待审核',
    detailLabel: '待审核',
    listClass: 'primary',
    detailClass: 'in-review',
    completed: false,
    canCancel: true,
    title: '待审核',
    description: '表示学生已完成所有前置操作，申请已提交至平台审核老师，当前正等待老师进行材料核验与审批。'
  },
  APPROVED: {
    code: 'APPROVED',
    listLabel: '已通过',
    detailLabel: '已通过',
    listClass: 'success',
    detailClass: 'approved',
    completed: true,
    canCancel: false,
    title: '已通过',
    description: '表示审核老师已完成审批，申请已通过并可继续下载或查看证明文件。'
  },
  REJECTED: {
    code: 'REJECTED',
    listLabel: '已驳回',
    detailLabel: '已驳回',
    listClass: 'muted',
    detailClass: 'rejected',
    completed: true,
    canCancel: false,
    title: '已驳回',
    description: '表示审核老师已完成审批，但当前申请未通过。请根据驳回意见补充材料后重新发起申请。'
  },
  CANCELED: {
    code: 'CANCELED',
    listLabel: '已撤回',
    detailLabel: '已撤回',
    listClass: 'muted',
    detailClass: 'canceled',
    completed: true,
    canCancel: false,
    title: '已撤回',
    description: '表示学生本人已主动撤回当前申请，流程不再继续推进。'
  }
}

const STATUS_ALIASES = {
  SUBMITTED: 'ACTION_REQUIRED',
  PENDING: 'ACTION_REQUIRED',
  ACTION_REQUIRED: 'ACTION_REQUIRED',
  IN_REVIEW: 'IN_REVIEW',
  REVIEWING: 'IN_REVIEW',
  APPROVED: 'APPROVED',
  COMPLETED: 'APPROVED',
  REJECTED: 'REJECTED',
  REFUSED: 'REJECTED',
  CANCELED: 'CANCELED',
  WITHDRAWN: 'CANCELED'
}

function normalizeApplyStatus(status = '') {
  const upper = String(status || '').trim().toUpperCase()
  return STATUS_ALIASES[upper] || upper || 'ACTION_REQUIRED'
}

function getApplyStatusMeta(status = '') {
  const normalized = normalizeApplyStatus(status)
  return APPLY_STATUS_META[normalized] || APPLY_STATUS_META.ACTION_REQUIRED
}

function isCompletedStatus(status = '') {
  return getApplyStatusMeta(status).completed
}

module.exports = {
  APPLY_STATUS_META,
  normalizeApplyStatus,
  getApplyStatusMeta,
  isCompletedStatus
}
