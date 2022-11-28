package io.gitlab.k4zoku.snowflake.common.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * An iterator that never ends.
 *
 * @param <E> type of elements
 */
public final class EndlessIterator<E> implements Iterator<E> {

    private final Supplier<E> operator;

    public EndlessIterator(Supplier<E> operator) {
        this.operator = operator;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return operator != null;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     */
    public E next() {
        if (hasNext()) return operator.get();
        throw new NoSuchElementException();
    }
}
