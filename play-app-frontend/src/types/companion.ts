/** 陪玩卡片展示 */
export interface CompanionVO {
  userId: number
  nickname: string
  coverUrl?: string
  rating: number
  totalRatingCount: number
  pricePerHour: number
  workStatus: number
  voiceUrl?: string
  voiceDuration?: number
  tags: TagVO[]
  distance?: string
}

/** 陪玩详情 */
export interface CompanionDetailVO {
  userId: number
  nickname: string
  realName?: string
  gender: number
  age?: number
  height?: number
  summary?: string
  voiceUrl?: string
  voiceDuration?: number
  pricePerHour: number
  rating: number
  totalRatingCount: number
  orderCount: number
  workStatus: number
  photoUrls: string[]
  tags: TagVO[]
}

export interface TagVO {
  id: number
  name: string
  tagType: number
  color?: string
}

export interface CompanionAlbum {
  id: number
  companionId: number
  imageUrl: string
  thumbnailUrl?: string
  sortOrder: number
  isCover: boolean
}

export interface CompanionSkill {
  id: number
  companionId: number
  categoryId: number
  pricePerHour: number
  experienceDesc?: string
}

export interface CompanionWallet {
  balance: number
  frozenAmount: number
  totalIncome: number
  totalWithdrawn: number
}

export interface WalletTransaction {
  id: number
  transactionType: number
  amount: number
  balanceBefore: number
  balanceAfter: number
  description?: string
  createTime: string
}

export interface PlayRequest {
  id: number
  userId: number
  nickname: string
  avatarUrl?: string
  description: string
  reserveTime?: string
  address?: string
  budget?: number
  status: number
  createTime: string
}
