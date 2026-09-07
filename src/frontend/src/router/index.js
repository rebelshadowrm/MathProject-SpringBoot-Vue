import {createRouter, createWebHistory} from "vue-router"
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import useUsers from '../composables/users'
import Leaderboard from "../views/Leaderboard.vue";
import Dashboard from '../views/Dashboard.vue'
import Courses from '../views/Courses.vue'
import Practice from '../views/Practice.vue'
import Assignments from '../views/Assignments.vue'
import ChangePassword from '../views/ChangePassword.vue'

const routes = [
    {
        path: '/change-password',
        name: 'ChangePassword',
        component: ChangePassword
    },
    {
        path: '/',
        name: 'Home',
        component: Home
    },
    {
        path: '/dashboard',
        name: 'Dashboard',
        component: Dashboard,
    },
    {
        path: '/profile',
        redirect: '/dashboard'
    },
    {
        path: '/leaderboard',
        name: 'Leaderboard',
        component: Leaderboard
    },
    {
        path: '/test',
        redirect: '/assignments'
    },
    {
        path: '/assignments',
        name: 'Assignments',
        component: Assignments
    },
    {
        path: '/flashcards',
        name: 'Flashcards',
        component: Practice,
        props: {mode: 'FLASHCARD'}
    },
    {
        path: '/drills',
        name: 'Drills',
        component: Practice,
        props: {mode: 'DRILL'}
    },
    {
        path: '/courses',
        name: 'Courses',
        component: Courses
    },
    {
        path: '/login',
        name: 'Login',
        component: Login
    },

]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

router.beforeEach(async(to) => {
    const {getIsLoggedIn, loadUser} = useUsers()
    if(localStorage.getItem('access_token') && !getIsLoggedIn().value) await loadUser()
    if (
        !getIsLoggedIn().value &&
        to.name !== 'Login' &&
        to.name !== 'Home'
    ) {
        return { name: 'Login'}
    }

    if (getIsLoggedIn().value && to.name !== 'ChangePassword') {
        const response = await fetch('/api/account/status', {headers:{Authorization:`Bearer ${localStorage.getItem('access_token')}`}})
        if (response.ok && (await response.json()).passwordChangeRequired) return {name:'ChangePassword'}
    }



})

export default router
