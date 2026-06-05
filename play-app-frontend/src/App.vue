<script setup lang="ts">
import {onHide, onLaunch, onShow} from "@dcloudio/uni-app";
import {useUserStore} from "./store/user";

onLaunch(async () => {
  console.log("App Launch");
  const userStore = useUserStore();
  if (!userStore.token) {
    // #ifdef H5
    console.log("H5 mode: skip auto wxLogin, use mock login on mine page");
    // #endif
    // #ifndef H5
    console.log("No token, attempting auto wxLogin...");
    await userStore.wxLogin();
    // #endif
  }
});

onShow(() => {
  console.log("App Show");
});

onHide(() => {
  console.log("App Hide");
});
</script>

<style>
/* 每个页面公共css */
page {
  background-color: #f7f8fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica,
    Segoe UI, Arial, Roboto, 'PingFang SC', 'miui', 'Hiragino Sans GB', 'Microsoft Yahei',
    sans-serif;
  color: #333;
  font-size: 28rpx;
  box-sizing: border-box;
}
</style>
