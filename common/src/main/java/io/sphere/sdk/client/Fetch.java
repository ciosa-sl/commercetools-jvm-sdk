package io.sphere.sdk.client;

import com.google.common.base.Function;

import com.google.common.base.Optional;

public interface Fetch<T> extends ClientRequest<Optional<T>> {
    @Override
    public abstract Function<HttpResponse, Optional<T>> resultMapper();
}
