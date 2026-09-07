<template>
  <main class="page-shell narrow-page">
    <section class="glass-card account-card">
      <p class="eyebrow">Account security</p>
      <h1>Choose a new password</h1>
      <p>Your teacher-created account uses a temporary password. Replace it before continuing.</p>
      <p v-if="error" class="error-state" role="alert">{{error}}</p>
      <form @submit.prevent="change">
        <div class="form-field"><label>Temporary password<input v-model="currentPassword" type="password" autocomplete="current-password" required></label></div>
        <div class="form-field"><label>New password<input v-model="newPassword" type="password" minlength="10" autocomplete="new-password" required></label></div>
        <button class="button primary" :disabled="loading">{{loading?'Saving…':'Save password'}}</button>
      </form>
    </section>
  </main>
</template>
<script>
import {api} from '../api.js'
export default{name:'ChangePassword',data:()=>({currentPassword:'',newPassword:'',error:'',loading:false}),methods:{async change(){this.error='';this.loading=true;try{await api('/api/account/password',{method:'POST',body:JSON.stringify({currentPassword:this.currentPassword,newPassword:this.newPassword})});await this.$router.push('/dashboard')}catch(e){this.error=e.message}finally{this.loading=false}}}}
</script>
<style scoped>.narrow-page{max-width:42rem;padding-block:5rem}.account-card{padding:2rem}.account-card h1{font:700 clamp(2rem,6vw,3.5rem)/1.05 var(--ff-serif);margin:.5rem 0 1rem}.account-card form{display:grid;gap:1.25rem;margin-top:2rem}</style>
