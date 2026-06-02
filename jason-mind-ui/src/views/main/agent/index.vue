<script>
import AgentItem from "@/components/item/AgentItem.vue";
import Intentions from "@/views/main/agent/intentions/index.vue";
import Plans from "@/views/main/agent/plans/index.vue";
import Beliefs from "@/views/main/agent/beliefs/index.vue";
import Chat from "@/views/main/agent/chat/index.vue";
import Header from "@/views/main/agent/Header.vue";
import agentService from "@/service/agent-service.js";
import Loading from "@/components/general/Loading.vue";
import {createAgent} from "@/domain/agent/createAgent.js";
import Console from "@/views/main/console/index.vue";

export default {
  name: "AgentContent",
  components: {
    Console, Loading, Header,
    Chat, Beliefs, Plans, Intentions, AgentItem
  },
  props: {
    agentName: String,
    cycle: Number
  },
  data() {
    return {
      agent: null,
      pollingTimeout: null,
      selectedGoal: null
    }
  },
  watch: {
    agentName(agentName) {
      agentService.find(agentName, this.cycle).then((response) => {
        this.agent = createAgent(response.data);
      });
    },
    cycle(cycle) {
      agentService.find(this.agentName, cycle).then((response) => {
        this.agent = createAgent(response.data);
      });
    }
  },
  mounted() {
    this.startPolling();
  },
  beforeUnmount() {
    this.stopPolling();
  },
  methods: {
    async pollAgent() {
      try {
        const response = await agentService.find(this.agentName, this.cycle);
        const newAgent = createAgent(response.data);

        if (this.cycle !== null && this.agent !== null) {
          this.agent.newerCycle = newAgent.newerCycle;
          this.agent.oldestCycle = newAgent.oldestCycle;
        } else {
          this.agent = newAgent;
        }
      } finally {
        this.pollingTimeout = setTimeout(this.pollAgent, 1000);
      }
    },
    startPolling() {
      this.stopPolling();
      this.pollAgent();
    },
    stopPolling() {
      if (this.pollingTimeout) {
        clearTimeout(this.pollingTimeout);
        this.pollingTimeout = null;
      }
    },
    doCycle(cycle) {
      this.selectedGoal = null
      this.$emit("update:cycle", cycle);
    },
    goTo(o) {
      this.$emit("update:agentName", o.agent);
      this.$emit("update:cycle", o.cycle);
    },
    resetCycle() {
      this.doCycle(null)
      agentService.find(this.agentName).then((response) => {
        this.agent = createAgent(response.data);
      });
    }
  }
};
</script>

<template>
  <section v-if="agent != null" class="agent-content">
    <Header :agent="agent" @close="$emit('close')" @cycle="doCycle" @reset="resetCycle"/>
    <div class="flex w-full h-full">
      <div class="flex flex-col w-2/3 h-full">
        <Intentions :agent="agent" @selectedGoal="selectedGoal = $event" :selected-goal="selectedGoal"/>
        <Plans :agent="agent" :selectedGoal="selectedGoal"/>
      </div>
      <hr class="vertical"/>
      <div class="flex flex-col grow h-full w-1/3">
        <Beliefs :agent="agent" @goTo="goTo"/>
        <Chat :agent="agent" @goTo="goTo"/>
      </div>
    </div>
  </section>
  <div v-else class="agent-content items-center justify-center">
    <Loading/>
  </div>
</template>

<style>
@import "style.css";

.agent-content {
  background-color: var(--pallete-1);
  @apply flex flex-col w-full rounded-md shadow overflow-hidden grow h-0;
}
</style>
