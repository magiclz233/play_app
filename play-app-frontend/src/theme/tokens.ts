/**
 * @deprecated 此文件保留向后兼容。新代码请直接从 @/theme 导入。
 *
 * 迁移指南：
 *   旧: import { themeTokens, type ThemeName } from '../theme/tokens';
 *   新: import { themeEngine, type ThemeName } from '@/theme';
 *
 * 页面中不再需要手动绑定 themeClass 和 themeStyle，
 * ThemeEngine 在 App.vue onLaunch 中自动注入 CSS 变量到页面根元素。
 */

import { themeEngine } from './engine';
import type { ThemeName, ThemeMode } from './theme.config';

// Re-export for backward compatibility
export type { ThemeName, ThemeMode };

/**
 * @deprecated 请使用 themeEngine.getCssVars() 代替
 */
export const themeTokens = {
  get light() {
    return {
      cssVars: themeEngine.getCssVars(),
      navigationBar: {
        frontColor: '#000000' as const,
        backgroundColor: '#FAF5F5',
      },
      tabBar: {
        color: '#A1A5B1',
        selectedColor: '#FF3B5C',
        backgroundColor: '#FFFFFF',
        borderStyle: 'black' as const,
      },
    };
  },
  get dark() {
    return {
      cssVars: themeEngine.getCssVars(),
      navigationBar: {
        frontColor: '#ffffff' as const,
        backgroundColor: '#121216',
      },
      tabBar: {
        color: '#737887',
        selectedColor: '#FF5C73',
        backgroundColor: '#24262D',
        borderStyle: 'white' as const,
      },
    };
  },
};

// 页面标题 — 保持不变
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
  admin: 'admin.title',
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
  'pages/admin/index': pageTitles.admin,
};

export const tabBarItems = [
  { index: 0, textKey: 'tabs.lobby' },
  { index: 1, textKey: 'tabs.orders' },
  { index: 2, textKey: 'tabs.mine' },
] as const;
