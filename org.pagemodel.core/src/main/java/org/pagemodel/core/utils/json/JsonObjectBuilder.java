package org.pagemodel.core.utils.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JsonObjectBuilder {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected Map<String, Object> jsonObj = new LinkedHashMap<>();

	public JsonObjectBuilder() {
	}

	public JsonObjectBuilder(Map<String, Object> obj) {
		if(obj != null) {
			jsonObj = obj;
		}
	}

	public JsonObjectBuilder addValue(String field, Object value) {
		if(field == null){
			log.debug("Ignoring addValue with null field.  value:[" + value + "]");
			return this;
		}
		jsonObj.put(field, value);
		return this;
	}

	public JsonObjectBuilder addObject(String field, Consumer<JsonObjectBuilder> objBuilder) {
		if(field == null){
			log.debug("Ignoring addObject with null field.  objBuilder:[" + objBuilder + "]");
			return this;
		}
		if(objBuilder == null){
			return this;
		}
		JsonObjectBuilder newObjBuilder = new JsonObjectBuilder();
		objBuilder.accept(newObjBuilder);
		jsonObj.put(field, newObjBuilder.toMap());
		return this;
	}

	public JsonObjectBuilder addArray(String field, Consumer<JsonArrayBuilder> arrBuilder) {
		if(field == null){
			log.debug("Ignoring addObject with null field.  objBuilder:[" + arrBuilder + "]");
			return this;
		}
		if(arrBuilder == null){
			return this;
		}
		JsonArrayBuilder newArrBuilder = new JsonArrayBuilder();
		arrBuilder.accept(newArrBuilder);
		jsonObj.put(field, newArrBuilder.toList());
		return this;
	}

	public JsonObjectBuilder removeField(String field) {
		if(jsonObj.containsKey(field)){
			jsonObj.remove(field);
		}
		return this;
	}

	public JsonObjectBuilder merge(Map<String, ?> src) {
		if(src == null){
			return this;
		}
		for (Map.Entry<?, ?> e : src.entrySet()) {
			jsonObj.put(e.getKey().toString(), e.getValue());
		}
		return this;
	}

	public JsonObjectBuilder deepMerge(Map<String, ?> src) {
		deepMerge(src, jsonObj);
		return this;
	}

	protected static void deepMerge(Map<String, ?> src, Map<String, Object> dest) {
		if(src == null || dest == null){
			return;
		}
		for (Map.Entry<String, ?> e : src.entrySet()) {
			String field  = e.getKey();
			Object srcVal = e.getValue();
			if(!dest.containsKey(field)){
				dest.put(field, e.getValue());
				continue;
			}
			Object destVal = dest.get(field);
			if(destVal instanceof Map && srcVal instanceof Map){
				deepMerge((Map<String, Object>) srcVal, (Map<String, Object>) destVal);
			}else if(destVal instanceof List && srcVal instanceof List){

			}else{
				dest.put(field, srcVal);
			}
		}
	}

	public JsonObjectBuilder updateObject(String field, Consumer<JsonObjectBuilder> objUpdate) {
		if(objUpdate == null){
			return this;
		}
		Object obj = jsonObj.get(field);
		if(!jsonObj.containsKey(field) || !(obj instanceof Map)){
			return addObject(field, objUpdate);
		}
		JsonBuilder.object((Map<String,Object>)obj).doAdd(objUpdate);
		return this;
	}

	public JsonObjectBuilder updateArray(String field, Consumer<JsonArrayBuilder> arrUpdate) {
		if(arrUpdate == null){
			return this;
		}
		Object obj = jsonObj.get(field);
		if(!jsonObj.containsKey(field) || !(obj instanceof List)){
			return addArray(field, arrUpdate);
		}
		JsonBuilder.array((List<Object>)obj).doAdd(arrUpdate);
		return this;
	}

	public JsonObjectBuilder doAdd(Consumer<JsonObjectBuilder> add) {
		if(add != null) {
			add.accept(this);
		}
		return this;
	}

	public JsonObjectBuilder doAddRec(Consumer<JsonObjectBuilder> add) {
		if(add == null){
			return this;
		}
		JsonObjectBuilder ob = JsonBuilder.object();
		add.accept(ob);
		Map<String,Object> res = ob.toMap();
		for(Map.Entry<String,Object> e : res.entrySet()){
			Object obj = e.getValue();
			try{
				JsonObjectBuilder b2 = doAddRec((Consumer<JsonObjectBuilder>) obj);
				res.put(e.getKey(), b2.toMap());
			}catch (ClassCastException ex){}
		}
		return deepMerge(res);
	}

	public Map<String, Object> toMap() {
		return jsonObj;
	}
}
