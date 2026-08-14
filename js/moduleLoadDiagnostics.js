/**
 * Module-load diagnostics
 *
 * Captures and reports failures to load/link lazy-loaded Vite chunks, e.g.
 *   "The requested module 'https://cdn-www.synapse.org/generated/vite/assets/UserOrTeamBadge-XXXX.js'
 *    doesn't provide an export named: 't'"
 *   "Failed to fetch dynamically imported module: ..."
 *
 * This module hooks the browser-level signals that fire
 * regardless of the boundary (Vite's `vite:preloadError`, global `error`, and
 * `unhandledrejection`) to answer two questions:
 *   1. How often and when is this happening? (an event is pushed to GTM `dataLayer` + logged)
 *   2. Why? When a failing chunk URL is known, we re-fetch BOTH the cached copy and a fresh
 *      network copy and record status / size / encoding / whether the trailing `export {...}`
 *      is present. That distinguishes the competing hypotheses at the point of failure:
 *        - truncated / corrupted body (parses but has no exports)
 *        - stale-or-wrong cached body (cached copy differs from network copy)
 *        - a missing export name (build/skew)
 *
 * This module must never throw or interfere with the app: every path is guarded.
 */

const MAX_REPORTS = 25
const seen = new Set()
let reportCount = 0

// Chunk load/link failures are usually transient (bad cache entry, edge
// glitch) - a page reload reliably picks up a clean copy (this mirrors
// Vite's own documented handling of vite:preloadError, and the existing
// GWT runAsync-failure reload in Portal.java). Cap attempts per tab so a
// genuinely broken deploy doesn't reload every affected session forever;
// past the cap, surface a visible fallback instead of looping silently.
const MAX_RELOAD_ATTEMPTS = 3
const RELOAD_COUNT_KEY = 'swcModuleReloadCount'
let hasAttemptedRecoveryThisLoad = false

window.__swcModuleLoadErrors = window.__swcModuleLoadErrors || []

/** Regexes that identify a lazy-module load/link failure (across browsers). */
const MODULE_ERROR_PATTERNS = [
  /doesn't provide an export named/i,
  /does not provide an export named/i,
  /failed to fetch dynamically imported module/i,
  /error loading dynamically imported module/i,
  /importing a module script failed/i,
  /unable to preload (css|module)/i,
]

function isModuleError(message) {
  if (!message) return false
  return MODULE_ERROR_PATTERNS.some(re => re.test(message))
}

/** Pull the offending asset URL out of an error message, when present. */
function extractUrl(message) {
  if (!message) return undefined
  const patterns = [
    /requested module '([^']+)'/i,
    /requested module "([^"]+)"/i,
    /dynamically imported module:?\s*(https?:\/\/\S+|\/\S+)/i,
    /preload (?:css|module) for\s+(https?:\/\/\S+|\/\S+)/i,
    /(https?:\/\/\S+?\.(?:js|mjs|css))/i,
  ]
  for (const re of patterns) {
    const m = message.match(re)
    if (m && m[1]) return m[1].replace(/[).,'"]+$/, '')
  }
  return undefined
}

function extractExportName(message) {
  if (!message) return undefined
  const m = message.match(/export named:?\s*'?([^'\s]+)'?/i)
  return m ? m[1] : undefined
}

function safeHeader(res, name) {
  try {
    return res.headers.get(name)
  } catch {
    return null
  }
}

/**
 * Re-fetch the failing module two ways and capture forensic detail.
 * `force-cache` reflects what the browser had cached (the suspect copy);
 * `reload` bypasses the cache to capture the authoritative current copy.
 * These run SEQUENTIALLY (force-cache first) to ensure the 'network' call does
 * not populate the cached version, overwriting the stale version we hope to
 * capture.
 */
async function inspectModule(url) {
  const probe = async init => {
    try {
      const res = await fetch(url, {
        cache: init,
        credentials: 'omit',
        mode: 'cors',
      })
      const text = await res.text()
      return {
        status: res.status,
        bytes: text.length,
        hasExport: /export\s*\{/.test(text),
        tail: text.slice(-96),
        xCache: safeHeader(res, 'x-cache'),
        age: safeHeader(res, 'age'),
        etag: safeHeader(res, 'etag'),
        contentEncoding: safeHeader(res, 'content-encoding'),
        contentLength: safeHeader(res, 'content-length'),
      }
    } catch (e) {
      return { error: String((e && e.message) || e) }
    }
  }

  const cached = await probe('force-cache')
  const network = await probe('reload')
  return { cached, network }
}

function report(kind, message, stack) {
  try {
    if (!isModuleError(message)) return
    if (reportCount >= MAX_REPORTS) return

    const url = extractUrl(message)
    const key = `${url || ''}|${message}`
    if (seen.has(key)) return
    seen.add(key)
    reportCount++

    const payload = {
      kind,
      message,
      url,
      expectedExport: extractExportName(message),
      stack,
      pageUrl: window.location && window.location.href,
      referrer: document.referrer || undefined,
      userAgent: navigator.userAgent,
      timestamp: new Date().toISOString(),
    }

    window.__swcModuleLoadErrors.push(payload)

    // Frequency tracking via GTM. Safe if GTM has not loaded yet.
    window.dataLayer = window.dataLayer || []
    window.dataLayer.push({
      event: 'swc_module_load_error',
      swcModuleError: payload,
    })

    // eslint-disable-next-line no-console
    console.error('[swc-module-load-error]', payload)

    // Forensic re-fetch (async, best-effort).
    if (url) {
      inspectModule(url)
        .then(inspection => {
          const enriched = { ...payload, inspection }
          window.dataLayer.push({
            event: 'swc_module_load_error_inspection',
            swcModuleError: {
              url,
              expectedExport: payload.expectedExport,
              cachedHasExport: inspection.cached && inspection.cached.hasExport,
              networkHasExport:
                inspection.network && inspection.network.hasExport,
              cachedBytes: inspection.cached && inspection.cached.bytes,
              networkBytes: inspection.network && inspection.network.bytes,
              cachedXCache: inspection.cached && inspection.cached.xCache,
            },
          })
          // eslint-disable-next-line no-console
          console.error('[swc-module-load-error] inspection', enriched)
        })
        .catch(() => {})
    }
  } catch {
    /* diagnostics must never break the app */
  }
}

function getReloadCount() {
  try {
    return parseInt(sessionStorage.getItem(RELOAD_COUNT_KEY), 10) || 0
  } catch {
    return 0
  }
}

function showReloadBanner() {
  try {
    if (document.getElementById('swc-module-load-banner')) return
    const banner = document.createElement('div')
    banner.id = 'swc-module-load-banner'
    banner.setAttribute(
      'style',
      'position:fixed;top:0;left:0;right:0;z-index:2147483647;' +
        'background:#b71c1c;color:#fff;font:14px/1.4 sans-serif;' +
        'padding:10px 16px;text-align:center;',
    )
    const text = document.createElement('span')
    text.textContent = 'Part of this page failed to load.'
    const button = document.createElement('button')
    button.textContent = 'Refresh to try again'
    button.setAttribute(
      'style',
      'margin-left:8px;padding:4px 10px;cursor:pointer;' +
        'border:1px solid #fff;background:transparent;color:#fff;border-radius:4px;',
    )
    button.addEventListener('click', () => window.location.reload())
    banner.appendChild(text)
    banner.appendChild(button)
    ;(document.body || document.documentElement).appendChild(banner)
  } catch {
    /* diagnostics must never break the app */
  }
}

/**
 * Report the recovery outcome to GTM so GA4 can measure whether the reload
 * actually resolves incidents (vs. exhausting the cap into a visible
 * banner) - separate from the raw error-volume signal, which only reflects
 * how often the underlying bug occurs, not whether this mitigation works.
 */
function pushRecoveryEvent(outcome, attempt, url) {
  try {
    window.dataLayer = window.dataLayer || []
    window.dataLayer.push({
      event: 'swc_module_load_recovery',
      swcModuleRecovery: { outcome, attempt, url },
    })
  } catch {
    /* diagnostics must never break the app */
  }
}

/**
 * Attempt to self-heal from a preload failure by reloading, bounded by
 * MAX_RELOAD_ATTEMPTS per tab session. At most one attempt is spent per
 * page load, even if several chunks fail together in the same load.
 */
function attemptRecovery(message) {
  try {
    if (hasAttemptedRecoveryThisLoad) return
    hasAttemptedRecoveryThisLoad = true

    const url = extractUrl(message)
    const count = getReloadCount()
    if (count >= MAX_RELOAD_ATTEMPTS) {
      pushRecoveryEvent('banner_shown', count, url)
      showReloadBanner()
      return
    }
    const attempt = count + 1
    sessionStorage.setItem(RELOAD_COUNT_KEY, String(attempt))
    pushRecoveryEvent('reloaded', attempt, url)
    window.location.reload()
  } catch {
    /* diagnostics must never break the app */
  }
}

function install() {
  try {
    // Vite fires this when a dynamically imported chunk fails to load/preload.
    window.addEventListener('vite:preloadError', event => {
      const err = event && (event.payload || event.detail)
      const message =
        (err && err.message) || String(err) || 'vite:preloadError'
      report('vite:preloadError', message, err && err.stack)
      attemptRecovery(message)
    })

    // Module instantiation/link failures surface as a global error event.
    window.addEventListener(
      'error',
      event => {
        const err = event && event.error
        const message = (err && err.message) || (event && event.message) || ''
        report('error', message, err && err.stack)
      },
      true,
    )

    // Rejected dynamic import() promises that aren't otherwise handled.
    window.addEventListener('unhandledrejection', event => {
      const reason = event && event.reason
      const message =
        (reason && reason.message) || (typeof reason === 'string' ? reason : '')
      report('unhandledrejection', message, reason && reason.stack)
    })
  } catch {
    /* never break startup */
  }
}

install()
