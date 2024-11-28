package tukano.impl;

import jakarta.ws.rs.Path;

public class Functions {
    @Path("/stats")
    public static String stats() {
        return "Hello, World!";
    }
}
