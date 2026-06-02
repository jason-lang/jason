<script>
import Button from "@/components/general/button/Button.vue";
import {getIcon} from "@/utils/utils.js";

export default {
  name: "SearchButton",
  props: {
    search: String,
    placeholder: {
      default: "Type to search...",
      type: String
    }
  },
  methods: {
    getIcon,
    open() {
      this.searching = true
      this.$nextTick(() => {
        this.$refs.input.focus()
      })
    },
    close() {
      this.$emit('update:search', '')
      this.searching = false
    }
  },
  components: {Button},
  data() {
    return {
      searching: false
    }
  }
}
</script>

<template>
  <div class="search-button">
    <Button v-if="!searching" :icon="getIcon('search-icon.svg')" @click.stop="open"
            @click="$emit('focus')"/>
    <img src="@/assets/img/search-icon.svg" alt="search-icon" v-if="searching" />
    <input v-if="searching" :placeholder="this.placeholder" type="text"
           @input="$emit('update:search', $event.target.value)" :value="search" ref="input"/>
    <Button @click="this.close()" :icon="getIcon('close-icon.svg')" v-if="searching"/>
  </div>
</template>

<style scoped>

.search-button {
  @apply flex items-center h-full gap-2
}

.search-button > input {
  background-color: transparent;
  border: none;
  border-left: 1px solid var(--pallete-trace);
  width: 300px;
  @apply h-full px-4 ml-2
}

</style>
