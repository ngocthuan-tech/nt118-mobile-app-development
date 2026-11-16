package com.aicoach.data.remote;

/** Pins sensitive requests to the session that initiated them, even if the user switches accounts. */
public final class AuthenticatedSession {
    final String token;
    public AuthenticatedSession(String token) { this.token = token; }
}
