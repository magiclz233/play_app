import {useAppStore} from '../store/app';
import {pageRouteTitleKeys, tabBarItems} from '../theme/tokens';

const translations = {
  'zh-Hans': {
    common: {
      confirm: '确定',
      cancel: '取消',
      loading: '加载中...',
      noMore: '没有更多了',
      save: '保存',
      submit: '提交',
      success: '成功',
      error: '错误',
      edit: '编辑',
      delete: '删除',
      status: '状态',
      operation: '操作',
      auditing: '审核中',
      approved: '已通过',
      rejected: '已驳回',
      contactCS: '联系客服',
      time: '时间',
      place: '地点',
      loginRequired: '请先登录',
      requestFailed: '请求失败',
      serverDisconnected: '无法连接服务器，请确认后端已启动'
    },
    tabs: {
      lobby: '大厅',
      orders: '订单',
      mine: '我的'
    },
    lobby: {
      title: '同城伴玩',
      searchPlaceholder: '搜索心仪搭子或技能',
      goToCompanion: '我要接单',
      goToCompanionSub: '成为陪玩，展示技能，获得收益',
      popularRank: '魅力排行榜',
      rank1: '人气王',
      recommend: '推荐搭子',
      requestHub: '需求大厅',
      all: '综合',
      nearby: '附近',
      newcomer: '新人',
      priceUnit: '¥{price}/小时',
      postRequest: '发布陪伴需求',
      postBtn: '发布需求',
      busy: '忙碌中',
      available: '可接单',
      wantToJoin: '我要入驻',
      emptyRecommend: '暂时没有可预约搭子',
      emptyRequests: '还没有新的陪伴需求',
      expectedTime: '期望时间（如：今晚 8 点）',
      expectedAddress: '期望服务区域或店名',
      requestRequired: '请输入需求描述',
      requestSuccess: '需求发布成功，系统已同步至客服。是否立即联系客服拉群对接？',
      contactForRequest: '联系客服拉群',
      contactForRequestContent: '确认联系专属企微客服协助建群对接吗？',
      contactJump: '正在跳转客服拉群'
    },
    detail: {
      voiceIntro: '真人语音介绍',
      attributes: '助教属性',
      bio: '自我介绍',
      reviews: '真实评价 ({count})',
      bookNow: '立即预约下单',
      hours: '预约时长',
      address: '预约大致地址',
      remark: '需求备注',
      noReviews: '暂无评价',
      ageUnit: '{age} 岁',
      ratingLabel: '评分',
      orderLabel: '预约',
      goodRateLabel: '好评',
      emptySummary: '这位搭子暂时还没有填写自我介绍'
    },
    mine: {
      title: '我的',
      unlogin: '未登录',
      wechatUser: '微信用户',
      clickLogin: '点击登录',
      loginPrompt: '体验完整功能',
      companionPortal: '助教工作台',
      myWallet: '我的钱包',
      sysSettings: '系统设置',
      theme: '主题模式',
      language: '系统语言',
      auto: '跟随系统',
      light: '浅色模式',
      dark: '深色模式',
      zhHans: '简体中文',
      zhHant: '繁體中文',
      en: 'English',
      customerMode: '找陪玩',
      companionMode: '去接单',
      companionModeTag: '陪玩模式',
      unboundPhone: '暂未绑定手机号',
      allOrders: '全部',
      waitingPay: '待付款',
      waitingAccept: '待接单',
      inProgress: '进行中',
      waitingReview: '待评价',
      companionEntrySub: '成为陪玩，开启赚钱之旅',
      companionManageSub: '管理接单、查看收益',
      todayOverview: '今日概览',
      totalOrders: '总订单',
      balance: '余额',
      totalIncome: '累计收入',
      orderManage: '接单管理',
      withdraw: '提现',
      profile: '个人资料',
      mockLogin: '模拟登录中...',
      loginSuccess: '登录成功',
      mockLoginSuccess: '本地 Mock 登录成功'
    },
    order: {
      create: '确认订单',
      pay: '订单支付',
      detail: '订单详情',
      review: '评价',
      refund: '申请退款',
      list: '我的订单',
      status: {
        '10': '待付款',
        '20': '待拉群',
        '30': '服务中',
        '40': '服务中',
        '50': '服务中',
        '60': '已完工',
        '70': '待结算',
        '80': '已结清',
        '100': '已取消',
        '110': '退款中',
        '120': '已退款',
        '130': '已退款',
        '200': '申诉中',
        '210': '已完成',
        '250': '已关闭'
      },
      time: '预约时间',
      duration: '时长',
      amount: '总金额',
      reserveAddress: '服务地址',
      groupChat: '微信三方沟通群',
      groupStatus0: '等待客服拉群',
      groupStatus1: '三方群沟通中',
      groupStatus2: '已解散',
      timeLine: '履约时间轴',
      actionConfirm: '确认服务完成',
      actionCancel: '取消订单',
      actionReview: '评价订单',
      actionRefund: '申请退款'
    },
    companion: {
      listTitle: '助教大厅',
      detailTitle: '助教详情',
      applyTitle: '助教入驻',
      dashboardTitle: '助教工作台',
      walletTitle: '我的钱包'
    },
    admin: {
      title: '管理后台'
    }
  },
  'zh-Hant': {
    common: {
      confirm: '確定',
      cancel: '取消',
      loading: '載入中...',
      noMore: '沒有更多了',
      save: '保存',
      submit: '提交',
      success: '成功',
      error: '錯誤',
      edit: '編輯',
      delete: '刪除',
      status: '狀態',
      operation: '操作',
      auditing: '審核中',
      approved: '已通過',
      rejected: '已駁回',
      contactCS: '聯繫客服',
      time: '時間',
      place: '地點',
      loginRequired: '請先登入',
      requestFailed: '請求失敗',
      serverDisconnected: '無法連接服務器，請確認後端已啟動'
    },
    tabs: {
      lobby: '大廳',
      orders: '訂單',
      mine: '我的'
    },
    lobby: {
      title: '同城伴玩',
      searchPlaceholder: '搜尋心儀搭檔或技能',
      goToCompanion: '我要接單',
      goToCompanionSub: '成為陪玩，展示技能，獲得收益',
      popularRank: '魅力排行榜',
      rank1: '人氣王',
      recommend: '推薦搭檔',
      requestHub: '需求大廳',
      all: '綜合',
      nearby: '附近',
      newcomer: '新人',
      priceUnit: '¥{price}/小時',
      postRequest: '發佈陪伴需求',
      postBtn: '發佈需求',
      busy: '忙碌中',
      available: '可接單',
      wantToJoin: '我要入駐',
      emptyRecommend: '暫時沒有可預約搭檔',
      emptyRequests: '還沒有新的陪伴需求',
      expectedTime: '期望時間（如：今晚 8 點）',
      expectedAddress: '期望服務區域或店名',
      requestRequired: '請輸入需求描述',
      requestSuccess: '需求發佈成功，系統已同步至客服。是否立即聯繫客服拉群對接？',
      contactForRequest: '聯繫客服拉群',
      contactForRequestContent: '確認聯繫專屬企微客服協助建群對接嗎？',
      contactJump: '正在跳轉客服拉群'
    },
    detail: {
      voiceIntro: '真人語音介紹',
      attributes: '助教屬性',
      bio: '自我介紹',
      reviews: '真實評價 ({count})',
      bookNow: '立即預約下單',
      hours: '預約時長',
      address: '預約大致地址',
      remark: '需求備註',
      noReviews: '暫無評價',
      ageUnit: '{age} 歲',
      ratingLabel: '評分',
      orderLabel: '預約',
      goodRateLabel: '好評',
      emptySummary: '這位搭檔暫時還沒有填寫自我介紹'
    },
    mine: {
      title: '我的',
      unlogin: '未登入',
      wechatUser: '微信用戶',
      clickLogin: '點擊登入',
      loginPrompt: '體驗完整功能',
      companionPortal: '助教工作台',
      myWallet: '我的錢包',
      sysSettings: '系統設置',
      theme: '主題模式',
      language: '系統語言',
      auto: '跟隨系統',
      light: '淺色模式',
      dark: '深色模式',
      zhHans: '简体中文',
      zhHant: '繁體中文',
      en: 'English',
      customerMode: '找陪玩',
      companionMode: '去接單',
      companionModeTag: '陪玩模式',
      unboundPhone: '暫未綁定手機號',
      allOrders: '全部',
      waitingPay: '待付款',
      waitingAccept: '待接單',
      inProgress: '進行中',
      waitingReview: '待評價',
      companionEntrySub: '成為陪玩，開啟賺錢之旅',
      companionManageSub: '管理接單、查看收益',
      todayOverview: '今日概覽',
      totalOrders: '總訂單',
      balance: '餘額',
      totalIncome: '累計收入',
      orderManage: '接單管理',
      withdraw: '提現',
      profile: '個人資料',
      mockLogin: '模擬登入中...',
      loginSuccess: '登入成功',
      mockLoginSuccess: '本地 Mock 登入成功'
    },
    order: {
      create: '確認訂單',
      pay: '訂單支付',
      detail: '訂單詳情',
      review: '評價',
      refund: '申請退款',
      list: '我的訂單',
      status: {
        '10': '待付款',
        '20': '待拉群',
        '30': '服務中',
        '40': '服務中',
        '50': '服務中',
        '60': '已完工',
        '70': '待結算',
        '80': '已結清',
        '100': '已取消',
        '110': '退款中',
        '120': '已退款',
        '130': '已退款',
        '200': '申訴中',
        '210': '已完成',
        '250': '已關閉'
      },
      time: '預約時間',
      duration: '時長',
      amount: '總金額',
      reserveAddress: '服務地址',
      groupChat: '微信三方溝通群',
      groupStatus0: '等待客服拉群',
      groupStatus1: '三方群溝通中',
      groupStatus2: '已解散',
      timeLine: '履約時間軸',
      actionConfirm: '確認服務完成',
      actionCancel: '取消訂單',
      actionReview: '評價訂單',
      actionRefund: '申請退款'
    },
    companion: {
      listTitle: '助教大廳',
      detailTitle: '助教詳情',
      applyTitle: '助教入駐',
      dashboardTitle: '助教工作台',
      walletTitle: '我的錢包'
    },
    admin: {
      title: '管理後台'
    }
  },
  'en': {
    common: {
      confirm: 'Confirm',
      cancel: 'Cancel',
      loading: 'Loading...',
      noMore: 'No more',
      save: 'Save',
      submit: 'Submit',
      success: 'Success',
      error: 'Error',
      edit: 'Edit',
      delete: 'Delete',
      status: 'Status',
      operation: 'Operation',
      auditing: 'Reviewing',
      approved: 'Approved',
      rejected: 'Rejected',
      contactCS: 'Support',
      time: 'Time',
      place: 'Place',
      loginRequired: 'Please sign in first',
      requestFailed: 'Request failed',
      serverDisconnected: 'Cannot connect to server. Please confirm backend is running'
    },
    tabs: {
      lobby: 'Lobby',
      orders: 'Orders',
      mine: 'Profile'
    },
    lobby: {
      title: 'Companions',
      searchPlaceholder: 'Search skill or companion',
      goToCompanion: 'Work Portal',
      goToCompanionSub: 'Become a companion & earn money',
      popularRank: 'Popular Ranks',
      rank1: 'No.1 Hero',
      recommend: 'Companions',
      requestHub: 'Request Hub',
      all: 'Default',
      nearby: 'Nearby',
      newcomer: 'New',
      priceUnit: '¥{price}/hr',
      postRequest: 'Post Booking Need',
      postBtn: 'Post',
      busy: 'Busy',
      available: 'Available',
      wantToJoin: 'Apply Now',
      emptyRecommend: 'No available companions yet',
      emptyRequests: 'No new requests yet',
      expectedTime: 'Expected time, e.g. tonight 8 PM',
      expectedAddress: 'Expected area or venue',
      requestRequired: 'Please enter request details',
      requestSuccess: 'Request posted and synced to support. Contact support now?',
      contactForRequest: 'Contact Support',
      contactForRequestContent: 'Confirm contacting support to coordinate this request?',
      contactJump: 'Opening support'
    },
    detail: {
      voiceIntro: 'Voice Intro',
      attributes: 'Specs',
      bio: 'Bio',
      reviews: 'Reviews ({count})',
      bookNow: 'Book Now',
      hours: 'Duration',
      address: 'General Location',
      remark: 'Notes',
      noReviews: 'No reviews yet',
      ageUnit: '{age} yrs',
      ratingLabel: 'Rating',
      orderLabel: 'Bookings',
      goodRateLabel: 'Positive',
      emptySummary: 'No personal intro yet'
    },
    mine: {
      title: 'Profile',
      unlogin: 'Guest',
      wechatUser: 'WeChat User',
      clickLogin: 'Tap to Login',
      loginPrompt: 'Sign in to access features',
      companionPortal: 'Work Station',
      myWallet: 'Wallet',
      sysSettings: 'Settings',
      theme: 'Theme Mode',
      language: 'Language',
      auto: 'System',
      light: 'Light',
      dark: 'Dark',
      zhHans: '简体中文',
      zhHant: '繁體中文',
      en: 'English',
      customerMode: 'Find',
      companionMode: 'Work',
      companionModeTag: 'Worker Mode',
      unboundPhone: 'Phone not linked',
      allOrders: 'All',
      waitingPay: 'Unpaid',
      waitingAccept: 'Pending',
      inProgress: 'Active',
      waitingReview: 'Review',
      companionEntrySub: 'Become a companion and start earning',
      companionManageSub: 'Manage orders and income',
      todayOverview: 'Today',
      totalOrders: 'Orders',
      balance: 'Balance',
      totalIncome: 'Income',
      orderManage: 'Order Management',
      withdraw: 'Withdraw',
      profile: 'Profile',
      mockLogin: 'Mock login...',
      loginSuccess: 'Logged in',
      mockLoginSuccess: 'Local mock login completed'
    },
    order: {
      create: 'Checkout',
      pay: 'Make Payment',
      detail: 'Order Info',
      review: 'Review',
      refund: 'Refund',
      list: 'My Bookings',
      status: {
        '10': 'Unpaid',
        '20': 'Pending Group',
        '30': 'Active',
        '40': 'Active',
        '50': 'Active',
        '60': 'Done',
        '70': 'Settling',
        '80': 'Paid',
        '100': 'Cancelled',
        '110': 'Refunding',
        '120': 'Refunded',
        '130': 'Refunded',
        '200': 'Disputing',
        '210': 'Completed',
        '250': 'Closed'
      },
      time: 'Reserve Time',
      duration: 'Duration',
      amount: 'Total Cost',
      reserveAddress: 'Service Location',
      groupChat: 'WeChat Tri-Party Group',
      groupStatus0: 'Waiting for agent to add',
      groupStatus1: 'Chat active',
      groupStatus2: 'Group disbanded',
      timeLine: 'Service Timeline',
      actionConfirm: 'Confirm Completed',
      actionCancel: 'Cancel Booking',
      actionReview: 'Leave Review',
      actionRefund: 'Request Refund'
    },
    companion: {
      listTitle: 'Companions',
      detailTitle: 'Companion Info',
      applyTitle: 'Apply',
      dashboardTitle: 'Work Station',
      walletTitle: 'Wallet'
    },
    admin: {
      title: 'Admin'
    }
  }
};

export type LocaleName = keyof typeof translations;

export const translate = (locale: LocaleName, path: string, args?: Record<string, any>): string => {
  const dict = translations[locale] || translations['zh-Hans'];
  const keys = path.split('.');
  let result: any = dict;
  for (const key of keys) {
    if (result && typeof result === 'object' && key in result) {
      result = result[key];
    } else {
      return path;
    }
  }

  if (typeof result !== 'string') {
    return path;
  }

  if (args) {
    let str = result;
    for (const [k, v] of Object.entries(args)) {
      str = str.replace(new RegExp(`{${k}}`, 'g'), String(v));
    }
    return str;
  }

  return result;
};

export const t = (path: string, args?: Record<string, any>): string => {
  const appStore = useAppStore();
  return translate(appStore.locale, path, args);
};

export const applyLocaleToNativeUi = () => {
  const appStore = useAppStore();
  const locale = appStore.locale;

  tabBarItems.forEach((item) => {
    uni.setTabBarItem({
      index: item.index,
      text: translate(locale, item.textKey)
    });
  });

  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  const titleKey = currentPage?.route ? pageRouteTitleKeys[currentPage.route] : '';
  if (titleKey) {
    uni.setNavigationBarTitle({ title: translate(locale, titleKey) });
  }
};
