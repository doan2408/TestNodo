import { createI18n } from 'vue-i18n'
import vi from './locales/vi.json'
import en from './locales/en.json'

const defaultLocale = localStorage.getItem('locale') || 'vi'

const i18n = createI18n({
  legacy: false,
  locale: defaultLocale,
  fallbackLocale: 'vi',
  messages: {
    vi,
    en
  },
  globalInjection: true,
  missingWarn: false,
  fallbackWarn: false
})

export default i18n
