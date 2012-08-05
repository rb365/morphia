package com.google.code.morphia.query;

import java.util.List;

import org.bson.types.CodeWScope;
/**
 * 
 * @author {@link "mailto:laszlo@hadadi.net","Laszlo_Hadadi"} 
 *
 * @param <T> Generic type for result
 */
public interface DistinctQuery<T> {
	
	/**
	 * <p>Create a filter based on the specified condition and value.
	 * </p><p>
	 * <b>Note</b>: Property is in the form of "name op" ("age >").
	 * </p><p>
	 * Valid operators are ["=", "==","!=", "<>", ">", "<", ">=", "<=", "in", "nin", "all", "size", "exists"]
	 * </p>
	 * <p>Examples:</p>
	 * 
	 * <ul>
	 * <li>{@code filter("yearsOfOperation >", 5)}</li>
	 * <li>{@code filter("rooms.maxBeds >=", 2)}</li>
	 * <li>{@code filter("rooms.bathrooms exists", 1)}</li>
	 * <li>{@code filter("stars in", new Long[]{3,4}) //3 and 4 stars (midrange?)}</li>
	 * <li>{@code filter("age >=", age)}</li>
	 * <li>{@code filter("age =", age)}</li>
	 * <li>{@code filter("age", age)} (if no operator, = is assumed)</li>
	 * <li>{@code filter("age !=", age)}</li>
	 * <li>{@code filter("age in", ageList)}</li>
	 * <li>{@code filter("customers.loyaltyYears in", yearsList)}</li>
	 * </ul>
	 * 
	 * <p>You can filter on id properties <strong>if</strong> this query is
	 * restricted to a Class<T>.
	 */
	DistinctQuery<T> filter(String condition, Object value);
	
	/** Fluent query interface: {@code createQuery(Ent.class).field("count").greaterThan(7)...} */
	FieldEnd<? extends DistinctQuery<T>> field(String field);

	/** Criteria builder interface */
	FieldEnd<? extends CriteriaContainerImpl> criteria(String field);

	CriteriaContainer and(Criteria... criteria);
	CriteriaContainer or(Criteria... criteria);

	/** Limit the query using this javascript block; only one per query*/
	DistinctQuery<T> where(String js);

    /** Limit the query using this javascript block; only one per query*/
    DistinctQuery<T> where(CodeWScope js);
	
	/** Turns on validation (for all calls made after); by default validation is on*/
	DistinctQuery<T> enableValidation();
	/** Turns off validation (for all calls made after)*/
	DistinctQuery<T> disableValidation();
	

	/** Route query to non-primary node  */
	DistinctQuery<T> queryNonPrimary();

	/** Route query to primary node  */
	DistinctQuery<T> queryPrimaryOnly();

	
	/**
	 * <p>Generates a string that consistently and uniquely specifies this query.  There
	 * is no way to convert this string back into a query and there is no guarantee that
	 * the string will be consistent across versions.</p>
	 * 
	 * <p>In particular, this value is useful as a key for a simple memcache query cache.</p>
	 */
	String toString();
	
	Class<T> getEntityClass();
	
	DistinctQuery<T> clone();

	List<T> asList();
	
	

}
