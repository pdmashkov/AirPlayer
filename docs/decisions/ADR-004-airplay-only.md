# ADR-004: AirPlay-Only, Single Build (Drop Miracast, Cast, Fire TV)

**Date:** 2026-08-19
**Status:** Accepted

---

## Context

Per [ADR-001](ADR-001-multi-protocol.md), AirPlayer originally set out to support three receiver protocols — AirPlay 2, Miracast, and Google Cast — across two product flavors, `googletv` and `firetv`.

In practice:
- Miracast and Google Cast were only ever implemented as control-plane stubs (service discovery / session negotiation). Neither ever decoded or played back real media — see `docs/spec/PROJECT_PLAN.md` Phase 6/7 status.
- The Fire TV flavor was never actually used or validated on real Fire TV hardware.
- AirPlay 2 is the only protocol in real use, and is the only one that is fully implemented end-to-end (mDNS, RTSP handshake, pairing, FairPlay decryption, mirroring, audio, photo).
- Carrying three protocols and two build flavors added ongoing build, test, settings-UI, permission, and documentation surface for functionality that provided no value to the actual use case.

## Decision

- Drop the Miracast receiver entirely (`miracast/` package, Wi-Fi Direct/WFD advertisement, RTSP control-plane, settings toggle, status card).
- Drop the Google Cast receiver entirely (`cast/` package, Cast Connect SDK dependency, Cast App ID build config, settings toggle, status card).
- Drop the Fire TV product flavor entirely. AirPlayer ships as a single universal build:
  - `applicationId` / `namespace`: `com.airplayer` (no flavor suffix)
  - `minSdk = 25` (unchanged — broad Android TV compatibility, not tied to any flavor)
- AirPlayer implements AirPlay 2 only.

## Consequences

- Smaller APK: no Cast SDK transitive dependency graph, no per-flavor duplicate builds.
- Simpler settings/UI: a single AirPlay status card and a single `airPlayEnabled` toggle instead of three.
- Fewer requested permissions: `CHANGE_WIFI_STATE`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, and `NEARBY_WIFI_DEVICES` (all Wi-Fi P2P/Miracast-only) are no longer requested.
- Less build/test/doc surface: one Gradle build variant, no flavor-specific source sets, no Cast App ID guide.
- If Fire TV or Cast/Miracast support is wanted again in the future, it should be reintroduced as its own ADR (following the ADR-001 pattern) rather than reverted wholesale — this decision does not preclude revisiting the tradeoff later.

## Alternatives Considered

1. **Keep flavors, keep protocols disabled by default** — rejected: dead code and unused dependencies still cost build time, test surface, and documentation upkeep even when disabled.
2. **Keep Fire TV flavor, drop only Miracast/Cast** — rejected: with only AirPlay left, the `googletv`/`firetv` flavor split served no purpose (it existed to gate Cast SDK availability and Fire TV's missing Google Play Services); a single build is strictly simpler.
