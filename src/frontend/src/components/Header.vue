<template>
  <header class="site-header">
    <nav aria-label="Primary navigation">
      <router-link class="brand" to="/">Math<span>Practice</span></router-link>
      <div class="nav-links">
        <router-link v-if="loggedIn" to="/dashboard">Dashboard</router-link>
        <router-link v-if="loggedIn" to="/courses">Courses</router-link>
        <router-link v-if="student" to="/assignments">Assignments</router-link>
        <router-link v-if="student" to="/drills">Drills</router-link>
        <router-link v-if="student" to="/flashcards">Flashcards</router-link>
        <router-link v-if="loggedIn" to="/leaderboard">Ranks</router-link>
        <router-link v-if="!loggedIn" to="/login">Sign in</router-link>
        <button v-else class="link-button" @click="logout">Sign out</button>
      </div>
    </nav>
  </header>
</template>
<script setup>
import { computed } from 'vue'
import useUsers from '../composables/users.js'
import router from '../router'
const { getIsLoggedIn, updateIsLoggedIn, loadUser, getRoles } = useUsers()
loadUser()
const loggedIn = getIsLoggedIn()
const roles = getRoles()
const student = computed(() => roles.value.some(role => role.name === 'ROLE_STUDENT'))
const logout = () => { updateIsLoggedIn(false); router.push('/') }
</script>
<style scoped>
.site-header { position: sticky; top: 0; z-index: 10; background: hsl(var(--clr-black-800) / .88); backdrop-filter: blur(18px); border-bottom: 1px solid hsl(var(--clr-white-200) / .12); }
nav { max-width: 76rem; margin: auto; min-height: 4.5rem; padding: .75rem 1.25rem; display: flex; align-items: center; justify-content: space-between; gap: 1.5rem; }
.nav-links {
  display: flex;
  align-items: center;
  gap: 1rem;
  overflow-x: auto;
}
.brand { color: white; font: 800 1.3rem var(--ff-serif); text-decoration: none; white-space: nowrap; }
.brand span { color: hsl(var(--clr-accent-200)); }
.nav-links a,.link-button { color: var(--clr-text); font: 600 .82rem var(--ff-mono); text-transform: uppercase; letter-spacing: .04em; text-decoration: none; white-space: nowrap; }
.nav-links a:hover,.nav-links a.router-link-active,.link-button:hover { color: hsl(var(--clr-accent-200)); }
.link-button { border: 0; background: none; cursor: pointer; }
@media(max-width:48rem){nav{align-items:flex-start;flex-direction:column}.nav-links{width:100%;padding-bottom:.25rem}}
</style>
