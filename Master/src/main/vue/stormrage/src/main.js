// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App.vue'
import ElementUI from 'element-ui'
import Router from 'vue-router'
import PageNotFound from '@/components/PageNotFound'

Vue.config.productionTip = false;

Vue.use(Router);
Vue.use(ElementUI);

function view(name) {
  return function(resolve) {
    require(['@/components/' + name + '.vue'], resolve);
  }
}

//路由
const router = new Router({
  routes: [
    {
      path: '/',
      name: 'HomePage',
      component: view("HomePage")
    },
    {
      path: '/ray',
      name: 'Ray',
      component: view("Ray")
    },
    {
      path: '/another',
      name: "Another",
      component: view("Another")
    },

    { path: "*", component: PageNotFound }
  ]
});



const vm = new Vue({
  el: '#app',
  router,
  template: '<App ref="app"/>',
  components: {App}
});


router.beforeEach((to, from, next) => {
  vm.$refs.app.isShowMenuBar = to.path !== "/";
  next();
});


export {router, vm};