package org.pagemodel.core.utils.json;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class JsonArrayBuilder {
	protected List<Object> jsonArr = new LinkedList<>();

	public JsonArrayBuilder() {
	}

	public JsonArrayBuilder(List<Object> arr) {
		if(arr != null) {
			jsonArr = arr;
		}
	}

	public JsonArrayBuilder addValue(Object value) {
		jsonArr.add(value);
		return this;
	}

	public JsonArrayBuilder addObject(Consumer<JsonObjectBuilder> objBuilder) {
		if(objBuilder == null) {
			return this;
		}
		JsonObjectBuilder newObjBuilder = new JsonObjectBuilder();
		objBuilder.accept(newObjBuilder);
		jsonArr.add(newObjBuilder.toMap());
		return this;
	}

	public JsonArrayBuilder addArray(Consumer<JsonArrayBuilder> arrBuilder) {
		if(arrBuilder == null) {
			return this;
		}
		JsonArrayBuilder newArrBuilder = new JsonArrayBuilder();
		arrBuilder.accept(newArrBuilder);
		jsonArr.add(newArrBuilder.toList());
		return this;
	}

	public JsonArrayBuilder merge(List<?> src) {
		if(src == null) {
			return this;
		}
		for (Object o : src) {
			jsonArr.add(o);
		}
		return this;
	}

	public JsonArrayBuilder doAdd(Consumer<JsonArrayBuilder> add) {
		if(add == null) {
			return this;
		}
		add.accept(this);
		return this;
	}

	public List<Object> toList() {
		return jsonArr;
	}
}
