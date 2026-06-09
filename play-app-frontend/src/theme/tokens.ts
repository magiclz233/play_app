export type ThemeName = 'light' | 'dark';
export type ThemeMode = 'auto' | ThemeName;

type ThemeTokens = {
  cssVars: Record<string, string>;
  navigationBar: {
    frontColor: '#000000' | '#ffffff';
    backgroundColor: string;
  };
  tabBar: {
    color: string;
    selectedColor: string;
    backgroundColor: string;
    borderStyle: 'black' | 'white';
  };
};

export const themeTokens: Record<ThemeName, ThemeTokens> = {
  light: {
    cssVars: {
      '--bg-main': '#FAF5F5',
      '--bg-card': '#FFFFFF',
      '--border-color': 'rgba(255, 59, 92, 0.08)',
      '--text-primary': '#1A1A1F',
      '--text-secondary': '#5E6066',
      '--text-muted': '#999A9F',
      '--shadow-card': '0 8px 30px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(255, 59, 92, 0.04)',
      '--shadow-floating': '0 12px 40px rgba(255, 59, 92, 0.12)',
      '--color-primary': '#FF3B5C',
      '--color-primary-rgb': '255, 59, 92',
      '--color-primary-light': '#FFF0F2',
      '--color-primary-dark': '#D92241',
      '--color-gradient-end': '#FF7B54',
      '--color-accent': '#FF9F1C',
      '--color-success': '#10B981',
      '--color-error': '#EF4444',
      '--color-glow': 'rgba(255, 59, 92, 0.05)',
      '--color-glow-secondary': 'rgba(255, 159, 28, 0.03)'
    },
    navigationBar: {
      frontColor: '#000000',
      backgroundColor: '#FAF5F5'
    },
    tabBar: {
      color: '#999A9F',
      selectedColor: '#FF3B5C',
      backgroundColor: '#FFFFFF',
      borderStyle: 'black'
    }
  },
  dark: {
    cssVars: {
      '--bg-main': '#18181C',
      '--bg-card': '#202127',
      '--border-color': 'rgba(255, 255, 255, 0.04)', /* Even softer border */
      '--text-primary': '#E2E4E9',
      '--text-secondary': '#A1A5B1',
      '--text-muted': '#737887',
      '--shadow-card': '0 10px 30px rgba(0, 0, 0, 0.4), 0 2px 12px rgba(255, 59, 92, 0.06)', /* Added subtle coral glow */
      '--shadow-floating': '0 12px 32px rgba(255, 59, 92, 0.15)',
      '--color-primary': '#FF3B5C',
      '--color-primary-rgb': '255, 59, 92',
      '--color-primary-light': '#31171E', // Soft dark crimson for subtle backgrounds
      '--color-primary-dark': '#D92241',
      '--color-gradient-end': '#FF7B54',
      '--color-accent': '#FF9F1C',
      '--color-success': '#10B981',
      '--color-error': '#EF4444',
      '--color-glow': 'rgba(255, 59, 92, 0.08)',
      '--color-glow-secondary': 'rgba(255, 159, 28, 0.05)'
    },
    navigationBar: {
      frontColor: '#ffffff',
      backgroundColor: '#18181C'
    },
    tabBar: {
      color: '#737887',
      selectedColor: '#FF3B5C',
      backgroundColor: '#202127',
      borderStyle: 'white'
    }
  }
};

export const pageTitles = {
  index: 'lobby.title',
  orderList: 'order.list',
  mine: 'mine.title',
  companionList: 'companion.listTitle',
  companionDetail: 'companion.detailTitle',
  companionApply: 'companion.applyTitle',
  companionDashboard: 'companion.dashboardTitle',
  companionWallet: 'companion.walletTitle',
  orderCreate: 'order.create',
  orderPay: 'order.pay',
  orderDetail: 'order.detail',
  orderReview: 'order.review',
  orderRefund: 'order.refund',
  admin: 'admin.title'
} as const;

export const pageRouteTitleKeys: Record<string, string> = {
  'pages/index/index': pageTitles.index,
  'pages/order/list': pageTitles.orderList,
  'pages/mine/index': pageTitles.mine,
  'pages/companion/list': pageTitles.companionList,
  'pages/companion/detail': pageTitles.companionDetail,
  'pages/companion/apply': pageTitles.companionApply,
  'pages/companion/dashboard': pageTitles.companionDashboard,
  'pages/companion/wallet': pageTitles.companionWallet,
  'pages/order/create': pageTitles.orderCreate,
  'pages/order/pay': pageTitles.orderPay,
  'pages/order/detail': pageTitles.orderDetail,
  'pages/order/review': pageTitles.orderReview,
  'pages/order/refund': pageTitles.orderRefund,
  'pages/admin/index': pageTitles.admin
};

export const tabBarItems = [
  { index: 0, textKey: 'tabs.lobby' },
  { index: 1, textKey: 'tabs.orders' },
  { index: 2, textKey: 'tabs.mine' }
] as const;
