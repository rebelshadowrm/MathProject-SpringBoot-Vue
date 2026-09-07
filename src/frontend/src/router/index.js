import {createRouter, createWebHistory} from "vue-router"
import Home from '../views/Home.vue'
import Profile from '../views/Profile.vue'
import Test from '../views/Test.vue'
import Flashcards from '../views/Flashcards.vue'
import Drills from '../views/Drills.vue'
import Login from '../views/Login.vue'
import QuestionEditor from "../views/QuestionEditor.vue"
import useUsers from '../composables/users'
import Leaderboard from "../views/Leaderboard.vue";

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home
    },
    {
        path: '/profile',
        name: 'Profile',
        component: Profile,
    },
    {
        path: '/profile/:username',
        name: 'UserProfile',
        component: Profile,
        props: true
    },
    {
        path: '/leaderboard',
        name: 'Leaderboard',
        component: Leaderboard
    },
    {
        path: '/test',
        name: 'Test',
        component: Test
    },
    {
        path: '/flashcards',
        name: 'Flashcards',
        component: Flashcards
    },
    {
        path: '/drills',
        name: 'Drills',
        component: Drills
    },
    {
        path: '/questions',
        name: 'QuestionEditor',
        component: QuestionEditor
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
    const {getIsLoggedIn} = useUsers()
    if (
        !getIsLoggedIn().value &&
        to.name !== 'Login' &&
        to.name !== 'Home'
    ) {
        return { name: 'Login'}
    }



})

export default router
