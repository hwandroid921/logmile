<script setup>
// Claude Design shared.jsx Icon 컴포넌트 → Vue 변환
defineProps({
  name: { type: String, required: true },
  size: { type: Number, default: 16 },
  className: { type: String, default: '' },
})

const paths = {
  truck:     `<path d="M3 16V6h11v10"/><path d="M14 9h4l3 3v4h-7"/><circle cx="7" cy="17.5" r="2"/><circle cx="17" cy="17.5" r="2"/>`,
  eye:       `<path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/>`,
  bell:      `<path d="M6 8a6 6 0 0 1 12 0c0 5 2 6 2 6H4s2-1 2-6"/><path d="M10 19a2 2 0 0 0 4 0"/>`,
  chart:     `<path d="M3 3v18h18"/><path d="M7 14l3-3 3 3 5-5"/>`,
  shield:    `<path d="M12 2 4 5v6c0 5 3.5 9 8 11 4.5-2 8-6 8-11V5l-8-3z"/><path d="m9 12 2 2 4-4"/>`,
  cpu:       `<rect x="5" y="5" width="14" height="14" rx="1.5"/><rect x="9" y="9" width="6" height="6"/><path d="M9 1v3M15 1v3M9 20v3M15 20v3M1 9h3M1 15h3M20 9h3M20 15h3"/>`,
  activity:  `<path d="M3 12h4l3-8 4 16 3-8h4"/>`,
  map:       `<path d="M9 3 3 5v16l6-2 6 2 6-2V3l-6 2-6-2z"/><path d="M9 3v16M15 5v16"/>`,
  arrow:     `<path d="M5 12h14"/><path d="m13 5 7 7-7 7"/>`,
  arrowDown: `<path d="M12 5v14"/><path d="m5 13 7 7 7-7"/>`,
  arrowUp:   `<path d="M12 19V5"/><path d="m5 11 7-7 7 7"/>`,
  play:      `<path d="M6 4l14 8L6 20z"/>`,
  search:    `<circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/>`,
  settings:  `<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1-1.5 1.7 1.7 0 0 0-1.8.3l-.1-.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3h.1a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8v.1a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/>`,
  user:      `<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>`,
  grid:      `<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>`,
  list:      `<path d="M8 6h13M8 12h13M8 18h13"/><path d="M3 6h.01M3 12h.01M3 18h.01"/>`,
  clock:     `<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>`,
  bolt:      `<path d="M13 2 4 14h7l-1 8 9-12h-7l1-8z"/>`,
  info:      `<circle cx="12" cy="12" r="9"/><path d="M12 8h.01M11 12h1v5h1"/>`,
  plus:      `<path d="M12 5v14M5 12h14"/>`,
  download:  `<path d="M12 3v12"/><path d="m7 10 5 5 5-5"/><path d="M5 21h14"/>`,
  filter:    `<path d="M3 4h18l-7 9v6l-4 2v-8z"/>`,
  refresh:   `<path d="M3 12a9 9 0 0 1 15-6.7L21 8"/><path d="M21 3v5h-5"/><path d="M21 12a9 9 0 0 1-15 6.7L3 16"/><path d="M3 21v-5h5"/>`,
  moon:      `<path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z"/>`,
  camera:    `<path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/>`,
  location:  `<path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>`,
  db:        `<ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M3 5v14a9 3 0 0 0 18 0V5"/><path d="M3 12a9 3 0 0 0 18 0"/>`,
  layers:    `<path d="m12 2 10 6-10 6L2 8z"/><path d="m2 14 10 6 10-6"/>`,
  check:     `<path d="m5 12 5 5L20 7"/>`,
  x:         `<path d="M18 6 6 18M6 6l12 12"/>`,
  chevronR:       `<path d="m9 6 6 6-6 6"/>`,
  'chevron-right':`<path d="m9 6 6 6-6 6"/>`,
  chevronL:       `<path d="m15 6-6 6 6 6"/>`,
  'chevron-left': `<path d="m15 6-6 6 6 6"/>`,
  moreH:     `<circle cx="5" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/>`,
  logout:    `<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>`,
  building:  `<path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/><path d="M9 9h1m-1 4h1m-1 4h1m4-8h1m-1 4h1"/>`,
  users:     `<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>`,
}
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.6"
    stroke-linecap="round"
    stroke-linejoin="round"
    :class="className"
    v-html="paths[name] || ''"
  />
</template>
