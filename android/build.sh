#!/usr/bin/env bash
# Builds dist/ProtoTimer.apk from the web pages in the repo root.
# Needs: JDK 8+, python3, and Ubuntu/Debian packages
#   apt-get install aapt dalvik-exchange zipalign apksigner android-sdk-platform-23
set -euo pipefail
cd "$(dirname "$0")"

SDK=/usr/lib/android-sdk
ANDROID_JAR=$SDK/platforms/android-23/android.jar
DX=$SDK/build-tools/debian/dx
KEYSTORE=prototimer.keystore
KEY_ALIAS=prototimer
KEY_PASS=prototimer   # personal sideload key, not a store key

rm -rf build
mkdir -p build/gen build/classes build/assets build/dex dist

echo "== Assets"
cp -r assets/fonts build/assets/
python3 - <<'PY'
import re
fonts = open('fonts.css').read()
for name in ('index.html', 'bloom.html'):
    s = open('../' + name).read()
    # Use the bundled fonts instead of Google Fonts so the app works offline.
    s, n = re.subn(r'<link rel="preconnect"[^>]*>\s*<link rel="preconnect"[^>]*>\s*<link rel="stylesheet" href="https://fonts\.googleapis\.com[^>]*>',
                   '<style>\n' + fonts + '</style>', s)
    assert n == 1, name
    # No manifest, icons or service worker inside the app.
    s = re.sub(r'<link rel="(manifest|icon|apple-touch-icon)"[^>]*>\s*', '', s)
    open('build/assets/' + name, 'w').write(s)
PY

echo "== Resources"
aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -I "$ANDROID_JAR"

echo "== Java"
javac -nowarn -Xlint:-options -source 8 -target 8 -encoding UTF-8 \
  -bootclasspath "$ANDROID_JAR" -d build/classes \
  $(find src build/gen -name '*.java')

echo "== Dex"
"$DX" --dex --min-sdk-version=24 --output=build/dex/classes.dex build/classes

echo "== Package"
aapt package -f -0 arsc -M AndroidManifest.xml -S res -A build/assets -I "$ANDROID_JAR" -F build/unsigned.apk
(cd build/dex && aapt add ../unsigned.apk classes.dex >/dev/null)
zipalign -f -p 4 build/unsigned.apk build/aligned.apk

echo "== Sign"
if [ ! -f "$KEYSTORE" ]; then
  keytool -genkeypair -keystore "$KEYSTORE" -alias "$KEY_ALIAS" -keyalg RSA -keysize 2048 \
    -validity 10000 -storepass "$KEY_PASS" -keypass "$KEY_PASS" -dname "CN=Proto-Timer"
fi
apksigner sign --ks "$KEYSTORE" --ks-key-alias "$KEY_ALIAS" --ks-pass "pass:$KEY_PASS" \
  --key-pass "pass:$KEY_PASS" --min-sdk-version 24 --v4-signing-enabled false --out dist/ProtoTimer.apk build/aligned.apk
apksigner verify --min-sdk-version 24 --print-certs dist/ProtoTimer.apk | head -2
ls -l dist/ProtoTimer.apk
