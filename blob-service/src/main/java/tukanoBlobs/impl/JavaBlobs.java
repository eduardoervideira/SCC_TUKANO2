package tukanoBlobs.impl;

import static java.lang.String.format;
import static tukanoBlobs.api.Result.ErrorCode.FORBIDDEN;
import static tukanoBlobs.api.Result.ErrorCode.UNAUTHORIZED;
import static tukanoBlobs.api.Result.error;

import java.util.logging.Logger;
import tukanoBlobs.api.Blobs;
import tukanoBlobs.api.Result;
import tukanoBlobs.impl.rest.BlobsRestServer;
import tukanoBlobs.impl.storage.BlobStorage;
import tukanoBlobs.impl.storage.FilesystemStorage;
import utils.Hash;
import utils.Hex;

public class JavaBlobs implements Blobs {

    private static Blobs instance;
    private static Logger Log = Logger.getLogger(JavaBlobs.class.getName());

    public String baseURI;
    private BlobStorage storage;

    synchronized public static Blobs getInstance() {
        if (instance == null)
            instance = new JavaBlobs();
        return instance;
    }

    private JavaBlobs() {
        storage = new FilesystemStorage();
        baseURI =
            String.format("%s/%s/", BlobsRestServer.serverURI, Blobs.NAME);
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes, String token) {
        Log.info(
            ()
                -> format("upload : blobId = %s, sha256 = %s, token = %s\n",
                          blobId, Hex.of(Hash.sha256(bytes)), token));

        if (!validBlobId(blobId, token))
            return error(FORBIDDEN);
        String userId = blobId.split("\\+")[0];
        if (!validCookieWithId(userId))
            return error(UNAUTHORIZED);

        return storage.write(toPath(blobId), bytes);
    }

    @Override
    public Result<byte[]> download(String blobId, String token) {
        Log.info(
            () -> format("download : blobId = %s, token=%s\n", blobId, token));

        if (!validBlobId(blobId, token))
            return error(FORBIDDEN);
        if (!validCookie())
            return error(UNAUTHORIZED);

        return storage.read(toPath(blobId));
    }

    @Override
    public Result<Void> delete(String blobId) {
        Log.info(
            () -> format("delete : blobId = %s\n", blobId));

        // removed token validation because if user deletes short
        // then admin could never acquire valid token
        // if (!validBlobId(blobId, token))
        //     return error(FORBIDDEN);
        if (!validCookieWithId("admin"))
            return error(UNAUTHORIZED);

        return storage.delete(toPath(blobId));
    }

    @Override
    public Result<Void> deleteAllBlobs(String userId) {
        Log.info(()
                     -> format("deleteAllBlobs : userId = %s\n",
                               userId));

        // removed token validation because if user deletes account
        // then admin could never acquire valid token
        // if (!Token.isValid(token, userId))
        //     return error(FORBIDDEN);
        if (!validCookieWithId("admin"))
            return error(UNAUTHORIZED);

        return storage.delete(toPath(userId));
    }

    private boolean validBlobId(String blobId, String token) {
        return Token.isValid(token, blobId);
    }

    private boolean validCookieWithId(String userId) {
        try {
            Authentication.validateSession(userId);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private boolean validCookie() {
        try {
            Authentication.validateSession();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private String toPath(String blobId) { return blobId.replace("+", "/"); }

    private String toURL(String blobId) { return baseURI + blobId; }
}
