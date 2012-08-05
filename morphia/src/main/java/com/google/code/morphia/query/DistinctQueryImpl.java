package com.google.code.morphia.query;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.CodeWScope;

import com.google.code.morphia.mapping.Mapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;

public class DistinctQueryImpl<T> extends CriteriaContainerImpl implements Criteria, CriteriaContainer, DistinctQuery<T> {

	private boolean validateName = true;
	private final String key;
	private final Mapper mapper;
	private final Class<T> clazz;
	private final QueryImpl<?> queryImpl;
	private ReadPreference preference = null;

	public DistinctQueryImpl(Class<T> clazz, QueryImpl<?> queryImpl, String key) {
		super(queryImpl, CriteriaJoin.AND);
		this.queryImpl = queryImpl;
		this.key = key;
		this.mapper = queryImpl.getDatastore().getMapper();
		this.clazz = clazz;
	}

	public DistinctQuery<T> filter(String condition, Object value) {
		this.queryImpl.filter(condition, value);
		return this;
	}

	public FieldEnd<? extends DistinctQuery<T>> field(String field) {
		return field(field, this.validateName);
	}

	private FieldEnd<? extends DistinctQuery<T>> field(String field, boolean validate) {
		return new FieldEndImpl<DistinctQueryImpl<T>>(this.queryImpl, field, this, validate);
	}

	public FieldEnd<? extends CriteriaContainerImpl> criteria(String field) {
		return criteria(field, validateName);
	}

	private FieldEnd<? extends CriteriaContainerImpl> criteria(String field, boolean validate) {
		CriteriaContainerImpl container = new CriteriaContainerImpl(this.queryImpl, CriteriaJoin.AND);
		this.add(container);

		return new FieldEndImpl<CriteriaContainerImpl>(this.queryImpl, field, this, validate);
	}

	public DistinctQuery<T> where(String js) {
		this.queryImpl.where(js);
		return this;
	}

	public DistinctQuery<T> where(CodeWScope js) {
		this.queryImpl.where(js);
		return this;
	}

	public DistinctQuery<T> enableValidation() {
		validateName = true;
		return this;
	}

	public DistinctQuery<T> disableValidation() {
		validateName = false;
		return this;
	}

	public DistinctQuery<T> queryNonPrimary() {
		preference = ReadPreference.SECONDARY;
		return this;
	}

	public DistinctQuery<T> queryPrimaryOnly() {
		preference = ReadPreference.PRIMARY;
		return this;
	}

	public Class<T> getEntityClass() {
		return this.clazz;
	}

	@Override
	public DistinctQueryImpl<T> clone() {
		DistinctQueryImpl<T> distinctQueryImpl = new DistinctQueryImpl<T>(clazz, queryImpl.clone(), new String(key));
		return distinctQueryImpl;
	}

	public void add(Criteria... criteria) {
		this.queryImpl.add(criteria);
	}

	public CriteriaContainer or(Criteria... criteria) {
		return this.queryImpl.or(criteria);
	}

	public void addTo(DBObject obj) {
		this.queryImpl.addTo(obj);
	}

	public void attach(CriteriaContainerImpl container) {
		this.queryImpl.attach(container);
	}

	public String getFieldName() {
		return this.queryImpl.getFieldName();
	}

	@SuppressWarnings("unchecked")
	public List<T> asList() {
		DBCollection dbCollection = this.queryImpl.getDatastore().getCollection(this.queryImpl.getEntityClass());
		dbCollection.setReadPreference(preference);
		final List<BasicDBObject> objList = dbCollection.distinct(key, this.queryImpl.getQueryObject());
		List<T> discinctList = new ArrayList<T>();
                for (BasicDBObject basicDBObject : objList) {
                        discinctList.add((T) mapper.fromDBObject(clazz, basicDBObject, mapper.createEntityCache()));
                }
		return discinctList;
	}
	
	@Override
	public String toString() {
		return "distinct( \"" + key + "\" , " + this.queryImpl.getQueryObject() + " )";
	}

}
