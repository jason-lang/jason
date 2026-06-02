import {mountTermsText, mountTermText} from "./utils/terms.js"
import {createRenderedTerm} from "./createRenderedTerm.js"

export function createDeed(data, isLast) {
  return {
    selected: data.selected,
    formType: data.formType,
    term: data.term,
    isLast,
    src: data.src,
    lineNumber: data.src.beginLine,
    isSelected: data.selected,

    getVars: () => {
      return data.term.terms
        .filter(t => t.type === "var" && t.value)
        .map(term => {
          let textValue
          if (term.value.type === "list") {
            textValue = term.value.terms.length > 1 ? `size ${term.value.terms.length} - [${mountTermsText(term.value.terms)}]` : `[${mountTermsText(term.value.terms)}]`
          } else if (typeof term.value === "object") {
            textValue = mountTermText(term.value)
          } else {
            textValue = term.value
          }
          return createRenderedTerm(term.name, term.value, textValue, term.type)
        })
    },

    toString: () => {
      if (data.term.functor === ' = ') {
        return mountTermText(data.term.terms[0]) + " = " + mountTermText(data.term.terms[1])
      }
      const termText =
        data.term.terms.length > 0
          ? "(" + data.term.terms.map(mountTermText).join(", ") + ")"
          : ""
      return data.formType + data.term.functor + termText + (isLast ? "." : ";")
    },
  }
}
