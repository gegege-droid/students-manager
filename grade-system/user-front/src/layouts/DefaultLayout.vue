<script setup>
import AiAssistant from '@/components/AiAssistant.vue'
import AppFooter from '@/components/AppFooter.vue'
import AppHeader from '@/components/AppHeader.vue'
import AppSidebar from '@/components/AppSidebar.vue'
</script>

<template>
  <div class="app-shell">
    <AppHeader />
    <div class="app-body">
      <AppSidebar />
      <main class="app-main">
        <slot />
      </main>
    </div>
    <AppFooter />
    <AiAssistant />
  </div>
</template>
