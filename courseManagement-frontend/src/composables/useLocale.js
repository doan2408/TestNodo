import { ref } from 'vue'
import i18n from '@/i18n'

const locale = ref(i18n.global.locale.value || 'vi')

export function useLocale() {
  const setLocale = (l) => {
    locale.value = l

    // Cập nhật UI i18n
    i18n.global.locale.value = l

    // Cập nhật backend language key
    localStorage.setItem('language', l)
  }

  const getLocale = () => locale.value

  // Khởi tạo mặc định
  setLocale(locale.value)

  return {
    locale,
    setLocale,
    getLocale
  }
}
