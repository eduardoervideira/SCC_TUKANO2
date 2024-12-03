package tukano.impl;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;

import java.util.UUID;
import tukano.api.Session;
import tukano.impl.cache.RedisCache;
import utils.auth.RequestCookies;
import utils.auth.ResponseCookies;

public class Authentication {
    static final String COOKIE_KEY = "scc:session";
    private static final int MAX_COOKIE_AGE = 3600;

    public static NewCookie login(String userId) {
        String uid = UUID.randomUUID().toString();
        var cookie = new NewCookie.Builder(COOKIE_KEY)
                         .value(uid)
                         .path("/")
                         .comment("sessionid")
                         .maxAge(MAX_COOKIE_AGE)
                         // TODO secure true for azure deployment?
                         .secure(false)
                         .httpOnly(true)
                         .build();

        var key = "session:" + uid;
        RedisCache.insertOne(key, new Session(uid, userId));
        ResponseCookies.set(cookie);
        return cookie;
    }

    static public Session validateSession(String userId)
        throws NotAuthorizedException {
        var cookies = RequestCookies.get();
        return validateSession(cookies.get(COOKIE_KEY), userId);
    }

    static public Cookie getRequestCookies() {
        return RequestCookies.get().get(COOKIE_KEY);
    }

    static public Session validateSession(Cookie cookie, String userId)
        throws NotAuthorizedException {

        if (cookie == null)
            throw new NotAuthorizedException("No session initialized");

        var key = "session:" + cookie.getValue();
        var session_res = RedisCache.getOne(key, Session.class);

        if (!session_res.isOK())
            throw new NotAuthorizedException("No valid session initialized");

        var session = session_res.value();
        if (session.user() == null || session.user().length() == 0)
            throw new NotAuthorizedException("No valid session initialized");

        if (!session.user().equals(userId))
            throw new NotAuthorizedException("Invalid user : " +
                                             session.user());

        return session;
    }

    static public Session validateSession() throws NotAuthorizedException {
        var cookies = RequestCookies.get();
        var cookie = cookies.get(COOKIE_KEY);

        if (cookie == null)
            throw new NotAuthorizedException("No session initialized");

        var key = "session:" + cookie.getValue();
        var session_res = RedisCache.getOne(key, Session.class);

        if (!session_res.isOK())
            throw new NotAuthorizedException("No valid session initialized");

        var session = session_res.value();
        if (session.user() == null || session.user().length() == 0)
            throw new NotAuthorizedException("No valid session initialized");

        return session;
    }
}
