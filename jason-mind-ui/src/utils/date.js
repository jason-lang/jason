export const getFormattedTimeFromDateString = (dateString) => {
  const date = new Date(dateString)

  const hours = date.getHours() < 10 ? '0' + date.getHours() : date.getHours();
  const minutes = date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes();
  const seconds = date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds();
  const millis = date.getMilliseconds() < 10 ? date.getMilliseconds() + '00' : date.getMilliseconds() < 100 ? date.getMilliseconds() + '0' : date.getMilliseconds()

  return `${hours}:${minutes}:${seconds}.${millis}`
}

export const getTimestampFromDateString = (dateString) => {
  const date = new Date(dateString)
  return date.getTime()
}
