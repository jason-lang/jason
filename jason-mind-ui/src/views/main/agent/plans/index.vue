<script>
import AgentItem from "@/components/item/AgentItem.vue";
import Tabs from "@/components/general/Tabs.vue";
import PlanDetails from "@/views/main/agent/plans/PlanDetails.vue";

export default {
  name: "Plans",
  components: {PlanDetails, Tabs, AgentItem},
  props: {
    agent: {},
    selectedGoal: {}
  },
  computed: {
    tabs() {
      if (this.currentExecutedPlan != null || this.selectedGoal != null) {
        return [
          {
            label: 'Triggered plan',
            selected: this.tab === 0
          },
          {
            label: 'All plans',
            qtd: this.agent.plans.length,
            selected: this.tab === 1
          }
        ]
      } else {
        return [
          {
            label: 'All plans',
            qtd: this.agent.plans.length
          }
        ]
      }
    }
  },
  watch: {
    selectedGoal(selectedGoal) {
      if (selectedGoal == null) {
        return
      }

      for (let runningPlan of this.agent.runningPlans) {
        if (runningPlan.isTriggeredByGoal(selectedGoal)) {
          this.currentPlan = runningPlan
          this.tab = 0
          break
        }
      }
    },
    "agent.currentCycle": {
      handler() {
        // Se tiver um plano sendo executado com a intenção atualmente selecionada.
        this.currentExecutedPlan = this.getCurrentExecutedPlan()
        if (this.currentExecutedPlan != null) {
          this.currentPlan = this.currentExecutedPlan
          this.tab = 0
        }
        // Se não tiverem planos em execução.
        else {
          if (this.selectedPlan == null) {
            this.tab = 1
            this.currentPlan = null
          }
        }
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    selectPlan(plan) {
      if (this.currentPlan != null && plan.id === this.currentPlan.id) {
        this.selectedPlan = null
        this.currentPlan = null
      } else {
        this.selectedPlan = plan
        this.currentPlan = plan
      }
    },
    selectTab(tab) {
      this.tab = tab

      if (tab === 1) {
        this.currentPlan = this.selectedPlan
      } else {
        // Se tiver um plano sendo executado com a intenção atualmente selecionada.
        if (this.currentExecutedPlan != null) {
          this.currentPlan = this.currentExecutedPlan
        }
        // Se tiverem planos sendo executados, mas não existe uma intenção atualmente selecionada.
        else if (this.agent.runningPlans.length > 0) {
          this.currentPlan = this.agent.runningPlans[0]
          this.tab = 0
        }
      }
    },
    getCurrentExecutedPlan() {
      if (this.agent.selectedIntention == null) {
        return null
      }
      const currentGoal = this.agent.selectedIntention.stackGoals[0]
      for (let runningPlan of this.agent.runningPlans) {
        if (runningPlan.isTriggeredByGoal(currentGoal)) {
          return runningPlan
        }
      }
      return null
    }
  },
  data() {
    return {
      tab: 1,
      currentPlan: null,
      selectedPlan: null,
      contentClosed: false,
      currentExecutedPlan: null,
      planExpanded: false
    }
  }
}
</script>

<template>
  <div class="agent-content__section grow">
    <Tabs v-model:closed="this.contentClosed" :tabs="this.tabs" border-top @selected="selectTab"/>
    <div v-if="!this.contentClosed" class="agent-content__section__main --is-plans">
      <div v-if="this.tab === 1 && !planExpanded"
           class="agent-content__section__main__plans">
        <AgentItem
          v-for="plan in this.agent.plans"
          :key="plan.id"
          :selectable="true"
          :selected="this.currentPlan != null && plan.id === this.currentPlan.id"
          :structure="plan"
          background-color="var(--pallete-plans-t-1)"
          color="var(--pallete-plans-1)"
          @click="selectPlan(plan)"
        />
      </div>
      <div v-else-if="!planExpanded"
           :class="{'agent-content__section__main__plans': true, 'p-0': planExpanded}">
        <AgentItem
          :structure="this.currentPlan"
          background-color="var(--pallete-plans-t-1)"
          color="var(--pallete-plans-1)"
          selected
        />
      </div>
      <PlanDetails v-if="this.currentPlan != null" v-model:plan-expanded="planExpanded"
                   :class="{'m-4 mt-2': !planExpanded, 'm-1': planExpanded}"
                   :plan="this.currentPlan"/>
      <div v-else class="p-4 pt-2 w-full h-full">
        <span class="select-plan code">Select a plan to view its structure</span>
      </div>
    </div>
  </div>
</template>

<style scoped>

.agent-content__section__main.--is-plans {
  @apply flex flex-col h-0 grow
}

.agent-content__section__main__plans {
  @apply flex items-center flex-wrap gap-1 m-4 mb-0
}

.select-plan {
  font-size: var(--text-little);
  color: var(--pallete-text-aside);
  border: 1px solid var(--pallete-trace);
  @apply grid place-items-center h-full w-full rounded-md
}

</style>

