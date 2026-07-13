# Design

## Source of truth
- Status: Active
- Last refreshed: 2026-07-13
- Primary product surfaces: Android Compose and iOS SwiftUI mobile applications
- Evidence reviewed: `Client/iOS/Sources/Shared/Theme/AppTheme.swift`, all views under `Client/iOS/Sources/Features`, Android theme and presentation components under `Client/AOS/app/src/main/java/com/woowacourse/runpamine`

## Brand
- Personality: energetic, friendly, clear, and lightweight.
- Trust signals: consistent Pretendard typography, restrained blue emphasis, predictable control sizing.
- Avoid: Android-only Material defaults that visibly change the iOS-authored hierarchy.

## Product goals
- Goals: keep the Android experience visually equivalent to the current iOS implementation.
- Non-goals: platform-specific redesign or a new component library.
- Success signals: matching hierarchy, typography weight, spacing, control dimensions, and loading geometry.

## Personas and jobs
- Primary personas: runners who track activity and participate in teams.
- User jobs: start and review runs, inspect team activity and rankings, manage account/team state.
- Key contexts of use: one-handed mobile use, outdoors, quick scanning.

## Information architecture
- Primary navigation: Home, Team, Record, Ranking with account access from Home.
- Core routes/screens: authentication, onboarding, home, team, record, ranking, running, account, team forms.
- Content hierarchy: primary metric or task first, supporting controls second, detail lists last.

## Design principles
- iOS SwiftUI is the visual source of truth; Android follows its explicit values.
- Preserve native behavior and accessibility while matching visible geometry.
- Prefer screen-level corrections over changing global tokens that already match.

## Visual language
- Color: use existing Runpamine palette and iOS `AppTheme.Colors` equivalents.
- Typography: Pretendard; match every explicit iOS size and weight. Brand screens use the bundled brand font.
- Spacing/layout rhythm: use the exact SwiftUI padding, spacing, and fixed-frame values where present.
- Shape/radius/elevation: mirror iOS continuous-rounded dimensions with Compose rounded shapes.
- Motion: short 0.16–0.18 second state transitions where implemented.
- Imagery/iconography: reuse matching assets; keep icon visual size separate from touch target.

## Components
- Existing components to reuse: `ScreenTopBar`, `BottomButton`, shared typography, skeleton boxes.
- New/changed components: screen-specific cards and rows when iOS variants differ.
- Variants and states: loaded, cached-loading, skeleton, empty, error, disabled.
- Token/component ownership: global values in theme; exceptional dimensions beside their screen component.

## Accessibility
- Target standard: retain Compose semantics and minimum 44–48dp touch targets.
- Keyboard/focus behavior: preserve current form navigation and validation.
- Contrast/readability: follow existing iOS palette and avoid reducing contrast.
- Screen-reader semantics: keep meaningful content descriptions and roles.
- Reduced motion and sensory considerations: no essential information conveyed only through animation.

## Responsive behavior
- Supported breakpoints/devices: phone layouts, including compact widths.
- Layout adaptations: flexible text widths and minimum scale/ellipsis equivalents for long content.
- Touch/hover differences: touch targets may exceed visual icon dimensions.

## Interaction states
- Loading: skeleton geometry matches final content to prevent layout shift.
- Empty: retain the iOS hierarchy and action prominence.
- Error: show readable inline recovery without replacing cached content when available.
- Success: update the current screen and invalidate affected caches.
- Disabled: reduce emphasis while preserving readable labels.
- Offline/slow network: show cached tab content while refreshing.

## Content voice
- Tone: concise, encouraging Korean.
- Terminology: use the same labels on both platforms.
- Microcopy rules: do not introduce Android-only wording.

## Implementation constraints
- Framework/styling system: Jetpack Compose on Android, SwiftUI as reference.
- Design-token constraints: do not broadly alter `Type.kt` for local mismatches.
- Performance constraints: retain tab caches and ignore stale network responses.
- Compatibility constraints: Android touch/accessibility behavior remains native.
- Test/screenshot expectations: formatting, unit tests, debug build, then device screenshot review when available.

## Open questions
- [ ] Confirm final device screenshot parity on a 393pt-wide iPhone reference and equivalent Android emulator.
