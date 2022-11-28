package io.gitlab.k4zoku.snowflake.common;

import io.gitlab.k4zoku.snowflake.common.util.EndlessIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Generator<T> extends Iterable<T> {
    T generate();

    @NotNull
    @Override
    default Iterator<T> iterator() {
        return new EndlessIterator<>(this::generate);
    }

    @Override
    default void forEach(Consumer<? super T> action) {
        for (T t : this) {
            action.accept(t);
        }
    }

    @Override
    default Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }

    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    default Stream<T> stream(long maxSize) {
        return StreamSupport.stream(spliterator(), false).limit(maxSize);
    }
}
