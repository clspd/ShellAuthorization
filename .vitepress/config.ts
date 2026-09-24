import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  srcDir: "files",
  
  title: "Shell Authorization",
  description: "A powerful Shell Authorization solution with fine-grained access controls",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Introduction', link: '/introduction' },
      { text: 'Quick Start', link: '/quick-start' },
      { text: 'Downloads', link: '/downloads' }
    ],

    sidebar: [
      {
        text: 'Guide',
        items: [
          { text: 'Introduction', link: '/introduction' },
          { text: 'Quick Start', link: '/quick-start' },
          { text: 'Downloads', link: '/downloads' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/clspd/ShellAuthorization' }
    ]
  }
})
