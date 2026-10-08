#!/usr/bin/env python3
import asyncio, json, time, uuid
from pathlib import Path
import websockets

ROOT = Path(__file__).resolve().parent.parent
CFG = json.loads((ROOT / "config" / "bridge.json").read_text(encoding="utf-8"))

HOST = CFG.get("host", "127.0.0.1")
PORT = int(CFG.get("port", 27861))
TOKEN = CFG.get("token", "ZMB-dev-token")
PATH = CFG.get("path", "/bridge")
VERSION = "0.3"

clients = set()
state = {}
seen = set()

def envelope(source, target, typ, payload=None, event=None):
    return {
        "protocol": "zzz-mc-bridge",
        "version": VERSION,
        "message_id": str(uuid.uuid4()),
        "timestamp": int(time.time() * 1000),
        "source": source,
        "target": target,
        "type": typ,
        **({"event": event} if event else {}),
        "payload": payload or {},
    }

async def send(ws, msg):
    await ws.send(json.dumps(msg, ensure_ascii=False))

async def broadcast(msg, exclude=None):
    data = json.dumps(msg, ensure_ascii=False)
    for ws in list(clients):
        if ws is not exclude:
            try:
                await ws.send(data)
            except Exception:
                pass

async def handler(ws):
    if ws.path != PATH:
        await ws.close(code=1008, reason="bad path")
        return

    authenticated = False
    clients.add(ws)
    try:
        async for raw in ws:
            try:
                msg = json.loads(raw)
            except json.JSONDecodeError:
                await send(ws, envelope("bridge", "minecraft", "error",
                                         {"message": "invalid_json"}))
                continue

            if msg.get("protocol") != "zzz-mc-bridge":
                continue

            mid = msg.get("message_id")
            if mid and mid in seen:
                continue
            if mid:
                seen.add(mid)
                if len(seen) > 5000:
                    seen.clear()

            if msg.get("type") == "auth":
                if msg.get("payload", {}).get("token") != TOKEN:
                    await send(ws, envelope("bridge", msg.get("source", "unknown"),
                                             "error", {"message": "auth_failed"}))
                    await ws.close(code=1008, reason="auth failed")
                    return
                authenticated = True
                await send(ws, envelope("bridge", msg.get("source", "unknown"),
                                         "auth_ack", {"ok": True}))
                continue

            if not authenticated:
                await send(ws, envelope("bridge", msg.get("source", "unknown"),
                                         "error", {"message": "not_authenticated"}))
                continue

            typ = msg.get("type")
            if typ in ("state", "state.response"):
                key = msg.get("event") or msg.get("source")
                state[key] = msg.get("payload", {})
                await send(ws, envelope("bridge", msg.get("source", "unknown"),
                                         "ack", {"stored": True}))
                await broadcast(msg, exclude=ws)

            elif typ == "event":
                await broadcast(msg, exclude=ws)
                await send(ws, envelope("bridge", msg.get("source", "unknown"),
                                         "ack", {"routed": True}))

            elif typ == "state.request":
                await send(ws, envelope("bridge", msg.get("source", "unknown"),
                                         "state.response", state))

            elif typ == "ping":
                await send(ws, envelope("bridge", msg.get("source", "unknown"),
                                         "ack", {"pong": True}))
    finally:
        clients.discard(ws)

async def main():
    print(f"ZZZ-MC Bridge v{VERSION}")
    print(f"Listening on ws://{HOST}:{PORT}{PATH}")
    async with websockets.serve(handler, HOST, PORT, ping_interval=20, ping_timeout=20):
        await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())
