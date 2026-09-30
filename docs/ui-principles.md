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
- Total interaction to proceed: tap the intention that fits (1 tap) — naming
  it is what opens the app; there is no separate confirm button. A slow
  breathing animation gives the moment room without adding a single thing to
  read or decide.
- Designed to be readable and tappable in low-attention contexts (one-
  handed, in a queue, half-distracted).
- No "level up" decorations or progress rewards.
- **Intentional variation, not gamification.** The pause deliberately
  rotates a short signed epigraph, and shuffles the order of its four
  action buttons (the three intention choices and the get-out button) so it
  can't be dismissed from pure muscle memory — the whole point is to make
  the person look. The rest of the screen stays put. This is *not* a dark
  pattern: both proceeding and leaving are always present and clearly
  labelled, and neither is ever hidden, disabled-by-trickery, or disguised
  as the other. Don't "fix" the moving buttons — the movement is the
  feature.
- **Answer back, briefly.** Choosing an intention is acknowledged: ink
  fills the answer from the finger, one haptic tick, the aura exhales, then
  the app opens (240 ms). "Not now" disperses the aura without a haptic.
  Nothing longer, nothing celebratory.
- **Don't ask twice for one visit.** Reopening the same app within two
  minutes of proceeding goes straight through (on by default, a switch in
  Settings). It is not logged, since it isn't a new reach.
- **The pause is its own window.** Pinned shortcuts open a small pause
  activity directly, so its first frame is the pause and it never lingers
  in Recents.

## 5. Minimum surface

- Bottom nav with at most 3 destinations: **Apps / Reflection / Settings**.
- The chosen apps live on Apps, with whether each is pinned. Once they are
  pinned, the app should say plainly that it won't need to be opened day
  to day.
- Selection, onboarding, and pause are flows, not destinations.

## 6. Accessibility

- All interactive elements have content descriptions.
- Touch targets ≥ 48dp.
- Color is never the only signal. Text contrast meets WCAG AA.
- Respect system font scaling up to large accessibility sizes. On the
  pause, answers grow with their text, the aura steps aside when there is
  no room, and the screen scrolls only as a last resort.
- With system animations turned off, the aura stands still and the
  acknowledgement is instant.
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
  journey!" Reflection speaks in plain observations ("Most came in the
  evening."), names something only when it is clear (a tie names
  nothing), and draws every pause the same.
- Zero selected apps: a one-line, factual hint of how to add one.
- Errors: state what happened in plain words, offer one obvious next step.

## 9. Visual identity: Papier & encre

- **Default look: Papier & encre.** Paper (`#F2EEE6`), blue-black ink
  (`#1D2433`) and one accent, fountain-pen blue (`#2B4C9B`). At night the
  paper turns warm black (`#15130F`) and the accent candlelight
  (`#E8C68A`). Neutrals lean warm, toward the launcher icon's umber.
- **Material look, kept as an option** in Settings: wallpaper-based dynamic
  colour and the system font, exactly the 1.0.x look. New behaviour (short
  answers, acknowledgement, fixes) applies to both looks.
- **Typeface:** Spectral (Production Type, Paris; SIL OFL 1.1), bundled and
  subset to Latin, for the app's voice: titles, the pause question and
  answers, epigraphs. Body and label text stay in the system sans. No
  downloadable fonts (they depend on Play Services).
- **Surfaces:** hairline outlines over filled slabs; at most one ink-filled
  element per screen.
- **Paris by the hour:** in light mode the Papier pause tints itself by
  local time: paper from 6 to 18 h, l'heure bleue until 22 h, candlelight
  after. Dark mode is always night. A switch in Settings turns it off.
- **Motion:** fades and gentle rises only. Screen changes 220 ms, pause
  acknowledgement 240 ms, breath 10 s.
- **Haptics:** one tick, only when an intention is named.
- Iconography: Material Symbols, outlined weight, plus a few local vector
  marks (the dial of dots, the splash cluster).

## 10. What we will not ship

- Onboarding longer than 4 screens.
- Modal dialogs that interrupt the pause flow.
- Notifications of any kind.
- Any screen whose primary purpose is to make the user feel something
  about their last session.
