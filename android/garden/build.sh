#!/usr/bin/env bash
# Builds ../dist/PomodoroGarden.apk from ../../garden.html.
# Needs: JDK 8+, python3, curl, and Ubuntu/Debian packages
#   apt-get install aapt dalvik-exchange zipalign apksigner android-sdk-platform-23
set -euo pipefail
cd "$(dirname "$0")"

SDK=/usr/lib/android-sdk
ANDROID_JAR=$SDK/platforms/android-23/android.jar   # resources (aapt)
DX=$SDK/build-tools/debian/dx
KEYSTORE=../prototimer.keystore
KEY_ALIAS=prototimer
KEY_PASS=prototimer   # personal sideload key, not a store key

# Java code uses Android 8+ APIs (notification channels, exact alarms), so it compiles
# against a newer framework jar from Maven Central.
FRAMEWORK_JAR=${FRAMEWORK_JAR:-$HOME/.cache/prototimer/android-all-14.jar}
if [ ! -s "$FRAMEWORK_JAR" ]; then
  mkdir -p "$(dirname "$FRAMEWORK_JAR")"
  curl -fL --retry 4 --retry-delay 5 -o "$FRAMEWORK_JAR" \
    https://repo1.maven.org/maven2/org/robolectric/android-all/14-robolectric-10818077/android-all-14-robolectric-10818077.jar
fi

rm -rf build
mkdir -p build/gen build/classes build/assets build/dex ../dist

echo "== Assets"
cp -r assets/fonts assets/three.min.js build/assets/
python3 - <<'PY'
import re
s = open('../../garden.html').read()
fonts = open('fonts.css').read()
# Bundled fonts and Three.js so the app works offline.
s, n = re.subn(r'<link rel="preconnect"[^>]*>\s*<link rel="preconnect"[^>]*>\s*<link rel="stylesheet" href="https://fonts\.googleapis\.com[^>]*>',
               '<style>\n' + fonts + '</style>', s)
assert n == 1
s, n = re.subn(r'<script src="https://cdnjs\.cloudflare\.com/ajax/libs/three\.js/r128/three\.min\.js"></script>',
               '<script src="three.min.js"></script>', s)
assert n == 1
open('build/assets/garden.html', 'w').write(s)
PY

echo "== Resources"
aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -I "$ANDROID_JAR"

echo "== Java"
javac -nowarn -Xlint:-options -source 8 -target 8 -encoding UTF-8 \
  -bootclasspath "$FRAMEWORK_JAR:$ANDROID_JAR" -d build/classes \
  $(find src build/gen -name '*.java')

echo "== Dex"
"$DX" --dex --min-sdk-version=24 --output=build/dex/classes.dex build/classes

echo "== Package"
aapt package -f -0 arsc -M AndroidManifest.xml -S res -A build/assets -I "$ANDROID_JAR" -F build/unsigned.apk
(cd build/dex && aapt add ../unsigned.apk classes.dex >/dev/null)
zipalign -f -p 4 build/unsigned.apk build/aligned.apk

echo "== Sign"
apksigner sign --ks "$KEYSTORE" --ks-key-alias "$KEY_ALIAS" --ks-pass "pass:$KEY_PASS" \
  --key-pass "pass:$KEY_PASS" --min-sdk-version 24 --v4-signing-enabled false \
  --out ../dist/PomodoroGarden.apk build/aligned.apk
apksigner verify --min-sdk-version 24 ../dist/PomodoroGarden.apk
ls -l ../dist/PomodoroGarden.apk
