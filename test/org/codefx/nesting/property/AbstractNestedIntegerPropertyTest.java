package org.codefx.nesting.property;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import org.codefx.nesting.Nesting;

/**
 * Abstract superclass to tests for {@link NestedObjectProperty NestedObjectProperties} which only leaves the creation
 * of the tested properties (by {@link #createNestedPropertyFromNesting(Nesting)}) to the subclasses.
 */
public abstract class AbstractNestedIntegerPropertyTest extends AbstractNestedPropertyTest<Number, IntegerProperty> {

	/**
	 * The next value returned by {@link #createNewValue()}.
	 */
	private int nextValue = 1;

	@Override
	protected boolean allowsNullValues() {
		return false;
	}

	@Override
	protected Number createNewValue() {
		return ++nextValue;
	}

	@Override
	protected IntegerProperty createNewObservableWithValue(Number value) {
		return new SimpleIntegerProperty(value.intValue());
	}

	@Override
	protected IntegerProperty createNewObservableWithSomeValue() {
		return createNewObservableWithValue(0);
	}

}
