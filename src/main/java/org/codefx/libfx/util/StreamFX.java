package org.codefx.libfx.util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

/**
 * Provides utility methods for {@link Stream}s.
 */
public class StreamFX {

	// #begin CREATION

    public static <T> Stream<T> sequentialStream(Iterable<T> iterable) {
		Spliterator<T> spliterator = iterable.spliterator();
		return StreamSupport.stream(spliterator, false);
	}

    public static <T> Stream<T> parallelStream(Iterable<T> iterable) {
		Spliterator<T> spliterator = iterable.spliterator();
		return StreamSupport.stream(spliterator, spliterator.hasCharacteristics(Spliterator.CONCURRENT));
	}

    public static Stream<String> split(String string, String regex) {
		return Pattern.compile(regex).splitAsStream(string);
	}

    public static Stream<String> splitIntoLines(String string) {
		return new BufferedReader(new StringReader(string)).lines();
	}

	// #end CREATION

	// #begin COLLECTION

	/**
	 * Returns a {@link BinaryOperator} that throws an {@link IllegalStateException} when it is called.
	 * <p>
	 * This can be used to reduce a stream to its only element, verifying the assumption that it indeed only contains
	 * the one.
	 * <pre>
	 * Stream.empty().reduce(toOnlyElement()); // returns empty Optional
	 * Stream.of("a").reduce(toOnlyElement()); // returns 'Optional["a"]'
	 * Stream.of("a", "b").reduce(toOnlyElement()); // throws 'IllegalStateException'
	 * </pre>
	 * Note that null elements also trigger the exception and that reducing a stream of a single null fails because
	 * the stream API will call {@code Optional.of(null)}:
	 * <pre>
	 * Stream.of("a", null).reduce(toOnlyElement()); // throws 'IllegalStateException'
	 * Stream.of(null).reduce(toOnlyElement()); // throws 'NullPointerException'
	 * </pre>
	 *
	 * @param <T> the type of the stream elements
	 * @return a BinaryOperator
	 *
	 * @see #toOnlyNonNullElement()
	 */
	public static <T> BinaryOperator<T> toOnlyElement() {
		return (element, otherElement) -> {
			throw new IllegalStateException("The stream contains more than one element.");
		};
	}
	/**
	 * Returns a {@link BinaryOperator} that throws an {@link IllegalStateException} when it is called with two
	 * non-null arguments.
	 * <p>
	 * This can be used to reduce a stream to its only non-null element, verifying the assumption that it indeed only
	 * contains the one.
	 * <pre>
	 * Stream.empty().reduce(toOnlyNonNullElement()); // returns empty Optional
	 * Stream.of("a").reduce(toOnlyNonNullElement()); // returns 'Optional["a"]'
	 * Stream.of("a", null).reduce(toOnlyNonNullElement()); // returns 'Optional["a"]'
	 * Stream.of("a", "b").reduce(toOnlyNonNullElement()); // throws 'IllegalStateException'
	 * </pre>
	 * Note that reducing a stream of a single null fails because the stream API will call {@code Optional.of(null)}:
	 * <pre>
	 * Stream.of(null).reduce(toOnlyNonNullElement()); // throws 'NullPointerException'
	 * </pre>
	 *
	 * @param <T> the type of the stream elements
	 * @return a BinaryOperator
	 *
	 * @see #toOnlyElement()
	 */
	public static <T> BinaryOperator<T> toOnlyNonNullElement() {
		return (element, otherElement) -> {
			if (element != null && otherElement != null)
				throw new IllegalStateException("The stream contains more than one non-null element.");
			if (element != null)
				return element;
			if (otherElement != null)
				return otherElement;
			return null;
		};
	}


	public static Collector<CharSequence, ?, String> joining(
			CharSequence delimiter, CharSequence prefix, CharSequence suffix, CharSequence emptyValue) {
		return joining(() -> new StringJoiner(delimiter, prefix, suffix).setEmptyValue(emptyValue));
	}

    public static Collector<CharSequence, ?, String> joining(Supplier<StringJoiner> stringJoinerSupplier) {
		return new FunctionalCollector<>(
				stringJoinerSupplier, StringJoiner::add, StringJoiner::merge, StringJoiner::toString);
	}

	// #end COLLECTION

	/**
	 * An implementation of {@link Collector} which delegates all method calls to functions specified during
	 * construction.
	 *
	 * @param <T>
	 *            the type of input elements to the reduction operation
	 * @param <A>
	 *            the mutable accumulation type of the reduction operation (often hidden as an implementation detail)
	 * @param <R>
	 *            the result type of the reduction operation
	 */
	public static final class FunctionalCollector<T, A, R> implements Collector<T, A, R> {

		private final Supplier<A> supplier;
		private final BiConsumer<A, T> accumulator;
		private final BinaryOperator<A> combiner;
		private final Function<A, R> finisher;
		private final Set<Characteristics> characteristics;

		/**
		 * Creates a new collector.
		 *
		 * @param supplier
		 *            see {@link Collector#supplier()}
		 * @param accumulator
		 *            see {@link Collector#accumulator()}
		 * @param combiner
		 *            see {@link Collector#combiner()}
		 * @param finisher
		 *            see {@link Collector#finisher()}
		 * @param characteristics
		 *            see {@link Collector#characteristics()}
		 */
		public FunctionalCollector(
				Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner,
				Function<A, R> finisher, Set<Characteristics> characteristics) {

			this.supplier = requireNonNull(supplier, "The argument 'supplier' must not be null.");
			this.accumulator = requireNonNull(accumulator, "The argument 'accumulator' must not be null.");
			this.combiner = requireNonNull(combiner, "The argument 'combiner' must not be null.");
			this.finisher = requireNonNull(finisher, "The argument 'finisher' must not be null.");
			this.characteristics = Collections.unmodifiableSet(new HashSet<>(
                    requireNonNull(characteristics, "The argument 'characteristics' must not be null.")));
		}

		/**
		 * Creates a new collector.
		 *
		 * @param supplier
		 *            see {@link Collector#supplier()}
		 * @param accumulator
		 *            see {@link Collector#accumulator()}
		 * @param combiner
		 *            see {@link Collector#combiner()}
		 * @param finisher
		 *            see {@link Collector#finisher()}
		 * @param characteristics
		 *            see {@link Collector#characteristics()}
		 */
		public FunctionalCollector(
				Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner,
				Function<A, R> finisher, Characteristics... characteristics) {
            this(supplier, accumulator, combiner, finisher, createCharacteristicsSet(characteristics));
		}

        private static Set<Characteristics> createCharacteristicsSet(Characteristics[] characteristics) {
            Set<Characteristics> characteristicsSet = EnumSet.noneOf(Characteristics.class);
            if (characteristics != null && characteristics.length > 0)
                Arrays.stream(characteristics).forEach(characteristicsSet::add);
            return Collections.unmodifiableSet(characteristicsSet);
        }

		@Override
		public Supplier<A> supplier() {
			return supplier;
		}

		@Override
		public BiConsumer<A, T> accumulator() {
			return accumulator;
		}

		@Override
		public BinaryOperator<A> combiner() {
			return combiner;
		}

		@Override
		public Function<A, R> finisher() {
			return finisher;
		}

		@Override
		public Set<Characteristics> characteristics() {
			return characteristics;
		}

	}

}