#!/bin/bash
PAQUETE="com.capiscan.app"
APK="app/build/outputs/apk/debug/Capi Scan.apk"

clear

./gradlew clean && ./gradlew assembleDebug

mv "app/build/outputs/apk/debug/app-debug.apk" "$APK"

adb push "$APK" /sdcard/Download/

# Mata procesos anteriores
adb logcat -c

# Instala la app
echo "📲 Instalando..."
adb install -r "$APK"

# Inicia la app
echo "🚀 Iniciando..."
adb shell monkey -p "$PAQUETE" 1

# Muestra logs con formato bonito
echo "📊 Logs en tiempo real (Ctrl+C para salir):"
adb logcat | grep --color=always -E "$PAQUETE|CRASH|Exception|Error"
