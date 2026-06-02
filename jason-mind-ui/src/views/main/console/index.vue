<script>
import Tabs from "@/components/general/Tabs.vue";
import Log from "@/views/main/console/Log.vue";
import masService from "@/service/mas-service.js";
import Button from "@/components/general/button/Button.vue";
import {getIcon} from "@/utils/utils.js";
import SearchButton from "@/components/general/button/SearchButton.vue";

export default {
  name: "Console",
  components: {SearchButton, Button, Log, Tabs},
  props: {
    height: String,
  },
  data() {
    return {
      logs: [],
      agentNames: [],
      selectedAgentName: null,
      contentClosed: false,
      logsSearch: "",
      currentHighlightLogIndex: null,
      pollingTimeout: null,
      pollingIntervalMs: 1000,
    };
  },
  mounted() {
    this.startPolling();
  },
  beforeUnmount() {
    this.stopPolling();
  },
  methods: {
    getIcon,
    startPolling() {
      this.stopPolling();
      this.pollLogs();
    },
    stopPolling() {
      if (this.pollingTimeout) {
        clearTimeout(this.pollingTimeout);
        this.pollingTimeout = null;
      }
    },
    async filterLogByAgent(agentName) {
      this.contentClosed = false
      this.selectedAgentName = agentName
      this.$refs.search.close()

      const response = await masService.findLogs(null);

      await this.loadLogs(response.data)
    },
    async loadLogs(loadedLogs) {
      const logs = [...this.logs, ...loadedLogs];

      if (this.selectedAgentName != null) {
        this.logs = logs.filter(log => log.agentName === this.selectedAgentName)
      } else {
        this.logs = logs
      }

      if (loadedLogs.length > 0) {
        await this.$nextTick(() => {
          this.$refs.logsContainer?.scrollTo(0, this.$refs.logsContainer.scrollHeight);
        });
      }
    },
    async pollLogs() {
      try {
        if (this.logsSearch && this.logsSearch.length > 0) {
          return;
        }

        const response = await masService.findLogs(this.logs.length > 0 ?
          this.logs[this.logs.length - 1].time : null);

        await this.loadLogs(response.data)
      } finally {
        this.pollingTimeout = setTimeout(this.pollLogs, this.pollingIntervalMs);
      }
    },
    previousLog() {
      if (this.searchLogResults.length === 0) {
        return;
      }
      if (this.currentHighlightLogIndex === 0) {
        return;
      }

      this.currentHighlightLogIndex--;
      this.updateCurrentSelectedHighlight();
    },
    nextLog() {
      if (this.searchLogResults.length === 0) {
        return;
      }
      if (this.currentHighlightLogIndex === this.searchLogResults.length - 1) {
        return;
      }

      this.currentHighlightLogIndex++;
      this.updateCurrentSelectedHighlight();
    },
    updateCurrentSelectedHighlight() {
      const i = this.logs.indexOf(this.searchLogResults[this.currentHighlightLogIndex]);
      if (i < 0) return;

      const el = this.$refs.logs[i].$el || this.$refs.logs[i];
      const container = this.$refs.logsContainer;

      const elRect = el.getBoundingClientRect();
      const containerRect = container.getBoundingClientRect();

      const offset = elRect.top - containerRect.top;

      container.scrollTo({
        top: container.scrollTop + offset - 100,
        behavior: "smooth"
      });
    },
    goToSender(log) {
      this.$emit("goTo", {agent: log.agentName, cycle: log.cycle});
    },
  },
  watch: {
    contentClosed(contentClosed) {
      if (contentClosed) {
        this.selectedAgentName = null
        this.$refs.search.close()
      }
    },
    logs: {
      deep: true,
      handler(logs) {
        logs.forEach((log) => {
          if (!this.agentNames.includes(log.agentName)) {
            this.agentNames.push(log.agentName)
          }
        })
      }
    },
    logsSearch(value) {
      if (!value || value.length === 0) {
        this.currentHighlightLogIndex = null;
        this.logs.forEach(log => (log.highlight = undefined));
        this.startPolling();
        return;
      }

      this.stopPolling();

      this.logs.forEach(log => {
        log.highlight = log.content.toLowerCase().includes(value.toLowerCase());
      });

      if (this.searchLogResults.length > 0) {
        this.currentHighlightLogIndex = 0;
        this.$nextTick(() => this.updateCurrentSelectedHighlight());
      } else {
        this.currentHighlightLogIndex = null;
      }
    },
  },
  computed: {
    searchLogResults() {
      return this.logs.filter(l => l.highlight);
    }
  },
};
</script>

<template>
  <div class="agent-content__section --is-console">
    <Tabs v-model:closed="contentClosed" :tabs="[{label: 'Console'}]"
          :closeable="this.height !== '100%'"/>

    <div  class="flex flex-col h-full">
      <div class="console__subheader">
        <div class="flex gap-2 items-center">
          <Button
            :selected="selectedAgentName === null"
            text="All"
            @click="filterLogByAgent(null)"
          />
          <Button
            v-for="agentName in this.agentNames"
            :key="agentName"
            :selected="selectedAgentName === agentName"
            :text="agentName"
            @click="filterLogByAgent(agentName)"
          />
        </div>
        <div class="flex items-center gap-2 h-full">
          <div v-if="logsSearch.length > 0" class="flex items-center gap-2 h-full">
            <Button :icon="getIcon('drop-down.svg')" style="transform: rotate(90deg)"
                    @click="previousLog"/>
            <span>{{ (currentHighlightLogIndex ?? -1) + 1 }} / {{ searchLogResults.length }}</span>
            <Button :icon="getIcon('drop-down.svg')" style="transform: rotate(270deg)"
                    @click="nextLog"/>
          </div>
          <SearchButton ref="search" v-model:search="logsSearch" @focus="this.contentClosed = false"/>
        </div>

      </div>
      <div ref="logsContainer"
           class="agent-content__section__main --is-console code" v-show="!contentClosed">
        <Log
          v-for="log in logs"
          :key="log.id"
          ref="logs"
          :class="{
          'current-highlight':
            currentHighlightLogIndex != null &&
            searchLogResults[currentHighlightLogIndex].id === log.id
        }"
          :log="log"
          @goToSender="goToSender(log)"
        />

        <div v-if="logs.length === 0" class="grid place-items-center h-full w-full">
          <span class="text-aside text-little">No logs</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.agent-content__section.--is-console {
  @apply w-full rounded-md shadow overflow-hidden;
}

.agent-content__section__main.--is-console {
  height: v-bind(height);
  @apply flex flex-col overflow-y-auto pb-2;
}

.current-highlight {
  background-color: var(--pallete-2);
}

.console__subheader {
  border-bottom: 1px solid var(--pallete-trace);
  height: var(--header-height);
  @apply flex items-center justify-between px-2
}

</style>
