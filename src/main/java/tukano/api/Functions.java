package tukano.api;

public interface Functions {
    String NAME = "functions";

    Result<String> stats(String shortId);
}
