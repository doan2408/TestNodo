<script setup>
import Header from './components/Header.vue';
</script>

<template>
  <Header></Header>
  <RouterView></RouterView>
</template>

<style>
/* Global Reset & Modern Font Stack */
:root {
  --font-primary: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  --bg-color: #f8fafc;
  --text-color: #1e293b;

  /* Container size */
  --container-width: 1200px;
  --container-padding: 20px;
}

body {
  margin: 0;
  padding: 0;
  font-family: var(--font-primary);
  background-color: var(--bg-color);
  color: var(--text-color);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  line-height: 1.5;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Wrapper cho tất cả trang → giúp không sát mép màn hình */
.page-wrapper {
  width: 100%;
  max-width: var(--container-width);
  margin: 0 auto;
  padding: 0 var(--container-padding);
}

/* Smooth scrolling for the whole app */
html {
  scroll-behavior: smooth;
}

/* Optional: Route transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
