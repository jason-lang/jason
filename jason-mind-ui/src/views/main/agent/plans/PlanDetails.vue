<script>

import {mountTermsText, mountTermText} from "@/domain/agent/utils/terms.js";
import Button from "@/components/general/button/Button.vue";
import {getIcon} from "@/utils/utils.js";

const expandIcon = getIcon('expand-icon.svg');
const retratIcon = getIcon('retrat-icon.svg');


export default {
  name: "PlanDetails",
  components: {Button},
  methods: {getIcon, mountTermsText, mountTermText},
  props: {
    plan: {},
    planExpanded: Boolean
  }
}
</script>

<template>
  <div class="plan-details">
    <!--    <div class="plan-details__content">-->
    <!--      <div class="plan-details__label absolute">Trigger</div>-->
    <!--      <div class="plan-details__label opacity-0">Annotations</div>-->
    <!--      <div class="plan-details__parameters code">-->
    <!--        {{ mountTermText(this.plan.trigger) }}-->
    <!--      </div>-->
    <!--    </div>-->

    <div v-if="!planExpanded" class="plan-details__contents">
      <div v-if="this.plan.trigger.terms && this.plan.trigger.terms.length > 0"
           class="plan-details__content">
        <div class="plan-details__label absolute">Parameters</div>
        <div class="plan-details__label opacity-0">Annotations</div>
        <div class="plan-details__parameters code">
          ({{ mountTermsText(this.plan.trigger.terms) }})
        </div>
      </div>

      <div v-if="this.plan.context && this.plan.context.length > 0" class="plan-details__content">
        <div class="plan-details__label absolute">Context</div>
        <div class="plan-details__label opacity-0">Annotations</div>
        <div class="flex flex-col gap-2 code">
        <span v-for="ci in this.plan.context" :key="ci">
          {{ mountTermText(ci) }}
        </span>
        </div>
      </div>

      <div v-if="this.plan.trigger.annotations && this.plan.trigger.annotations.length > 0"
           class="plan-details__content">
        <div class="plan-details__label absolute">Annotations</div>
        <div class="plan-details__label opacity-0">Annotations</div>
        <div class="flex flex-col gap-2 code">
          <span>[{{ mountTermsText(this.plan.trigger.annotations) }}]</span>
        </div>
      </div>
    </div>

    <div :class="{'plan-details__deeds code': true, '--is-expanded': planExpanded}">
      <div class="plan-details__deeds__numbers">
        <div v-for="(deed, index) in this.plan.deeds" :key="index"
             :class="{'plan-details__deeds__number': true, '--is-selected': deed.isSelected}">
          <span>{{ this.plan.deeds[0].lineNumber + index }}</span>
        </div>
      </div>
      <div class="plan-details__deeds__code">
        <div v-for="(deed, index) in this.plan.deeds" :key="index"
             :class="{'plan-details__deed': true, '--is-selected': deed.isSelected,
             '--already-passed': plan.getSelectedDeed() != null && plan.getSelectedDeed().lineNumber
              >= deed.lineNumber}">

          <span class="plan-details__deed__code">{{ deed.toString() }}</span>

          <div v-if="this.plan.isTriggered && deed.getVars().length > 0"
               class="plan-details__deed__variables">
            <span v-for="v in deed.getVars()" :key="v.name" class="plan-details__deed__variable">
              <span>{{ v.name }}:</span>
              <span class="plan-details__deed__variable__value">{{ v.valueText }}</span>
            </span>
          </div>
        </div>
      </div>
      <div class="details__deeds__buttons">
        <Button :icon="planExpanded ? getIcon('retrat-icon.svg') : getIcon('expand-icon.svg')"
                @click="$emit('update:planExpanded', !planExpanded)"/>
      </div>
    </div>

  </div>
</template>

<style scoped>

.plan-details {
  @apply flex flex-col select-none h-0 grow overflow-hidden gap-2
}

.plan-details__contents {
  border: 1px solid var(--pallete-trace);
  @apply flex flex-col rounded-md
}

.plan-details__content {
  height: 30px;
  border-bottom: 1px solid var(--pallete-trace);
  @apply flex items-center gap-4 w-full overflow-hidden flex-shrink-0 px-3
}

.plan-details__content:last-child {
  @apply border-none
}

.plan-details__label {
  font-family: "JetBrains Mono", monospace;
  font-size: var(--text-little);
  @apply flex-shrink-0 font-medium
}

.plan-details__parameters {
  @apply flex gap-2 py-4
}

.plan-details__deeds {
  --line-height: 30px;
  background-color: var(--pallete-bg);
  border: 1px solid var(--pallete-trace);
  @apply flex h-full overflow-y-auto rounded-md relative
}

.plan-details__deeds.--is-expanded {
}

.details__deeds__buttons {
  background-color: var(--pallete-bg);
  @apply sticky top-0 right-0 p-1
}

.details__deeds__buttons::before {
  display: block;
  content: "";
  background: linear-gradient(to left, transparent, var(--pallete-bg));
  width: 10px;
  left: -10px;
  top: 0;
  transform: scaleX(-100%);
  @apply h-full absolute
}

.plan-details__deeds__numbers {
  color: var(--pallete-text-aside);
  background-color: var(--pallete-bg);
  border-right: 1px solid var(--pallete-trace);
  @apply flex flex-col flex-shrink-0 justify-start py-2 sticky left-0 z-10
}

.plan-details__deeds__number {
  height: var(--line-height);
  margin-right: -1px;
  border-right: 1px solid var(--pallete-trace);
  @apply flex items-center pl-4 pr-9 flex-shrink-0
}

.plan-details__deeds__number.--is-selected {
  background-color: var(--pallete-2);
  color: var(--pallete-plans-1);
  border-right: 1px solid var(--pallete-trace);
}

.plan-details__deeds__code {
  @apply flex flex-col relative justify-start grow py-2
}

.plan-details__deed {
  height: var(--line-height);
  width: 100vw;
  @apply flex items-center gap-8 px-4 flex-shrink-0
}

.plan-details__deed__code {
  @apply whitespace-nowrap
}

.plan-details__deed__variables {
  color: var(--pallete-text-aside);
  @apply flex items-center gap-8 italic flex-shrink-0
}

.plan-details__deed__variable {
  @apply flex items-center gap-2 flex-shrink-0
}

.plan-details__deed__variable__value {
  color: var(--pallete-plans-1);
}

/** Linha destacada **/

.plan-details__deed.--is-selected {
  background-color: var(--pallete-2);
  color: var(--pallete-plans-1);
}


</style>
