package tukanoBlobs.impl;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Cookie;
import tukanoBlobs.api.Session;
import tukanoBlobs.impl.cache.RedisCache;
import utils.auth.RequestCookies;

public class Authentication {
    static final String COOKIE_KEY = "scc:session";

    static public Session validateSession(String userId)
        throws NotAuthorizedException {
        var cookies = RequestCookies.get();
        return validateSession(cookies.get(COOKIE_KEY), userId);
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
