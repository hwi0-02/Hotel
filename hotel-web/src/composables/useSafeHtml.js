import { computed, isRef } from 'vue';
import DOMPurify from 'dompurify';

const DEFAULT_PROFILE = Object.freeze({ USE_PROFILES: { html: true } });

export function sanitizeHtml(input, options = DEFAULT_PROFILE) {
  const value = typeof input === 'function' ? input() : input;
  if (!value) {
    return '';
  }
  return DOMPurify.sanitize(value, options);
}

export function useSafeHtml(source, options = DEFAULT_PROFILE) {
  return {
    sanitizedHtml: computed(() => {
      const value = isRef(source) ? source.value : source;
      return sanitizeHtml(value, options);
    }),
  };
}
