import {mountTermText} from "./utils/terms.js"

export function createBelief(data) {
  return {
    annotations: data.annotations,
    functor: data.functor,
    terms: data.terms,
    cycle: data.cycle,

    toString: () => {
      let text = data.functor
      if (data.terms.length > 0) {
        text += "(" + data.terms.map(mountTermText).join(", ") + ")"
      }
      return text
    },

    getSource() {
      for (let index in data.annotations) {
        let annotation = data.annotations[index]
        if (annotation.functor === "source") {
          return annotation.terms[0].functor
        }
      }
    }
  }
}
