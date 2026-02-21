<script>
import {getIcon} from "@/utils/utils.js";
import Detail from "@/components/general/Detail.vue";
import Button from "@/components/general/button/Button.vue";

export default {
  name: "Tabs",
  components: {Button, Detail},
  props: {
    tabs: Array,
    closed: Boolean,
    closeable: {
      type: Boolean,
      default: true
    },
    borderTop: Boolean
  },
  methods: {
    getIcon,
    select(index) {
      if (this.tabs.length === 1) {
        return
      }

      this.$refs.tabs.forEach((tab) => {
        tab.classList.remove("selected")
      })
      this.$refs.tabs[index].classList.add("selected")

      this.$emit("selected", index)
    },
    dropDown() {
      this.$emit("update:closed", !this.closed)
    }
  }
}
</script>

<template>
  <nav :class="{'tabs': true, closed: this.closed, 'border-top': this.borderTop}">

    <div v-for="(tab, index) in this.tabs" :key="index"
         ref="tabs" :class="{tabs__tab: true, selected: tab.selected,
         'selectable': this.tabs.length > 1}"
         @click="select(index)">
      <div class="flex items-center gap-2">
        <img v-if="tab.icon" :src="getIcon(tab.icon)" alt="tab-icon"/>
        <span class="font-medium">{{ tab.label }}</span>
        <Detail v-if="tab.qtd" :content="tab.qtd"/>
      </div>
    </div>

    <div class="flex items-center gap-2 h-full ml-auto">
      <Button v-if="closeable" :class="{'dropdown': true, 'closed': this.closed}"
              :icon="getIcon('drop-down.svg')"
              @click="dropDown"/>
    </div>
  </nav>
</template>

<style scoped>

.tabs {
  color: var(--pallete-text);
  height: var(--header-height);
  border-bottom: 1px solid var(--pallete-trace);
  @apply flex items-center px-4 gap-4 select-none flex-shrink-0 pr-2
}

.tabs.border-top {
  border-top: 1px solid var(--pallete-trace);
}

.tabs.closed {
  height: calc(var(--header-height) - 1px);
  border-bottom: 0;
}

.tabs__tab {
  @apply flex items-center h-full
}

.tabs__tab.selectable > div {
  border-radius: 1px;
}

.tabs__tab.selectable:hover > div {
  color: var(--pallete-text);
}

.tabs:not(:has(.closed)) .tabs__tab.selectable.selected {
  box-shadow: inset 0 -1px 0 0 var(--pallete-text);
}

.tab__drop-dow {
  height: var(--actuators-height);
  transform: rotate(180deg);
  @apply grid place-items-center px-2 rounded ml-auto
}

.dropdown {
  transform: rotate(0deg);
}

.dropdown.closed {
  transform: rotate(180deg);
}

</style>
