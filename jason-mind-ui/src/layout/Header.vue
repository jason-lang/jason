<script>
import masService from "@/service/mas-service.js";

export default {
  name: "Header",
  async mounted() {
    this.mas = (await masService.getMas()).data
  },
  data() {
    return {
      mas: null
    }
  }
}
</script>

<template>
  <header class="header">
    <div class="header__logo">
      <div class="header__logo__circ"></div>
      <div class="header__logo__circ"></div>
      <div class="header__logo__circ"></div>
    </div>
    <h1 class="header__title" v-if="this.mas != null">{{ this.mas.name }}</h1>
    <div class="header__versions">
      <strong class="text-aside text-little font-normal">Mind 0.1.0</strong>
      <strong class="text-aside text-little font-normal">Jason 3.3.1</strong>
    </div>
  </header>
</template>

<style scoped>

.header {
  background-color: var(--pallete-1);
  height: var(--header-height);
  @apply flex items-center justify-between rounded-md shadow px-4
}

.header__title {
  left: 50%;
  transform: translateX(-50%);
  font-size: 1em;
  @apply font-medium absolute select-none
}

.header__logo {
  @apply flex items-center relative select-none
}

.header__versions {
  @apply flex items-center gap-2 select-none
}

.header__logo__circ {
  height: 10px;
  aspect-ratio: 1/1;
  border: 2px solid var(--pallete-1);
  box-sizing: content-box;
  @apply rounded-full absolute
}

.header__logo__circ:nth-child(1) {
  background-color: var(--pallete-beliefs-1);
  left: 0;
}

.header__logo__circ:nth-child(2) {
  background-color: var(--pallete-intention-1);
  left: 10px;
}

.header__logo__circ:nth-child(3) {
  background-color: var(--pallete-plans-1);
  left: 20px;
}


</style>
