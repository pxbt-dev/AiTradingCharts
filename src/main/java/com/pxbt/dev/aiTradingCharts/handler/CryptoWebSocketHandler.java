package com.pxbt.dev.aiTradingCharts.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;

@Component
public class CryptoWebSocketHandler implements WebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(CryptoWebSocketHandler.class);
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        logger.info("🔌 NEW CLIENT CONNECTED - Session: {}, Remote: {}",
                session.getId(), session.getRemoteAddress());
        logger.info("✅ Total connected clients: {}", sessions.size());

        // Send welcome message to confirm connection
        try {
            String welcomeMsg = "{\"type\": \"welcome\", \"message\": \"Connected to AI Trading Data\", \"timestamp\": " + System.currentTimeMillis() + "}";
            session.sendMessage(new TextMessage(welcomeMsg));
            logger.debug("✅ Welcome message sent to client: {}", session.getId());
        } catch (Exception e) {
            logger.error("❌ Failed to send welcome message to client {}: {}", session.getId(), e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        logger.info("🔌 CLIENT DISCONNECTED - Session: {}, Reason: {}, Code: {}",
                session.getId(), status.getReason(), status.getCode());
        logger.info("📊 Remaining clients: {}", sessions.size());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        logger.debug("📨 CLIENT MESSAGE - Session: {}, Message: {}",
                session.getId(), payload);

        // Handle any incoming messages from clients
        if (payload.equals("ping") || payload.equals("{\"type\":\"ping\"}")) {
            try {
                String pongMsg = "{\"type\": \"pong\", \"timestamp\": " + System.currentTimeMillis() + "}";
                session.sendMessage(new TextMessage(pongMsg));
                logger.debug("✅ Sent pong response to client: {}", session.getId());
            } catch (Exception e) {
                logger.error("❌ Failed to send pong to client {}: {}", session.getId(), e.getMessage());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("💥 TRANSPORT ERROR - Session: {}, Error: {}",
                session.getId(), exception.getMessage(), exception);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * Broadcast message to all connected WebSocket clients
     */
    public void broadcast(String message) {
        if (sessions.isEmpty()) {
            logger.debug("📢 No clients connected to broadcast message");
            return;
        }

        logger.debug("📢 BROADCASTING to {} clients - Message size: {} bytes",
                sessions.size(), message.length());

        int successCount = 0;
        int errorCount = 0;
        List<WebSocketSession> closedSessions = new CopyOnWriteArrayList<>();

        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                    successCount++;
                    logger.trace("✅ Message sent to session: {}", session.getId());
                } else {
                    logger.debug("🔄 Session {} is closed, marking for removal", session.getId());
                    closedSessions.add(session);
                }
            } catch (IOException e) {
                errorCount++;
                logger.error("❌ Failed to send message to session {}: {}",
                        session.getId(), e.getMessage());
                closedSessions.add(session);
            }
        }

        // Remove closed sessions
        if (!closedSessions.isEmpty()) {
            sessions.removeAll(closedSessions);
            logger.info("🧹 Cleaned up {} closed sessions", closedSessions.size());
        }

        logger.info("📢 BROADCAST RESULTS - Success: {}, Errors: {}, Total Clients: {}",
                successCount, errorCount, sessions.size());
    }

    /**
     * Get the number of currently connected clients
     */
    public int getClientCount() {
        return sessions.size();
    }

    /**
     * Get list of connected session IDs for debugging
     */
    public List<String> getConnectedSessionIds() {
        return sessions.stream()
                .map(WebSocketSession::getId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Send a message to a specific client session
     */
    public void sendToSession(String sessionId, String message) {
        for (WebSocketSession session : sessions) {
            if (session.getId().equals(sessionId) && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                    logger.debug("✅ Message sent to specific session: {}", sessionId);
                    return;
                } catch (IOException e) {
                    logger.error("❌ Failed to send message to specific session {}: {}",
                            sessionId, e.getMessage());
                }
            }
        }
        logger.warn("⚠️ Session {} not found or closed for direct message", sessionId);
    }

    /**
     * Close all WebSocket connections (for cleanup)
     */
    public void closeAllConnections() {
        logger.info("🛑 Closing all WebSocket connections ({} clients)", sessions.size());
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.close(CloseStatus.NORMAL);
                    logger.debug("✅ Closed session: {}", session.getId());
                }
            } catch (IOException e) {
                logger.error("❌ Error closing session {}: {}", session.getId(), e.getMessage());
            }
        }
        sessions.clear();
        logger.info("✅ All WebSocket connections closed");
    }
}