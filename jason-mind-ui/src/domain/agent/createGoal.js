import {mountTermText} from "@/domain/agent/utils/terms.js";

export function createGoal(data) {
  return {
    trigger: data.trigger,
    state: data.state,

    toString: () => {
      return mountTermText(data.trigger)
    }
  }
}
