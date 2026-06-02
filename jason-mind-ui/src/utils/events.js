export function preventNotNumbers(e) {
  const allowedKeys = [
    'Backspace', 'Tab', 'ArrowLeft', 'ArrowRight', 'Delete', 'Control'
  ];
  const isNumber = /^[0-9]$/.test(e.key);

  if (!isNumber && !allowedKeys.includes(e.key)) {
    e.preventDefault();
  }
}
