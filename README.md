# Proto-Timer

A countdown timer with a glowing 7‑segment display and a "charging" battery meter
that shows progress. It's laid out for the Samsung Galaxy S25 Ultra's portrait screen
(about 412 × 915 CSS px) and scales to other phones.

- `index.html`: version 2 (current)
- `bloom.html`: Bloom variation (a flower grows instead of the bars)
- `v1.html`: version 1, kept for reference

## Bloom variation

`bloom.html` has every version 2 feature, but a flower inside a glowing terrarium
replaces the bar meter:

1. **Roots** spread into the soil.
2. A **sprout** breaks the surface, and the stem climbs with a glowing growth tip.
3. **Leaves** open one by one up the stem.
4. A **bud** forms at the top.
5. The petals open into full **bloom** as the timer reaches zero. The flower then pulses and
   releases pollen while the alarm rings.

Stage labels beside the glass light up as each stage is reached. The glass turns amber
near the end, red in the final seconds, and blue during rest rounds. In stopwatch mode, the
flower takes one hour to reach full bloom. The Look tab adds a **flower colour** (pink,
gold, violet, white, red or sky) on top of the glow colour. Bloom saves its settings
separately, starting from your version 2 choices.

## Version 2

### New features
- **Intervals**: repeat the time limit for up to 20 rounds, with an optional rest between
  rounds. The panel shows `REST` and `RND 2/5`, and the display and bars turn blue while
  you rest. A chime and a short vibration mark each change.
- **Stopwatch mode**: counts up to 99:59:59. The bars fill once every minute.
- **Quick adjust**: the −1m, +1m and +5m buttons change the running timer. When the alarm
  is ringing, +1m or +5m starts a snooze.
- **Status line**: shows when the timer will end (clock time) and the percentage done.
  While paused it shows `PAUSED`. After the alarm it shows overtime, for example `+00:01:12`.
- **Warning colours**: the meter turns amber when 15% is left, and the display and meter
  turn red in the last 10 seconds.
- **My presets**: name and save your own time limits, including rounds and rest. The
  quick presets now go from 1 min to 1 hr and include 25 min.
- **Sound**: choose the Beep, Chime or Pulse alarm, set the volume, play a test, and turn
  on soft ticks for the last 5 seconds.
- **Glow colour**: green, amber, cyan, red, violet or white.
- **Keyboard**: <kbd>Space</kbd> starts or stops, <kbd>R</kbd> resets, <kbd>S</kbd> opens
  settings, and <kbd>+</kbd> / <kbd>−</kbd> add or remove one minute.
- You can tap the display to open settings while the timer is stopped.

### Look
- The meter looks like a battery, with a terminal cap and a 0–100 scale.
- Buttons have icons, press down like physical keys, and have status LEDs. The START
  button says RESUME while paused.
- The screen has faint scanlines. Labels use Chakra Petch and readouts use Share Tech Mono.
- The settings sheet has four tabs: Time, Presets, Sound and Look.

Settings are saved on the device. Choices you made in version 1 carry over.

## Android app (APK)

`android/dist/ProtoTimer.apk` is a ready-to-install Android app (about 150 KB, works fully
offline). It adds two icons to your home screen: **Proto-Timer** (version 2) and
**Timer Bloom**.

To install it on the S25 Ultra:
1. Copy the APK to the phone, or open this repo on the phone and download
   `android/dist/ProtoTimer.apk`.
2. Tap the file. If Android asks, allow your browser or file manager to
   **Install unknown apps**.
3. Tap **Install**. Play Protect may warn that the app is unrecognised because it isn't
   from the Play Store. Choose **More details → Install anyway**.

In the app:
- **Keep screen on** holds the screen awake while a timer runs, and vibration uses the
  phone's motor.
- **Back** closes the settings sheet. Otherwise it moves the app to the background without
  stopping the timer.
- The alarm plays at **media volume**. Keep the app open while a timer runs, because
  Android can delay the alarm if the app has been in the background for a long time.

To rebuild it after changing `index.html` or `bloom.html`, run `android/build.sh`. The
script lists the Ubuntu packages it needs. The app wraps the same pages in a WebView. The
fonts (Chakra Petch and Share Tech Mono, SIL Open Font License) are bundled in
`android/assets/fonts`.
`android/prototimer.keystore` is the app's signing key. Keep it, because updates only
install over the existing app when they're signed with the same key.

## Put it on your phone as a web app

1. Host the folder on any HTTPS static host. For GitHub Pages: repo **Settings → Pages →
   Deploy from branch**, then pick this branch and `/ (root)`.
2. Open the URL on the S25 Ultra in Samsung Internet or Chrome.
3. Open the browser menu and tap **Add to Home screen** (or **Install app**). It opens
   full screen and works offline.

To try it locally, run `python3 -m http.server 8000` in this folder and open
`http://localhost:8000`.
