#!/usr/bin/env python3
"""Safe application-level ZZZ event simulator.

It does not access or automate the ZZZ client.
"""
import asyncio, json, time, uuid
import websockets

URL = "ws://127.0.0.1:27861/bridge"
TOKEN = "ZMB-dev-token"

def msg(event, payload):
    return {
        "protocol":"zzz-mc-bridge","version":"0.3",
        "message_id":str(uuid.uuid4()),"timestamp":int(time.time()*1000),
        "source":"zzz","target":"bridge","type":"event",
        "event":event,"payload":payload
    }

async def main():
    async with websockets.connect(URL) as ws:
        await ws.send(json.dumps({
            "protocol":"zzz-mc-bridge","version":"0.3",
            "message_id":str(uuid.uuid4()),"timestamp":int(time.time()*1000),
            "source":"zzz","target":"bridge","type":"auth",
            "payload":{"token":TOKEN}
        }))
        print("ZZZ simulator connected.")
        await asyncio.sleep(1)
        await ws.send(json.dumps(msg("zzz.character.switch",
                                      {"character":"demo_character"})))
        await asyncio.sleep(2)
        await ws.send(json.dumps(msg("zzz.combat.start",
                                      {"mode":"demo","difficulty":1})))
        await asyncio.sleep(3)
        await ws.send(json.dumps(msg("zzz.combat.end",
                                      {"result":"clear","score":100})))
        print("Demo events sent.")

if __name__ == "__main__":
    asyncio.run(main())
