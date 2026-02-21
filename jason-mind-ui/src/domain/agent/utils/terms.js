export function mountTermText(term) {
  if (!term) return ""
  switch (term.type) {
    case "pred":
      return term.functor + "(" + term.terms.map(mountTermText).join(", ") + ")"
    case "atom":
      return term.functor
    case "string":
      return `"${term.value}"`
    case "var":
      return term.name
    case "trigger":
      return term.operator + term.triggerType + term.functor
    case "expression":
      return mountTermText(term.terms[0]) + term.functor + mountTermText(term.terms[1])
    case "list":
      return "[" + term.terms.map(mountTermText).join(", ") + "]"
    default:
      return term.value ?? "..."
  }
}

export function mountTermsText(terms) {
  return `${terms.map(mountTermText).join(", ")}`
}
