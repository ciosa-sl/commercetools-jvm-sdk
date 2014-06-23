package io.sphere.sdk.client;

import com.google.common.base.Function;

public interface ClientRequest<T> extends Requestable {
    Function<HttpResponse, T> resultMapper();
}
