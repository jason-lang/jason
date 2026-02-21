<script>
import AgentItem from "@/components/item/AgentItem.vue";
import Tabs from "@/components/general/Tabs.vue";
import Detail from "@/components/general/Detail.vue";
import IntentionStack from "@/views/main/agent/intentions/Intention.vue";

export default {
  name: "Intentions",
  components: { Intention: IntentionStack, Detail, Tabs, AgentItem },
  props: {
    agent: {
      type: Object,
      required: true
    },
    selectedGoal: {}
  },
  data() {
    return {
      contentClosed: false
    }
  },
  computed: {
    orderedIntentions() {
      const list = (this.agent?.intentions ?? []).slice();

      const rank = (i) => {
        if (i.isSelected) return 0;
        if (i.isInQueue) return 1;
        if (i.isPending) return 2;
        return 3;
      };

      return list.sort((a, b) => {
        const r = rank(a) - rank(b);
        if (r !== 0) {
          return r;
        }
        if (a.id != null && b.id != null){
          return String(a.id).localeCompare(String(b.id));
        }
        return 0;
      });
    }
  }
}
</script>

<template>
  <div class="agent-content__section">
    <Tabs
      v-model:closed="contentClosed"
      :tabs="[{ label: 'Intentions', qtd: agent.intentions.length }]"
    />

    <div v-if="!contentClosed && this.orderedIntentions.length > 0"
         class="agent-content__section__main --is-intentions">
      <TransitionGroup name="flip-x" tag="div" class="flex relative gap-1 ui py-2 px-4">
        <Intention
          v-for="intention in orderedIntentions"
          :key="intention.id"
          :intention="intention"
          :selected-goal="selectedGoal"

          @selectedGoal="$emit('selectedGoal', $event)"
        />
      </TransitionGroup>
    </div>

    <div v-else-if="!contentClosed" class="no-intentions">
      <span class="text-little text-aside">No intentions</span>
    </div>
  </div>
</template>

<style scoped>
.agent-content__section__main.--is-intentions {
  @apply flex gap-4 h-full overflow-hidden;
}

.no-intentions {
  height: calc(var(--header-height) - 2px);
  @apply flex items-center mx-4
}

/* Transições de intenções */
.flip-x-move {
  transition: transform .50s ease;
}

.flip-x-enter-from {
  transform: translateX(24px);
  opacity: 0;
}

.flip-x-enter-to {
  transform: translateX(0);
  opacity: 1;
}

.flip-x-leave-from {
  transform: translateX(0);
  opacity: 1;
}

.flip-x-leave-to {
  transform: translateX(-24px);
  opacity: 0;
}

.flip-x-enter-active,
.flip-x-leave-active {
  transition: transform .50s ease, opacity .50s ease;
}

.flip-x-leave-active {
  position: absolute;
}
</style>
