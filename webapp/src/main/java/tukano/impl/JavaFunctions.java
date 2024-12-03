package tukano.impl;

import static tukano.api.Result.*;
import static tukano.api.Result.ErrorCode.FORBIDDEN;

import java.util.logging.Logger;
import tukano.api.Functions;
import tukano.api.Result;
import tukano.api.Short;
import utils.DB;

public class JavaFunctions implements Functions {

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
                    // var query = "UPDATE Short SET totalViews = totalViews + 1 "
                    //             + "WHERE shortId = :shortId";
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

    private boolean validShortId(String shortId, String token) {
        return Token.isValid(token, shortId);
    }
}
