package com.tfc.pdf.pdfdancer.api.client.http;

import java.util.List;
import java.util.Objects;

/**
 * Minimal replacement for Micronaut's Argument type used for capturing generics.
 */
public final class Argument<T> {

    private final Class<?> rawType;
    private final Class<?>[] typeArguments;

    private Argument(Class<?> rawType, Class<?>... typeArguments) {
        this.rawType = Objects.requireNonNull(rawType, "rawType");
        this.typeArguments = typeArguments == null ? new Class<?>[0] : typeArguments.clone();
    }

    public static <E> Argument<List<E>> listOf(Class<E> elementType) {
        return new Argument<>(List.class, elementType);
    }

    @SafeVarargs
    public static <T> Argument<T> of(Class<? super T> rawType, Class<?>... typeArguments) {
        return new Argument<>(rawType, typeArguments);
    }

    public Class<?> rawType() {
        return rawType;
    }

    public Class<?>[] typeArguments() {
        return typeArguments.clone();
    }
}
