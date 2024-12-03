package tukano.impl;

import static tukano.api.Result.*;
import static tukano.api.Result.ErrorCode.FORBIDDEN;

import java.util.logging.Logger;

import tukano.api.Functions;
import tukano.api.Result;
import tukano.api.Short;
import utils.DB;

public class JavaFunctions implements Functions {
    public static final String TUK_RECS = "tukRecs";

    private static Functions instance;
    private static Logger Log = Logger.getLogger(JavaFunctions.class.getName());

    public String baseURI;

    synchronized public static Functions getInstance() {
        if (instance == null)
            instance = new JavaFunctions();
        return instance;
    }

    private JavaFunctions() {}

    @Override
    public Result<String> countView(String shortId, String token) {
        Log.info(() -> "Increment stats for shortId=\n" + shortId);

        if (!validShortId(shortId, token))
            return error(FORBIDDEN);

        return errorOrResult(
            JavaShorts.getInstance().getShort(shortId), shrt -> {
                return DB.transaction(hibernate -> {
                    var query = """
                            INSERT INTO Stats (shortId, views) 
                            VALUES (:shortId, 1)
                            ON CONFLICT (shortId) 
                            DO UPDATE SET views = Stats.views + 1
                        """;

                    hibernate.createNativeMutationQuery(query)
                        .setParameter("shortId", shortId)
                        .executeUpdate();
                    return Result.ok(shortId);
                });
            });
    }

    @Override
    public Result<Void> tukanoRecommends() {
        System.out.println("\n\nTUKANO RECOMMENDS");
        
        var query = """
                SELECT s.* FROM Short s
                WHERE s.shortId IN (
                    (
                        SELECT shortId FROM Stats
                        ORDER BY views DESC
                        LIMIT 5
                    )
                        UNION
                    (
                        SELECT l.shortId FROM Likes l
                        GROUP BY l.shortId
                        ORDER BY COUNT(*) DESC
                        LIMIT 5
                    )
                )
        """;
        var shorts = DB.sql(query, Short.class);
        if(shorts.isEmpty()) {
            return Result.error(ErrorCode.NOT_FOUND);
        }

        DB.transaction(hibernate -> {
            var deleteQuery = String.format("DELETE FROM Short s WHERE s.ownerId = '%s'", TUK_RECS);
            hibernate.createNativeMutationQuery(deleteQuery)
                    .executeUpdate();

            for (Short s : shorts) {
                s.setOwnerId(JavaFunctions.TUK_RECS);
                s.setShortId(JavaFunctions.TUK_RECS + "_" + s.getShortId());

                hibernate.persist(s);
            }
        });

        return Result.ok();
    }

    private boolean validShortId(String shortId, String token) {
        return Token.isValid(token, shortId);
    }
}
