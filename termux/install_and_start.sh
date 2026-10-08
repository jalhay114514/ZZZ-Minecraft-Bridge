#!/data/data/com.termux/files/usr/bin/bash
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
bash "$ROOT/termux/install.sh"
bash "$ROOT/termux/check_android.sh"
exec bash "$ROOT/termux/start_bridge.sh"
