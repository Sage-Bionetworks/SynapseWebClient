import { defineConfig } from 'vite'
import { nodePolyfills } from 'vite-plugin-node-polyfills'

/**
 * Vite config to generate the ESM & CJS bundles for Synapse React Client.
 */
const config = defineConfig({
  // Assets will be served from the `/Portal/cdn` servlet, which will redirect requests to the CDN when appropriate
  base: '/Portal/cdn/generated/vite/',
  server: {
    cors: {
      origin: ['http://localhost:8888', 'http://127.0.0.1:8888'],
    },
  },
  build: {
    manifest: true,
    outDir: './src/main/webapp/generated/vite',
    rollupOptions: {
      input: 'js/main.js',
      onwarn(warning, warn) {
        // Suppress "Module level directives cause errors when bundled" warnings
        if (warning.code === 'MODULE_LEVEL_DIRECTIVE') {
          return
        }
        warn(warning)
      },
    },
  },
  optimizeDeps: {
    include: [
      // We must import @emotion libraries so MUI uses them
      '@emotion/react',
      '@emotion/styled',
    ],
  },
  resolve: {
    dedupe: ['@emotion/react', '@emotion/styled', 'react', 'react-dom'],
  },
  plugins: [nodePolyfills()],
  define: {
    __TEST__: JSON.stringify(false),
    __DEV__: JSON.stringify(false),
  },
})

export default config
