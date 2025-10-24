import Vue from "vue";
import Router from "vue-router";
import * as Sentry from "@sentry/vue";

Sentry.init({
  app,
  dsn: "https://acacef884a7f193be8308f7eb4cbdb98@o4510221734772736.ingest.us.sentry.io/4510221763543041",
  // Setting this option to true will send default PII data to Sentry.
  // For example, automatic IP address collection on events
  sendDefaultPii: true
});

new Vue({
  router,
  render: (h) => h(App),
}).$mount("#app");