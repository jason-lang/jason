export const hashCode = (string) => {
  let hash = 0;
  for (let i = 0; i < string.length; i++) {
    let code = string.charCodeAt(i);
    hash = ((hash << 5) - hash) + code;
    hash = hash & hash;
  }
  return hash;
}
