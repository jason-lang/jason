<script>
import AgentItem from "@/components/item/AgentItem.vue";
import Tabs from "@/components/general/Tabs.vue";
import BeliefSection from "@/views/main/agent/beliefs/BeliefSection.vue";
import {getIcon} from "@/utils/utils.js";
import {SOURCE_SELF} from "@/domain/agent/utils/constants.js";
import Belief from "@/views/main/agent/beliefs/Belief.vue";

export default {
  name: "Beliefs",
  methods: {
    getIcon,
    goToByBelief(belief) {
      let source = belief.getSource()
      if (source === "self") {
        source = this.agent.name
      }
      this.$emit("goTo", {
        agent: source,
        cycle: belief.cycle
      })
    },
    goToAgent(agent, cycle) {
      this.$emit("goTo", {
        agent: agent,
        cycle: cycle
      })
    }
  },
  components: {Belief, BeliefSection, Tabs, AgentItem},
  props: {
    agent: {}
  },
  data() {
    return {
      contentClosed: false
    }
  },
  computed: {
    mentalNotes() {
      let mentalNotes = this.agent.getSourceMappedBeliefs().get(SOURCE_SELF)
      if (mentalNotes == null) {
        return []
      }
      return mentalNotes
    },
    messages() {
      let messages = []
      for (let key of this.agent.getSourceMappedBeliefs().keys()) {
        if (key === SOURCE_SELF) {
          continue
        }
        let beliefs = this.agent.getSourceMappedBeliefs().get(key);
        messages.push({
          agent: key,
          beliefs: beliefs
        })
      }
      return messages
    }
  }
}
</script>

<template>
  <div :class="{'agent-content__section': true, 'grow': !this.contentClosed}">
    <Tabs v-model:closed="contentClosed"
          :tabs="[{label: 'Beliefs', qtd: this.agent.beliefs.length}]"/>
    <div v-if="!contentClosed" class="agent-content__section__main --is-beliefs">
      <BeliefSection v-if="this.mentalNotes.length > 0"
                     :image="getIcon('mental-notes-icon.svg')" title="Mental notes">
        <TransitionGroup class="flex flex-wrap gap-1" name="fade" tag="div">
          <div v-for="belief in this.mentalNotes" :key="belief.toString()">
            <Belief :agent="agent" :belief="belief" @click="goToByBelief(belief)"/>
          </div>
        </TransitionGroup>
      </BeliefSection>

      <BeliefSection v-if="this.messages.length > 0"
                     :image="getIcon('message-icon.svg')" class="mt-1" title="Messages">
        <BeliefSection v-for="(m, index) in this.messages" :key="index" :title="m.agent">
          <TransitionGroup class="flex flex-wrap gap-1" name="fade" tag="div">
            <div v-for="belief in m.beliefs" :key="belief.toString()">
              <Belief :agent="agent" :belief="belief" :show-annotations="false"
                      @click="goToAgent(m.agent, belief.cycle)"/>
            </div>
          </TransitionGroup>
        </BeliefSection>
      </BeliefSection>

      <div v-if="this.agent.beliefs.length === 0" class="grid place-items-center h-full w-full">
        <span class="text-aside text-little">No beliefs</span>
      </div>

    </div>
  </div>
</template>

<style scoped>

.agent-content__section__main.--is-beliefs {
  @apply h-0 grow flex flex-col overflow-y-auto p-0 pt-2 pb-4
}


</style>
