package utils.auth;

import jakarta.ws.rs.core.Cookie;

public class ResponseCookies {

    private static final ThreadLocal<Cookie>
        responseCookiesThreadLocal = new ThreadLocal<>();

    public static void set(Cookie cookie) {
        responseCookiesThreadLocal.set(cookie);
    }

    public static Cookie get() {
        return responseCookiesThreadLocal.get();
    }

    public static void clear() { responseCookiesThreadLocal.remove(); }
}
