import { resolve } from 'node:path'

import { defineConfig } from 'vite'
import * as packageJson from './package.json'

// https://vitejs.dev/config/
export default defineConfig((configEnv) => ({
  build: {
    lib: {
     // TODO: pkg.source ... or simila r?
      entry: resolve('src', 'index.js'),
      name: packageJson.name,
      formats: ['es'],
      // fileName: (format) => `${packageJson.name}.${format}.js`,
      fileName: (format) => `index.js`,
    },
    rollupOptions: {
      external: [...Object.keys(packageJson.peerDependencies)],
    },
  },
}))