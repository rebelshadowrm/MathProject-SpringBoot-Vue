<template>
  <div class="login-page page-shell">
    <section><p class="eyebrow">Welcome back</p><h1>Continue learning.</h1><p>Use your account, or step into one of the prepared portfolio experiences.</p></section>
    <section class="login-card glass-card">
      <form @submit.prevent="login(username,password)"><h2>Sign in</h2><p v-if="error" class="error-state" role="alert">{{ error }}</p><div class="form-field"><label for="username">Username</label><input id="username" v-model="username" autocomplete="username" required></div><div class="form-field"><label for="password">Password</label><input id="password" v-model="password" type="password" autocomplete="current-password" required></div><button class="button primary" :disabled="loading">{{loading?'Signing in…':'Sign in'}}</button></form>
      <div class="divider"><span>Portfolio demo</span></div>
      <div class="demo-grid"><button v-for="role in roles" :key="role" class="demo-button" @click="demo(role)" :disabled="loading"><b>{{role}}</b><span>{{descriptions[role]}}</span></button></div>
    </section>
  </div>
</template>
<script>
import useUsers from '../composables/users.js'
import router from '../router'
export default {
  name: 'Login',
  created(){ document.title = 'Sign in · Math Practice' },
  data:()=>({username:'',password:'',error:'',loading:false,roles:['Student','Teacher','Parent','Admin'],descriptions:{Student:'Practice and assignments',Teacher:'Classes and reports',Parent:'Family progress',Admin:'Platform overview'}}),
  methods: {
    async finish(response){if(!response.ok)throw new Error('Unable to sign in');const data=await response.json();localStorage.setItem('access_token',data.access_token);localStorage.setItem('refresh_token',data.refresh_token);await useUsers().loadUser();await router.push('/dashboard')},
    async login(username,password){this.error='';this.loading=true;try{await this.finish(await fetch('/api/login',{method:'POST',body:new URLSearchParams({username,password})}))}catch{this.error='Check your username and password and try again.'}finally{this.loading=false}},
    async demo(role){this.error='';this.loading=true;try{await this.finish(await fetch(`/api/demo/login/${role.toLowerCase()}`,{method:'POST'}))}catch{this.error='The demo is not available in this environment.'}finally{this.loading=false}}
  }
}
</script>
<style scoped>
.login-page{display:grid;grid-template-columns:.8fr 1.2fr;gap:4rem;align-items:center;padding-block:4rem}.login-page>section:first-child h1{font:800 clamp(3rem,7vw,6rem)/1 var(--ff-serif);margin:.5rem 0 1rem}.login-card{padding:2rem}.login-card form{display:grid;gap:1.25rem}.login-card h2{font:700 2rem var(--ff-serif)}.divider{display:flex;align-items:center;gap:1rem;margin:2rem 0 1rem;color:hsl(var(--clr-white-600));font:600 .75rem var(--ff-mono);text-transform:uppercase}.divider:before,.divider:after{content:'';height:1px;flex:1;background:hsl(var(--clr-white-200)/.15)}.demo-grid{display:grid;grid-template-columns:1fr 1fr;gap:.75rem}.demo-button{text-align:left;padding:1rem;background:hsl(var(--clr-white-200)/.05);border:1px solid hsl(var(--clr-white-200)/.14);border-radius:1rem;color:var(--clr-text);cursor:pointer}.demo-button b,.demo-button span{display:block}.demo-button span{font-size:.78rem;color:hsl(var(--clr-white-600));margin-top:.25rem}@media(max-width:45rem){.login-page{grid-template-columns:1fr;gap:2rem}.demo-grid{grid-template-columns:1fr}}
</style>
