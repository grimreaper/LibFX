package org.codefx.libfx.nesting.property;

import javafx.beans.property.ReadOnlyProperty;

import org.codefx.libfx.nesting.Nested;

/**
 * A read only version of a {@link NestedProperty}.
 * 
 * @param <T>
 *            the type of the value wrapped by the property
 */
public interface ReadOnlyNestedProperty<T> extends Nested, ReadOnlyProperty<T> {
	// no additional members defined
}
