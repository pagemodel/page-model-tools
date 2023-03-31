package org.pagemodel.core.utils.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
/**
 * A builder class for creating JSON objects using a functional API. This class uses Consumers to provide a functional
 * API for building JSON objects and evaluating only if needed. The builder can add values, objects, and arrays to the
 * JSON object, remove fields, merge maps, and update objects and arrays. The resulting JSON object can be obtained as a
 * Map using the toMap() method.
 */
public class JsonObjectBuilder {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected Map<String, Object> jsonObj = new LinkedHashMap<>();

	/**
	 * Constructs a new JsonObjectBuilder with an empty JSON object.
	 */
	public JsonObjectBuilder() {
	}

	/**
	 * Constructs a new JsonObjectBuilder with the given JSON object.
	 * @param obj the JSON object to use as the initial state of the builder
	 */
	public JsonObjectBuilder(Map<String, Object> obj) {
		if(obj != null) {
			jsonObj = obj;
		}
	}

	/**
	 * Adds a key-value pair to the JSON object being built.
	 * @param field the key of the pair
	 * @param value the value of the pair
	 * @return this JsonObjectBuilder instance
	 */
	public JsonObjectBuilder addValue(String field, Object value) {
		if(field == null){
			log.debug("Ignoring addValue with null field.  value:[" + value + "]");
			return this;
		}
		jsonObj.put(field, value);
		return this;
	}

	/**
	 * Adds a nested JSON object to the JSON object being built.
	 * @param field the key of the nested object
	 * @param objBuilder a Consumer that builds the nested object using a new JsonObjectBuilder
	 * @return this JsonObjectBuilder instance
	 */
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

	/**
	 * Adds a nested JSON array to the JSON object being built.
	 * @param field the key of the nested array
	 * @param arrBuilder a Consumer that builds the nested array using a new JsonArrayBuilder
	 * @return this JsonObjectBuilder instance
	 */
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

	/**
	 * Removes a key-value pair from the JSON object being built.
	 * @param field the key of the pair to remove
	 * @return this JsonObjectBuilder instance
	 */
	public JsonObjectBuilder removeField(String field) {
		if(jsonObj.containsKey(field)){
			jsonObj.remove(field);
		}
		return this;
	}

	/**
	 * Merges the given map into the JSON object being built.
	 * @param src the map to merge into the JSON object
	 * @return this JsonObjectBuilder instance
	 */
	public JsonObjectBuilder merge(Map<String, ?> src) {
		if(src == null){
			return this;
		}
		for (Map.Entry<?, ?> e : src.entrySet()) {
			jsonObj.put(e.getKey().toString(), e.getValue());
		}
		return this;
	}

	/**
	 * Deep merges the given map into the JSON object being built.
	 * @param src the map to deep merge into the JSON object
	 * @return this JsonObjectBuilder instance
	 */
	public JsonObjectBuilder deepMerge(Map<String, ?> src) {
		deepMerge(src, jsonObj);
		return this;
	}

	/**
	 * Recursively deep merges the given map into the JSON object being built. This method is used to handle nested JSON
	 * objects and arrays.
	 * @param src the map to deep merge into the JSON object
	 * @param dest the JSON object to merge the map into
	 */
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

	/**
	 * Updates a nested JSON object in the JSON object being built. If the nested object does not exist, it is added.
	 * @param field the key of the nested object
	 * @param objUpdate a Consumer that updates the nested object using a new JsonObjectBuilder
	 * @return this JsonObjectBuilder instance
	 */
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

	/**
	 * Updates a nested JSON array in the JSON object being built. If the nested array does not exist, it is added.
	 * @param field the key of the nested array
	 * @param arrUpdate a Consumer that updates the nested array using a new JsonArrayBuilder
	 * @return this JsonObjectBuilder instance
	 */
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

	/**
	 * Executes the given Consumer on this JsonObjectBuilder instance.
	 * @param add a Consumer that operates on this JsonObjectBuilder instance
	 * @return this JsonObjectBuilder instance
	 */
	public JsonObjectBuilder doAdd(Consumer<JsonObjectBuilder> add) {
		if(add != null) {
			add.accept(this);
		}
		return this;
	}

	/**
	 * Recursively executes the given Consumer on this JsonObjectBuilder instance and its nested JSON objects and arrays.
	 * @param add a Consumer that operates on this JsonObjectBuilder instance and its nested JSON objects and arrays
	 * @return this JsonObjectBuilder instance
	 */
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

	/**
	 * Returns the JSON object being built.
	 * @return the JSON object being built
	 */
	public Map<String, Object> toMap() {
		return jsonObj;
	}
}
