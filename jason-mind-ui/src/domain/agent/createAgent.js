import {createBelief} from "./createBelief.js"
import {createIntention} from "./createIntention.js"
import {createPlan} from "./createPlan.js"

export function createAgent(data) {
  const beliefs = data.beliefs.map(b => createBelief(b))
  const intentions = data.intentions.map(i => createIntention(i))
  const plans = data.allPlans.map(p => createPlan(p))
  const runningPlans = data.runningPlans.map(p => createPlan(p))
  const cycleInfo = {...data.cycleInfo}
  const messages = {...data.messageBox}

  return {
    name: data.name,
    currentCycle: cycleInfo.currentCycleNumber,
    newerCycle: cycleInfo.newerCycleNumber,
    oldestCycle: cycleInfo.olderCycleNumber,
    beliefs: beliefs,
    intentions: intentions,
    selectedIntention: intentions.find(i => i.isSelected) || null,
    plans: plans,
    runningPlans: runningPlans,
    messages: messages,
    time: data.time,

    getSourceMappedBeliefs: () => {
      const map = new Map()
      beliefs.forEach(b => {
        for (let annotation of b.annotations) {
          if (annotation.functor === "source") {
            let source = annotation.terms[0].functor

            if (!map.has(source)) {
              map.set(source, [])
            }
            map.get(source).push(b)
          }
        }
      })
      return map
    }
  }
}
