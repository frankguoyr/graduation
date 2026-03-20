<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="title">AI 编程助手</h2>
      <p class="subtitle">欢迎回来 👋</p>

      <input v-model="username" placeholder="请输入用户名" />
      <input v-model="password" type="password" placeholder="请输入密码" />

      <button @click="login">登录</button>

      <div class="extra">
        <span>没有账号？</span>
        <a @click="$router.push('/register')">去注册</a>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios"

export default {
  data() {
    return {
      username: "",
      password: ""
    }
  },
  methods: {
    async login() {
      try {
        const res = await axios.post("http://localhost:8081/api/user/login", {
          username: this.username,
          password: this.password
        })

        localStorage.setItem("userId", res.data)

        this.$router.push("/")
      } catch (e) {
        alert("用户名或密码错误")
      }
    }
  }
}
</script>

<style scoped>
/* 背景 */
.login-page {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;

  background: linear-gradient(135deg, #667eea, #764ba2);
}

/* 卡片 */
.login-card {
  width: 360px;
  padding: 40px;
  border-radius: 16px;
  background: #ffffff;

  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);

  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 标题 */
.title {
  text-align: center;
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}

.subtitle {
  text-align: center;
  color: #888;
  margin-bottom: 10px;
}

/* 输入框 */
input {
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #ddd;
  outline: none;
  transition: 0.3s;
}

input:focus {
  border-color: #667eea;
  box-shadow: 0 0 5px rgba(102, 126, 234, 0.5);
}

/* 按钮 */
button {
  padding: 12px;
  border: none;
  border-radius: 8px;
  background: #667eea;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: 0.3s;
}

button:hover {
  background: #5a67d8;
}

/* 底部 */
.extra {
  text-align: center;
  font-size: 14px;
}

.extra a {
  color: #667eea;
  cursor: pointer;
  margin-left: 5px;
}
</style>