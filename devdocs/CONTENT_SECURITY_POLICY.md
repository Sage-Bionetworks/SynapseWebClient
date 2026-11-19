# Content Security Policy

Every response from the web client carries a Content Security Policy (CSP) that lists the sources allowed to supply script, frames, and other content. This is the web client's definition of acceptable mobile code for NIST SP 800-53 SC-18 ([SWC-8037](https://sagebionetworks.jira.com/browse/SWC-8037)).

## Allowed sources

`src/main/java/org/sagebionetworks/web/server/servlet/filter/ContentSecurityPolicy.java` is the documented allowlist. Each entry records the source, the directives it is allowed in, the feature that needs it, and who operates it:

- `ALLOWED_SOURCES`: every stack.
- `DEV_ALLOWED_SOURCES`: added only in development mode (`-Dorg.sagebionetworks.web.client.dev.mode=true`): the GWT code server, the Vite dev server, and local repository services.

`SYNAPSE_CDN` stands for the CDN of the requested site (`https://cdn-www.synapse.org`, `cdn-staging` or `cdn-tst`; see `HtmlInjectionFilter.getCdnHost`). A directive with no allowed source is `'none'`.

No site may frame the web client, including the web client itself: both policies send `frame-ancestors 'none'`, and `XFrameOptionsFilter` sends `X-Frame-Options: DENY` for browsers that don't support `frame-ancestors`. The web client's own frames (the GWT code iframes and history frame) are `about:blank` and `javascript:` documents with no response, so `frame-ancestors` never applies to them.

Exceptions that a stricter policy cannot remove yet:

| Source                                            | Why                                                                                                                                                                                                                 |
| ------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `script-src 'unsafe-inline'`                      | The GWT `xsiframe` linker installs the compiled code and runAsync fragments as inline scripts. Trusted users' HTML and notebook previews render in `srcdoc` iframes, which inherit the page's policy.               |
| `script-src 'unsafe-eval'`                        | GWT-RPC (stream version 7) decodes responses with `eval`. `@rjsf/validator-ajv8` compiles JSON schemas into functions. jquery.tablesorter, the plotly full bundle, and the Google Maps JavaScript API also need it. |
| `style-src 'unsafe-inline'`                       | MUI/Emotion, plotly and GWT `<ui:style>` insert `<style>` elements. KaTeX, sanitized wiki HTML and GWT-built HTML use `style` attributes, which nonces can't cover.                                                 |
| `img-src *`, `media-src https:`                   | Wiki markdown and file previews can show images and video from any host. Display-only content; `script-src` is unaffected.                                                                                          |
| `connect-src https:`                              | Uploads and downloads go to user-configured external object stores, which can be any host.                                                                                                                          |
| `wss://*.execute-api.us-east-1.amazonaws.com`     | The Synapse Grid websocket API is created per stack release, so its host changes.                                                                                                                                   |
| `https://*.synapse.org`, `https://*.sagebase.org` | `frame-src` only: the External Website widget accepts any Synapse or Sage site (`ShinySiteWidget`).                                                                                                                 |

No script may come from a wildcard host, `s3.amazonaws.com`, `*.sagebase.org` (Synapse file storage), `*.googleapis.com` other than `maps.googleapis.com`, `data:` or `blob:`. `ContentSecurityPolicyFilterTest` pins the exact `script-src` list.

## How it is delivered

`ContentSecurityPolicyFilter` (all paths) sets three headers:

- `Content-Security-Policy`: the enforced policy. It is the permissive policy formerly in the `Portal.html` `<meta>` tag (every host in `default-src`), without development hosts outside development mode.
- `Content-Security-Policy-Report-Only`: the policy built from the allowlist. Browsers report violations but don't block anything.
- `Reporting-Endpoints`: names `/Portal/csp-report`. Both policies report there through `report-to`, with `report-uri` as the fallback. `CSPReportServlet` logs each report to CloudWatch; the report's `disposition` is `enforce` or `report`.

Once the Report-Only reports have been reviewed, the allowlist policy replaces the enforced one.

## Adding a source

1. Add an `AllowedSource` to `ContentSecurityPolicy` with the narrowest directive and exact host, its purpose, and its owner. Avoid wildcards.
2. Until the allowlist policy is enforced, the enforced policy must allow the source too: add the host to `ENFORCED_DEFAULT_SRC` (or `ENFORCED_CONNECT_SRC`) in `ContentSecurityPolicyFilter`, or the browser blocks it. `ContentSecurityPolicyFilterTest.enforcedPolicyAllowsEverythingTheAllowlistAllows` fails if you don't.
3. Load the affected page. The browser console reports anything either policy blocks.
