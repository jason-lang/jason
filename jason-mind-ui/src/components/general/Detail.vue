<script>
export default {
  name: "Detail",
  props: {
    content: String,
    fontSize: {
      default: "0.7em",
      type: String
    }
  }
}
</script>

<template>
  <div class="detail">
    <span>{{ content }}</span>
  </div>
</template>

<style scoped>

.detail {
  height: 18px;
  background-color: var(--pallete-2);
  color: v-bind(color);
  font-size: v-bind(fontSize);
  @apply flex items-center justify-center rounded px-1.5 font-medium select-none
}

</style>
