<script>
import Button from "@/components/general/button/Button.vue";
import TabContent from "@/views/main/agent/index.vue";
import LeftBar from "@/layout/LeftBar/index.vue";
import Header from "@/layout/Header.vue";
import Console from "@/views/main/console/index.vue";

export default {
  name: "Main",
  components: {Console, Header, LeftBar, AgentContent: TabContent, Button},
  methods: {
    selectAgent(agent) {
      this.selectedAgentName = agent.name
      this.cycle = null
    },
    close() {
      this.selectedAgentName = null
      this.$refs.leftbar.unselect()
    },
    goTo(o) {
      this.selectedAgentName = o.agent
      this.cycle = o.cycle
    }
  },
  data() {
    return {
      selectedAgentName: null,
      cycle: null
    }
  }
}
</script>

<template>
  <main class="main">
    <Header/>
    <div class="flex gap-1 w-full grow h-0">
      <LeftBar ref="leftbar" style="width: 14%" @selectAgent="selectAgent"/>
      <div class="flex flex-col items-center gap-1 w-0 grow">
        <AgentContent v-if="selectedAgentName"
                      v-model:agent-name="selectedAgentName"
                      v-model:cycle="cycle"
                      @close="close"/>
        <Console
          :class="selectedAgentName == null ? 'h-full' : ''"
          :height="this.selectedAgentName != null ? '30vh' : '100%'"
          @goTo="goTo"
        />
      </div>
    </div>
  </main>
</template>

<style scoped>

.main {
  @apply flex flex-col w-screen h-screen justify-end p-1 gap-1
}

.main__select-agent-message {
  background-color: var(--pallete-1);
  color: var(--pallete-text-aside);
  @apply grid place-items-center rounded shadow w-full grow
}

</style>
