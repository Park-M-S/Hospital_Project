import Vuex from 'vuex';

export default new Vuex.Store({
    state: {
      userLat: localStorage.getItem('userLat') || null,
      userLng: localStorage.getItem('userLng') || null,  
      department: localStorage.getItem('department') || null,
    },
    mutations: {
      setLocation(state, { userLat, userLng }) {
        state.userLat = userLat;
        state.userLng = userLng;

        localStorage.setItem('userLat', userLat);
        localStorage.setItem('userLng', userLng);
      },
      setDepartment(state, { department }) {
        state.department = department;

        localStorage.setItem('department', department);
      }
    },
    actions: {
      updateLocation({ commit }, location) {
        commit('setLocation', location);
      },
      updateDepartment({ commit }, department) {
        commit('setDepartment', department);
      }
    },
    getters: {
      userLat: state => state.userLat,
      userLng: state => state.userLng,
      department: state => state.department,
    }
})