package org.pagemodel.core.utils.json;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JsonBuilder {

	public static JsonObjectBuilder object(){
		return new JsonObjectBuilder();
	}

	public static JsonObjectBuilder object(Map<String,Object> obj){
		return new JsonObjectBuilder(obj);
	}

	public static JsonArrayBuilder array(){
		return new JsonArrayBuilder();
	}

	public static JsonArrayBuilder array(List<Object> arr){
		return new JsonArrayBuilder(arr);
	}

	public static Object value(Object value){
		return value;
	}

	public static String toJsonString(Consumer<JsonObjectBuilder> object){
		return valString(toMap(object));
	}
	public static String toJsonString(Object object){
		return valString(object);
	}


	public static Map<String,Object> toMap(Consumer<JsonObjectBuilder> obj){
		return object().doAdd(obj).toMap();
	}
	public static Map<String,Object> toMapRec(Consumer<JsonObjectBuilder> obj){
		return object().doAddRec(obj).toMap();
	}
	private static String objString(Map<?,?> map){
		return "{" + String.join(", ", map.entrySet().stream().map(e -> entryString(e)).toArray(String[]::new)) + "}";
	}

	private static String arrString(List<?> arr){
		return "[" + String.join(", ", arr.stream().map(obj -> valString(obj)).toArray(String[]::new)) + "]";

	}

	private static String entryString(Map.Entry<?,?> entry){
		String field = "\"" + escapeJsonString(entry.getKey().toString()) + "\"";
		String value = valString(entry.getValue());
		return field +": " + value;
	}

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
