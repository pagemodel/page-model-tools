package org.pagemodel.core.utils.json;

import java.util.LinkedHashMap;
import java.util.Map;

public class OutputFilter {
	private final static Map<String,String> replacements = new LinkedHashMap<>();
	private final static String MASK = "********";

	public static void addMaskedString(String string){
		addReplacement(string, MASK);
	}

	public static void addReplacement(String string, String replacement){
		if(string == null){
			return;
		}
		if(replacement == null){
			replacement = MASK;
		}
		replacements.put(string, replacement);
	}

	public static String mask(Object object){
		if(object == null){
			return "null";
		}
		String string = object.toString();
		for(Map.Entry<String,String> e : replacements.entrySet()){
			string = string.replace(e.getKey(), e.getValue());
		}
		return string;
	}
}
