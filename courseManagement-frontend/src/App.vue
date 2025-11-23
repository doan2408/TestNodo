<script setup>
import Header from './components/Header.vue';
</script>

<template>
  <Header class="glass"></Header>
  <RouterView></RouterView>
</template>

<style>
/* ================================
   GLOBAL DESIGN SYSTEM
=================================== */
:root {
  /* Font */
  --font-primary: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;

  /* Colors */
  --bg-color: #f5f7fa;
  --text-color: #1e293b;

  /* Accent */
  --accent: #6366f1;            /* Indigo */
  --accent-light: #a5b4fc;
  --accent-soft: rgba(99, 102, 241, 0.1);

  /* Shadow & Blur */
  --shadow-soft: 0 10px 25px rgba(0, 0, 0, 0.06);
  --shadow-hover: 0 12px 35px rgba(0, 0, 0, 0.12);
  --glass-blur: blur(12px);
  --glass-bg: rgba(255, 255, 255, 0.4);
  --glass-border: rgba(255, 255, 255, 0.5);

  /* Layout */
  --container-width: 1200px;
  --container-padding: 22px;

  /* Animation */
  --transition-fast: 0.15s ease;
  --transition-normal: 0.25s ease;
}

/* ================================
   BASE
=================================== */
html {
  scroll-behavior: smooth;
}

body {
  margin: 0;
  padding: 0;
  font-family: var(--font-primary);
  background: var(--bg-color);
  color: var(--text-color);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  line-height: 1.6;
}

/* ================================
   APP WRAPPER
=================================== */
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Container cho mỗi page */
.page-wrapper {
  width: 100%;
  max-width: var(--container-width);
  margin: 0 auto;
  padding: 0 var(--container-padding);
  animation: fadeIn 0.5s ease;
}

/* ================================
   ELEGANT ROUTE TRANSITION
=================================== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ================================
   GLASS-EFFECT UTILS
=================================== */
.glass {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-soft);
  border-radius: 16px;
  padding: 20px;
}

/* ================================
   SOFT CARD (Neumorphism)
=================================== */
.soft-card {
  background: #fff;
  border-radius: 18px;
  padding: 24px;
  box-shadow:
    8px 8px 20px rgba(0,0,0,0.06),
    -8px -8px 20px rgba(255,255,255,0.6);
  transition: var(--transition-normal);
}

.soft-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

/* ================================
   SMOOTH HOVER LINKS
=================================== */
a {
  color: var(--accent);
  text-decoration: none;
  transition: var(--transition-fast);
}

a:hover {
  color: var(--accent-light);
}

/* ================================
   SCROLLBAR (Modern)
=================================== */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-thumb {
  background: var(--accent-soft);
  border-radius: 8px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--accent-light);
}

/* ================================
   ANIMATIONS
=================================== */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes float {
  0%   { transform: translateY(0); }
  50%  { transform: translateY(-6px); }
  100% { transform: translateY(0); }
}
</style>
