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
 *   1. How often and when is this happening? (logged to CloudWatch via
 *      `POST /repo/v1/log`, see `sendLog`, and to the console)
 *   2. Why? When a failing chunk URL is known, we re-fetch BOTH the cached copy and a fresh
 *      network copy and record status / size / head+tail bytes / encoding / how many times the
 *      URL was actually requested on this page (via Resource Timing) / whether the *specific*
 *      named export Vite reported missing is present (`hasNamedExport` - not just "some export
 *      {...} exists somewhere in the body", which is true of every valid module and false of
 *      every stylesheet, and so cannot distinguish any of the following). That distinguishes the
 *      competing hypotheses at the point of failure:
 *        - a short read (body ends cleanly, but before the trailing export statement)
 *        - wrong bytes (a different module's body under this URL)
 *        - a duplicate module record (the URL was fetched more than once on this page)
 *
 * This module must never throw or interfere with the app: every path is guarded.
 */
const MAX_REPORTS = 25
const seen = new Set()
// Separate from `seen`: which (url, message) pairs have already been counted
// against MAX_REPORTS and had the forensic re-fetch run. `seen` dedupes per
// *boundary* (kind is part of its key); this dedupes per *failure*, so a
// failure caught by two boundaries only spends one report-budget slot and
// only triggers one forensic re-fetch. See the comment at its use in
// `report()`.
const firstSeenFailures = new Set()
let reportCount = 0

// Chunk load/link failures are usually transient (bad cache entry, edge
// glitch); a page reload reliably picks up a clean copy (this mirrors
// Vite's own documented handling of vite:preloadError, and the existing
// GWT runAsync-failure reload in Portal.java). Cap attempts per tab so a
// genuinely broken deploy doesn't reload every affected session forever;
// past the cap, surface a visible fallback instead of looping silently.
const MAX_RELOAD_ATTEMPTS = 3
const RELOAD_COUNT_KEY = 'swcModuleReloadCount'
let hasAttemptedRecoveryThisLoad = false

// A reload cancels any in-flight fetches, which would silently drop the
// forensic re-fetch (and its `swc_module_load_error_inspection` log entry)
// this module just kicked off. Give it a bounded amount of time to finish
// before navigating away.
const INSPECTION_TIMEOUT_MS = 3000

window.__swcModuleLoadErrors = window.__swcModuleLoadErrors || []

/**
 * The raw `message` is free-form JS-engine wording, so it can't be grouped
 * or counted on its own. This maps each engine's wording onto a short,
 * stable enum so the *shape* of a failure is groupable in CloudWatch (via
 * `messageShape` in the JSON `message` body) without parsing the full text.
 * Emitted alongside the raw `message`, never instead of it.
 *
 * Covers known engine wordings, plus Vite's own preload-helper wording:
 *
 *   missing-export      Firefox/SpiderMonkey "doesn't provide an export named"
 *                       Chrome-Edge/V8       "does not provide an export named"
 *                       Safari/JSC           "Importing binding name '...' is
 *                                             not found."
 *   fetch-failed        Firefox/SpiderMonkey "error loading dynamically
 *                                             imported module"
 *                       Chrome-Edge/V8       "Failed to fetch dynamically
 *                                             imported module"
 *                       Safari/JSC           "Importing a module script
 *                                             failed."
 *                       Vite helper          "Unable to preload module ..."
 *   css-preload-failed  Vite helper          "Unable to preload CSS for ..."
 *   other               Defensive default only. `MODULE_ERROR_PATTERNS` is
 *                       derived from this table, so any message that passes
 *                       `isModuleError` necessarily matches a shape above.
 *                       This is the single place a new engine wording gets
 *                       added, which makes a message both *reportable* and
 *                       *classifiable* in one edit. That coupling is the
 *                       point: Safari's missing-export wording once existed
 *                       in one list but not the other.
 *
 * Order matters: the CSS test runs first because "Unable to preload CSS" is a
 * distinct population (the redirector path) that must not be folded into the
 * generic fetch-failure bucket.
 */
const MESSAGE_SHAPE_PATTERNS = [
  ['css-preload-failed', [/unable to preload css/i]],
  [
    'missing-export',
    [
      /doesn't provide an export named/i,
      /does not provide an export named/i,
      /importing binding name .* is not found/i,
    ],
  ],
  [
    'fetch-failed',
    [
      /failed to fetch dynamically imported module/i,
      /error loading dynamically imported module/i,
      /importing a module script failed/i,
      /unable to preload module/i,
    ],
  ],
]

/**
 * Regexes that identify a lazy-module load/link failure (across browsers),
 * derived from MESSAGE_SHAPE_PATTERNS so the two can never drift apart. Do not
 * re-inline this list: a wording present here but absent from the shape table
 * would be reported as an unclassifiable `other`, and a wording present there
 * but absent here would not be reported at all.
 */
const MODULE_ERROR_PATTERNS = MESSAGE_SHAPE_PATTERNS.flatMap(
  ([, patterns]) => patterns,
)

function isModuleError(message) {
  if (!message) return false
  return MODULE_ERROR_PATTERNS.some(re => re.test(message))
}

function classifyMessageShape(message) {
  try {
    if (!message) return 'other'
    for (const [shape, patterns] of MESSAGE_SHAPE_PATTERNS) {
      if (patterns.some(re => re.test(message))) return shape
    }
  } catch {
    /* diagnostics must never break the app */
  }
  return 'other'
}

/** Matches a content-hashed Vite asset filename, e.g. `main-D2GRFy7k.js`. */
const HASHED_ASSET_RE = /([^/]+-[A-Za-z0-9_-]{8,})\.(?:js|mjs)(?:$|[?#])/

function buildIdFromUrl(value) {
  if (typeof value !== 'string') return undefined
  const m = value.match(HASHED_ASSET_RE)
  return m ? m[1] : undefined
}

/**
 * Which JavaScript build the failing tab is actually *executing*, e.g.
 * `main-D2GRFy7k.js`.
 *
 * We can use this to determine which version the Synapse UI is running in the
 * tab, and would identify the case where a user is using stale UI assets
 * that they acquired before the latest release of Synapse.
 */
function detectBuildId() {
  try {
    const fromSelf = buildIdFromUrl(
      typeof import.meta !== 'undefined' && import.meta
        ? import.meta.url
        : undefined,
    )
    if (fromSelf) return fromSelf

    if (typeof document !== 'undefined' && document.querySelectorAll) {
      const tags = [
        ...document.querySelectorAll('script[type="module"][src]'),
        ...document.querySelectorAll('link[rel="modulepreload"][href]'),
      ]
      for (const tag of tags) {
        const fromTag = buildIdFromUrl(
          tag.getAttribute('src') || tag.getAttribute('href'),
        )
        if (fromTag) return fromTag
      }
    }
  } catch {
    /* diagnostics must never break the app */
  }
  return 'unknown'
}

const BUILD_ID = detectBuildId()

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

/**
 * Whether `text` (a fetched module body) actually exports `name` as a
 * binding. Unlike `/export\s*\{/.test(text)`, this checks for the specific
 * name Vite's helper reported missing - true of every valid ESM module and
 * false of every stylesheet, so it can't tell a truncated/corrupted module
 * apart from a merely-different one. Vite/rolldown emit named exports as a
 * single trailing statement, e.g. `export{f as t}` or `export{y as n,w as
 * t}` - `t`/`n` (right of `as`, or bare) are the *exported* names.
 *
 * Returns `true`/`false` when the final `export{...}` statement resolves
 * the question, or `null` when it can't be answered from static text alone
 * (no `export{...}` found at all, e.g. a CSS response; or the module only
 * re-exports via `export * from '...'`, which would require following the
 * chain to know whether it supplies `name`). Callers must not treat `null`
 * as `false` - that would reintroduce the exact ambiguity this replaces.
 */
function hasNamedExport(text, name) {
  if (!name || typeof text !== 'string') return null

  const exportBlockRe = /export\s*\{([^}]*)\}/g
  let lastBindings = null
  let match
  while ((match = exportBlockRe.exec(text)) !== null) {
    lastBindings = match[1]
  }

  if (lastBindings !== null) {
    const bindings = lastBindings.split(',')
    for (const binding of bindings) {
      const trimmed = binding.trim()
      if (!trimmed) continue
      const asMatch = trimmed.match(/^(\S+)\s+as\s+(\S+)$/)
      const exportedName = asMatch ? asMatch[2] : trimmed
      if (exportedName === name) return true
    }
    // The final export statement doesn't bind `name` - but only trust that
    // as a genuine "missing" if there's no `export * from` re-export chain
    // elsewhere in the body that could be supplying it instead.
    if (!/export\s*\*\s*from/.test(text)) return false
  }

  return null
}

function safeHeader(res, name) {
  try {
    return res.headers.get(name)
  } catch {
    return null
  }
}

/**
 * How many times `url` was actually requested by this page, via the
 * browser's Resource Timing entries. This is the direct evidence for
 * whether a chunk was fetched at two different URLs that happened to
 * resolve to the same module, e.g. a `/Portal/cdn/...` preload vs. a direct
 * `import()`.
 *
 * Cross-origin size fields on these entries (decodedBodySize/encodedBodySize/
 * transferSize) read as 0 unless the response sends `Timing-Allow-Origin`;
 * see CORSFilter.
 */
function collectResourceTiming(url) {
  try {
    if (
      typeof performance === 'undefined' ||
      typeof performance.getEntriesByName !== 'function'
    ) {
      return { fetchCount: null, entries: [], error: 'no-resource-timing' }
    }

    const candidates = [url]
    // `PerformanceResourceTiming.name` is the entry's serialized, absolute
    // requested URL, so `getEntriesByName` only matches an absolute URL.
    // Every candidate below must therefore be absolute, or the whole
    // duplicate-fetch probe (`fetchCount`, `resourceTimingEntries`) silently
    // becomes dead code.
    try {
      const documentOrigin =
        (typeof window !== 'undefined' &&
          window.location &&
          window.location.href) ||
        undefined
      const parsed = new URL(url, documentOrigin)
      // The reported URL is itself sometimes root-relative, which for the
      // same reason can never match either; add its absolute form too.
      if (parsed.href !== url) candidates.push(parsed.href)

      const m = parsed.pathname.match(/\/generated\/vite\/(.*)$/)
      if (m) {
        const redirectorPath = `/Portal/cdn/generated/vite/${m[1]}`
        const origins = [parsed.origin]
        if (
          typeof window !== 'undefined' &&
          window.location &&
          window.location.origin
        ) {
          origins.push(window.location.origin)
        }
        for (const origin of origins) {
          if (origin && origin !== 'null') {
            candidates.push(`${origin}${redirectorPath}`)
          }
        }
      }
    } catch {
      /* url wasn't absolute/parseable; skip the alternate forms */
    }

    const uniqueCandidates = [...new Set(candidates)]
    const entries = uniqueCandidates.flatMap(candidate =>
      performance.getEntriesByName(candidate),
    )

    return {
      fetchCount: entries.length,
      entries: entries.map(e => ({
        name: e.name,
        decodedBodySize: e.decodedBodySize,
        encodedBodySize: e.encodedBodySize,
        transferSize: e.transferSize,
      })),
    }
  } catch (e) {
    return {
      fetchCount: null,
      entries: [],
      error: String((e && e.message) || e),
    }
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
async function inspectModule(url, expectedExport) {
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
        hasNamedExport: hasNamedExport(text, expectedExport),
        head: text.slice(0, 64),
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

// --- Backend logging --------------------------------------------------
//
// The server logs each entry as `"{label} - {userAgent}: {message}"` (see
// LogServiceImpl in Synapse-Repository-Services), so `label` is the only
// field cheaply filterable without parsing; the full payload travels in
// `message` as a JSON string instead, and includes `portalVersion`/
// `repoVersion` (see `getVersions`) on every entry. A typical Logs Insights
// query:
//   fields @timestamp, @message
//   | filter @message like /swc_module_load_error/
//   | parse @message '* - *: *' as label, userAgent, jsonBody
//   | parse jsonBody '"buildId":"*"' as buildId
//   | stats count(*) by buildId

const LOG_ENDPOINT_PATH = '/log'
// Mirrors the fallback in synapse-react-client's getEndpoint(): if GWT
// hasn't set `window.SRC_OVERRIDE_ENDPOINT_CONFIG` yet (this module can run
// before GWT bootstraps), assume production.
const PRODUCTION_REPO_ENDPOINT = 'https://repo-prod.prod.sagebase.org/repo/v1'
// Same idea for the Portal's own origin (used only for `GET /Portal/versions`
// below, not for the log service itself).
const PRODUCTION_PORTAL_ENDPOINT = 'https://www.synapse.org/'
const PORTAL_VERSIONS_PATH = 'Portal/versions'
const LOG_LABEL_ERROR = 'swc_module_load_error'
const LOG_LABEL_INSPECTION = 'swc_module_load_error_inspection'
const LOG_LABEL_RECOVERY = 'swc_module_load_recovery'

function getRepoEndpoint() {
  try {
    const override =
      window.SRC_OVERRIDE_ENDPOINT_CONFIG &&
      window.SRC_OVERRIDE_ENDPOINT_CONFIG.REPO
    return override || PRODUCTION_REPO_ENDPOINT
  } catch {
    return PRODUCTION_REPO_ENDPOINT
  }
}

function getPortalEndpoint() {
  try {
    const override =
      window.SRC_OVERRIDE_ENDPOINT_CONFIG &&
      window.SRC_OVERRIDE_ENDPOINT_CONFIG.PORTAL
    return override || PRODUCTION_PORTAL_ENDPOINT
  } catch {
    return PRODUCTION_PORTAL_ENDPOINT
  }
}

// `/repo/v1/log` doesn't require a bearer token. We still attach one when
// available so an authenticated user's failures are attributable to their
// account instead of logged anonymously. Its absence, e.g. very early in page
// life, before GWT starts `SynapseSessionManager`, is not a failure mode.
function getAccessToken() {
  try {
    return (
      (window.SynapseSessionManager &&
        window.SynapseSessionManager.getSnapshot &&
        window.SynapseSessionManager.getSnapshot().token) ||
      undefined
    )
  } catch {
    return undefined
  }
}

let versionsPromise = null

/**
 * Fetch and cache `{ portalVersion, repoVersion }`.
 */
function getVersions() {
  if (!versionsPromise) {
    versionsPromise = fetch(
      `${getPortalEndpoint().replace(/\/+$/, '')}/${PORTAL_VERSIONS_PATH}`,
      { mode: 'cors', credentials: 'omit', keepalive: true },
    )
      .then(res => res.text())
      .then(text => {
        const [portalVersion, repoVersion] = text.split(',')
        return { portalVersion, repoVersion }
      })
      .catch(() => ({}))
  }
  return versionsPromise
}

/**
 * Send one entry to the backend log service. `keepalive` lets the request
 * survive a page reload (e.g. the one `attemptRecovery` triggers) without
 * needing to be awaited first.
 */
function sendLog(label, messagePayload, stacktrace) {
  try {
    return getVersions()
      .then(versions => {
        const token = getAccessToken()
        return fetch(`${getRepoEndpoint()}${LOG_ENDPOINT_PATH}`, {
          method: 'POST',
          mode: 'cors',
          credentials: 'omit',
          keepalive: true,
          headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
          body: JSON.stringify({
            label,
            message: JSON.stringify({ ...messagePayload, ...versions }),
            stacktrace: stacktrace || '',
          }),
        })
      })
      .catch(() => {})
  } catch {
    return Promise.resolve()
  }
}

/** Build the payload reported for one failure/boundary combination. */
function buildPayload(kind, message, stack, url) {
  return {
    kind,
    message,
    // Short groupable enum for `message`; see MESSAGE_SHAPE_PATTERNS.
    messageShape: classifyMessageShape(message),
    url,
    expectedExport: extractExportName(message),
    stack,
    buildId: BUILD_ID,
    reloadAttempts: getReloadCount(),
    recoveryAttemptedThisLoad: hasAttemptedRecoveryThisLoad,
    pageUrl: window.location && window.location.href,
    referrer: document.referrer || undefined,
    userAgent: navigator.userAgent,
    timestamp: new Date().toISOString(),
  }
}

/**
 * Forensic re-fetch and its own log entry (async, best-effort). Returned so
 * callers (the recovery path) can wait for the re-fetch before navigating
 * away - a reload cancels in-flight fetches, which would silently drop it.
 */
function runInspectionAndReport(url, payload) {
  return inspectModule(url, payload.expectedExport)
    .then(inspection => {
      const timing = collectResourceTiming(url)
      const enriched = { ...payload, inspection, timing }
      // `probe()` always resolves to an object, so these defaults only
      // matter if `inspectModule` itself is ever changed to resolve without
      // one; they keep every key below present rather than throwing.
      const cached = inspection.cached || {}
      const network = inspection.network || {}
      sendLog(LOG_LABEL_INSPECTION, {
        url,
        expectedExport: payload.expectedExport,
        messageShape: payload.messageShape,
        buildId: BUILD_ID,
        cachedHasExport: cached.hasExport,
        networkHasExport: network.hasExport,
        cachedHasNamedExport: cached.hasNamedExport,
        networkHasNamedExport: network.hasNamedExport,
        cachedBytes: cached.bytes,
        networkBytes: network.bytes,
        cachedHead: cached.head,
        networkHead: network.head,
        cachedTail: cached.tail,
        networkTail: network.tail,
        cachedXCache: cached.xCache,
        cachedStatus: cached.status,
        networkStatus: network.status,
        cachedEtag: cached.etag,
        networkEtag: network.etag,
        cachedContentLength: cached.contentLength,
        networkContentLength: network.contentLength,
        cachedContentEncoding: cached.contentEncoding,
        networkContentEncoding: network.contentEncoding,
        cachedAge: cached.age,
        networkAge: network.age,
        cachedError: cached.error,
        networkError: network.error,
        fetchCount: timing.fetchCount,
        resourceTimingEntries: timing.entries,
      })
      // eslint-disable-next-line no-console
      console.error('[swc-module-load-error] inspection', enriched)
    })
    .catch(() => {})
}

function report(kind, message, stack) {
  try {
    if (!isModuleError(message)) return undefined
    if (reportCount >= MAX_REPORTS) return undefined

    const url = extractUrl(message)
    const key = `${kind}|${url || ''}|${message}`
    if (seen.has(key)) return undefined
    seen.add(key)

    const payload = buildPayload(kind, message, stack, url)

    const failureKey = `${url || ''}|${message}`
    const isFirstSightingOfFailure = !firstSeenFailures.has(failureKey)
    if (isFirstSightingOfFailure) {
      firstSeenFailures.add(failureKey)
      reportCount++
    }

    window.__swcModuleLoadErrors.push(payload)

    const loggablePayload = { ...payload }
    // Remove `stack` from the payload, put it in the `stack` argument to the log method
    delete loggablePayload.stack
    sendLog(LOG_LABEL_ERROR, loggablePayload, stack)

    // eslint-disable-next-line no-console
    console.error('[swc-module-load-error]', payload)

    if (!url || !isFirstSightingOfFailure) return undefined

    return runInspectionAndReport(url, payload)
  } catch {
    /* diagnostics must never break the app */
    return undefined
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
 * Report the recovery outcome so we can measure whether the reload actually
 * resolves incidents (vs. exhausting the cap into a visible banner), separate
 * from the raw error-volume signal, which only reflects how often the
 * underlying bug occurs, not whether this mitigation works.
 */
function pushRecoveryEvent(outcome, attempt, url) {
  sendLog(LOG_LABEL_RECOVERY, { outcome, attempt, url })
}

/**
 * Attempt to self-heal from a preload failure by reloading, bounded by
 * MAX_RELOAD_ATTEMPTS per tab session. At most one attempt is spent per
 * page load, even if several chunks fail together in the same load.
 *
 * `inspectionPromise` (the in-flight forensic re-fetch kicked off by
 * `report()`, if any) is awaited - with a timeout, since it must never
 * block recovery indefinitely - before reloading, so the navigation
 * doesn't cancel it and drop the `swc_module_load_error_inspection` log
 * entry.
 */
async function attemptRecovery(message, inspectionPromise) {
  try {
    if (hasAttemptedRecoveryThisLoad) return

    const url = extractUrl(message)
    const count = getReloadCount()

    if (count >= MAX_RELOAD_ATTEMPTS) {
      hasAttemptedRecoveryThisLoad = true
      pushRecoveryEvent('banner_shown', count, url)
      showReloadBanner()
      return
    }

    hasAttemptedRecoveryThisLoad = true

    await Promise.race([
      inspectionPromise || Promise.resolve(),
      new Promise(resolve => setTimeout(resolve, INSPECTION_TIMEOUT_MS)),
    ])

    const attempt = count + 1
    sessionStorage.setItem(RELOAD_COUNT_KEY, String(attempt))
    pushRecoveryEvent('reloaded', attempt, url)
    window.location.reload()
  } catch {
    /* diagnostics must never break the app */
  }
}

/**
 * Report a failure and, if it's a module error, attempt recovery. Shared by
 * all three listeners in `install()` below; only how `message`/`stack` get
 * pulled off the event differs between them.
 */
function handleFailure(kind, message, stack, { alwaysRecover = false } = {}) {
  const inspectionPromise = report(kind, message, stack)
  if (alwaysRecover || isModuleError(message)) {
    attemptRecovery(message, inspectionPromise)
  }
  return inspectionPromise
}

function install() {
  try {
    // Vite fires this when a dynamically imported chunk fails to load/preload.
    window.addEventListener('vite:preloadError', event => {
      const err = event && (event.payload || event.detail)
      const message = (err && err.message) || String(err) || 'vite:preloadError'
      // Vite's default handling for this event is to rethrow the original
      // error (see Vite's vite:preloadError docs) unless we call
      // preventDefault(). Only take over when we're actually about to
      // reload ourselves - if recovery is already spent for this load, or
      // the per-tab cap is exhausted (banner shown instead), let the
      // original error propagate as it did before this change; we aren't
      // superseding it with anything in that case.
      const willReload =
        !hasAttemptedRecoveryThisLoad && getReloadCount() < MAX_RELOAD_ATTEMPTS
      if (willReload && event && typeof event.preventDefault === 'function') {
        event.preventDefault()
      }
      handleFailure('vite:preloadError', message, err && err.stack, {
        alwaysRecover: true,
      })
    })

    // Module instantiation/link failures can also surface as a global error
    // event, so recovery must be attempted from here too.
    window.addEventListener(
      'error',
      event => {
        const err = event && event.error
        const message = (err && err.message) || (event && event.message) || ''
        handleFailure('error', message, err && err.stack)
      },
      true,
    )

    // Rejected dynamic import() promises that aren't otherwise handled.
    window.addEventListener('unhandledrejection', event => {
      const reason = event && event.reason
      const message =
        (reason && reason.message) || (typeof reason === 'string' ? reason : '')
      handleFailure('unhandledrejection', message, reason && reason.stack)
    })
  } catch {
    /* never break startup */
  }
}

install()
