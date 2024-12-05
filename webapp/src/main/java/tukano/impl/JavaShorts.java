package tukano.impl;

import static java.lang.String.format;
import static tukano.api.Result.ErrorCode.BAD_REQUEST;
import static tukano.api.Result.ErrorCode.FORBIDDEN;
import static tukano.api.Result.error;
import static tukano.api.Result.errorOrResult;
import static tukano.api.Result.errorOrValue;
import static tukano.api.Result.errorOrVoid;
import static tukano.api.Result.ok;
import static utils.DB.getOne;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import tukano.api.Blobs;
import tukano.api.Result;
import tukano.api.Short;
import tukano.api.Shorts;
import tukano.api.User;
import tukano.impl.cache.RedisCache;
import tukano.impl.data.Following;
import tukano.impl.data.Likes;
import tukano.impl.data.Stats;
import tukano.impl.rest.TukanoRestServer;
import utils.DB;

public class JavaShorts implements Shorts {

    private static Logger Log = Logger.getLogger(JavaShorts.class.getName());

    private static Shorts instance;

    synchronized public static Shorts getInstance() {
        if (instance == null)
            instance = new JavaShorts();
        return instance;
    }

    private JavaShorts() {}

    @Override
    public Result<Short> createShort(String userId, String password) {
        Log.info(()
                     -> format("createShort : userId = %s, pwd = %s\n", userId,
                               password));

        return errorOrResult(okUser(userId, password), user -> {
            var shortId = format("%s+%s", userId, UUID.randomUUID());
            var blobUrl = format("%s/%s/%s", TukanoRestServer.serverURI,
                                 Blobs.NAME, shortId);
            var shrt = new Short(shortId, userId, blobUrl);

            return errorOrValue(DB.insertOne(shrt),
                                s -> s.copyWithLikes_Views_And_Token(0, 0));
        });
    }

    @Override
    public Result<Short> getShort(String shortId) {
        Log.info(() -> format("getShort : shortId = %s\n", shortId));

        if (shortId == null)
            return error(BAD_REQUEST);

        var likesQuery = format("SELECT count(*) FROM Likes l WHERE l.shortId = '%s'", shortId);
		var likes = DB.sql(likesQuery, Long.class);

        var viewsQuery = format("SELECT s.views FROM Stats s WHERE s.shortId = '%s'", shortId);
        var viewsRes = DB.sql(viewsQuery, Long.class);
        var views = viewsRes.isEmpty() ? 0L : viewsRes.get(0);

        return errorOrValue(
            getOne(shortId, Short.class),
            shrt -> shrt.copyWithLikes_Views_And_Token(likes.get(0), views));
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        Log.info(()
                     -> format("deleteShort : shortId = %s, pwd = %s\n",
                               shortId, password));

        return errorOrResult(getShort(shortId), shrt -> {
            return errorOrResult(okUser(shrt.getOwnerId(), password), user -> {
                return DB.transaction(hibernate -> {
                    hibernate.remove(shrt);

                    var query = format(
                        "DELETE FROM Likes l WHERE l.shortId = '%s'", shortId);
                    hibernate.createNativeQuery(query, Likes.class)
                        .executeUpdate();

                    if (user.getUserId().equals("admin")) {
                        deleteBlob(shortId);
                    }
                });
            });
        });
    }

    private void deleteBlob(String blobId) {
        var service = System.getenv("BLOBS_SERVICE_URL");
        var endpoint = "http://" + service + ":8080/blob-service-2/rest/blobs/" +
                       blobId;
        var uri = URI.create(endpoint);

        try {
            var cookie = Authentication.getRequestCookies();
            var cookieStr = cookie.getName() + "=" + cookie.getValue();

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
    public Result<List<String>> getShorts(String userId) {
        Log.info(() -> format("getShorts : userId = %s\n", userId));

        var query = format(
            "SELECT s.shortId FROM Short s WHERE s.ownerId = '%s'", userId);
        return errorOrValue(okUser(userId), DB.sql(query, String.class));
    }

    @Override
    public Result<Void> follow(String userId1, String userId2,
                               boolean isFollowing, String password) {
        Log.info(()
                     -> format("follow : userId1 = %s, userId2 = %s, " +
                               "isFollowing = %s, pwd = %s\n",
                               userId1, userId2, isFollowing, password));

        return errorOrResult(okUser(userId1, password), user -> {
            var f = new Following(userId1, userId2);
            return errorOrVoid(okUser(userId2),
                               isFollowing ? DB.insertOne(f) : DB.deleteOne(f));
        });
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        Log.info(()
                     -> format("followers : userId = %s, pwd = %s\n", userId,
                               password));

        var query =
            format("SELECT f.follower FROM Following f WHERE f.followee = '%s'",
                   userId);
        return errorOrValue(okUser(userId, password),
                            DB.sql(query, String.class));
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked,
                             String password) {
        Log.info(()
                     -> format("like : shortId = %s, userId = %s, isLiked = " +
                               "%s, pwd = %s\n",
                               shortId, userId, isLiked, password));

        return errorOrResult(getShort(shortId), shrt -> {
            var l = new Likes(userId, shortId, shrt.getOwnerId());
            return errorOrVoid(okUser(userId, password),
                               isLiked ? DB.insertOne(l) : DB.deleteOne(l));
        });
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        Log.info(()
                     -> format("likes : shortId = %s, pwd = %s\n", shortId,
                               password));

        return errorOrResult(getShort(shortId), shrt -> {
            var query = format(
                "SELECT l.userId FROM Likes l WHERE l.shortId = '%s'", shortId);

            return errorOrValue(okUser(shrt.getOwnerId(), password),
                                DB.sql(query, String.class));
        });
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        Log.info(()
                     -> format("getFeed : userId = %s, pwd = %s\n", userId,
                               password));

        final var QUERY_FMT = """
                SELECT s.shortId, s.timestamp FROM Short s WHERE s.ownerId = '%s'
                UNION
                SELECT s.shortId, s.timestamp FROM Short s
                    JOIN Following f ON f.followee = s.ownerId
                        WHERE f.follower = '%s'
                ORDER BY timestamp DESC """;

                  return errorOrValue(
                      okUser(userId, password),
                      DB.sql(format(QUERY_FMT, userId, userId), String.class));
    }

    protected Result<User> okUser(String userId, String pwd) {
        return JavaUsers.getInstance().getUser(userId, pwd);
    }

    private Result<Void> okUser(String userId) {
        var res = okUser(userId, "");
        if (res.error() == FORBIDDEN)
            return ok();
        else
            return error(res.error());
    }

    @Override
    public Result<Void> deleteAllShorts(String userId, String password,
                                        String token) {
        Log.info(()
                     -> format("deleteAllShorts : userId = %s, password = " +
                               "%s, token = %s\n",
                               userId, password, token));

        if (!Token.isValid(token, userId))
            return error(FORBIDDEN);

        var invalidIdRes =
            DB.transaction(hibernate -> {
                  // delete shorts
                  var query1 = format("DELETE FROM Short s WHERE s.ownerId = " +
                                      "'%s' RETURNING s.shortId",
                                      userId);
                  var shortIds =
                      hibernate.createNativeQuery(query1, String.class)
                          .getResultStream()
                          .map(id -> "short:" + id)
                          .collect(Collectors.toList());

                  // delete follows
                  var query2 = format("DELETE FROM Following f WHERE " +
                                      "f.follower = '%s' OR f.followee = '%s'",
                                      userId, userId);
                  hibernate.createNativeQuery(query2, Following.class)
                      .executeUpdate();

                  // delete likes
                  var query3 = format("DELETE FROM Likes l WHERE l.ownerId = " +
                                      "'%s' OR l.userId = '%s'",
                                      userId, userId);
                  hibernate.createNativeQuery(query3, Likes.class)
                      .executeUpdate();

                  // delete stats
                    var query4 = format("DELETE FROM Stats st WHERE st.shortId LIKE '%%%s%%'", userId);
                  hibernate.createNativeQuery(query4, Stats.class)
                      .executeUpdate();

                  return Result.ok(shortIds);
              });

        if (invalidIdRes.isOK()) {
            var invalidIds = invalidIdRes.value();
            if (!invalidIds.isEmpty()) {
                RedisCache.invalidate(invalidIds.toArray(String[]::new));
                System.out.println("invalidated: " + invalidIds);
            }
        }


        return Result.ok();
    }
}
