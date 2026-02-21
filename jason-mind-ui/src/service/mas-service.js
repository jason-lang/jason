import axios from "axios";
import {API_PATH} from "@/service/api.js";
import {hashCode} from "@/domain/agent/utils/general.js";

const MAS_API_PATH = API_PATH + "/mas";

export default {
  findLogs: (referenceTime) => {
    return axios.get(MAS_API_PATH + "/logs").then((response) => {
      if (referenceTime) {
        response.data = response.data.filter(l => referenceTime != null && new Date(l.time) > new Date(referenceTime));
      }
      response.data.sort((l1, l2) => new Date(l1.time) > new Date(l2.time) ? 1 : -1)

      response.data.forEach((log) => {
        log.id = hashCode(log.agentName + log.time + log.content)
      })

      return response
    })
  }, getMas: () => {
    return axios.get(MAS_API_PATH)
  }
}
