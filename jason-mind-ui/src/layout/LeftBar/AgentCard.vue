<script>
export default {
  name: "AgentCard",
  props: {
    agent: {}
  },
  computed: {
    mainBeliefs() {
      return this.agent.beliefs
    }
  }
}
</script>

<template>
  <div class="agent-card">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-2">
        <span class="agent-card__name">
          {{ agent.name }}
        </span>
      </div>
      <span class="agent-card__cycle">{{ agent.newerCycle }} cycles</span>
    </div>
    <div v-if="agent.intentions.length > 0 ||
    mainBeliefs.length > 0" class="flex flex-col gap-2 relative">
      <span v-if="agent.intentions.length > 0" class="agent-card__intention">
        {{ agent.intentions[0].toString() }}
      </span>
      <div v-if="mainBeliefs.length > 0" class="agent-card__beliefs">
        <span v-for="belief in mainBeliefs" class="agent-card__belief">
          {{ belief.toString() }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>

.agent-card {
  @apply flex flex-col p-2 gap-2 rounded select-none
}

.agent-card.selected {
  background-color: var(--pallete-2);
}

.agent-card.selected .agent-card__beliefs:after {
  background: linear-gradient(to right, transparent, var(--pallete-2));
}

.agent-card:hover {
  background-color: var(--pallete-2);
}

.agent-card__name {
  @apply font-medium
}

.agent-card__cycle {
  color: var(--pallete-text-aside);
  font-size: var(--text-little);
}

.agent-card__intention {
  color: var(--pallete-intention-1);
  @apply whitespace-nowrap
}

.agent-card__beliefs {
  @apply flex items-center gap-2 overflow-hidden
}

.agent-card__beliefs:after {
  content: "";
  display: block;
  width: 10px;
  background: linear-gradient(to right, transparent, var(--pallete-1));
  @apply h-full absolute right-0
}

.agent-card:hover .agent-card__beliefs:after {
  background: linear-gradient(to right, transparent, var(--pallete-2));
}

.agent-card__belief {
  color: var(--pallete-beliefs-1);
  @apply whitespace-nowrap
}

</style>
