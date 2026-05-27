Component({
  data: {
    selected: 0,
    switching: false,
    list: [
      {
        pagePath: 'pages/index/index',
        text: '首页',
        iconPath: '/static/icons/home.svg',
        selectedIconPath: '/static/icons/home-active.svg'
      },
      {
        pagePath: 'pages/message/index',
        text: '消息',
        iconPath: '/static/icons/message.svg',
        selectedIconPath: '/static/icons/message-active.svg'
      },
      {
        pagePath: 'pages/profile/index',
        text: '我的',
        iconPath: '/static/icons/profile.svg',
        selectedIconPath: '/static/icons/profile-active.svg'
      }
    ]
  },
  lifetimes: {
    attached() {
      this.updateSelected()
    }
  },
  pageLifetimes: {
    show() {
      this.updateSelected()
    }
  },
  methods: {
    updateSelected() {
      const pages = getCurrentPages()
      const current = pages[pages.length - 1]
      const rawRoute = (current && current.route) || ''
      const route = rawRoute.startsWith('/') ? rawRoute.slice(1) : rawRoute
      const index = this.data.list.findIndex((item) => item.pagePath === route)
      if (index > -1 && index !== this.data.selected) {
        this.setData({ selected: index })
      }
    },
    onTabItemTap(e) {
      const index = Number(e.currentTarget.dataset.index)
      const path = e.currentTarget.dataset.path
      if (!Number.isFinite(index)) return
      if (this.data.switching) return
      if (index === this.data.selected) return
      if (!path) return
      this.setData({ selected: index, switching: true })
      wx.switchTab({
        url: `/${path}`,
        complete: () => {
          this.setData({ switching: false })
          this.updateSelected()
        }
      })
    }
  }
})
