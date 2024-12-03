package tukano.impl.rest;

import jakarta.inject.Singleton;
import tukano.api.Functions;
import tukano.api.rest.RestFunctions;
import tukano.impl.JavaFunctions;

@Singleton
public class RestFunctionsResource
        extends RestResource implements RestFunctions {

    final Functions impl;

    public RestFunctionsResource() {
        this.impl = JavaFunctions.getInstance();
    }

    @Override
    public void countView(String shortId, String token) {
        super.resultOrThrow(impl.countView(shortId, token));
    }

    @Override
    public void tukanoRecommends() {
        super.resultOrThrow(impl.tukanoRecommends());
    }
}
