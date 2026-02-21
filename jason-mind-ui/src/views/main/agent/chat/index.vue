<script>
import AgentItem from "@/components/item/AgentItem.vue";
import Tabs from "@/components/general/Tabs.vue";
import agentService from "@/service/agent-service.js";
import Message from "@/views/main/agent/chat/Message.vue";
import {getTimestampFromDateString} from "@/utils/date.js";

export default {
  name: "Chat",
  components: {Message, Tabs, AgentItem},
  props: {
    agent: {}
  },
  watch: {
    agent: {
      immediate: true,
      deep: true,
      async handler(agent) {
        if (agent == null) {
          return
        }
        let messages = await agentService.findMessages(agent.name,
          getTimestampFromDateString(agent.time))

        const msgQtd = this.allMessages.length

        this.allMessages = messages

        await this.$nextTick(() => {
          if (msgQtd !== messages.length && messages.length > 0) {
            this.$refs.messagesContainer.scrollTo(0, this.$refs.messagesContainer.scrollHeight)
          }
        })
      }
    }
  },
  methods: {
    goTo(message) {
      this.$emit("goTo", {
        agent: message.agent,
        cycle: message.cycle
      })
    }
  },
  mounted() {
    setTimeout(() => {
      this.$refs.messagesContainer.scrollTo(0, this.$refs.messagesContainer.scrollHeight)
    }, 300)
  },
  data() {
    return {
      allMessages: [],
      loading: false,
      contentClosed: false
    }
  }
}
</script>

<template>
  <div :class="{'agent-content__section': true, 'grow': !contentClosed}">
    <Tabs v-model:closed="contentClosed" :tabs="[{label: 'Chat'}]" border-top/>
    <div v-if="!contentClosed" ref="messagesContainer"
         class="agent-content__section__main --is-chat">
      <Message v-for="message in this.allMessages" v-if="this.allMessages.length > 0"
               :key="message.id"
               ref="messages"
               :agent="this.agent" :message="message" @goTo="goTo($event)"/>
      <div v-else class="grid place-items-center h-full w-full">
        <span class="text-aside text-little">No messages</span>
      </div>
    </div>
  </div>
</template>

<style scoped>

.agent-content__section__main.--is-chat {
  @apply flex flex-col overflow-y-auto h-0 grow gap-2 p-4
}

</style>
