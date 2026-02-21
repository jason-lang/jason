import axios from "axios";
import {API_PATH} from "@/service/api.js";

const AGENT_API_PATH = API_PATH + "/agents/"

export default {
  find(name = null, cycle = null) {
    if (name === null) {
      return axios.get(AGENT_API_PATH)
    }
    return axios.get(AGENT_API_PATH + name, {
      params: {
        cycle: cycle
      }
    })
  },
  async findMessages(name, time = null) {
    let inMessages = await axios.get(AGENT_API_PATH + name + "/messages/in", {
      params: {
        time: time
      }
    }).catch(() => {
      return {
        data: []
      }
    })

    let outMessages = await axios.get(AGENT_API_PATH + name + "/messages/out", {
      params: {
        time: time
      }
    }).catch(() => {
      return {
        data: []
      }
    })

    return mergeMessages(inMessages.data, outMessages.data)
  },
}

const mergeMessages = (inMessages, outMessages) => {
  let messages = [...inMessages, ...outMessages]
  return messages.sort((m1, m2) => new Date(m1.time) > new Date(m2.time) ? 1 : -1)
}
