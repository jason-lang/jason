<script>
export default {
  name: "Loading"
}
</script>

<template>
  <div class="loading code"></div>
</template>

<style scoped>
.loading:after {
  display: block;
  content: "Loading";
  color: var(--pallete-text-aside);
  animation: loading 2s infinite;
  font-size: var(--text-little);
}

@keyframes loading {
  0% {
    content: "Loading";
  }
  25% {
    content: "Loading.";
  }
  50% {
    content: "Loading..";
  }
  75% {
    content: "Loading...";
  }
}
</style>
