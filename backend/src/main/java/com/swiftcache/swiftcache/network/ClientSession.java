package com.swiftcache.swiftcache.network;
import java.nio.channels.SocketChannel;

public class ClientSession {
    public final SocketChannel channel;
    public final StringBuilder buffer = new StringBuilder();

    public boolean isAuthenticated = false;

    public ClientSession(SocketChannel channel) {
        this.channel = channel;
    }
}