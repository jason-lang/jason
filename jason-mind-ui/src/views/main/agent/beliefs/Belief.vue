<script>
import AgentItem from "@/components/item/AgentItem.vue";
import Button from "@/components/general/button/Button.vue";

export default {
  name: "Belief",
  components: {Button, AgentItem},
  props: {
    belief: {},
    agent: {},
    showAnnotations: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    isNew() {
      return this.belief.cycle === this.agent.currentCycle
    }
  }
}
</script>

<template>
  <div class="belief">
    <div :class="{'belief__go-to': true, '--is-new': isNew}">
      <img alt="arrow-icon" src="@/assets/img/arrow/belief-arrow-icon.svg"/>
      <span class="whitespace-nowrap">cycle {{ belief.cycle }}</span>
    </div>
    <div v-if="isNew" class="belief__is-new">
      <span>new</span>
    </div>
    <AgentItem
      :structure="belief"
      background-color="var(--pallete-beliefs-t-1)"
      color="var(--pallete-beliefs-1)"
      :style="isNew ? 'border: 1px solid var(--pallete-beliefs-1);' : ''"
      :show-annotations="showAnnotations"
    />
  </div>
</template>

<style scoped>

.belief {
  @apply relative
}

.belief:hover > .belief__go-to {
  @apply flex
}

.belief__go-to {
  display: none;
  background-color: var(--pallete-beliefs-t-1);
  color: var(--pallete-beliefs-1);
  border: 1px solid rgba(0, 0, 0, 0.05);
  @apply items-center justify-center gap-1.5 h-full w-full z-10 absolute cursor-pointer rounded
}

.belief__go-to.--is-new {
  border: 1px solid var(--pallete-beliefs-1);
}

.belief__is-new {
  background-color: var(--pallete-beliefs-1);
  transform: translateX(30%) translateY(-58%);
  color: var(--pallete-1);
  font-size: 0.8em;
  padding: 0 3px;
  @apply flex items-center justify-center rounded right-0 top-0
  absolute z-20 font-semibold
}

.belief__go-to > img {
  transform: rotate(90deg);
}

</style>
