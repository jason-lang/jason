import {createGoal} from "@/domain/agent/createGoal.js";

export function createIntention(data) {
  const stackGoals = data.stackGoals.map(s => createGoal(s))
  return {
    id: data.id,
    state: data.state,
    stackGoals: stackGoals,
    isSelected: data.state === "selected",
    isPending: data.state === "pending",
    isSuspended: data.state === "suspended",
    isInQueue: data.state === "queue",

    toString: () => {
      return stackGoals[data.stackGoals.length - 1].toString()
    }
  }
}
