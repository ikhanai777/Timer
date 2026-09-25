# Proto-Timer

A countdown timer with a glowing green 7‑segment display and a "charging" bar meter
that shows progress. It's laid out for the Samsung Galaxy S25 Ultra's portrait screen
(about 412 × 915 CSS px) and scales to other phones.

## Features

- **START / STOP / RESET**: STOP pauses the timer and START resumes it. When the alarm
  is ringing, STOP silences it.
- **Charging bar**: 25 green segments. The segment currently filling pulses while the
  timer runs. In Timer Settings you can pick **Charge up** (bars fill as time passes)
  or **Drain** (bars empty as time runs out).
- **Timer Settings**: set hours, minutes and seconds with the steppers or the keyboard
  (up to 99:59:59), or tap a preset from 1 min to 1 hr. You can also turn the alarm
  sound, vibration and keep‑screen‑on on or off. Settings are saved on the device.
- **When the timer ends**: the display blinks, the bars flash, a beep plays for up to
  one minute, and the phone vibrates.
- **Accurate timing**: time is calculated from the system clock, so the timer stays
  correct when the screen is locked or the tab is in the background.
- **Installable and offline**: includes a web app manifest and a service worker, so you
  can add it to the home screen, where it opens full screen with no browser bars.

It's a single `index.html` with no build step and no dependencies.

## Put it on your phone

1. Host the folder on any HTTPS static host. For GitHub Pages: repo **Settings → Pages →
   Deploy from branch**, then pick this branch and `/ (root)`.
2. Open the URL on the S25 Ultra in Samsung Internet or Chrome.
3. Open the browser menu and tap **Add to Home screen** (or **Install app**).

To try it locally, run `python3 -m http.server 8000` in this folder and open
`http://localhost:8000`.
