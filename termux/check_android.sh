#!/data/data/com.termux/files/usr/bin/bash
set -u
echo "=== ZZZ-MC Bridge Android check ==="
echo "Python: $(python --version 2>&1 || echo missing)"
echo "WebSockets:"
python - <<'PY'
try:
    import websockets
    print("OK:", websockets.__version__)
except Exception as e:
    print("MISSING:", e)
PY
echo
echo "Bridge files:"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
test -f "$ROOT/bridge/server.py" && echo "server.py OK" || echo "server.py MISSING"
test -f "$ROOT/config/bridge.json" && echo "config OK" || echo "config MISSING"
