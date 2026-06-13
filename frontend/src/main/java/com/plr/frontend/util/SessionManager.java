package com.plr.frontend.util;

/**
 * Singleton untuk menyimpan state sesi pengguna (JWT token, username, userId).
 */
public class SessionManager {

    // Eager initialization — thread-safe tanpa synchronized
    private static final SessionManager instance = new SessionManager();

    private String jwtToken;
    private String username;
    private Long userId;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return instance;
    }

    public void setSession(String jwtToken, String username, Long userId) {
        this.jwtToken = jwtToken;
        this.username = username;
        this.userId = userId;
    }

    public void clearSession() {
        this.jwtToken = null;
        this.username = null;
        this.userId = null;
    }

    public boolean isLoggedIn() {
        return jwtToken != null && !jwtToken.isEmpty();
    }

    public String getJwtToken() { return jwtToken; }
    public String getUsername() { return username; }
    public Long getUserId() { return userId; }

    public String getAuthorizationHeader() {
        return "Bearer " + jwtToken;
    }
}
