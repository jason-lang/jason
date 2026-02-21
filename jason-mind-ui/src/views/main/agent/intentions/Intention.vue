<script>
import Detail from "@/components/general/Detail.vue";
import AgentItem from "@/components/item/AgentItem.vue";

export default {
  name: "Intention",
  methods: {
    selectGoal(goalTrigger) {
      this.$emit("selectedGoal", goalTrigger)
    }
  },
  data() {
    return {
      open: false
    }
  },
  components: {AgentItem, Detail},
  props: {
    intention: {},
    selectedGoal: {}
  },
  watch: {
    "intention.stackGoals": {
      deep: true,
      handler(stackGoals) {
        if (stackGoals.length === 1) {
          this.open = false
        }
      }
    }
  },
  computed: {
    state() {
      if (this.intention.isSelected) {
        return "#" + this.intention.id + " is selected"
      } else if (this.intention.isInQueue) {
        return "#" + this.intention.id + " is in queue"
      } else if (this.intention.isPending) {
        return "#" + this.intention.id + " is pending"
      } else {
        return "#" + this.intention.id + " is " + this.intention.state
      }
    },
    stackGoals() {
      return this.open ? this.intention.stackGoals : [this.intention.stackGoals[0]]
    }
  },
}
</script>

<template>
  <div :class="'intention ' + this.intention.state">
    <div class="intention__header">
      <span class="intention__text">{{ state }}</span>
    </div>
    <div class="intention__goals-container">
      <div class="intention__goals">
        <div v-for="(goal, index) in stackGoals" :key="index"
             class="flex items-center gap-2">

          <AgentItem
            :background-color="this.intention.isSelected && index === 0 ?
                   'var(--pallete-intention-1)'  :
                   'var(--pallete-intention-t-2)'"
            :color="this.intention.isSelected && index === 0 ? 'white' :
                   'var(--pallete-intention-1)'"
            :selected="selectedGoal === goal"
            :structure="goal" selectable
            @click="selectGoal(goal)"/>

          <span
            v-if="index === this.intention.stackGoals.length - 1 && open"
            class="intention__subtext">
            Main goal
          </span>

          <span
            v-if="index === 0 && open"
            class="intention__subtext">
            Current goal
          </span>
        </div>
      </div>
      <div v-if="this.intention.stackGoals.length > 1" class="flex items-center gap-1 mt-1"
           @click="open = !open">
        <span v-if="!open" class="intention__subtext">
          + {{ this.intention.stackGoals.length - 1 }} goal
        </span>
        <div :class="{'intention__drop-down': true, open: open, 'mt-0.5': open}">
          <img alt="drop-down-icon" src="@/assets/img/drop-down.svg"/>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

.intention {
  background-color: var(--pallete-intention-t-1);
  border: 1px solid rgba(0, 0, 0, 0.05);
  @apply flex flex-col rounded select-none h-fit
}

.intention.selected {
  border: 1px solid var(--pallete-intention-1);
}

.intention__goals-container {
  @apply flex gap-1 p-1 items-start
}

.intention__goals {
  max-height: 100px;
  @apply flex flex-col gap-1 overflow-y-auto pr-2
}

.intention__goals::-webkit-scrollbar {
  width: 2px;
}

/* Track */
.intention__goals::-webkit-scrollbar-track {
  @apply overflow-hidden
}

/* Handle */
.intention__goals::-webkit-scrollbar-thumb {
  background-color: var(--pallete-intention-t-2);
  @apply rounded
}


.intention__goal {
  @apply cursor-pointer
}

.intention__header {
  border-bottom: 1px solid var(--pallete-intention-t-2);
  @apply flex items-center justify-between gap-6 p-1
}

.intention__drop-down {
  height: 10px;
  aspect-ratio: 1/1;
  opacity: 80%;
  @apply grid place-items-center
}

.intention__drop-down.open {
  transform: rotate(180deg);
}

.intention__text {
  color: var(--pallete-intention-1);
  font-size: 0.9em;
}

.intention__subtext {
  color: var(--pallete-intention-2);
  font-size: 0.9em;
}

</style>
