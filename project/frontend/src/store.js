import Vuex from 'vuex';

export default new Vuex.Store({
    state: {
      userLat: null,
      userLng: null,
      department: null,
    },
    mutations: {
      setLocation(state, { userLat, userLng }) {
        state.userLat = userLat
        state.userLng = userLng
      },
      setDepartment(state, { department }) {
        state.department = department
      }
    },
    actions: {
      updateLocation({ commit }, location) {
        commit('setLocation', location)
      },
      updateDepartment({ commit }, department) {
        commit('setDepartment', department)
      }
    },
    getters: {
      userLat: state => state.userLat,
      userLng: state => state.userLng,
      department: state => state.department,
    }
})