package me.lauriichan.spigot.justlootit.nms.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class Option<T> {
    /**
     * Common instance for {@code empty()}.
     */
    private static final Option<?> EMPTY = new Option<>();

    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final T value;

    /**
     * Returns an empty {@code Option} instance. No value is present for this
     * {@code Option}.
     *
     *
     * @param  <T> The type of the non-existent value
     * 
     * @return     an empty {@code Option}
     */
    @SuppressWarnings("unchecked")
    public static <T> Option<T> empty() {
        return (Option<T>) EMPTY;
    }

    /**
     * Constructs an unmodifiable instance.
     */
    private Option() {
        this.value = null;
    }

    /**
     * Constructs an instance with the described value.
     *
     * @param value the value to describe; it's the caller's responsibility to
     *              ensure the value is non-{@code null} unless creating the
     *              singleton instance returned by {@code empty()}.
     */
    private Option(final T value) {
        this.value = value;
    }

    /**
     * Returns an {@code Option} describing the given value.
     *
     * @param  value the value to describe
     * @param  <T>   the type of the value
     * 
     * @return       an {@code Option} with the value
     */
    public static <T> Option<T> of(final T value) {
        if (value == null) {
            return empty();
        }
        return new Option<>(value);
    }

    /**
     * returns the value as an optional
     * 
     * @return the optional
     */
    public Optional<T> asOptional() {
        return Optional.ofNullable(value);
    }

    /**
     * returns the value as an reference
     * 
     * @return the reference
     */
    public Ref<T> asReference() {
        return Ref.of(value);
    }

    /**
     * returns the value
     *
     * @return the value described by this {@code Option}
     */
    public T get() {
        return value;
    }

    /**
     * If a value is present, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if a value is present, otherwise {@code false}
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * If a value is not present, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if a value is not present, otherwise {@code false}
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * If a value is present, performs the given action with the value, otherwise
     * does nothing.
     *
     * @param  action               the action to be performed, if a value is
     *                              present
     * 
     * @throws NullPointerException if value is present and the given action is
     *                              {@code null}
     */
    public void ifPresent(final Consumer<? super T> action) {
        if (isEmpty()) {
            return;
        }
        action.accept(value);
    }

    /**
     * If a value is present, performs the given action with the value, otherwise
     * performs the given empty-based action.
     *
     * @param  action               the action to be performed, if a value is
     *                              present
     * @param  emptyAction          the empty-based action to be performed, if no
     *                              value is present
     * 
     * @throws NullPointerException if a value is present and the given action is
     *                              {@code null}, or no value is present and the
     *                              given empty-based action is {@code null}.
     */
    public void ifPresentOrElse(final Consumer<? super T> action, final Runnable emptyAction) {
        if (isEmpty()) {
            emptyAction.run();
            return;
        }
        action.accept(value);
    }

    /**
     * If a value is absent, performs the given action with the value, otherwise
     * does nothing.
     *
     * @param  action               the action to be performed, if a value is absent
     * 
     * @throws NullPointerException if value is absent and the given action is
     *                              {@code null}
     */
    public void ifAbsent(final Runnable action) {
        if (isPresent()) {
            return;
        }
        action.run();
    }

    /**
     * If a value is present, and the value matches the given predicate, returns an
     * {@code Option} describing the value, otherwise returns an empty
     * {@code Option}.
     *
     * @param  predicate            the predicate to apply to a value, if present
     * 
     * @return                      an {@code Option} describing the value of this
     *                              {@code Option}, if a value is present and the
     *                              value matches the given predicate, otherwise an
     *                              empty {@code Option}
     * 
     * @throws NullPointerException if the predicate is {@code null}
     */
    public Option<T> filter(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return isPresent() ? predicate.test(value) ? this : empty() : this;
    }

    /**
     * If a value is present, returns an {@code Option} describing the result of
     * applying the given mapping function to the value, otherwise returns an empty
     * {@code Option}.
     *
     * <p>
     * If the mapping function returns a {@code null} result then this method
     * returns an empty {@code Option}.
     *
     * @param  mapper               the mapping function to apply to a value, if
     *                              present
     * @param  <U>                  The type of the value returned from the mapping
     *                              function
     * 
     * @return                      an {@code Option} describing the result of
     *                              applying a mapping function to the value of this
     *                              {@code Option}, if a value is present, otherwise
     *                              an empty {@code Option}
     * 
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public <U> Option<U> map(final Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return isPresent() ? Option.of(mapper.apply(value)) : empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code Option}-bearing mapping function to the value, otherwise returns an
     * empty {@code Option}.
     *
     * <p>
     * This method is similar to {@link #map(Function)}, but the mapping function is
     * one whose result is already an {@code Option}, and if invoked,
     * {@code flatMap} does not wrap it within an additional {@code Option}.
     *
     * @param  <U>                  The type of value of the {@code Option} returned
     *                              by the mapping function
     * @param  mapper               the mapping function to apply to a value, if
     *                              present
     * 
     * @return                      the result of applying an {@code Option}-bearing
     *                              mapping function to the value of this
     *                              {@code Option}, if a value is present, otherwise
     *                              an empty {@code Option}
     * 
     * @throws NullPointerException if the mapping function is {@code null} or
     *                              returns a {@code null} result
     */
    @SuppressWarnings("unchecked")
    public <U> Option<U> flatMap(final Function<? super T, ? extends Option<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        return isPresent() ? Objects.requireNonNull((Option<U>) mapper.apply(value)) : empty();
    }

    /**
     * If a value is present, returns an {@code Option} describing the value,
     * otherwise returns an {@code Option} produced by the supplying function.
     *
     * @param  supplier             the supplying function that produces an
     *                              {@code Option} to be returned
     * 
     * @return                      returns an {@code Option} describing the value
     *                              of this {@code Option}, if a value is present,
     *                              otherwise an {@code Option} produced by the
     *                              supplying function.
     * 
     * @throws NullPointerException if the supplying function is {@code null} or
     *                              produces a {@code null} result
     */
    @SuppressWarnings("unchecked")
    public Option<T> or(final Supplier<? extends Option<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        return isPresent() ? this : Objects.requireNonNull((Option<T>) supplier.get());
    }

    /**
     * If a value is present, returns a sequential {@link Stream} containing only
     * that value, otherwise returns an empty {@code Stream}.
     *
     * @return the optional value as a {@code Stream}
     */
    public Stream<T> stream() {
        return isPresent() ? Stream.of(value) : Stream.empty();
    }

    /**
     * If a value is present, returns the value, otherwise returns {@code other}.
     *
     * @param  other the value to be returned, if no value is present. May be
     *               {@code null}.
     * 
     * @return       the value, if present, otherwise {@code other}
     */
    public T orElse(final T other) {
        return value != null ? value : other;
    }

    /**
     * If a value is present, returns the value, otherwise returns the result
     * produced by the supplying function.
     *
     * @param  supplier             the supplying function that produces a value to
     *                              be returned
     * 
     * @return                      the value, if present, otherwise the result
     *                              produced by the supplying function
     * 
     * @throws NullPointerException if no value is present and the supplying
     *                              function is {@code null}
     */
    public T orElseGet(final Supplier<? extends T> supplier) {
        return isPresent() ? value : supplier.get();
    }

    /**
     * If a value is present, returns the value, otherwise runs runnable
     *
     * @param  runnable             the runnable to be execute if no value is
     *                              present
     *
     * @return                      the value described by this {@code Option}
     * 
     * @throws NullPointerException if no value is present and the runnable is
     *                              {@code null}
     */
    public T orElseRun(final Runnable runnable) {
        if (isPresent()) {
            return value;
        }
        runnable.run();
        return null;
    }

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @return                        the non-{@code null} value described by this
     *                                {@code Option}
     * 
     * @throws NoSuchElementException if no value is present
     */
    public T orElseThrow() {
        if (isPresent()) {
            return value;
        }
        throw new NoSuchElementException("No value present");
    }

    /**
     * If a value is present, returns the value, otherwise throws an exception
     * produced by the exception supplying function.
     *
     * @param  <X>                  Type of the exception to be thrown
     * @param  exceptionSupplier    the supplying function that produces an
     *                              exception to be thrown
     * 
     * @return                      the value, if present
     * 
     * @throws X                    if no value is present
     * @throws NullPointerException if no value is present and the exception
     *                              supplying function is {@code null}
     */
    public <X extends Throwable> T orElseThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return value;
        }
        throw exceptionSupplier.get();
    }

    /**
     * Indicates whether some other object is "equal to" this {@code Option}. The
     * other object is considered equal if:
     * <ul>
     * <li>it is also an {@code Option} and;
     * <li>both instances have no value present or;
     * <li>the present values are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param  obj an object to be tested for equality
     * 
     * @return     {@code true} if the other object is "equal to" this object
     *             otherwise {@code false}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Option)) {
            return false;
        }

        final Option<?> other = (Option<?>) obj;
        return Objects.equals(value, other.value);
    }

    /**
     * Returns the hash code of the value, if present, otherwise {@code 0} (zero) if
     * no value is present.
     *
     * @return hash code value of the present value or {@code 0} if no value is
     *         present
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns a non-empty string representation of this {@code Option} suitable for
     * debugging. The exact presentation format is unspecified and may vary between
     * implementations and versions.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return isPresent() ? String.format("Option[%s]", value) : "Option.empty";
    }
}
