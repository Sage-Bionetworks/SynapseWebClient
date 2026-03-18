/**
 * Browser stub for jsdom.
 *
 * SanitizeHtmlUtils in synapse-react-client requires jsdom only in non-browser
 * environments (guarded by `typeof window === "undefined"`). This stub satisfies
 * Vite/Rollup's static module resolution without shipping any jsdom code into
 * the browser bundle. The JSDOM constructor will never be called at runtime.
 */
export class JSDOM {
  constructor() {
    throw new Error('jsdom is not available in the browser')
  }
}
