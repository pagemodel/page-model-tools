package org.pagemodel.core.utils.json;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A utility class for building JSON objects, arrays, and values.
 */
public class JsonBuilder {

	/**
	 * Returns a new {@link JsonObjectBuilder} instance.
	 * @return a new {@link JsonObjectBuilder} instance
	 */
	public static JsonObjectBuilder object(){
		return new JsonObjectBuilder();
	}

	/**
	 * Returns a new {@link JsonObjectBuilder} instance initialized with the given map.
	 * @param obj the map to initialize the builder with
	 * @return a new {@link JsonObjectBuilder} instance initialized with the given map
	 */
	public static JsonObjectBuilder object(Map<String,Object> obj){
		return new JsonObjectBuilder(obj);
	}

	/**
	 * Returns a new {@link JsonArrayBuilder} instance.
	 * @return a new {@link JsonArrayBuilder} instance
	 */
	public static JsonArrayBuilder array(){
		return new JsonArrayBuilder();
	}

	/**
	 * Returns a new {@link JsonArrayBuilder} instance initialized with the given list.
	 * @param arr the list to initialize the builder with
	 * @return a new {@link JsonArrayBuilder} instance initialized with the given list
	 */
	public static JsonArrayBuilder array(List<Object> arr){
		return new JsonArrayBuilder(arr);
	}

	/**
	 * Returns the given value as is.
	 * @param value the value to return
	 * @return the given value as is
	 */
	public static Object value(Object value){
		return value;
	}

	/**
	 * Returns a JSON string representation of the object built by the given consumer.
	 * @param object the consumer that builds the JSON object
	 * @return a JSON string representation of the object built by the given consumer
	 */
	public static String toJsonString(Consumer<JsonObjectBuilder> object){
		return valString(toMap(object));
	}

	/**
	 * Returns a JSON string representation of the given object.
	 * @param object the object to convert to a JSON string
	 * @return a JSON string representation of the given object
	 */
	public static String toJsonString(Object object){
		return valString(object);
	}

	/**
	 * Converts the object built by the given consumer to a map.
	 * @param obj the consumer that builds the JSON object
	 * @return a map representation of the object built by the given consumer
	 */
	public static Map<String,Object> toMap(Consumer<JsonObjectBuilder> obj){
		return object().doAdd(obj).toMap();
	}

	/**
	 * Converts the object built by the given consumer to a map recursively.
	 * @param obj the consumer that builds the JSON object
	 * @return a map representation of the object built by the given consumer, including nested objects
	 */
	public static Map<String,Object> toMapRec(Consumer<JsonObjectBuilder> obj){
		return object().doAddRec(obj).toMap();
	}

	/**
	 * Returns a JSON string representation of a Map object.
	 * @param map the Map object to be converted to a JSON string
	 * @return a JSON string representation of the Map object
	 */
	private static String objString(Map<?,?> map){
		return "{" + String.join(", ", map.entrySet().stream().map(e -> entryString(e)).toArray(String[]::new)) + "}";
	}

	/**
	 * Returns a JSON string representation of a List object.
	 * @param arr the List object to be converted to a JSON string
	 * @return a JSON string representation of the List object
	 */
	private static String arrString(List<?> arr){
		return "[" + String.join(", ", arr.stream().map(obj -> valString(obj)).toArray(String[]::new)) + "]";
	}

	/**
	 * Returns a JSON string representation of an object field name and value.
	 * @param entry the Map.Entry object to be converted to a JSON string
	 * @return a JSON string representation of the Map.Entry object
	 */
	private static String entryString(Map.Entry<?,?> entry){
		String field = "\"" + escapeJsonString(entry.getKey().toString()) + "\"";
		String value = valString(entry.getValue());
		return field +": " + value;
	}

	/**
	 * Returns a JSON string representation of an Object, Array, or value.
	 * @param object the Object to be converted to a JSON string
	 * @return a JSON string representation of the Object
	 */
	private static String valString(Object object){
		if(object instanceof String){
			return "\"" + escapeJsonString((String)object) + "\"";
		}else if(object instanceof Number){
			return object.toString();
		}else if(object instanceof Boolean){
			return object.toString();
		}else if(object instanceof Map){
			return objString((Map) object);
		}else if(object instanceof List){
			return arrString((List) object);
		}else{
			if(object == null){
				return null;
			}
			return "\"" + escapeJsonString(object.toString()) + "\"";
		}
	}

	/**
	 * Escapes special characters in a string to be used in a JSON string.
	 * @param raw the string to be escaped
	 * @return the escaped string
	 */
	private static String escapeJsonString(Object raw) {
		String escaped = "" + raw;
		escaped = escaped.replace("\\", "\\\\");
		escaped = escaped.replace("\"", "\\\"");
		escaped = escaped.replace("\b", "\\b");
		escaped = escaped.replace("\f", "\\f");
		escaped = escaped.replace("\n", "\\n");
		escaped = escaped.replace("\r", "\\r");
		escaped = escaped.replace("\t", "\\t");
		return escaped;
	}
}