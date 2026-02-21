<script>
import {getFormattedTimeFromDateString} from "@/utils/date.js";

export default {
  name: "Log",
  props: {
    log: {}
  },
  computed: {
    time() {
      return getFormattedTimeFromDateString(this.log.time)
    },
    styleClasses() {
      let classes = "log"

      if (this.log.highlight !== undefined) {
        classes += this.log.highlight ? " highlight" : " aside"
      }

      return classes
    }
  },
  methods: {
    goToSender() {
      this.$emit("goToSender")
    }
  }
}
</script>

<template>
  <div :class="styleClasses">
    <span class="log__time">{{ time }}</span>
    <span class="log__agent-name">{{ log.agentName }}</span>
    <div class="log__content">
      <span v-for="line in log.content.split('\n')">{{ line }}</span>
    </div>
    <div class="log__cycle" @click="goToSender">
      <img alt="arrow-icon" src="@/assets/img/arrow/grey-arrow-icon.svg"/>
      <span>cycle {{ log.cycle }} of {{ log.agentName }}</span>
    </div>
  </div>
</template>

<style scoped>

.log {
  @apply flex items-start gap-4 px-4 py-2
}

.log.highlight {
}

.log.aside {
  @apply opacity-50
}

.log__cycle > img {
  transform: rotate(90deg);
}

.log__agent-name {
  width: 100px;
  @apply select-none font-medium
}

.log:hover {
  background-color: var(--pallete-bg);
}

.log:hover > .log__cycle {
  @apply opacity-100
}

.log__time {
  color: var(--pallete-text-aside);
  @apply select-none
}

.log__content {
  @apply flex flex-col
}

.log__cycle {
  color: var(--pallete-text-aside);
  @apply flex items-center gap-2 ml-auto relative opacity-0 cursor-pointer
}

.log__cycle:hover {
  @apply underline
}

</style>
