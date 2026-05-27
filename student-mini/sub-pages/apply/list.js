// sub-pages/apply/list.js
Page({
  data: {
    applyList: [
      { id: '3', title: '教师资格证申请', iconText: '申', status: '审核中', statusClass: 'warning', time: '2025-03-25' },
      { id: '1', title: '在读证明', iconText: '证', status: '已通过', statusClass: 'primary', time: '2025-03-25' }
    ]
  },
  
  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/sub-pages/apply/detail?id=${id}` })
  }
})
