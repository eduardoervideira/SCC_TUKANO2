package tukanoBlobs.impl.rest;

import jakarta.inject.Singleton;
import tukanoBlobs.api.Blobs;
import tukanoBlobs.api.rest.RestBlobs;
import tukanoBlobs.impl.JavaBlobs;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {

	final Blobs impl;
	
	public RestBlobsResource() {
		this.impl = JavaBlobs.getInstance();
	}
	
	@Override
	public void upload(String blobId, byte[] bytes, String token) {
		super.resultOrThrow( impl.upload(blobId, bytes, token));
	}

	@Override
	public byte[] download(String blobId, String token) {
		return super.resultOrThrow( impl.download( blobId, token ));
	}

	@Override
	public void delete(String blobId) {
		super.resultOrThrow( impl.delete( blobId));
	}
	
	@Override
	public void deleteAllBlobs(String userId) {
		super.resultOrThrow( impl.deleteAllBlobs( userId));
	}
}
