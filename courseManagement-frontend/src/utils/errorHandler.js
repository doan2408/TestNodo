/**
 * Extract error message từ API response
 * @param {Error} error - Error object từ axios
 * @returns {string|string[]} - Error message(s)
 */
export const extractErrorMessage = (error) => {
  const errorMessages = error.response?.data?.message;
  
  if (Array.isArray(errorMessages)) {
    return errorMessages; // Return array để caller hiển thị từng lỗi
  } else if (typeof errorMessages === 'string') {
    return errorMessages;
  }
  
  return null; // Trả về null nếu không có message
};

/**
 * Display error với i18n support
 * @param {Error} error - Error object
 * @param {Function} t - i18n translate function
 * @param {Function} elMessage - ElMessage function
 * @param {string} defaultMessageKey - i18n key cho default message
 */
export const handleErrorDisplay = (error, t, elMessage, defaultMessageKey) => {
  const errorMessages = extractErrorMessage(error);
  
  if (Array.isArray(errorMessages)) {
    const errorText = errorMessages.join("\n");
    elMessage.error(errorText);
    return errorMessages; // Return array để set formErrors
  } else if (typeof errorMessages === 'string') {
    elMessage.error(errorMessages);
    return null;
  } else {
    elMessage.error(t(defaultMessageKey));
    return null;
  }
};
