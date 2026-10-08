# ZZZ-MC Bridge Protocol v0.3

Transport: WebSocket
Default endpoint: ws://127.0.0.1:27861/bridge
Development token: ZMB-dev-token

Envelope:

{
  "protocol": "zzz-mc-bridge",
  "version": "0.3",
  "message_id": "uuid",
  "timestamp": 0,
  "source": "minecraft|zzz|bridge",
  "target": "minecraft|zzz|bridge",
  "type": "auth|auth_ack|event|state|ack|error|state.request|state.response",
  "event": "optional.event",
  "payload": {}
}

Minecraft events:
- player.join
- player.leave
- player.death
- minecraft.position
- minecraft.dimension_change
- minecraft.item

ZZZ application events:
- zzz.combat.start
- zzz.combat.end
- zzz.character.switch
- zzz.notification

v0.3 keeps the protocol application-level. It does not require or imply access to private game internals.
