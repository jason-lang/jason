<script>
export default {
  name: "Button",
  props: {
    text: String,
    icon: String,
    ratio: {
      default: "inherit",
      type: String
    },
    selected: Boolean
  }
}
</script>

<template>
  <button :class="{button: true, '--has-icon': this.icon, selected: selected}">
    <span v-if="this.text">{{ this.text }}</span>
    <img v-if="this.icon" :src="this.icon" alt="icon" class="button__icon"/>
  </button>
</template>

<style scoped>

.button {
  height: var(--actuators-height);
  background-color: transparent;
  @apply grid place-items-center px-2 rounded border-none flex-shrink-0
}

.button.selected {
  background-color: var(--pallete-2);
}

.button:hover {
  background-color: var(--pallete-2);
}

.button.--has-icon {
  width: var(--actuators-height);
  @apply p-0
}

.button__icon {
  width: v-bind(ratio);
  aspect-ratio: 1/1;
  box-sizing: content-box;
  @apply flex-shrink-0
}

</style>
