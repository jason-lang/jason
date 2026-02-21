<script>
import {getFormattedTimeFromDateString} from "@/utils/date.js";
import {mountTermText} from "@/domain/agent/utils/terms.js";
import AgentItem from "@/components/item/AgentItem.vue";

// illForces: "tell","untell","achieve","unachieve","askOne","askAll","tellHow", "untellHow","askHow"

export default {
  name: "Message",
  components: {AgentItem},
  props: {
    message: {},
    agent: {}
  },
  computed: {
    isSender() {
      return this.message.sender === this.agent.name
    },
    time() {
      return getFormattedTimeFromDateString(this.message.time)
    },
    term() {
      let content = mountTermText(this.message.content)
      if (this.isContentPlan) {
        return content.replaceAll("\"", "")
      }
      return this.message.content
    },
    isContentPlan() {
      return this.message.illocutionaryForce.toLowerCase().includes("ask") ||
        this.message.illocutionaryForce.toLowerCase().includes("how")
    },
    illForce() {
      return this.message.illocutionaryForce
    },
    contentType() {
      if (this.message.illocutionaryForce === "tell") {
        return {
          bgColor: "var(--pallete-beliefs-t-1)",
          color: "var(--pallete-beliefs-1)",
        }
      } else if (this.message.illocutionaryForce.includes("achieve")) {
        return {
          bgColor: "var(--pallete-intention-t-1)",
          color: "var(--pallete-intention-1)",
        }
      } else if (this.isContentPlan) {
        return {
          bgColor: "var(--pallete-plans-t-1)",
          color: "var(--pallete-plans-1)",
        }
      } else {
        return {
          bgColor: "var(--pallete-2)",
          color: "var(--pallete-text)",
        }
      }
    },
  },
  methods: {
    goTo(agent, cycle) {
      this.$emit("goTo", {
        agent: agent,
        cycle: cycle
      })
    }
  }
}
</script>

<template>
  <div :class="{message: true, '--is-sender': this.isSender}">
    <div class="message__content">
      <div class="flex items-center gap-1">
        <span class="message__sender" @click="goTo(this.message.sender, null)">
          {{message.sender }}
        </span>
        <span class="message__ill-force">{{ illForce }}</span>
        <span class="message__sender" @click="goTo(this.message.sender, null)">
          {{ message.receiver }}
        </span>
      </div>
      <div class="flex items-end gap-4">
        <div class="message__content__term">
          <AgentItem :background-color="contentType.bgColor" :color="contentType.color"
                     :structure="term"/>
        </div>
        <div
            v-if="!isSender"
            class="message__time"
            @click="goTo(this.message.receiver, this.message.receivedCycle)"
        >
          <span>received at cycle {{ message.receivedCycle }}</span>
          <img src="@/assets/img/arrow/grey-arrow-icon.svg" alt="arrow-icon"/>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

.message {
  min-width: 40%;
  @apply flex items-start gap-1 w-fit overflow-hidden flex-shrink-0
}

.message.--is-sender {
  @apply self-end
}

.message.--is-sender .message__content {
  background-color: rgb(244, 244, 244);
  border: 1px solid var(--pallete-1);
  @apply rounded-tl-xl rounded-bl-xl rounded-tr-xl rounded-br-sm
}

.message__content {
  border: 1px solid var(--pallete-trace);
  @apply flex flex-col gap-2 p-2 rounded-tr-xl rounded-br-xl rounded-tl-xl rounded-bl-sm
  relative w-full
}

.message__content__term {
  box-shadow: 0 0 0 2px var(--pallete-1);
  @apply rounded
}

svg {
  width: 100px;
}

path {
  fill: var(--pallete-1);
  stroke: #10b981;
  stroke-width: 10;
}

.message__time {
  font-size: var(--text-little);
  color: var(--pallete-text-aside);
  margin-right: -22px;
  transition: margin-right 0.2s;
  @apply flex items-center gap-2 ml-auto cursor-pointer
}

.message__time:hover {
  margin-right: 0;
  @apply underline
}

.message__time > img {
  transform: rotate(90deg);
}

.message__sender {
  @apply cursor-pointer
}

.message__sender:hover {
  @apply underline
}

.message__ill-force {
  color: var(--pallete-text-aside);
}

</style>
