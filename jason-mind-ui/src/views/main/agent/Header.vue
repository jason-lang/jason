<script>
import {preventNotNumbers} from "@/utils/events.js";
import Button from "@/components/general/button/Button.vue";
import {getIcon} from "@/utils/utils.js";

export default {
  name: "Header",
  components: {Button},
  props: {
    agent: {}
  },
  methods: {
    getIcon,
    preventNotNumbers,
    previousCycle() {
      if (this.currentCycle <= this.agent.oldestCycle) {
        this.currentCycle = this.agent.oldestCycle
        return
      }
      this.currentCycle--
      this.editing = true
      this.doCycle()
    },
    nextCycle() {
      if (this.currentCycle >= this.agent.newerCycle) {
        this.currentCycle = this.agent.newerCycle
        return
      }
      this.currentCycle++
      this.editing = true
      this.doCycle()
    },
    doCycle() {
      this.$emit("cycle", this.currentCycle)
    },
    topCycle() {
      this.editing = false
      this.$emit("reset")
    },
    bottomCycle() {
      this.editing = true
      this.currentCycle = this.agent.oldestCycle
      this.doCycle()
    }
  },
  computed: {
    isNewerCycle() {
      return this.currentCycle === this.agent.newerCycle
    },
    isOldestCycle() {
      return this.currentCycle === this.agent.oldestCycle
    }
  },
  watch: {
    agent: {
      handler(agent) {
        this.currentCycle = agent.currentCycle
      },
      deep: true
    }
  },
  data() {
    return {
      currentCycle: 0,
      editing: false
    }
  }
}
</script>

<template>
  <header class="agent-content__header">
    <div class="flex items-center gap-2">
      <img alt="agent-icon" src="@/assets/img/agent-icon.svg"/>
      <h2 class="agent-content__name">
        {{ agent.name }}
      </h2>
    </div>

    <div class="agent-content__header__cycle-selector">
      <div class="flex items-center gap-1">
        <input
          v-model="currentCycle"
          class="agent-content__header__current-cycle"

          @click="editing = true"
          @keydown="preventNotNumbers"
          @keyup.enter="doCycle"
        />
      </div>

      <div class="flex items-center h-full">
        <div :class="this.isOldestCycle ? 'opacity-50 pointer-events-none' : ''"
             class="agent-content__header__cycle-selector__button"
             @click="previousCycle">
          <img alt="left-arrow-icon" class="y-arrow-icon left"
               src="../../../assets/img/arrow/arrow-icon.svg"/>
        </div>
        <div :class="this.isNewerCycle ? 'opacity-50 pointer-events-none' : ''"
             class="agent-content__header__cycle-selector__button"
             @click="nextCycle">
          <img alt="left-arrow-icon" class="y-arrow-icon right"
               src="../../../assets/img/arrow/arrow-icon.svg"/>
        </div>
      </div>

      <div class="flex items-center h-full">
        <div v-if="!this.isNewerCycle"
             :class="this.isNewerCycle ? 'opacity-50 pointer-events-none' : ''"
             class="agent-content__header__cycle-selector__button" @click="topCycle">
          <img alt="up-arrow-icon" class="y-arrow-icon" src="../../../assets/img/arrow/arrow-icon.svg"/>
          <span>Last: {{ agent.newerCycle }}</span>
        </div>
        <div v-if="!this.isOldestCycle"
          :class="this.isOldestCycle ? 'opacity-50 pointer-events-none' : ''"
             class="agent-content__header__cycle-selector__button"
             @click="bottomCycle">
          <img alt="up-arrow-icon" class="y-arrow-icon bottom" src="../../../assets/img/arrow/arrow-icon.svg"/>
          <span>First: {{ agent.oldestCycle }}</span>
        </div>
      </div>
    </div>

    <Button :icon="getIcon('close-icon.svg')" @click="$emit('close')" class="ml-auto"/>
  </header>
</template>

<style scoped>
.agent-content__header {
  height: var(--header-height);
  border-bottom: 1px solid var(--pallete-trace);
  @apply flex items-center pl-4 pr-2 gap-4 relative w-full flex-shrink-0
}

.agent-content__header__close-button {
  height: var(--actuators-height);
  @apply flex items-center justify-center px-1.5 rounded  ml-auto
}

.agent-content__header__close-button:hover {
  background-color: var(--pallete-2);
}

.agent-content__name {
  font-size: 1em;
  @apply font-medium select-none
}

.agent-content__header__cycle-selector {
  @apply flex items-center h-full gap-2 w-fit mr-2
}

.agent-content__header__cycle-selector__button {
  height: var(--actuators-height);
  font-size: var(--text-little);
  @apply flex items-center gap-1.5 justify-center rounded  select-none px-1.5
}

.agent-content__header__cycle-selector__button:hover {
  background-color: var(--pallete-2);
}

.agent-content__header__current-cycle {
  width: 40px;
  height: var(--actuators-height);
  font-size: var(--text-little);
  border: 1px solid var(--pallete-trace);
  @apply text-center rounded select-none cursor-pointer
}

.agent-content__header__current-cycle:hover {
  background-color: var(--pallete-1);
}

.agent-content__header__current-cycle:focus {
  background-color: var(--pallete-2);
  border: 1px solid var(--pallete-2);
}

.y-arrow-icon.left {
  rotate: 270deg;
}

.y-arrow-icon.right {
  rotate: 90deg;
}

.y-arrow-icon.bottom {
  rotate: 180deg;
}
</style>
