package utils.auth;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ResponseCookiesFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        responseContext.getHeaders().add("Set-Cookie", ResponseCookies.get());
        // TODO not sure if I should clear every time but it makes sense
        // to not leave any cookies laying around? lmk
        ResponseCookies.clear();
    }
}
