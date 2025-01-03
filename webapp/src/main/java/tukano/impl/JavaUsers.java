package tukano.impl;

import static java.lang.String.format;
import static tukano.api.Result.ErrorCode.BAD_REQUEST;
import static tukano.api.Result.ErrorCode.FORBIDDEN;
import static tukano.api.Result.error;
import static tukano.api.Result.errorOrResult;
import static tukano.api.Result.errorOrValue;
import static tukano.api.Result.ok;

import jakarta.ws.rs.core.Cookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import tukano.api.Result;
import tukano.api.User;
import tukano.api.Users;
import tukano.impl.data.Following;
import utils.DB;

public class JavaUsers implements Users {

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    private static Users instance;

    synchronized public static Users getInstance() {
        if (instance == null)
            instance = new JavaUsers();
        return instance;
    }

    private JavaUsers() {}

    @Override
    public Result<String> createUser(User user) {
        Log.info(() -> format("createUser : %s\n", user));

        if (badUserInfo(user))
            return error(BAD_REQUEST);

        return errorOrValue(DB.insertOne(user), u -> {
            String userId = u.getUserId();
            Authentication.login(userId);
            Executors.defaultThreadFactory().newThread(() -> {
                followRecommendations(userId);
            }).start();
            return userId;
        });
    }

    private void followRecommendations(String userId) {
            var f = new Following(userId, JavaFunctions.TUK_RECS);
            DB.insertOne(f);
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        Log.info(
            () -> format("getUser : userId = %s, pwd = %s\n", userId, pwd));

        if (userId == null)
            return error(BAD_REQUEST);

        var res = validatedUserOrError(DB.getOne(userId, User.class), pwd);
        if (res.isOK()) {
            Authentication.login(userId);
        }

        return res;
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User other) {
        Log.info(()
                     -> format("updateUser : userId = %s, pwd = %s, user: %s\n",
                               userId, pwd, other));

        if (badUpdateUserInfo(userId, pwd, other))
            return error(BAD_REQUEST);

        return errorOrResult(
            validatedUserOrError(DB.getOne(userId, User.class), pwd),
            user -> DB.updateOne(user.updateFrom(other)));
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        Log.info(
            () -> format("deleteUser : userId = %s, pwd = %s\n", userId, pwd));

        if (userId == null || pwd == null)
            return error(BAD_REQUEST);

        return errorOrResult(
            validatedUserOrError(DB.getOne(userId, User.class), pwd), user -> {
                // Delete user shorts and related info asynchronously in a
                // separate thread
                var cookie = Authentication.getRequestCookies();
                Executors.defaultThreadFactory()
                    .newThread(() -> {
                        JavaShorts.getInstance().deleteAllShorts(
                            userId, pwd, Token.get(userId));

                        if (user.getUserId().equals("admin")) {
                            deleteBlobs(userId, cookie);
                        }
                    })
                    .start();

                return DB.deleteOne(user);
            });
    }

    private void deleteBlobs(String userId, Cookie cookie) {
        var service = System.getenv("BLOBS_SERVICE_URL");
        // TODO clean this up somehow
        var endpoint = "http://" + service +
                       ":8080/blob-service-2/rest/blobs/" + userId + "/blobs";
        var uri = URI.create(endpoint);

        var cookieStr = cookie.getName() + "=" + cookie.getValue();
        try {
            HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", cookieStr)
                    .DELETE()
                    .build(),
                HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        Log.info(() -> format("searchUsers : patterns = %s\n", pattern));

        var query =
            format("SELECT * FROM Users u WHERE UPPER(u.userId) LIKE '%%%s%%'",
                   pattern.toUpperCase());
        var hits = DB.sql(query, User.class)
                       .stream()
                       .map(User::copyWithoutPassword)
                       .toList();

        return ok(hits);
    }

    private Result<User> validatedUserOrError(Result<User> res, String pwd) {
        if (res.isOK())
            return res.value().getPwd().equals(pwd) ? res : error(FORBIDDEN);
        else
            return res;
    }

    private boolean badUserInfo(User user) {
        return (user.userId() == null || user.pwd() == null ||
                user.displayName() == null || user.email() == null);
    }

    private boolean badUpdateUserInfo(String userId, String pwd, User info) {
        return (userId == null || pwd == null ||
                info.getUserId() != null && !userId.equals(info.getUserId()));
    }
}
