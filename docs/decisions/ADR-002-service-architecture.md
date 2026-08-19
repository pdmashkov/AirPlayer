# ADR-002: ForegroundService Architecture

**Date:** 2026-03-23
**Status:** Accepted

---

## Context

The AirPlay receiver needs to run continuously in the background — even when the user switches to a screensaver or a different app. Android may kill background processes. A ForegroundService with a persistent notification is the correct pattern for long-running background operations in Android.

> Originally written when AirPlayer also hosted Miracast and Cast receivers (see ADR-001). Both were removed in [ADR-004](ADR-004-airplay-only.md); the ForegroundService rationale below still holds for the single AirPlay receiver.

## Decision

Implement `AirPlayerService` as an Android `ForegroundService`:
- Shows a persistent notification with status and quick actions (Stop, Restart)
- `ServiceController` provides a clean API from the UI layer to the service
- `MainActivity` binds to the service to receive state updates for the UI
- Service survives Activity lifecycle (rotation, screensaver, task switch)

## Architecture

```
MainActivity / HomeFragment
      │  bind()
      ▼
AirPlayerService (ForegroundService)
  └── AirPlayReceiver
```

## Consequences

- Requires `FOREGROUND_SERVICE` permission
- Requires a persistent notification (Android 8+ requirement for foreground services)
- Slightly more complex than a simple Activity-owned receiver
- Service can be started via `start on boot` BroadcastReceiver in the future

## Alternatives Considered

1. **Activity-bound only** — simple but dies when Activity is backgrounded.
2. **WorkManager** — for periodic work, not continuous background operation.
3. **JobScheduler** — not suitable for a persistent network server.
