<script>
export default {
  name: "ToggleButton",
  methods: {
    click() {
      this.value = !this.value
    }
  },
  data() {
    return {
      value: false
    }
  }
}
</script>

<template>
  <div class="toggle-button" @click="click">
    <div :class="{'toggle-button__circle': true, active: value}"></div>
  </div>
</template>

<style scoped>

.toggle-button {
  width: 25px;
  height: 14px;
  border: 1px solid var(--pallete-text);
  @apply flex items-center rounded-full relative
}

.toggle-button__circle {
  background-color: var(--pallete-text);
  height: 10px;
  aspect-ratio: 1/1;
  margin: 0 1px;
  transition: inset 0.3s;
  left: 0;
  right: unset;
  @apply rounded-full absolute
}

.toggle-button__circle.active {
  left: unset;
  right: 0;
}

</style>
