// sub-pages/apply/new.js
const app = getApp()
const { post } = require('../../api/request')
const applyApi = require('../../api/apply')
const authApi = require('../../api/auth')
const {
  buildFormDefaults,
  getApplyDisplayTitle,
  validateApplyForm,
  resolveApplyInitialStatus
} = require('../../api/apply-business')
const { getApplyStatusMeta } = require('../../api/apply-status')

// 防抖定时器
let submitTimer = null

function buildRenderedFields(typeConfig = null, formValues = {}) {
  return (typeConfig?.formFields || []).map((field) => {
    const value = formValues[field.key] == null ? '' : formValues[field.key]
    let displayValue = value

    if (field.type === 'selector' && value) {
      displayValue = value
    }

    return {
      ...field,
      value,
      displayValue
    }
  })
}

function buildPurposeFromFields(typeConfig = null, formValues = {}) {
  if (!typeConfig) return ''

  if (typeConfig.key === 'read-cert') {
    return `${formValues.useScenario || '证明申请'} / ${formValues.recipientOrg || '待填写接收单位'}`
  }

  if (typeConfig.key === 'transcript') {
    return `${formValues.useScenario || '成绩单申请'} / ${formValues.termRange || '待填写成绩范围'}`
  }

  return `${formValues.applyStage || '教师资格认定'} / ${formValues.applicationArea || '待填写认定地区'}`
}

async function ensureStudentProfile() {
  const cached = app.globalData.userInfo || {}
  if (cached.studentId) {
    return cached
  }
  if (typeof app.syncCurrentUser === 'function') {
    return app.syncCurrentUser(true)
  }
  const res = await authApi.getCurrentUser()
  const profile = res && res.data ? res.data : null
  if (!profile) {
    return cached
  }
  const nextUserInfo = {
    ...cached,
    id: profile.userId || cached.id || '',
    studentId: profile.studentId || cached.studentId || '',
    name: profile.name || profile.username || cached.name || '',
    studentNo: profile.studentNo || cached.studentNo || '',
    role: profile.role || cached.role || '',
    major: profile.major || cached.major || '',
    grade: profile.grade || cached.grade || ''
  }
  app.setLoginData(app.globalData.token, nextUserInfo)
  return nextUserInfo
}

Page({
  data: {
    types: [],
    selectedType: null,
    selectedTypeIndex: -1,
    formValues: {},
    renderedFields: [],
    attachments: [],
    loading: false,
    submitting: false,
    enableExtraFields: false,
    enableDraft: false
  },
  
  onLoad() {
    if (!app.isLoggedIn()) {
      wx.navigateTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadTypes()
  },
  
  // 加载申请类型 - 使用后端 /knowledge/templates 接口
  async loadTypes() {
    this.setData({ loading: true })
    try {
      const res = await applyApi.getApplyTypes()
      const types = (res.data || []).map((item) => {
        const title = typeof item === 'string' ? item : (item.applyTitle || item.title || '')
        return {
          ...((typeof item === 'object' && item) ? item : {}),
          title,
          submitTitle: title,
          displayTitle: getApplyDisplayTitle(typeof item === 'object' ? item : title)
        }
      })
      this.setData({ types })
    } catch (e) {
      console.error('加载申请类型失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 选择类型
  onTypeChange(e) {
    const index = Number(e.detail.value)
    const selectedType = this.data.types[index]
    const formValues = this.data.enableExtraFields ? buildFormDefaults(selectedType) : {}
    this.setData({
      selectedTypeIndex: index,
      selectedType,
      formValues,
      renderedFields: this.data.enableExtraFields ? buildRenderedFields(selectedType, formValues) : [],
      attachments: []
    })
  },
  
  onFieldInput(e) {
    const { key } = e.currentTarget.dataset
    const value = e.detail.value
    const formValues = {
      ...this.data.formValues,
      [key]: value
    }
    this.setData({
      formValues,
      renderedFields: buildRenderedFields(this.data.selectedType, formValues)
    })
  },

  onFieldSelect(e) {
    const fieldIndex = Number(e.currentTarget.dataset.index)
    const field = this.data.renderedFields[fieldIndex]
    if (!field) return

    const optionIndex = Number(e.detail.value)
    const nextValue = field.options[optionIndex] || ''
    const formValues = {
      ...this.data.formValues,
      [field.key]: nextValue
    }

    this.setData({
      formValues,
      renderedFields: buildRenderedFields(this.data.selectedType, formValues)
    })
  },
  
  // 上传附件（使用 chooseMessageFile 支持 PDF/Word/Excel 等多格式）
  chooseFile() {
    const remaining = 5 - this.data.attachments.length
    if (remaining <= 0) {
      wx.showToast({ title: '最多上传5个附件', icon: 'none' })
      return
    }

    wx.showToast({
      title: '请先将文件发送到微信聊天（如文件传输助手），然后在此选择',
      icon: 'none',
      duration: 3000
    })

    wx.chooseMessageFile({
      count: remaining,
      type: 'file',
      success: (res) => {
        const files = res.tempFiles || []
        const validFiles = files.filter(f => f.size <= 20 * 1024 * 1024)
        if (validFiles.length < files.length) {
          wx.showToast({ title: '部分文件超过20MB', icon: 'none' })
        }

        const baseUrl = String(app.globalData.baseUrl || '').trim()
        const token = app.globalData.token
        if (!baseUrl || !token) {
          wx.showToast({ title: '登录状态异常，无法上传', icon: 'none' })
          return
        }

        wx.showLoading({ title: '上传中...' })
        const uploaded = []
        const uploadNext = (idx) => {
          if (idx >= validFiles.length) {
            wx.hideLoading()
            if (uploaded.length) {
              this.setData({ attachments: [...this.data.attachments, ...uploaded] })
              wx.showToast({ title: '附件已上传', icon: 'success' })
            }
            return
          }
          const file = validFiles[idx]
          wx.uploadFile({
            url: `${baseUrl}/platform/files/upload?bizType=CERTIFICATE`,
            filePath: file.path,
            name: 'file',
            header: {
              'Authorization': `Bearer ${token}`
            },
            success: (resp) => {
              let data = null
              try {
                data = resp && resp.data ? (JSON.parse(resp.data) || null) : null
              } catch (e) {
                data = null
              }
              if (!data || !data.success || !data.data || !data.data.id) {
                wx.showToast({ title: (data && data.message) ? data.message : '上传失败', icon: 'none' })
                uploadNext(idx + 1)
                return
              }
              uploaded.push({
                id: data.data.id,
                name: data.data.fileName || file.name || ('附件#' + data.data.id),
                fileSize: data.data.fileSize || file.size || 0,
                contentType: data.data.contentType || ''
              })
              uploadNext(idx + 1)
            },
            fail: () => {
              wx.showToast({ title: '上传失败', icon: 'none' })
              uploadNext(idx + 1)
            }
          })
        }
        uploadNext(0)
      },
      fail: (err) => {
        console.error('选择文件失败', err)
        wx.showToast({ title: '选择文件失败', icon: 'none' })
      }
    })
  },
  
  // 删除附件
  removeAttachment(e) {
    const index = e.currentTarget.dataset.index
    const attachments = [...this.data.attachments]
    attachments.splice(index, 1)
    this.setData({ attachments })
  },
  
  // 保存草稿
  async saveDraft() {
    wx.showLoading({ title: '保存中...' })
    try {
      await post('/student/certificates/draft', {
        typeId: this.data.selectedType?.id,
        typeKey: this.data.selectedType?.key,
        certificateType: this.data.selectedType?.submitTitle || this.data.selectedType?.applyTitle || this.data.selectedType?.title,
        purpose: buildPurposeFromFields(this.data.selectedType, this.data.formValues),
        remark: this.data.formValues.applicationNote || '',
        fieldValues: this.data.formValues,
        attachments: this.data.attachments.map(f => ({ name: f.name, path: f.path }))
      })
      wx.showToast({ title: '草稿已保存', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: '保存失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },
  
  // 提交申请（防抖）
  handleSubmit() {
    // 防抖处理
    if (submitTimer) {
      wx.showToast({ title: '请勿重复提交', icon: 'none' })
      return
    }
    
    // 表单验证
    const { selectedType, formValues } = this.data

    if (!selectedType) {
      wx.showToast({ title: '请选择申请类型', icon: 'none' })
      return
    }

    if (this.data.enableExtraFields) {
      const validateResult = validateApplyForm(selectedType, formValues)
      if (!validateResult.valid) {
        wx.showToast({ title: validateResult.message, icon: 'none' })
        return
      }
    }
    
    this.setData({ submitting: true })
    
    // 2秒防抖
    submitTimer = setTimeout(() => {
      submitTimer = null
    }, 2000)
    
    this.submitApply()
  },
  
  // 执行提交 - 使用后端 /certificates/requests 接口
  async submitApply() {
    wx.showLoading({ title: '提交中...' })
    try {
      const profile = await ensureStudentProfile()
      const studentId = Number(profile.studentId || 0)
      if (!studentId || Number.isNaN(studentId)) {
        wx.showToast({ title: '登录信息缺少学生ID', icon: 'none' })
        return
      }

      const certificateType = this.data.selectedType.submitTitle || this.data.selectedType.applyTitle || this.data.selectedType.title || ''
      if (!certificateType) {
        wx.showToast({ title: '证明类型不能为空', icon: 'none' })
        return
      }

      const payload = {
        studentId,
        certificateType,
        reason: '',
        attachments: (this.data.attachments || []).map(item => item && item.id).filter(Boolean)
      }
      const res = await applyApi.submitApply(payload)
      const statusCode = res?.data?.status || resolveApplyInitialStatus(this.data.selectedType, payload)
      const statusMeta = getApplyStatusMeta(statusCode)

      wx.showToast({
        title: statusCode === 'ACTION_REQUIRED' ? '已提交，待继续补充' : '已提交，进入待审核',
        icon: 'success'
      })
      setTimeout(() => {
        wx.navigateBack()
        // 刷新列表
        const pages = getCurrentPages()
        const listPage = pages.find(p => p.route.includes('apply/list'))
        if (listPage) listPage.onShow()
        const homePage = pages.find(p => p.route === 'pages/index/index')
        if (homePage && homePage.syncPendingProgress) {
          homePage.syncPendingProgress()
        }
        console.log('当前申请状态:', statusMeta.title)
      }, 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '提交失败', icon: 'none' })
    } finally {
      wx.hideLoading()
      this.setData({ submitting: false })
    }
  }
})
