import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import SafeHtml from '../SafeHtml.vue';
import { sanitizeHtml } from '@/composables/useSafeHtml';

describe('SafeHtml', () => {
  it('strips script tags and event handlers', () => {
    const payload = '<img src="x" onerror="alert(1)">Hello<script>alert(1)</script>';
    const wrapper = mount(SafeHtml, {
      props: {
        content: payload,
      },
    });

    expect(wrapper.html()).toContain('<img src="x">');
    expect(wrapper.html()).toContain('Hello');
    expect(wrapper.html()).not.toContain('onerror');
    expect(wrapper.html()).not.toContain('<script>');
  });

  it('renders alternative tag when provided', () => {
    const wrapper = mount(SafeHtml, {
      props: {
        content: '<strong>safe</strong>',
        tag: 'section',
      },
    });

    expect(wrapper.element.tagName).toBe('SECTION');
    expect(wrapper.html()).toContain('<strong>safe</strong>');
  });
});

describe('sanitizeHtml', () => {
  it('returns empty string for falsy inputs', () => {
    expect(sanitizeHtml(null)).toBe('');
    expect(sanitizeHtml('')).toBe('');
  });
});
