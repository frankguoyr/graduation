<template>
  <div class="app">
    <div class="main-card">
      <!-- 左侧会话 -->
      <div :class="['sidebar', { collapsed: sidebarCollapsed }]">

        <button class="collapse-btn" @click="toggleSidebar">
          {{ sidebarCollapsed ? '➡️' : '⬅️' }}
        </button>

        <button
          v-if="!sidebarCollapsed"
          class="new-chat-btn"
          @click="createNewConversation"
        >
          + 新建聊天
        </button>

        <div class="conversation-list">
          <div
              v-for="c in conversations"
              :key="c.id"
              class="conversation-item"
              @click="loadMessages(c.id)"
          >

            <span class="conversation-title">
              {{ c.title }}
            </span>

            <div class="conversation-actions">

              <button
                  class="rename-btn"
                  @click.stop="renameConversationTitle(c)"
              >
                ✏️
              </button>

              <button
                  class="delete-btn"
                  @click.stop="handleDeleteConversation(c.id)"
              >
                ×
              </button>

            </div>

          </div>
        </div>

        <div class="sidebar-footer" v-if="!sidebarCollapsed">

          <div class="user-card">
            <div class="avatar">
              {{ username ? username.charAt(0).toUpperCase() : 'U' }}
            </div>

            <div class="user-name">
              {{ username }}
            </div>
          </div>

          <button class="logout-btn" @click="handleLogout">
            退出登录
          </button>

        </div>
      </div>

      <!-- 右侧聊天 -->
      <div class="chat-main">
        <!-- 头部标题 -->
        <div class="app-header">
          <h1 class="app-title">基于 LangChain4j 与通义千问的 AI 编程学习与求职辅导系统</h1>
          <div class="app-subtitle">帮助您解答编程学习和求职面试相关问题</div>
        </div>

        <!-- 聊天区域 -->
        <div class="chat-container">
          <!-- 消息列表 -->
          <div class="messages-container" ref="messagesContainer">
            <div v-if="messages.length === 0" class="welcome-message">
              <div class="welcome-content">
                <div class="welcome-icon">🤖</div>
                <h2>欢迎使用 AI 编程小助手</h2>
                <p>我可以帮助您：</p>
                <ul>
                  <li>解答编程技术问题</li>
                  <li>提供代码示例和解释</li>
                  <li>协助求职面试准备</li>
                  <li>分享编程学习建议</li>
                </ul>
                <p>请随时向我提问吧！</p>
              </div>
            </div>

            <!-- 历史消息 -->
            <ChatMessage
                v-for="message in messages"
                :key="message.id"
                :message="message.content"
                :is-user="message.isUser"
                :timestamp="message.timestamp"
            />

            <!-- AI 正在回复的消息 -->
            <div v-if="isAiTyping" class="chat-message ai-message">
              <div class="message-avatar">
                <div class="avatar ai-avatar">AI</div>
              </div>
              <div class="message-content">
                <div class="message-bubble">
                  <div class="ai-typing-content">
                    <div class="ai-response-text message-markdown" v-html="currentAiResponseRendered"></div>
                    <LoadingDots v-if="isStreaming" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入框 -->
          <ChatInput
              :disabled="isAiTyping"
              @send-message="sendMessage"
              placeholder="请输入您的编程问题..."
          />
        </div>

        <!-- 连接状态提示 -->
        <div v-if="connectionError" class="connection-error">
          <div class="error-content">
            <span class="error-icon">⚠️</span>
            <span>连接服务器失败，请检查后端服务是否启动</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ChatMessage from '../components/ChatMessage.vue'
import ChatInput from '../components/ChatInput.vue'
import LoadingDots from '../components/LoadingDots.vue'
import {
  chatWithSSE,
  // createConversation,
  // getConversations,
  // getMessages
} from '../api/chatApi.js'
// import { generateMemoryId } from './utils/index.js'
import { marked } from 'marked'
import { createConversation, getConversationList, getMessages, deleteConversation, renameConversation} from '../api/chatApi.js'

export default {
  name: 'ChatView',
  components: {
    ChatMessage,
    ChatInput,
    LoadingDots
  },
  data() {
    return {
      username: "",
      messages: [],
      conversationId: null,
      conversations: [],
      messageCache: {},
      isAiTyping: false,
      isStreaming: false,
      currentAiResponse: '',
      currentEventSource: null,
      connectionError: false,
      sidebarCollapsed: false
    }
  },
  computed: {
    currentAiResponseRendered() {
      if (!this.currentAiResponse) return ''
      // 配置marked选项
      marked.setOptions({
        breaks: true, // 支持换行
        gfm: true, // 支持GitHub风格的Markdown
        sanitize: false, // 不过滤HTML（根据需要可以开启）
        highlight: function(code, lang) {
          // 可以在这里添加代码高亮功能
          return code
        }
      })
      return marked(this.currentAiResponse)
    }
  },
  methods: {

    sendMessage(message) {
      // 添加用户消息
      this.addMessage(message, true)

      // 开始AI回复
      this.startAiResponse(message)
    },

    addMessage(content, isUser = false) {
      const message = {
        id: Date.now() + Math.random(),
        content,
        isUser,
        timestamp: new Date()
      }
      this.messages.push(message)
      this.scrollToBottom()
    },

    startAiResponse(userMessage) {
      this.isAiTyping = true
      this.isStreaming = true
      this.currentAiResponse = ''
      this.connectionError = false

      // 关闭之前的连接
      if (this.currentEventSource) {
        this.currentEventSource.close()
      }

      // 开始SSE连接
      this.currentEventSource = chatWithSSE(
          this.conversationId,
          userMessage,
          this.handleAiMessage,
          this.handleAiError,
          this.handleAiClose
      )
    },

    handleAiMessage(data) {
      this.currentAiResponse += data
      this.scrollToBottom()
    },

    handleAiError(error) {
      console.error('AI 回复出错:', error)
      this.connectionError = true
      this.finishAiResponse()

      // 5秒后自动隐藏错误提示
      setTimeout(() => {
        this.connectionError = false
      }, 5000)
    },

    handleAiClose() {
      this.finishAiResponse()
    },

    async finishAiResponse() {
      this.isStreaming = false

      // 如果有内容，添加到消息列表
      if (this.currentAiResponse.trim()) {
        this.addMessage(this.currentAiResponse.trim(), false)
        this.messageCache[this.conversationId] = this.messages
      }

      // 重置状态
      this.isAiTyping = false
      this.currentAiResponse = ''

      // 重置连接错误状态（确保正常结束时清除错误提示）
      this.connectionError = false

      // 关闭连接
      if (this.currentEventSource) {
        this.currentEventSource.close()
        this.currentEventSource = null
      }

      await this.loadConversations()
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },

    async initializeChat() {
      const res = await createConversation()

      this.conversationId = res.data.id
      console.log('聊天室ID:', this.conversationId)
    },

    async loadConversations() {
      const res = await getConversationList()
      this.conversations = res.data
    },

    async loadMessages(conversationId) {

      // ⭐ 如果缓存存在直接使用
      if (this.messageCache[conversationId]) {

        this.messages = this.messageCache[conversationId]
        this.conversationId = conversationId
        return
      }

      // ⭐ 否则请求后端
      const res = await getMessages(conversationId)

      const formattedMessages = res.data.map(msg => ({
        id: msg.id,
        content: msg.content,
        isUser: msg.role === 'user',
        timestamp: new Date(msg.createTime)
      }))

      // ⭐ 存入缓存
      this.messageCache[conversationId] = formattedMessages

      this.messages = formattedMessages
      this.conversationId = conversationId
    },

    async createNewConversation() {

      const res = await createConversation()

      const newId = res.data.id

      await this.loadConversations()

      await this.loadMessages(newId)

    },

    async handleDeleteConversation(id) {

      if (!confirm("确定删除这个对话吗？")) {
        return
      }

      await deleteConversation(id)

      delete this.messageCache[id]

      await this.loadConversations()

      // 如果删除的是当前对话
      if (this.conversationId === id) {

        this.messages = []
        this.conversationId = null

      }

    },

    async renameConversationTitle(c) {

      const newTitle = prompt("输入新的话题名称", c.title)

      if (!newTitle) return

      await renameConversation(c.id, newTitle)

      // 更新本地数据
      c.title = newTitle
    },

    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },

    handleLogout() {

      if (!confirm("确定退出登录吗？")) return

      // 清除用户信息
      localStorage.removeItem("userId")
      localStorage.removeItem("username")

      // 跳转登录页
      this.$router.push("/login")
    }

  },

  async mounted() {

    if (!localStorage.getItem("userId")) {
      this.$router.push("/login")
      return
    }

    this.username = localStorage.getItem("username")
    await this.loadConversations()

    if (this.conversations.length > 0) {
      this.loadMessages(this.conversations[0].id)
    } else {
      await this.initializeChat()
      await this.loadConversations()
    }

  },

  beforeUnmount() {
    // 组件销毁前关闭连接
    if (this.currentEventSource) {
      this.currentEventSource.close()
    }
  },

}
</script>

<style scoped>
.app {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;

  /* 和登录页一致的渐变 */
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.main-card {
  width: 95%;
  height: 90vh;
  display: flex;

  border-radius: 20px;
  overflow: hidden;

  /* 毛玻璃效果 */
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);

  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.app-header {
  padding: 20px;

  background: transparent;
  border-bottom: 1px solid rgba(0,0,0,0.1);
}

.app-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.app-subtitle {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.welcome-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.welcome-content {
  text-align: center;
  max-width: 400px;
  color: #666;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.welcome-content h2 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #333;
}

.welcome-content p {
  margin-bottom: 10px;
  line-height: 1.5;
}

.welcome-content ul {
  text-align: left;
  margin: 15px 0;
}

.welcome-content li {
  margin-bottom: 5px;
}

/* AI 正在回复时的消息样式 */
.chat-message {
  display: flex;
  margin-bottom: 20px;
  padding: 0 20px;
}

.ai-message {
  padding-left: 10px;
  justify-content: flex-start;
  flex-direction: row;
}

.message-avatar {
  display: flex;
  align-items: center;   
  margin: 0 10px;
}

.avatar {
  width: 36px;
  height: 36px;

  border-radius: 50%;

  background: linear-gradient(135deg,#ff9a44,#ff6a00);

  display: flex;
  align-items: center;
  justify-content: center;

  font-weight: bold;
  color: white;
}

.ai-avatar {
  background-color: #6c757d;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;

  background: transparent;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.ai-typing-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-response-text {
  font-size: 14px;
  line-height: 1.5;
}

/* AI实时回复的Markdown样式 */
.ai-response-text.message-markdown h1,
.ai-response-text.message-markdown h2,
.ai-response-text.message-markdown h3,
.ai-response-text.message-markdown h4,
.ai-response-text.message-markdown h5,
.ai-response-text.message-markdown h6 {
  margin: 0.5em 0;
  font-weight: bold;
}

.ai-response-text.message-markdown h1 { font-size: 1.5em; }
.ai-response-text.message-markdown h2 { font-size: 1.3em; }
.ai-response-text.message-markdown h3 { font-size: 1.2em; }
.ai-response-text.message-markdown h4 { font-size: 1.1em; }
.ai-response-text.message-markdown h5 { font-size: 1em; }
.ai-response-text.message-markdown h6 { font-size: 0.9em; }

.ai-response-text.message-markdown p {
  margin: 0.5em 0;
}

.ai-response-text.message-markdown ul,
.ai-response-text.message-markdown ol {
  margin: 0.5em 0;
  padding-left: 1.5em;
}

.ai-response-text.message-markdown li {
  margin: 0.2em 0;
}

.ai-response-text.message-markdown code {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.ai-response-text.message-markdown pre {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 1em;
  border-radius: 5px;
  overflow-x: auto;
  margin: 0.5em 0;
}

.ai-response-text.message-markdown pre code {
  background-color: transparent;
  padding: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.ai-response-text.message-markdown blockquote {
  border-left: 4px solid #ccc;
  padding-left: 1em;
  margin: 0.5em 0;
  font-style: italic;
  color: #666;
}

.ai-response-text.message-markdown a {
  color: #007bff;
  text-decoration: underline;
}

.ai-response-text.message-markdown table {
  border-collapse: collapse;
  width: 100%;
  margin: 0.5em 0;
}

.ai-response-text.message-markdown th,
.ai-response-text.message-markdown td {
  border: 1px solid #ddd;
  padding: 0.5em;
  text-align: left;
}

.ai-response-text.message-markdown th {
  background-color: #f2f2f2;
  font-weight: bold;
}

.ai-response-text.message-markdown hr {
  border: none;
  border-top: 1px solid #ddd;
  margin: 1em 0;
}

.connection-error {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background-color: #ff4444;
  color: white;
  padding: 10px 20px;
  border-radius: 5px;
  z-index: 1000;
  animation: slideDown 0.3s ease-out;
}

.error-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.error-icon {
  font-size: 16px;
}

@keyframes slideDown {
  from {
    transform: translateX(-50%) translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateX(-50%) translateY(0);
    opacity: 1;
  }
}

/* 滚动条样式 */
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.messages-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

@media (max-width: 768px) {
  .app-header {
    padding: 15px;
  }

  .app-title {
    font-size: 20px;
  }

  .messages-container {
    padding: 15px 0;
  }

  .welcome-content {
    padding: 0 10px;
  }

  .message-content {
    max-width: 85%;
  }

  .chat-message {
    padding: 0 10px;
  }
}

.sidebar {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 260px;

  padding: 15px;
  padding-top: 60px;   /* ⭐ 关键：给按钮让位置 */

  transition: all 0.3s;

  background: rgba(0,0,0,0.6);
  backdrop-filter: blur(10px);
  color: white;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255,255,255,0.15);
}

.sidebar.collapsed {
  width: 60px;
  padding: 15px 8px;
}

.sidebar.collapsed .conversation-title {
  display: none;
}

.sidebar.collapsed .conversation-actions {
  display: none;
}

.collapse-btn {
  position: absolute;

  left: 50%;     /* ✅ 在左边 */
  top: 15px;      /* 默认在上面 */

  width: 30px;
  height: 30px;

  border-radius: 50%;
  border: none;

  background: #667eea;
  color: white;

  cursor: pointer;

  display: flex;
  align-items: center;
  justify-content: center;
  
  z-index: 10;
  transform: translateX(-50%);
  transition: all 0.3s;
}

.sidebar:not(.collapsed) .collapse-btn {
  transform: translateX(-120px); /* ⭐ 往左移 */
}

.sidebar.collapsed .collapse-btn {
  left: 50%;
  transform: translateX(-50%);  /* ⭐ 精准居中 */
}

.new-chat-btn {
  width: 100%;              /* ⭐ 占满整行 */
  padding: 12px;
  margin-bottom: 10px;      /* 和下面列表拉开 */

  border-radius: 8px;
  border: none;

  background: linear-gradient(135deg, #667eea, #5a67d8);
  color: white;

  font-size: 14px;
  font-weight: 500;

  cursor: pointer;
  transition: all 0.3s;
}

.new-chat-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.conversation-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  flex: 1;          /* ⭐关键：占满剩余空间 */
  overflow-y: auto; /* ⭐聊天多的时候可滚动 */
}

.conversation-item {
  padding: 10px;
  border-radius: 8px;
  margin-top: 6px;
  transition: 0.2s;
}

.conversation-item:hover {
  background: rgba(255,255,255,0.1);
}

.conversation-item:hover {
  background: #2a2b32;
}

.chat-main {
  flex: 1;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;

  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);

  border-radius: 0 20px 20px 0;
}

.chat-input-container {
  padding: 15px;
  background: white;
  border-top: 1px solid #eee;
}

.conversation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  cursor: pointer;
}

.delete-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 16px;
}

.delete-btn:hover {
  color: red;
}

.conversation-actions {
  display: flex;
  gap: 6px;
}

.rename-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 14px;
}

.rename-btn:hover {
  color: #4CAF50;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 10px;

  padding: 8px;
  border-radius: 8px;

  cursor: pointer;
  transition: background 0.2s;
}

.user-card:hover {
  background: rgba(255,255,255,0.1);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}

.logout-btn {
  width: 100%;
  margin-top: 8px;

  padding: 8px;

  border: none;
  border-radius: 6px;

  background: rgba(255,255,255,0.1);
  color: white;

  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(255,255,255,0.2);
}

</style>