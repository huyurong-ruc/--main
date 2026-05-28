function pickFirstNonEmpty(values = []) {
  for (let i = 0; i < values.length; i += 1) {
    const value = values[i]
    if (value === null || value === undefined) continue
    const text = String(value).trim()
    if (text) return text
  }
  return ''
}

function normalizeUserProfile(raw = {}, defaults = {}) {
  const merged = {
    ...defaults,
    ...raw
  }

  const userId = pickFirstNonEmpty([
    merged.userId,
    merged.id,
    defaults.userId,
    defaults.id
  ])

  const studentId = pickFirstNonEmpty([
    merged.studentId,
    defaults.studentId,
    merged.userId,
    merged.id
  ])

  const studentNo = pickFirstNonEmpty([
    merged.studentNo,
    defaults.studentNo,
    merged.username,
    defaults.username
  ])

  const name = pickFirstNonEmpty([
    merged.name,
    merged.fullName,
    merged.realName,
    merged.nickname,
    defaults.name
  ])

  const username = pickFirstNonEmpty([
    merged.username,
    defaults.username,
    studentNo
  ])

  return {
    ...merged,
    id: userId || studentId || '',
    userId: userId || '',
    studentId: studentId || '',
    username,
    studentNo,
    name,
    displayName: name || '未设置姓名',
    displayStudentNo: studentNo,
    profileFieldMap: {
      name: 'name',
      studentNo: 'studentNo'
    }
  }
}

module.exports = {
  normalizeUserProfile
}
