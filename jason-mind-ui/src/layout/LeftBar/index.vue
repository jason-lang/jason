<script>
import agentService from "@/service/agent-service.js";
import {createAgent} from "@/domain/agent/createAgent.js";
import AgentCard from "@/layout/LeftBar/AgentCard.vue";
import Tabs from "@/components/general/Tabs.vue";
import Loading from "@/components/general/Loading.vue";

export default {
  name: "LeftBar",
  components: {Loading, Tabs, AgentCard},
  data() {
    return {
      agents: [],
      selectedAgent: null
    }
  },
  mounted() {
    setInterval(() => {
      agentService.find().then((response) => {
        response.data.forEach((agent) => {
          let index = this.agents.findIndex(a => a.name === agent.name)
          if (index === -1) {
            this.agents.push(createAgent(agent))
          } else {
            this.agents[index] = createAgent(agent)
          }
        })
      }).catch(() => {
        this.agents = []
      })
    }, 1000)
  },
  methods: {
    unselect() {
      this.selectedAgent = null
    },
    selectAgent(agent) {
      this.selectedAgent = agent
      this.$emit("selectAgent", agent)
    }
  }
}
</script>

<template>
  <aside class="left-bar">
    <Tabs :closeable="false"
          :tabs="[{label: 'Agents', icon: 'agent-icon.svg', qtd: this.agents.length}]"/>
    <div v-if="this.agents.length > 0" class="flex flex-col mx-2 gap-2">
      <AgentCard v-for="agent in this.agents" :key="agent.name" :agent="agent"
                 :class="{'agent-card': true, selected: this.selectedAgent != null &&
                 this.selectedAgent.name === agent.name}"
                 @click="selectAgent(agent)"/>
    </div>
    <div v-else class="grid place-items-center h-full w-full">
      <Loading/>
    </div>
  </aside>
</template>

<style scoped>

.left-bar {
  background-color: var(--pallete-1);
  @apply flex flex-col h-full rounded-md gap-2 shadow overflow-hidden flex-shrink-0
}

.left-bar__header {
  height: var(--header-height);
  border-bottom: 1px solid var(--pallete-trace);
  @apply flex items-center px-4 gap-2 font-medium
}

.left-bar__title {
  @apply text-lg font-medium
}

</style>
