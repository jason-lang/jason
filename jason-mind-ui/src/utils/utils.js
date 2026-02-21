export const getIcon = (icon) => {
  const images = import.meta.glob('/src/assets/img/*.svg', {eager: true})
  return images[`/src/assets/img/${icon}`].default
}
