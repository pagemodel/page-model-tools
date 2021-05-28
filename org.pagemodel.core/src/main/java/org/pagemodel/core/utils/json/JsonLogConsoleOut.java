package org.pagemodel.core.utils.json;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JsonLogConsoleOut {
	public static String formatEvent(Map<?,?> jsonEvent) {
		return new JsonLogConsoleOut().toEventString(jsonEvent);
	}
	public static String formatEvent(Consumer<JsonObjectBuilder> jsonEvent) {
		return formatEvent(JsonBuilder.toMap(jsonEvent));
	}

	public String toEventString(Consumer<JsonObjectBuilder> jsonEvent) {
		return getEventString(JsonBuilder.toMap(jsonEvent));
	}
	public String toEventString(Map<?,?> jsonEvent) {
		return getEventString(jsonEvent);
	}

	protected String getEventString(Map<?,?> jsonEvent){
		if(jsonEvent == null || jsonEvent.isEmpty()){
			return null;
		}
		String type = getString("type", jsonEvent);
		Map<String,Object> eval = getObject("eval", jsonEvent);
		Map<String,Object> op = getObject("op", jsonEvent);
		List<Map<String,Object>> source = getValue("source", List.class, jsonEvent);
		return buildEventString(type, eval, op, source);
	}

	protected String buildEventString(String type, Map<String, Object> eval, Map<String, Object> op, List<Map<String,Object>> source) {
		String evalType = getString("type", eval);
		String action = getString("action", op);
		String opString = opToString(op);

		StringBuilder sb = new StringBuilder();
		if(source != null && source.size() == 1){
			sb.append(getEventString(source.get(0))).append("\n\t\t");
		}
		sb.append(type);
		if(evalType != null){
			sb.append(" ").append(evalType);
		}
		sb.append(" ").append(action);
		if(opString != null && !opString.isEmpty()){
			sb.append(": ").append(opString);
		}
		if(source != null && source.size() > 1){
			for(Map<String,Object> src : source) {
				sb.append("\n\t\t").append(getEventString(src));
			}
		}
		return sb.toString();
	}

	protected String opToString(Map<?,?> op){
		if(op == null){
			return null;
		}
		List<String> values = new LinkedList<>();
		for(Map.Entry<?,?> e : op.entrySet()){
			if(e.getKey().equals("exception-obj")){
				if(e.getValue() instanceof Throwable) {
					Throwable t = (Throwable)e.getValue();
					t.printStackTrace();
				}
			}else if(e.getKey().equals("action")){
				continue;
			}else if(e.getKey().equals("html-src")){
				continue;
			}else if(e.getKey().equals("iframe")){
				continue;
			}else if(e.getKey().equals("stacktrace")){
				continue;
			}else if(e.getKey().equals("img-base64")){
				continue;
			}else if(e.getValue() instanceof Map){
				values.add(String.format("%s: [%s]",  e.getKey(), opToString((Map)e.getValue())));
			}else{
				String val = OutputFilter.mask(e.getValue());
				values.add(String.format("%s: [%s]",  e.getKey(), val));
			}
		}
		return String.join(", ", values);
	}

	protected  <T> T getValue(String key, Class<T> type, Map<?, ?> map){
		if(map == null){
			return null;
		}
		Object obj = map.get(key);
		if(obj != null && type.isAssignableFrom(obj.getClass())){
			return (T)obj;
		}
		return null;
	}

	protected String getString(String key, Map<?, ?> map){
		return getValue(key, String.class, map);
	}

	protected Map<String,Object> getObject(String key, Map<?, ?> map){
		return getValue(key, Map.class, map);
	}
}
