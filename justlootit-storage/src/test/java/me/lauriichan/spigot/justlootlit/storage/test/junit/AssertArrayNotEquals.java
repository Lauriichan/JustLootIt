package me.lauriichan.spigot.justlootlit.storage.test.junit;

import static me.lauriichan.spigot.justlootlit.storage.test.junit.AssertionUtils.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Supplier;

public final class AssertArrayNotEquals {

	private AssertArrayNotEquals() {
		/* no-op */
	}

	public static void assertArrayNotEquals(boolean[] expected, boolean[] actual) {
		assertArrayNotEquals(expected, actual, (String) null);
	}

	public static void assertArrayNotEquals(boolean[] expected, boolean[] actual, String message) {
		assertArrayNotEquals(expected, actual, null, message);
	}

	public static void assertArrayNotEquals(boolean[] expected, boolean[] actual, Supplier<String> messageSupplier) {
		assertArrayNotEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayNotEquals(char[] expected, char[] actual, String message) {
		assertArrayNotEquals(expected, actual, null, message);
	}

	public static void assertArrayNotEquals(char[] expected, char[] actual) {
		assertArrayNotEquals(expected, actual, (String) null);
	}

	public static void assertArrayNotEquals(char[] expected, char[] actual, Supplier<String> messageSupplier) {
		assertArrayNotEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayNotEquals(byte[] expected, byte[] actual) {
	    assertArrayNotEquals(expected, actual, (String) null);
	}

	public static void assertArrayNotEquals(byte[] expected, byte[] actual, String message) {
	    assertArrayNotEquals(expected, actual, null, message);
	}

	public static void assertArrayNotEquals(byte[] expected, byte[] actual, Supplier<String> messageSupplier) {
	    assertArrayNotEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayNotEquals(short[] expected, short[] actual) {
	    assertArrayNotEquals(expected, actual, (String) null);
	}

	public static void assertArrayNotEquals(short[] expected, short[] actual, String message) {
		assertArrayNotEquals(expected, actual, null, message);
	}

	public static void assertArrayNotEquals(short[] expected, short[] actual, Supplier<String> messageSupplier) {
		assertArrayNotEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayNotEquals(int[] expected, int[] actual) {
		assertArrayNotEquals(expected, actual, (String) null);
	}

	public static void assertArrayNotEquals(int[] expected, int[] actual, String message) {
		assertArrayNotEquals(expected, actual, null, message);
	}

	public static void assertArrayNotEquals(int[] expected, int[] actual, Supplier<String> messageSupplier) {
		assertArrayNotEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayNotEquals(long[] expected, long[] actual) {
		assertArrayNotEquals(expected, actual, (String) null);
	}

	public static void assertArrayNotEquals(long[] expected, long[] actual, String message) {
		assertArrayNotEquals(expected, actual, null, message);
	}

	public static void assertArrayNotEquals(long[] expected, long[] actual, Supplier<String> messageSupplier) {
		assertArrayNotEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayNotEquals(float[] expected, float[] actual) {
		assertArrayNotEquals(expected, actual, (String) null);
	}

	public static void assertArrayNotEquals(float[] expected, float[] actual, String message) {
		assertArrayNotEquals(expected, actual, null, message);
	}

	public static void assertArrayNotEquals(float[] expected, float[] actual, Supplier<String> messageSupplier) {
		assertArrayNotEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayNotEquals(float[] expected, float[] actual, float delta) {
		assertArrayNotEquals(expected, actual, delta, (String) null);
	}

	public static void assertArrayNotEquals(float[] expected, float[] actual, float delta, String message) {
		assertArrayNotEquals(expected, actual, delta, null, message);
	}

	public static void assertArrayNotEquals(float[] expected, float[] actual, float delta, Supplier<String> messageSupplier) {
		assertArrayNotEquals(expected, actual, delta, null, messageSupplier);
	}

	public static void assertArrayNotEquals(double[] expected, double[] actual) {
		assertArrayNotEquals(expected, actual, (String) null);
	}

	public static void assertArrayNotEquals(double[] expected, double[] actual, String message) {
		assertArrayNotEquals(expected, actual, null, message);
	}

	public static void assertArrayNotEquals(double[] expected, double[] actual, Supplier<String> messageSupplier) {
		assertArrayNotEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayNotEquals(double[] expected, double[] actual, double delta) {
		assertArrayNotEquals(expected, actual, delta, (String) null);
	}

	public static void assertArrayNotEquals(double[] expected, double[] actual, double delta, String message) {
		assertArrayNotEquals(expected, actual, delta, null, message);
	}

	public static void assertArrayNotEquals(double[] expected, double[] actual, double delta, Supplier<String> messageSupplier) {
		assertArrayNotEquals(expected, actual, delta, null, messageSupplier);
	}

	public static void assertArrayNotEquals(Object[] expected, Object[] actual) {
		assertArrayNotEquals(expected, actual, (String) null);
	}

	public static void assertArrayNotEquals(Object[] expected, Object[] actual, String message) {
		assertArrayNotEquals(expected, actual, new ArrayDeque<>(), message);
	}

	public static void assertArrayNotEquals(Object[] expected, Object[] actual, Supplier<String> messageSupplier) {
		assertArrayNotEquals(expected, actual, new ArrayDeque<>(), messageSupplier);
	}

	private static void assertArrayNotEquals(boolean[] expected, boolean[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] == actual[i]) {
			    failArraysEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayNotEquals(char[] expected, char[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] == actual[i]) {
			    failArraysEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayNotEquals(byte[] expected, byte[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] == actual[i]) {
			    failArraysEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayNotEquals(short[] expected, short[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] == actual[i]) {
			    failArraysEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayNotEquals(int[] expected, int[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] == actual[i]) {
			    failArraysEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayNotEquals(long[] expected, long[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] == actual[i]) {
			    failArraysEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayNotEquals(float[] expected, float[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (AssertionUtils.floatsAreEqual(expected[i], actual[i])) {
			    failArraysEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayNotEquals(float[] expected, float[] actual, float delta, Deque<Integer> indexes,
			Object messageOrSupplier) {

		AssertionUtils.assertValidDelta(delta);
		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (AssertionUtils.floatsAreEqual(expected[i], actual[i], delta)) {
			    failArraysEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayNotEquals(double[] expected, double[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (AssertionUtils.doublesAreEqual(expected[i], actual[i])) {
			    failArraysEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayNotEquals(double[] expected, double[] actual, double delta, Deque<Integer> indexes,
			Object messageOrSupplier) {

		AssertionUtils.assertValidDelta(delta);
		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (AssertionUtils.doublesAreEqual(expected[i], actual[i], delta)) {
				failArraysEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayNotEquals(Object[] expected, Object[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			Object expectedElement = expected[i];
			Object actualElement = actual[i];

			if (expectedElement == actualElement) {
				continue;
			}

			indexes.addLast(i);
			assertArrayElementsEqual(expectedElement, actualElement, indexes, messageOrSupplier);
			indexes.removeLast();
		}
	}

	private static void assertArrayElementsEqual(Object expected, Object actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected instanceof Object[] && actual instanceof Object[]) {
			assertArrayNotEquals((Object[]) expected, (Object[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof byte[] && actual instanceof byte[]) {
			assertArrayNotEquals((byte[]) expected, (byte[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof short[] && actual instanceof short[]) {
			assertArrayNotEquals((short[]) expected, (short[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof int[] && actual instanceof int[]) {
			assertArrayNotEquals((int[]) expected, (int[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof long[] && actual instanceof long[]) {
			assertArrayNotEquals((long[]) expected, (long[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof char[] && actual instanceof char[]) {
			assertArrayNotEquals((char[]) expected, (char[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof float[] && actual instanceof float[]) {
			assertArrayNotEquals((float[]) expected, (float[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof double[] && actual instanceof double[]) {
			assertArrayNotEquals((double[]) expected, (double[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof boolean[] && actual instanceof boolean[]) {
			assertArrayNotEquals((boolean[]) expected, (boolean[]) actual, indexes, messageOrSupplier);
		}
		else if (Objects.equals(expected, actual)) {
		    failArraysEqual(expected, actual, indexes, messageOrSupplier);
		}
	}

	private static void assertArraysNotNull(Object expected, Object actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == null) {
			failExpectedArrayIsNull(indexes, messageOrSupplier);
		}
		if (actual == null) {
			failActualArrayIsNull(indexes, messageOrSupplier);
		}
	}

	private static void failExpectedArrayIsNull(Deque<Integer> indexes, Object messageOrSupplier) {
		fail(buildPrefix(nullSafeGet(messageOrSupplier)) + "expected array was <null>" + formatIndexes(indexes));
	}

	private static void failActualArrayIsNull(Deque<Integer> indexes, Object messageOrSupplier) {
		fail(buildPrefix(nullSafeGet(messageOrSupplier)) + "actual array was <null>" + formatIndexes(indexes));
	}

	private static void assertArraysHaveSameLength(int expected, int actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected != actual) {
			String prefix = buildPrefix(nullSafeGet(messageOrSupplier));
			String message = "array lengths differ" + formatIndexes(indexes) + ", expected: <" + expected
					+ "> but was: <" + actual + ">";
			fail(prefix + message);
		}
	}

	private static void failArraysEqual(Object expected, Object actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		String prefix = buildPrefix(nullSafeGet(messageOrSupplier));
		String message = "array contents do not differ" + formatIndexes(indexes) + ", " + formatValues(expected, actual);
		fail(prefix + message);
	}

	private static Deque<Integer> nullSafeIndexes(Deque<Integer> indexes, int newIndex) {
		Deque<Integer> result = (indexes != null ? indexes : new ArrayDeque<>());
		result.addLast(newIndex);
		return result;
	}

}
