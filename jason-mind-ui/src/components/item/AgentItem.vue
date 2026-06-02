<script>
import {mountTermsText, mountTermText} from "@/domain/agent/utils/terms.js";

export default {
  name: "AgentItem",
  methods: {mountTermText, mountTermsText},
  props: {
    structure: {},
    color: String,
    backgroundColor: String,
    selectable: Boolean,
    selected: Boolean,
    showAnnotations: {
      default: true,
      type: Boolean
    }
  },
  computed: {
    hasCustomToString() {
      const s = this.structure;
      return s && typeof s.toString === "function" && s.toString !== Object.prototype.toString;
    },
    annotations() {
      if (!this.structure.annotations) {
        return []
      }
      let annotationsList = this.structure.annotations;
      return annotationsList.filter(a => a.terms[0].functor !== "self")
    }
  }
}
</script>

<template>
  <div :class="{item: true, selected: this.selected, selectable: this.selectable}">
    <div class="flex items-center gap-1">
      <span v-if="hasCustomToString">{{ structure.toString() }}</span>
      <span v-else>{{ mountTermText(structure) }}</span>
      <span v-if="annotations.length > 0 && showAnnotations" class="item__annotations">
        [{{ mountTermsText(annotations) }}]
      </span>
    </div>
    <div class="item__background"></div>
  </div>
</template>

<style scoped>

.item {
  background-color: v-bind(backgroundColor);
  padding: 2px 4px;
  color: v-bind(color);
  transition: transform 0.1s;
  border: 1px solid rgba(0, 0, 0, 0.05);
  @apply rounded w-fit h-fit select-none relative
}

.item__annotations {
  font-size: 0.9em;
  opacity: 80%;
}

.item.selected {
  border: 1px solid v-bind(color);
}

.item__background {
  @apply w-full h-full absolute top-0 left-0 rounded
}

.item.selectable:hover {
  transform: scale(0.97);
}
</style>
