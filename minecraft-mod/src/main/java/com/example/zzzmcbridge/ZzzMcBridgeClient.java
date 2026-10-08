package com.example.zzzmcbridge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZzzMcBridgeClient implements ClientModInitializer {
    private static final String URL = "ws://127.0.0.1:27861/bridge";
    private static final String TOKEN = "ZMB-dev-token";
    private static final String PROTOCOL = "zzz-mc-bridge";
    private static final String VERSION = "0.3.1";

    private final Gson gson = new Gson();
    private WebSocket socket;
    private long lastPosition = 0;
    private long lastState = 0;
    private long lastConnectAttempt = 0;
    private int reconnectSeconds = 1;
    private final AtomicBoolean connecting = new AtomicBoolean(false);

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        System.out.println("[ZZZ-MC Bridge] v" + VERSION + " loaded.");
    }

    private void tick(MinecraftClient client) {
        long now = System.currentTimeMillis();

        if (!isOpen() && now - lastConnectAttempt >= reconnectSeconds * 1000L) {
            lastConnectAttempt = now;
            connect();
        }

        if (client.player == null || !isOpen()) return;

        if (now - lastPosition >= 250) {
            lastPosition = now;
            sendPosition(client.player);
        }

        if (now - lastState >= 1000) {
            lastState = now;
            sendState(client.player);
        }
    }

    private boolean isOpen() {
        return socket != null && socket.isOutputClosed() == false;
    }

    private void connect() {
        if (!connecting.compareAndSet(false, true)) return;

        HttpClient.newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(URI.create(URL), new WebSocket.Listener() {
                @Override
                public void onOpen(WebSocket webSocket) {
                    socket = webSocket;
                    connecting.set(false);
                    reconnectSeconds = 1;
                    sendAuth();
                    System.out.println("[ZZZ-MC Bridge] connected.");
                    WebSocket.Listener.super.onOpen(webSocket);
                }

                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                    handleMessage(data.toString());
                    webSocket.request(1);
                    return null;
                }

                @Override
                public void onError(WebSocket webSocket, Throwable error) {
                    connecting.set(false);
                    reconnectSeconds = Math.min(reconnectSeconds * 2, 30);
                    System.out.println("[ZZZ-MC Bridge] websocket error: " + error.getMessage());
                }

                @Override
                public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                    connecting.set(false);
                    socket = null;
                    reconnectSeconds = Math.min(reconnectSeconds * 2, 30);
                    System.out.println("[ZZZ-MC Bridge] closed: " + statusCode + " " + reason);
                    return null;
                }
            });
    }

    private void sendAuth() {
        JsonObject payload = new JsonObject();
        payload.addProperty("token", TOKEN);
        send("auth", null, "minecraft", "bridge", payload);
    }

    private void sendPosition(ClientPlayerEntity player) {
        JsonObject p = new JsonObject();
        p.addProperty("uuid", player.getUuidAsString());
        p.addProperty("name", player.getName().getString());
        p.addProperty("x", player.getX());
        p.addProperty("y", player.getY());
        p.addProperty("z", player.getZ());
        p.addProperty("dimension", player.getWorld().getRegistryKey().getValue().toString());
        send("state", "minecraft.position", "minecraft", "bridge", p);
    }

    private void sendState(ClientPlayerEntity player) {
        JsonObject p = new JsonObject();
        p.addProperty("uuid", player.getUuidAsString());
        p.addProperty("name", player.getName().getString());
        p.addProperty("health", player.getHealth());
        p.addProperty("food", player.getHungerManager().getFoodLevel());
        send("state", "minecraft.player", "minecraft", "bridge", p);
    }

    private void sendEvent(String event, JsonObject payload) {
        send("event", event, "minecraft", "bridge", payload);
    }

    private void send(String type, String event, String source, String target, JsonObject payload) {
        if (!isOpen()) return;

        JsonObject root = new JsonObject();
        root.addProperty("protocol", PROTOCOL);
        root.addProperty("version", VERSION);
        root.addProperty("message_id", UUID.randomUUID().toString());
        root.addProperty("timestamp", System.currentTimeMillis());
        root.addProperty("source", source);
        root.addProperty("target", target);
        root.addProperty("type", type);
        if (event != null) root.addProperty("event", event);
        root.add("payload", payload == null ? new JsonObject() : payload);

        socket.sendText(gson.toJson(root), true);
    }

    private void handleMessage(String raw) {
        try {
            JsonObject msg = JsonParser.parseString(raw).getAsJsonObject();
            String type = msg.has("type") ? msg.get("type").getAsString() : "";
            String event = msg.has("event") ? msg.get("event").getAsString() : "";

            if ("auth_ack".equals(type)) {
                System.out.println("[ZZZ-MC Bridge] authenticated.");
                return;
            }

            // Reserved application-level ZZZ events.
            // Keep effects benign and local to Minecraft.
            if ("event".equals(type)) {
                if ("zzz.notification".equals(event)) {
                    System.out.println("[ZZZ-MC Bridge] ZZZ notification: " + msg.get("payload"));
                } else if ("zzz.character.switch".equals(event)) {
                    System.out.println("[ZZZ-MC Bridge] ZZZ character switch: " + msg.get("payload"));
                } else if ("zzz.combat.start".equals(event)) {
                    System.out.println("[ZZZ-MC Bridge] ZZZ combat start.");
                } else if ("zzz.combat.end".equals(event)) {
                    System.out.println("[ZZZ-MC Bridge] ZZZ combat end.");
                }
            }
        } catch (Exception e) {
            System.out.println("[ZZZ-MC Bridge] invalid message: " + e.getMessage());
        }
    }
}
