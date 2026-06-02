<script>
import AgentItem from "@/components/item/AgentItem.vue";

export default {
  name: "BeliefSection",
  components: {AgentItem},
  props: {
    title: String,
    image: String
  },
  methods: {
    tab() {
      this.show = !this.show
    }
  },
  data() {
    return {
      show: true
    }
  }
}
</script>

<template>
  <div class="belief-section">
    <div class="belief-section__header" @click="tab">
      <div class="close-icon-container">
        <img :class="{'close-icon': true, 'closed': !this.show}" alt="close-icon"
             src="@/assets/img/drop-down.svg"/>
      </div>
      <div class="belief-section__header__img" v-if="this.image">
        <img :src="this.image" alt="message-icon"/>
      </div>
      <span>{{ title }}</span>
    </div>
    <div class="ml-8 pr-4">
      <slot v-if="show"></slot>
    </div>
  </div>
</template>

<style scoped>

.belief-section {
  background-color: var(--pallete-1);
  @apply flex flex-col
}

.belief-section__header {
  @apply flex items-center gap-2  select-none py-2 px-4
}

.belief-section__header__img {
  width: 14px;
  @apply flex justify-center
}

.close-icon-container {
  @apply flex items-center justify-center
}

.close-icon {
  width: 8px;
}

.close-icon.closed {
  rotate: -90deg;
}

</style>
