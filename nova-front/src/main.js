import { createSSRApp } from "vue";
import App from "./App.vue";
import SvgIcon from "@/components/svg-icon/svg-icon.vue";

export function createApp() {
  const app = createSSRApp(App);
  app.component("svg-icon", SvgIcon);
  return {
    app,
  };
}
