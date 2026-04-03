---
name: pnpm-security-audit
description: Systematically patches security vulnerabilities in this pnpm monorepo. Use this skill whenever the user wants to fix CVEs, address pnpm audit findings, update vulnerable dependencies, or patch security vulnerabilities. Trigger on phrases like "fix vulnerabilities", "patch CVEs", "address security audit", "update vulnerable deps", "investigate <package> dependency", or any mention of pnpm audit results or GHSA advisories.
---

# pnpm Security Audit & Patch Workflow

Patch security vulnerabilities one package group at a time — preferring lockfile updates over parent package updates (less likely to introduce breaking changes), and both over pnpm overrides.

## Step 1: Run the audit, triage, and initialize the doc

```bash
pnpm audit 2>&1 | tee /tmp/pnpm-audit.txt; echo "Exit: $?"
```

Capture the output to `/tmp/pnpm-audit.txt` so you can re-read specific sections without re-running the audit. The `tee` also prints to terminal so you see it live. Use the Read tool on `/tmp/pnpm-audit.txt` if you need to revisit any section — don't re-run `pnpm audit`.

**Group all findings by package name** before doing anything — multiple CVEs for the same package should be fixed together. Sort groups by severity: critical → high → moderate → low.

**Important: do all grouping and triage from this single audit run.** The full output is in your context — do not run `pnpm audit` again, do not grep its output for individual packages. Extract every package name, severity, vulnerable range, patched version, and dependency path from the output you already have. Only move on to `pnpm why` (Step 2) once you've fully triaged and presented the complete grouped summary to the user.

### Initialize DEPENDENCIES_UPDATE.md

Check whether `DEPENDENCIES_UPDATE.md` exists at the repo root. If it doesn't, create it with a header:

```markdown
# Dependency Security Updates

This file documents the investigation and resolution of security vulnerabilities found by `pnpm audit`.
```

If it does exist, append to it — don't overwrite prior work.

### Write the CVE table(s) for the current group before investigating

Before starting any investigation for a vulnerability group, append its CVE table(s) to `DEPENDENCIES_UPDATE.md` using the exact format that `pnpm audit` outputs (the box-drawing table format). If a group has multiple CVEs for the same package, include all tables together. This creates a clear record of what you're fixing before you make any changes.

Example format:

````markdown
```
┌─────────────────────┬────────────────────────────────────────────────────────┐
│ high                │ Package Name vulnerable to ...                         │
├─────────────────────┼────────────────────────────────────────────────────────┤
│ Package             │ package-name                                           │
├─────────────────────┼────────────────────────────────────────────────────────┤
│ Vulnerable versions │ <x.y.z                                                 │
├─────────────────────┼────────────────────────────────────────────────────────┤
│ Patched versions    │ >=x.y.z                                                │
├─────────────────────┼────────────────────────────────────────────────────────┤
│ Paths               │ packages__foo>bar>package-name                         │
├─────────────────────┼────────────────────────────────────────────────────────┤
│ More info           │ https://github.com/advisories/GHSA-xxxx-xxxx-xxxx      │
└─────────────────────┴────────────────────────────────────────────────────────┘
```
````

Then add your analysis block immediately after (populated as you investigate).

**After grouping and sorting: pick the highest-severity group and work Steps 2–8 for that group alone before touching any other group.** Do not investigate multiple groups in parallel — complete the full fix-verify-commit cycle for one group, then move to the next.

## Step 2: Trace all dependency chains (for the current group only)

Use the bundled investigation script — it runs `pnpm why`, fetches every immediate parent's declared range, checks whether that range covers the patched version, and looks up the latest parent to determine if a simple update will fix it:

```bash
npx tsx .claude/skills/pnpm-security-audit/scripts/investigate.ts <package> <patched-version> [vulnerable-range]
# Example:
npx tsx .claude/skills/pnpm-security-audit/scripts/investigate.ts flatted 3.4.2
```

Pass the optional `vulnerable-range` when the CVE's vulnerable versions don't start at 0 — e.g. `>=4.0.0 <4.0.4`. Without it, the script treats any version below `patched-version` as vulnerable, which produces false positives when multiple major version lines are installed and each has its own patched version. Read the `Vulnerable versions` field in the audit table: if it starts with `>=` rather than `<`, always pass the range.

The script outputs per-version status and a concrete `→ FIX:` recommendation for each parent:

- **`pnpm update <pkg> --recursive`** — parent's range already covers the patched version
- **`pnpm update <parent> --recursive`** — latest parent ships a fix or drops the dep
- **`pnpm override "<parent@ver>><pkg>": "<patched>"`** — no upstream fix; override needed

For packages where the vulnerable instance is a deep transitive dep (the script will flag "Not a direct dep"), manually run:

```bash
pnpm why <package> --recursive
pnpm info <parent>@<version> dependencies --json | grep <package>
```

## Step 3: Evaluate the fix strategy

Work through this decision tree:

### Option A — Lockfile update (best, try first)

**When:** The vulnerable package is a **direct** dep of some parent with a **flexible range** (`^`, `~`) that includes the patched version.

```bash
pnpm update <package> --recursive
pnpm why <package> --recursive   # verify patched version is now used
```

This works because pnpm re-resolves within the declared range. It does **not** work for packages locked inside a lockfile snapshot where they aren't a direct dep of the immediate parent.

### Option B — Update the parent package (preferred over overrides)

**When:** Option A fails AND a newer parent version either ships the patched dep or uses a flexible range covering it.

```bash
pnpm info <parent> versions --json
pnpm info <parent>@latest dependencies --json | grep <vulnerable-package>
```

**Before applying a parent update, check for breaking changes** — especially for major version bumps or core tools (eslint, vite, storybook, typescript-eslint):

```bash
# Check the changelog or release notes
pnpm info <parent>@latest --json | grep -E '"version"|"description"'
# For GitHub-hosted packages, check releases page for BREAKING CHANGES entries
# For major version bumps, check the migration guide before proceeding
```

If there are breaking changes, weigh them against the security risk. For dev-only deps, breaking changes are usually lower risk. For production deps or core build tools, confirm with the user before upgrading.

### Option C — pnpm override (last resort)

**When:** The dep is hard-pinned in an upstream package with no available patched parent version.

Add to root `package.json` under `pnpm.overrides`:

```json
"<package>@>=<vulnerable-start> <<patched>": "<patched-version>"
```

Use targeted syntax to limit blast radius: `"parent@version>child": "version"`.

Run `pnpm install` (not just `pnpm update`) after adding overrides.

> **`minimumReleaseAge` gotcha:** This repo has `minimumReleaseAge: 4320` (72h). Very recently published packages will be silently blocked. If an override doesn't resolve, check publish dates with `pnpm info <pkg> --json` and fall back to the previous patched version.

## Step 4: Present options when trade-offs exist

When multiple approaches are viable with different trade-offs, present them before acting:

```
For <package>@<version> via <chain>:

Option 1 (recommended): pnpm update <package> --recursive
  - Lockfile only, no package.json changes | Risk: low

Option 2: Upgrade <parent> X.Y → X.Z
  - Fixes this + removes other transitive issues
  - Breaking changes: [none found / see changelog]
  - Risk: moderate

Option 3: pnpm override
  - Guaranteed to work, adds entry to root package.json
  - Risk: low
```

If one option is clearly superior, proceed directly and explain the choice.

## Step 5: Apply the fix and verify

**Before editing any package.json, note the existing version specifier style** — exact pin (`"4.1.0"`), caret range (`"^4.1.0"`), or tilde (`"~4.1.0"`). When bumping a version for security, preserve the original style. If the original was exact-pinned, use the exact patched version, not a range. Changing pinning policy is a separate decision that should be made deliberately, not silently as a side effect of a security fix.

After applying, always confirm the vulnerable version is gone:

```bash
pnpm why <package> --recursive | grep "<package>@"
```

If the vulnerable version is still present, diagnose why:

- The dep is nested deeper than the parent you targeted → go one level up
- The semver range still satisfies the old version → check the math
- A separate path uses a different lockfile snapshot → check all chains from `pnpm why`
- `minimumReleaseAge` silently blocked the target version → use an older patched version

Repeat until only patched versions appear.

## Step 6: Document findings

Fill in the analysis block you pre-created in Step 1 (below the CVE tables for this group):

```markdown
**Analysis:**

[Number] path(s) found:

- `<path>`: [vulnerable/patched] — [brief reason]

**Fix rationale:** [Why this approach over alternatives — especially if override was needed]

**Fix applied:** [Exact command or package.json change made]

**Testing:** [Command to verify, e.g., `pnpm nx run synapse-react-client:test`]
```

Document:

- Why an override was used instead of a parent update (or vice versa)
- Any hard-pin that prevented a cleaner fix
- If `minimumReleaseAge` required an older patched version
- Whether paths are production vs. dev-only

## Step 7: Run tests

Scope tests to the impact:

- **Production dep changed** → `pnpm nx run <affected-package>:test`
- **Dev tooling dep** (eslint, vitest, rollup, storybook) → `pnpm lint` and/or a quick test run
- **CLI tool dep** (openapi-generator-cli, etc.) → run the relevant generate/build command

If tests fail, check: API breaking change in the updated package, accidentally too-broad override bumping a major version, or pre-existing failure on `main`.

## Step 8: Draft the commit message

```
fix(deps): <short description> (<GHSA IDs>)

<1-2 sentences on fix approach and any notable constraints.>
```

Example:

```
fix(deps): update flatted to 3.4.2 (GHSA-25h7-pfq9-p65f, GHSA-rf6f-7fwh-wjgh)

All consumers had flexible ranges allowing 3.4.2, so a recursive lockfile
update was sufficient — no package.json changes needed.
```

## Step 9: Move to the next group

After presenting the commit message, move to the next vulnerability group (next highest severity). Repeat from Step 2.

---

## Decision matrix

| Scenario                                                       | Fix                                 |
| -------------------------------------------------------------- | ----------------------------------- |
| Direct dep, flexible range covers patched version              | `pnpm update <pkg> --recursive`     |
| Direct dep, flexible range doesn't cover patched version       | Bump range in package.json          |
| Transitive dep, immediate parent flexible range covers patched | `pnpm update <pkg> --recursive`     |
| Transitive dep locked in lockfile snapshot                     | Check if grandparent can be updated |
| Parent latest version ships patched dep                        | Update parent package               |
| Hard-pin all the way up, no upstream fix                       | pnpm override (targeted)            |
| Override blocked by minimumReleaseAge                          | Use previous patched version        |

## pnpm lockfile behavior to keep in mind

- `pnpm update <pkg> --recursive` re-resolves within the range declared by the **immediate parent**. It cannot reach packages locked inside a snapshot.
- Overrides apply globally at install time. Targeted syntax (`parent>child`) limits scope.
- Always `pnpm install` after adding/changing overrides — `pnpm update` alone won't apply them.
- `pnpm why <pkg> --recursive` is the source of truth for what's actually installed.
