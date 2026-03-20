<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="title">AI 编程助手</h2>
      <p class="subtitle">创建你的账号 🚀</p>

      <input v-model="username" placeholder="请输入用户名" />
      <input v-model="password" type="password" placeholder="请输入密码" />
      <input v-model="confirmPassword" type="password" placeholder="确认密码" />

      <button @click="register">注册</button>

      <div class="extra">
        <span>已有账号？</span>
        <a @click="$router.push('/login')">去登录</a>
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
      password: "",
      confirmPassword: ""
    }
  },
  methods: {
    async register() {

      if (!this.username || !this.password) {
        alert("用户名和密码不能为空")
        return
      }

      if (this.password !== this.confirmPassword) {
        alert("两次密码不一致")
        return
      }

      try {
        await axios.post("http://localhost:8081/api/user/register", {
          username: this.username,
          password: this.password
        })

        alert("注册成功！请登录")

        this.$router.push("/login")

      } catch (e) {
        alert("注册失败，用户名可能已存在")
      }
    }
  }
}
</script>

<style scoped>
/* 直接复用登录页样式（保持统一） */

.login-page {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;

  background: linear-gradient(135deg, #667eea, #764ba2);
}

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