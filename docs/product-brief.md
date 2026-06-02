# Product brief — l’entre-deux

## What this app is

l’entre-deux is an open-source Android app that helps people stay off their
phone by interrupting autopilot behavior **before** a distracting app is
opened.

The name (French: "the in-between") describes the moment we care about: the
small gap between reaching for an app and actually using it. That gap is where
intention either reasserts itself or gets lost.

## What this app is not

- It is not a productivity suite.
- It is not a parental control app.
- It is not a surveillance or monitoring tool.
- It is not a punishment-based blocker. It does not lock, shame, or score the
  user.
- It is not a habit tracker with streaks, badges, or rewards.

## Who it is for

People who already know they pick up their phone too often and want a calm,
private tool that helps them be a little more intentional. The user is treated
as an adult making their own choices — never as a problem to be solved.

## Core insight

Most distracting phone use is autopilot, not deliberate choice. Users do not
need more information about how bad their habit is; they need a small
interruption at the exact moment of the reach, so they can choose again.

## Core experience

1. The user picks the apps that pull them in (e.g. social, news, video).
2. l’entre-deux offers to add a home-screen shortcut for each chosen app.
   The shortcut looks and feels like the original icon but routes through
   the pause flow first. The user replaces the original icon with this
   shortcut on their home screen. From that point on, every tap on that
   icon goes through the pause.
3. When the user taps one of those shortcuts, l’entre-deux shows a short,
   non-judgmental pause flow.
4. The user names their intention in one tap:
   - "I need this for one specific task"
   - "I am checking something briefly"
   - "I opened this automatically"
5. They proceed to the app.
6. Later, the user can open a private, local-only reflection screen showing
   patterns: which intentions, which apps, which times of day. No scores. No
   shame.

## What this approach does and does not catch

The shortcut route catches every tap that goes through the replaced home-screen
icon. It does **not** catch launches from notifications, the recent-apps
switcher, deep links from other apps, or any icon the user did not replace.

This is a deliberate trade-off. No special permissions are needed, and the
app cannot see anything the user did not explicitly route through it. We are
transparent about this limitation and trust the user to decide whether the
partial coverage is useful.

If real users tell us this misses too many autopilot reaches, a future version
may offer an opt-in, more invasive interception path. See
[`docs/permissions-and-risks.md`](permissions-and-risks.md) for the technical
options and their costs.

## Differentiation

There are many blockers and digital wellbeing tools. l’entre-deux is different
because:

1. **Intentional pause, not block.** The pause is friction, not a wall.
2. **Intention selection.** Naming the intent is the core therapeutic step —
   and, since 0.7.0, the *only* thing the pause asks. A calm breathing
   animation accompanies it.
3. **Lapse-friendly.** The pause never punishes; the user can always proceed.
4. **Privacy and trust by construction.** Open source, offline-first, no
   runtime permissions, no accounts, no tracking.

## Tone

Calm, plainspoken, respectful. Never clinical, never preachy, never cute.
Closer to a thoughtful note from a friend than to a wellness app.

## Success criteria (qualitative)

- A user can describe what the app does in one sentence.
- A user trusts the app with their phone usage data because they can see, in
  the source and on their device, that nothing leaves it.
- A user reports that the pause itself — independently of any analytics —
  changed how often they open distracting apps.
- A new contributor can read the docs and submit a meaningful PR within an
  hour.
