package tukano.impl.rest;

import jakarta.inject.Singleton;
import tukano.api.Functions;
import tukano.api.rest.RestFunctions;
import tukano.impl.JavaFunctions;

@Singleton
public class RestFunctionsResource extends RestResource implements RestFunctions {
    final Functions impl;
    public RestFunctionsResource() { this.impl = JavaFunctions.getInstance(); }

    @Override
    public String stats(String shortId) { return super.resultOrThrow( impl.stats(shortId)); }
}
