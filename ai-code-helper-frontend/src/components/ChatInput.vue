<template>
  <div class="chat-input">
    <div class="input-container">
      <textarea
        ref="inputRef"
        v-model="inputMessage"
        :placeholder="placeholder"
        :disabled="disabled"
        class="input-textarea"
        rows="1"
        @keydown="handleKeyDown"
        @input="adjustHeight"
      />
      <button
        :disabled="disabled || !inputMessage.trim()"
        @click="sendMessage"
        class="send-button"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M2 21l21-9L2 3v7l15 2-15 2v7z" fill="currentColor"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatInput',
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String,
      default: '请输入您的问题...'
    }
  },
  data() {
    return {
      inputMessage: ''
    }
  },
  methods: {
    sendMessage() {
      if (this.inputMessage.trim() && !this.disabled) {
        this.$emit('send-message', this.inputMessage.trim())
        this.inputMessage = ''
        this.adjustHeight()
      }
    },
    handleKeyDown(event) {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault()
        this.sendMessage()
      }
    },
    adjustHeight() {
      this.$nextTick(() => {
        const textarea = this.$refs.inputRef
        textarea.style.height = 'auto'
        textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
      })
    },
    focus() {
      this.$refs.inputRef.focus()
    }
  },
  mounted() {
    this.adjustHeight()
  }
}
</script>

<style scoped>
.chat-input {
  padding: 15px;

  background: rgba(255,255,255,0.8);
  backdrop-filter: blur(10px);

  border-top: 1px solid rgba(0,0,0,0.05);
}

.input-container {
  display: flex;
  align-items: center;
  gap: 10px;

  max-width: 800px;
  margin: 0 auto;

  background: rgba(255,255,255,0.9);
  padding: 10px 14px;

  border-radius: 30px;

  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.input-textarea {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
}

.send-button {
  width: 42px;
  height: 42px;

  border-radius: 50%;
  border: none;

  background: linear-gradient(135deg, #667eea, #5a67d8);
  color: white;

  cursor: pointer;

  transition: 0.2s;
}

.send-button:hover {
  transform: scale(1.05);
}
</style> 