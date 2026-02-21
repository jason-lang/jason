import {createDeed} from "./createDeed.js"
import {mountTermsText, mountTermText} from "./utils/terms.js"
import {hashCode} from "@/domain/agent/utils/general.js";

export function createPlan(data) {
  const deeds = data.deeds.map((d, i) => createDeed(d, i === data.deeds.length - 1))

  return {
    id: hashCode(JSON.stringify(data)),
    trigger: data.trigger,
    goalTrigger: data.goalTrigger,
    annotations: data.trigger.annotations,
    context: data.context,
    isTriggered: data.goalTrigger != null,
    deeds: deeds,

    isTriggeredByGoal(goal) {
      return hashCode(JSON.stringify(goal.trigger)) === hashCode(JSON.stringify(this.goalTrigger))
    },

    getSelectedDeed: () => {
      return deeds.find(d => d.isSelected)
    },

    toString: () => {
      let text = mountTermText(data.trigger)
      if (data.trigger.terms && data.trigger.terms.length > 0) {
        text += "(" + mountTermsText(data.trigger.terms) + ")"
      }
      return text
    }
  }
}
