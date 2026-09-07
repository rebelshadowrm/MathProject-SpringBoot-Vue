import { reactive, readonly, computed } from 'vue'

  const TOKEN = "access_token"
  const REFRESH = "refresh_token"

  const state = reactive({
    userData: {
      userName: null,
      firstName: null,
      lastName: null,
      email: null,
      roles: [],
    },
    isLoggedIn: false,
    loading: false,
    error: "",
  })

  const getters = {
      getFullName: () => {
        return computed(
          () => `${state.userData.firstName} ${state.userData.lastName}`
        )
      },
      getUsername: () => {
        return computed(() => state.userData?.userName)
      },
      getEmail: () => {
        return computed(() => state.userData?.email)
      },
      getRoles: () => {
        return computed(() => state.userData?.roles)
      },
      getIsLoggedIn: () => {
        return computed(() => state.isLoggedIn)
      },
      getError: () => {
        return computed(() => state.error)
      },
      AuthHeader: (type, body) => {
        return  {
          method: `${type}`,
          headers: {
            'Authorization': 'Bearer ' + methods.getAccessToken(),
            "Content-type": "application/json;charset=UTF-8"
          },
          body
        }
      },
      RefreshHeader: (type, body) => {
        return  {
          method: `${type}`,
          headers: {
            'Authorization': 'Bearer ' + methods.getRefreshToken(),
            "Content-type": "application/json;charset=UTF-8"
          },
          body
        }
      }
    }

  const actions = {
    updateUserData: ({ first_name, last_name, email, username, roles }) => {
      actions.updateIsLoggedIn(true)
      state.userData.firstName = first_name
      state.userData.lastName = last_name
      state.userData.email = email
      state.userData.userName = username
      state.userData.roles = roles || []
    },
    updateIsLoggedIn: (isLoggedIn) => {
      state.isLoggedIn = isLoggedIn;

      if (isLoggedIn === false) {
        state.userData.firstName = ''
        state.userData.lastName = ''
        state.userData.email = ''
        state.userData.userName = ''
        state.userData.roles = []
        methods.removeTokens()
      }
    },
    updateLoading: (loadingStatus) => {
      state.loading = loadingStatus;
    },
    updateError: (error) => {
      state.error = error
    },
    updateAccessToken: (token) => {
      localStorage.setItem(TOKEN, token)
    },
    loadUser: async () => {
      if(methods.getRefreshToken() !== undefined) {
        const data = await methods.getUserData()
        if(data.status === 401 || data.status === 403) {
          actions.updateError("Attempting to refresh token")
          const response = await fetch('/api/token/refresh', getters.RefreshHeader('GET'))
          const {access_token} = await response.json()
          if(access_token !== undefined) {
            actions.updateAccessToken(access_token)
          }
          const newData = await methods.getUserData()
          if(newData.status === 200 || newData.status === 201) {
            actions.updateUserData(await newData.json())
          }
        } else if (data.status === 200 || data.status === 201) {
          actions.updateError('')
          actions.updateUserData(await data.json())
        } else {
          actions.updateError(data)
        }
      }
    },
  }

  const methods = {
    getAccessToken: () => {
      if (localStorage.getItem(TOKEN) !== null) {
        return localStorage.getItem(TOKEN)
      }
    },
    getRefreshToken: () => {
      if (localStorage.getItem(REFRESH) !== null) {
        return localStorage.getItem(REFRESH)
      }
    },
    removeTokens: () => {
      localStorage.removeItem(TOKEN)
      localStorage.removeItem(REFRESH)
    },
    decodeJWT: (token) => {
      try {
        return JSON.parse(atob(token.split('.')[1]))
      } catch {
        return null
      }
    },
    getUserData: async () => {
      try {
        const token = methods.getAccessToken()
        if(token !== undefined) {
          const {sub} = await methods.decodeJWT(token)
          return await fetch(`/api/user/${sub}`, getters.AuthHeader('GET'))
        } else {
          throw new Error("Access token not available")
        }
      } catch(err) { 
        actions.updateError(err)
      }
    }
  }

  
  export default () => {
    return {
      state: readonly(state),
      ...getters,
      ...actions,
      ...methods,
    };
  };
