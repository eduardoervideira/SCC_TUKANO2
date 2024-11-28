package tukano.impl;

import tukano.api.Functions;
import tukano.api.Result;
import utils.JSON;

import java.util.logging.Logger;

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
        Log.info(() -> "stats\n");

        //DB.sql();
        // UPDATE stats SET views = views + 1 WHERE shortId = ?

        return Result.ok(JSON.encode("shortId" + shortId));
    }
}
