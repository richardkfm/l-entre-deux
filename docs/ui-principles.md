# UI principles

The interface is a tool for noticing, not a stage for the app to perform on.
Every screen should feel like the app is getting out of the user’s way.

## 1. Calm over clever

- Plain language. No emoji in labels, no exclamation marks, no
  encouragements ("You got this!"), no scolding.
- Generous whitespace. One primary action per screen.
- Default to the system theme; never override against the user’s system
  setting unless they ask.
- Animations are short and subtle. Nothing bouncy, nothing celebratory.

## 2. Respect the user

- No streak counters, badges, points, percentages, or comparisons.
- Every pause is shown the same in the log, proceeded or backed out: no
  color coding designed to provoke.
- The pause flow always has a visible, non-hidden way to back out. Friction
  is intentional; coercion is not.
- Never block the user from their own device.

## 3. Honest copy

- Tell the user what the app actually does, in the words a non-technical
  friend would use.
- When the app needs a permission or capability, explain in one sentence
  what it lets the app see and what the app will do with it. Then explain
  what the app won’t do.
- Don’t use the word "smart." Don’t use the word "AI." Don’t promise
  outcomes. The app helps; it does not fix.

## 4. The pause flow is the product

- Reachable in ≤ 2 taps from any path that opens a watched app.
- Total interaction to proceed: pick an intention (1 tap), confirm (1 tap).
  Two taps. A slow breathing animation gives the moment room without adding
  a single thing to read or decide.
- Designed to be readable and tappable in low-attention contexts (one-
  handed, in a queue, half-distracted).
- No "level up" decorations or progress rewards.
- **Intentional variation, not gamification.** The pause deliberately
  rotates a short reflective phrase and shuffles between a few layouts (the
  position and order of the aura, the intention cards, and the proceed /
  leave buttons) so it can't be dismissed from pure muscle memory — the
  whole point is to make the person look. This is *not* a dark pattern:
  both the proceed and leave actions are always present and clearly
  labelled, and neither is ever hidden, disabled-by-trickery, or disguised
  as the other. Don't "fix" the moving buttons — the movement is the
  feature.

## 5. Minimum surface

- Bottom nav with at most 3 destinations: **Home / Reflection / Settings**.
- The launcher grid lives on Home.
- Selection, onboarding, and pause are flows, not destinations.

## 6. Accessibility

- All interactive elements have content descriptions.
- Touch targets ≥ 48dp.
- Color is never the only signal. Text contrast meets WCAG AA.
- Respect system font scaling up to large accessibility sizes.
- Screen reader: the pause flow announces the chosen intention before
  proceeding. The breathing animation is decorative and is not announced.

## 7. Localization-readiness

- All user-facing strings live in `strings.xml` from day one. No literal
  strings in Compose composables.
- Copy is written assuming translation: no idioms that don’t travel, no
  puns, no clever tense. The product is named in French; English is the
  initial source language; French is the first translation target.

## 8. Empty states, not anxious states

- Empty reflection screen: "Nothing to look at yet." Not "Start your
  journey!"
- Zero selected apps: a one-line, factual hint of how to add one.
- Errors: state what happened in plain words, offer one obvious next step.

## 9. Visual identity (provisional)

- Material 3, dynamic color where supported.
- Type scale: stick to defaults; do not introduce a custom typeface for the
  MVP. Typeface decisions deferred until v0.5.
- Iconography: Material Symbols, outlined weight. App-icon design deferred;
  use a placeholder until the visual identity is settled.

## 10. What we will not ship

- Onboarding longer than 4 screens.
- Modal dialogs that interrupt the pause flow.
- Notifications of any kind.
- Any screen whose primary purpose is to make the user feel something
  about their last session.
