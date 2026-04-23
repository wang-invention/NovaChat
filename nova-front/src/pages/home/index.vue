<template>
  <view class="home-page">
    <!-- 顶部导航栏 - 固定 -->
    <view class="nav-bar">
      <text class="nav-title">ChatNova</text>
      <view class="nav-right">
        <view class="search-box">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
            <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <text class="search-placeholder">搜索</text>
        </view>
        <view class="add-btn">
          <svg class="add-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M12 8V16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M8 12H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </view>
      </view>
    </view>

    <!-- 聊天列表 - 可滚动区域 -->
    <scroll-view class="chat-list" scroll-y>
      <chat-list-item
        v-for="(item, index) in chatList"
        :key="index"
        :data="item"
        :has-border="index < chatList.length - 1"
      />
    </scroll-view>

    <!-- 底部 TabBar - 固定 -->
    <custom-tab-bar current="chat" :badge="{ chat: 12, contacts: 0, discover: 0, me: 0 }" />
  </view>
</template>

<script>
import ChatListItem from '@/components/chat-list-item/chat-list-item.vue';
import CustomTabBar from '@/components/custom-tab-bar/custom-tab-bar.vue';

export default {
  components: {
    ChatListItem,
    CustomTabBar
  },
  data() {
    return {
      chatList: [
        {
          name: 'AI 助手',
          avatarType: 'icon',
          iconBg: '#10AEFF',
          iconName: 'robot',
          lastMessage: '[图片] 你好，我是你的AI助手',
          time: '10:30',
          unread: 0
        },
        {
          name: '产品设计讨论组',
          avatarType: 'group',
          avatarList: [
            'https://api.dicebear.com/7.x/avataaars/svg?seed=1',
            'https://api.dicebear.com/7.x/avataaars/svg?seed=2',
            'https://api.dicebear.com/7.x/avataaars/svg?seed=3',
            'https://api.dicebear.com/7.x/avataaars/svg?seed=4'
          ],
          lastMessage: '到底用了异步还是同步？',
          time: '09:10',
          unread: 2
        },
        {
          name: '小美',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=meimei',
          lastMessage: '[语音]',
          time: '09:10',
          unread: 0
        },
        {
          name: '文件传输助手',
          avatarType: 'icon',
          iconBg: '#07C160',
          iconName: 'file',
          lastMessage: '[文件] 产品需求文档.pdf',
          time: '昨天',
          unread: 0
        },
        {
          name: '张三',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan',
          lastMessage: '[图片]',
          time: '昨天',
          unread: 0
        },
        {
          name: '订阅号消息',
          avatarType: 'icon',
          iconBg: '#10AEFF',
          iconName: 'subscription',
          lastMessage: 'ChatNova 更新了新功能',
          time: '昨天',
          unread: 0
        },
        {
          name: '李四',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi',
          lastMessage: '明天下午三点开会',
          time: '昨天',
          unread: 5
        },
        {
          name: '王五',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu',
          lastMessage: '[语音] 好的，没问题',
          time: '前天',
          unread: 0
        },
        {
          name: '技术交流群',
          avatarType: 'group',
          avatarList: [
            'https://api.dicebear.com/7.x/avataaars/svg?seed=tech1',
            'https://api.dicebear.com/7.x/avataaars/svg?seed=tech2',
            'https://api.dicebear.com/7.x/avataaars/svg?seed=tech3'
          ],
          lastMessage: '有人遇到过这个问题吗？',
          time: '前天',
          unread: 8
        },
        {
          name: '赵六',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoliu',
          lastMessage: '周末有空一起吃饭吗？',
          time: '星期一',
          unread: 0
        },
        {
          name: '孙七',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=sunqi',
          lastMessage: '[图片] 这个设计怎么样？',
          time: '星期一',
          unread: 1
        },
        {
          name: '周八',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhouba',
          lastMessage: '收到，谢谢！',
          time: '星期日',
          unread: 0
        },
        {
          name: '吴九',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=wujiu',
          lastMessage: '项目进度怎么样了？',
          time: '星期六',
          unread: 0
        },
        {
          name: '家庭群',
          avatarType: 'group',
          avatarList: [
            'https://api.dicebear.com/7.x/avataaars/svg?seed=family1',
            'https://api.dicebear.com/7.x/avataaars/svg?seed=family2',
            'https://api.dicebear.com/7.x/avataaars/svg?seed=family3',
            'https://api.dicebear.com/7.x/avataaars/svg?seed=family4',
            'https://api.dicebear.com/7.x/avataaars/svg?seed=family5'
          ],
          lastMessage: '妈妈：[图片] 今天做的菜',
          time: '星期五',
          unread: 15
        },
        {
          name: '郑十',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhengshi',
          lastMessage: '好的，明天见',
          time: '星期四',
          unread: 0
        },
        {
          name: '钱十一',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=qianshiyi',
          lastMessage: '[语音] 我在路上了',
          time: '星期三',
          unread: 0
        },
        {
          name: '客户群',
          avatarType: 'group',
          avatarList: [
            'https://api.dicebear.com/7.x/avataaars/svg?seed=client1',
            'https://api.dicebear.com/7.x/avataaars/svg?seed=client2'
          ],
          lastMessage: '需求文档已发送',
          time: '星期二',
          unread: 3
        },
        {
          name: '冯十二',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=fengshier',
          lastMessage: '收到，我会尽快处理',
          time: '4月15日',
          unread: 0
        },
        {
          name: '陈十三',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=chenshisan',
          lastMessage: '好的，没问题',
          time: '4月14日',
          unread: 0
        },
        {
          name: '褚十四',
          avatarType: 'single',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=chushisi',
          lastMessage: '周末一起去打球吗？',
          time: '4月13日',
          unread: 0
        }
      ]
    };
  }
};
</script>

<style lang="scss" scoped>
page {
  background-color: #f5f5f5;
}

.home-page {
  height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 - 固定 */
.nav-bar {
  flex-shrink: 0;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20rpx;
  background-color: #ffffff;
  border-bottom: 1rpx solid #e5e5e5;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #111111;
  margin-right: 20rpx;
}

.nav-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 20rpx;
}

.search-box {
  flex: 1;
  height: 64rpx;
  background-color: #f5f5f5;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
}

.search-icon {
  width: 32rpx;
  height: 32rpx;
  color: #999999;
  margin-right: 12rpx;
}

.search-placeholder {
  font-size: 28rpx;
  color: #999999;
}

.add-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: #f5f5f5;
}

.add-icon {
  width: 40rpx;
  height: 40rpx;
  color: #111111;
}

/* 聊天列表 - 可滚动区域 */
.chat-list {
  flex: 1;
  background-color: #ffffff;
  overflow-y: auto;
}

/* 底部 TabBar - 固定 */
.tab-bar {
  flex-shrink: 0;
}
</style>
