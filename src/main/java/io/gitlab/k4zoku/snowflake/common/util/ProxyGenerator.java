package io.gitlab.k4zoku.snowflake.common.util;

import io.gitlab.k4zoku.snowflake.common.Generator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class ProxyGenerator<F, T> implements Generator<T> {

    private final Generator<F> generator;
    private final Function<F, T> mapper;

    public ProxyGenerator(Generator<F> generator, Function<F, T> mapper) {
        this.generator = generator;
        this.mapper = mapper;
    }

    public static <F, T> ProxyGenerator<F, T> create(Generator<F> generator, Function<F, T> mapper) {
        return new ProxyGenerator<>(generator, mapper);
    }

    public static <F, T> ProxyGenerator<F, T> create(Generator<F> generator, Class<T> clazz) {
        return new ProxyGenerator<>(generator, clazz::cast);
    }

    @Override
    public T generate() {
        return mapper.apply(generator.generate());
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new EndlessIterator<>(this::generate);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (T t : this) {
            action.accept(t);
        }
    }

    @Override
    public Spliterator<T> spliterator() {
        Spliterator<F> spliterator = generator.spliterator();
        int characteristics = spliterator.characteristics();
        return Spliterators.spliteratorUnknownSize(iterator(), characteristics);
    }

    @Override
    public Stream<T> stream() {
        return generator.stream().map(mapper);
    }

    @Override
    public Stream<T> stream(long maxSize) {
        return generator.stream(maxSize).map(mapper);
    }
}
