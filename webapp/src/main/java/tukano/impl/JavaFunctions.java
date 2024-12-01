package tukano.impl;

import tukano.api.Functions;
import tukano.api.Result;
import tukano.api.Short;
import utils.DB;
import utils.JSON;

import java.util.logging.Logger;

import static java.lang.String.format;
import static tukano.api.Result.*;
import static tukano.api.Result.ErrorCode.BAD_REQUEST;
import static utils.DB.getOne;

public class JavaFunctions implements Functions {

    private static Logger Log = Logger.getLogger(JavaFunctions.class.getName());

    private static Functions instance;

    synchronized public static Functions getInstance() {
        if( instance == null )
            instance = new JavaFunctions();
        return instance;
    }

    private JavaFunctions() {}

    @Override
    public Result<String> stats(String shortId) {
        Log.info(() -> "Increment stats for shortId=\n" + shortId);

        return DB.transaction(hibernate -> {
            return errorOrResult(JavaShorts.getInstance().getShort(shortId), shrt -> {
                var query = format("UPDATE stats SET views = views + 1 WHERE shortId = :shortId");
                hibernate.createQuery(query).setParameter("shortId", shortId).executeUpdate();
                return Result.ok(shortId);
            });
        });
    }
}
