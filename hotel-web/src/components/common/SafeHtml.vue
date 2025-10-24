<script setup>
import { computed, toRefs } from 'vue';
import { sanitizeHtml } from '@/composables/useSafeHtml';

const props = defineProps({
  content: {
    type: String,
    default: '',
  },
  tag: {
    type: String,
    default: 'div',
  },
  sanitizeOptions: {
    type: Object,
    default: undefined,
  },
});

const { tag, sanitizeOptions } = toRefs(props);

const sanitizedHtml = computed(() => sanitizeHtml(() => props.content, sanitizeOptions.value));
</script>

<template>
  <component :is="tag" v-html="sanitizedHtml"></component>
</template>
