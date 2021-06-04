package org.pagemodel.core.utils.json;

import org.pagemodel.core.utils.HtmlUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonLogHtmlOut extends JsonLogConsoleOut{

	public static String formatEvent(Map<?,?> jsonEvent) {
		return new JsonLogHtmlOut().toEventString(jsonEvent);
	}
	public static String formatEvent(Consumer<JsonObjectBuilder> jsonEvent) {
		return JsonLogHtmlOut.formatEvent(JsonBuilder.toMap(jsonEvent));
	}

	@Override
	protected String buildEventString(String type, Map<String, Object> eval, Map<String, Object> op, List<Map<String, Object>> source) {
		DivBuilder div = new DivBuilder();
		openEventDiv(div, type, op);
		if(source != null && source.size() == 1) {
			div.openDiv("event-row source-row");
			addObject(div, source.get(0), false);
			div.closeDiv();
		}
		addRowModifier(div, type, op);
		if(source != null && source.size() > 1) {
			div.openDiv("event-row source-row");
		} else {
			div.openDiv("event-row");
		}
		addField(div, "type", type);
		addField(div, "eval", eval);
		addField(div, "op", op);
		div.closeDiv();
		addRowModifierClose(div, type, op);

		if(source != null && source.size() > 1) {
			for(Map<String, Object> src : source.subList(0, source.size() - 1)){
				div.openDiv("event-row source-row");
				addObject(div, src, false);
				div.closeDiv();
			}
			div.openDiv("event-row");
			addObject(div, source.get(source.size()-1), false);
			div.closeDiv();
		}
		closeEventDiv(div, type, op);
		return div.append("\n").toString();
	}

	protected DivBuilder addField(DivBuilder div, String name, Object item){
		String type = "value";
		if(item instanceof Map){
			type = "object";
		}else if(item instanceof List){
			type = "array";
		}
		div.openDiv("field-display " + type + "-field name-" + name);
		if(name != null){
			div.openDiv("field-name").append(name).closeDiv();
		}
		addFilteredFieldItem(div, name, item);
		return div.closeDiv();
	}

	protected void openEventDiv(DivBuilder div, String type, Map<String, Object> op){
		Object action = op.get("action");
		String actionClass = ("event-action-" + action).replaceAll("[^a-zA-Z0-9_-]", "-");
		if(type.equals("Test")){
			Object testId = op.get("testId");
			if(testId != null && testId instanceof String && action != null && action instanceof String){
				if(action.equals("start")){
					div.append("<a class='test-anchor test-start-a' name='start-" + testId + "'></a>");
				}else if(action.equals("end")){
					div.append("<a class='test-anchor test-end-a' name='end-" + testId + "'></a>");
				}
			}
			if(action.equals("summary")){
				div.openEl("span", "event-item event-type-" + type + " " + actionClass);
			}else {
				div.openDiv("event-item event-type-" + type + " " + actionClass);
			}
		}else{
			div.openDiv("event-ts").append(new SimpleDateFormat("[HH:mm:ss.SSS]").format(new Date())).closeDiv();
			div.openDiv("event-item event-type-" + type + " " + actionClass);
		}
	}

	protected void closeEventDiv(DivBuilder div, String type, Map<String, Object> op){
		Object action = op.get("action");
		if(type.equals("Test") && action != null && action.equals("summary")){
			div.closeEl("span");
		}else{
			div.closeDiv();
		}
	}

	protected void addRowModifier(DivBuilder div, String type, Map<String, Object> op){
		if(type.equals("Test")){
			Object testId = op.get("testId");
			Object action = op.get("action");
			if(testId != null && testId instanceof String && action != null && action instanceof String){
				if(action.equals("pass")){
					div.append("<a class='test-link test-pass-a' href='#start-" + testId + "'>");
				}else if(action.equals("fail")){
					div.append("<a class='test-link test-fail-a' href='#start-" + testId + "'>");
				}
			}
		}
	}

	protected void addRowModifierClose(DivBuilder div, String type, Map<String, Object> op){
		if(type.equals("Test")){
			Object testId = op.get("testId");
			if(testId != null && testId instanceof String){
				Object action = op.get("action");
				if(action != null && action instanceof String){
					if(action.equals("pass") || action.equals("fail")){
						div.append("</a>");
					}
				}
			}
		}
	}

	protected DivBuilder addFilteredFieldItem(DivBuilder div, String name, Object item) {
		if (name.equals("exception-obj")) {
			return div;
		}else if (name.equals("img-base64")) {
			return div.openDiv("field-value")
					.append("<img class='inline-image' src='data:image/png;base64, ")
					.append(OutputFilter.mask(item.toString())).append("'>")
					.closeDiv();
		} else if (name.equals("iframe")) {
			return addHtmlValue(div,item);
		} else if(name.equals("duration") && item instanceof Long){
			//assume milliseconds for long
			item = formatDuration((long)item);
		} else if(name.equals("duration") && item instanceof Integer){
			//assume seconds for integers
			item = formatDuration((int)item * 1000l);
		}
		return addItem(div, item);
	}

	public static String formatDuration(long msec){
		long days = TimeUnit.MILLISECONDS.toDays(msec);
		long hours = TimeUnit.MILLISECONDS.toHours(msec) % 24;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(msec) % 60;
		long seconds = TimeUnit.MILLISECONDS.toSeconds(msec) % 60;
		long milliseconds = msec % 1000;
		List<String> parts = new LinkedList<>();
		if(days > 0){
			parts.add(days + "d");
		}
		if(hours > 0 || !parts.isEmpty()){
			parts.add(hours + "h");
		}
		if(minutes > 0 || !parts.isEmpty()){
			parts.add(minutes + "m");
		}
		String sec = Long.toString(seconds);
		if(milliseconds > 0){
			sec += "." + milliseconds;
		}
		sec += "s";
		parts.add(sec);
		return String.join(" ",parts);
	}

	public static String linkUrls(String text){
		String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(text);

		StringBuilder sb = new StringBuilder();
		int lastEnd = 0;
		while (urlMatcher.find()) {
			int start = urlMatcher.start(0);
			int end = urlMatcher.end(0);
			String pre = text.substring(lastEnd, start);
			String url = text.substring(start, end);
			sb.append(pre);
			sb.append(makeLink(url));
			lastEnd = end;
		}
		sb.append(text.substring(lastEnd));
		return sb.toString();
	}

	private static String makeLink(String url){
		if(url == null || url.isEmpty()){
			return url;
		}
		return "<a class='url-field' href='" + url + "' target='_blank' rel='noopener noreferrer'>" + url + "</a>";
	}

	protected DivBuilder addObject(DivBuilder div, Map<String,Object> obj){
		return addObject(div, obj,  true);
	}

	protected DivBuilder addObject(DivBuilder div, Map<String,Object> obj, boolean addDiv){
		if(addDiv) {
			div.openDiv("field-value object-value");
		}
		for(Map.Entry<String,Object> e : obj.entrySet()){
			addField(div, e.getKey(), e.getValue());
		}
		if(addDiv) {
			div.closeDiv();
		}
		return div;
	}

	protected DivBuilder addArray(DivBuilder div, List<Object> objs){
		div.openDiv("field-value array-value");
		for(Object obj : objs){
			div.openDiv("array-item-display");
			addItem(div, obj);
			div.closeDiv();
		}
		return div.closeDiv();
	}

	protected String escapeHtml(String string){
		String str = OutputFilter.mask(string)
				.replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
		str = linkUrls(str);
		return str;
	}

	protected String escapeHtmlPre(String string){
		String str = OutputFilter.mask(string)
				.replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
		str = linkUrls(str);
		return str;
	}

	protected DivBuilder addValue(DivBuilder div, Object value){
		return addValue(div, value, null);
	}

	protected DivBuilder addValue(DivBuilder div, Object value, String classnames, String...attrs){
		String str = escapeHtml("" + value);
		String cn = classnames == null || classnames.isEmpty() ? "" : " " + classnames;
		if(str.contains("\n")){
			cn += " multiline";
		}
		return div.openDiv("field-value" + cn, attrs)
				.append(str)
				.closeDiv();
	}

	protected DivBuilder addHtmlValue(DivBuilder div, Object value){
		String dataURI = HtmlUtils.htmlDataUri("" + value);
		return div.openDiv("field-value")
				.openEl("iframe", "html-view", "src", dataURI, "sandbox", "")
				.closeEl("iframe")
				.closeDiv();
	}



	protected DivBuilder addItem(DivBuilder div, Object item){
		if(item instanceof Map){
			return addObject(div, (Map<String, Object>)item);
		}else if(item instanceof List){
			return addArray(div, (List<Object>)item);
		}else{
			return addValue(div, item);
		}
	}

	static class DivBuilder {
		private StringBuilder sb = new StringBuilder();

		public String toString(){
			return sb.toString();
		}

		public DivBuilder append(String string){
			sb.append(string);
			return this;
		}

		public DivBuilder openDiv(String classes, String...attrs){
			return openEl("div", classes, attrs);
		}

		public DivBuilder openEl(String tagName, String classes, String...attrs){
			sb.append("<").append(tagName);
			if(classes != null && !classes.isEmpty()){
				sb.append(" class=\"").append(classes).append("\"");
			}
			int len = attrs.length;
			for(int i=0; i<len; i+=2){
				sb.append(" ").append(attrs[i]);
				String val = i+1 < len ? attrs[i+1] : null;
				if(val != null){
					sb.append("=\"" + val + "\"");
				}
			}
			sb.append(">");
			return this;
		}

		public DivBuilder closeDiv(){
			return closeEl("div");
		}

		public DivBuilder closeEl(String tagName){
			sb.append("</" + tagName + ">\n");
			return this;
		}
	}

	public static final String htmlHeader = "<html>\n"
			+ "<head>\n"
			+ "  <style>\n"
			+ "    body {\n"
			+ "        background-color: #fff;\n"
			+ "        font-family: Verdana, Arial, SunSans-Regular, Sans-Serif;\n"
			+ "        font-size: 0.8em;\n"
			+ "        color: #000;\n"
			+ "        padding: 0px;\n"
			+ "        margin: 0px;\n"
			+ "        margin-top:10px;\n"
			+ "    }\n"
			+ "\n"
			+ "    .value-field > .field-name {\n"
			+ "        padding-left: 10px;\n"
			+ "    }\n"
			+ "\n"
			+ "    .field-name, .value-field > .field-value, .array-item-display {\n"
			+ "        border-right: 1px solid #000;\n"
			+ "        border-bottom: 1px solid #000;\n"
			+ "        border-top: 1px solid #000;\n"
			+ "        border-left: 1px solid #000;\n"
			+ "        margin-left: -1px;\n"
			+ "        margin-bottom: -1px;\n"
			+ "        padding: 5px 5px;\n"
			+ "        min-height:1.3em;\n"
			+ "        background-color: #f9f9f9;\n"
			+ "    }\n"
			+ "\n"
			+ "    .value-field > .field-value, .array-item-display > .field-value {\n"
			+ "        font-family: monospace;\n"
			+ "        font-size: 1.1em;\n"
			+ "        line-height: 1.3em;\n"
			+ "\n"
			+ "        white-space: pre-wrap;\n"
			+ "        white-space: -moz-pre-wrap;\n"
			+ "        white-space: -pre-wrap;\n"
			+ "        white-space: -o-pre-wrap;\n"
			+ "\n"
			+ "        word-wrap: anywhere;\n"
			+ "        word-break: break-word;\n"
			+ "    }\n"
			+ "\n"
			+ "    .multiline {\n"
			+ "        max-height:800px;\n"
			+ "        overflow: auto;\n"
			+ "    }\n"
			+ "\n"
			+ "    .object-value, .array-value {\n"
			+ "        padding: 0px;\n"
			+ "        border-right: 0px none #000;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-item {\n"
			+ "        margin-bottom: 10px;\n"
			+ "        border-right: 1px solid #aeaeae;\n"
			+ "        background: #f5f5f5;\n"
			+ "        border-left: 5px solid #555;\n"
			+ "\n"
			+ "        margin-left: 20px;\n"
			+ "        margin-right: 10px;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-item.event-type-Test.event-action-start,\n"
			+ "    .event-item.event-type-Test.event-action-end {\n"
			+ "        margin-bottom: 25px;\n"
			+ "        border-left: 15px solid #15a;\n"
			+ "        margin-left: 10px;\n"
			+ "        padding-bottom: 1px;\n"
			+ "    }\n"
			+ "    .event-item.event-type-Test.event-action-start {\n"
			+ "        position: sticky;\n"
			+ "        top: 3em;\n"
			+ "        margin-top: 25px;\n"
			+ "    }\n"
			+ "    .event-item.event-type-Test .event-row > .name-eval,\n"
			+ "    .event-item.event-type-Test .event-row > .name-op > .object-value > .name-class > .field-name,\n"
			+ "    .event-item.event-type-Test .event-row > .name-op > .object-value > .name-method > .field-name {\n"
			+ "        display:none;\n"
			+ "    }\n"
			+ "    \n"
			+ "    .event-item.event-action-property > .event-row > .name-eval {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-row > .name-type > .field-value {\n"
			+ "        font-family: unset;\n"
			+ "        border-left: 1px solid #000;\n"
			+ "        width: 4.28em;\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-eval > .field-value > .name-type > .field-value {\n"
			+ "        font-family: unset;\n"
			+ "        width: 4em;\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-op > .field-value > .name-action > .field-value {\n"
			+ "        font-family: unset;\n"
			+ "        min-width: 9em;\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-parent {\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-parent > div {\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-parent > .field-name {\n"
			+ "        border-left: 1px solid #000;\n"
			+ "    }\n"
			+ "\n"
			+ "    .field-name::after {\n"
			+ "        content: \":\";\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-type > .field-name,\n"
			+ "    .name-eval > .field-name,\n"
			+ "    .name-eval > .field-value > .name-timeout,\n"
			+ "    .name-eval > .field-value > .name-pageSetup,\n"
			+ "    .name-op > .field-name,\n"
			+ "    .name-op > .field-value > .name-action > .field-name,\n"
			+ "    .name-by,\n"
			+ "    .name-element > .object-value > .name-name > .field-name,\n"
			+ "    .name-parent > .object-value > .name-name > .field-name,\n"
			+ "    .name-element > .field-name {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "\n"
			+ "    .check-style {\n"
			+ "        margin-right: 20px;\n"
			+ "        display:none;\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-img-base64 {\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-img-base64 > .field-name {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "\n"
			+ "    .inline-image {\n"
			+ "        max-width: min(100%, 800px);\n"
			+ "        max-height:800px;\n"
			+ "        height: auto;\n"
			+ "    }\n"
			+ "\n"
			+ "    .header-menu {\n"
			+ "        position: fixed;\n"
			+ "        top: 0;\n"
			+ "        width: 100%;\n"
			+ "        height: 1.5em;\n"
			+ "        border-bottom: 1px solid #333;\n"
			+ "        background-color: #f9f3e9;\n"
			+ "        padding: 0.5em 1em;\n"
			+ "    }\n"
			+ "    .header-menu::after {\n"
			+ "        content: \"\";\n"
			+ "        clear: both;\n"
			+ "        display: table;\n"
			+ "    }\n"
			+ "\n"
			+ "    .header-menu > * {\n"
			+ "        margin-left: 1.5em;\n"
			+ "        color: #117;\n"
			+ "    }\n"
			+ "    .header-menu > label {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "    .header-menu > label:hover,\n"
			+ "    .header-menu > a:hover {\n"
			+ "        color: #25d;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-flex {\n"
			+ "        display: flex;\n"
			+ "        flex-wrap: wrap;\n"
			+ "        flex-direction: column;\n"
			+ "        margin-top: 3.5em;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-action-start:nth-child(1) {\n"
			+ "        margin-top: 25px;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-type-Test.event-action-run,\n"
			+ "    .event-type-Test.event-action-pass,\n"
			+ "    .event-type-Test.event-action-fail {\n"
			+ "        order: -1;\n"
			+ "        flex: 1 0 100%;\n"
			+ "        margin-left: 10px;\n"
			+ "        margin-bottom: 8px;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-type-Test.event-action-run {\n"
			+ "        border-left: 15px solid #15a;\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-type-Test.event-action-fail {\n"
			+ "        border-left: 15px solid #d11;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-type-Test.event-action-pass {\n"
			+ "        border-left: 15px solid #1a1;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-type-Test .name-op .name-testId,\n"
			+ "    .event-type-Test.event-action-pass .name-op .name-end,\n"
			+ "    .event-type-Test.event-action-pass .name-op .name-start,\n"
			+ "    .event-type-Test.event-action-fail .name-op .name-end,\n"
			+ "    .event-type-Test.event-action-fail .name-op .name-start {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "\n"
			+ "    a.test-anchor {\n"
			+ "        display: block;\n"
			+ "        visibility: hidden;\n"
			+ "    }\n"
			+ "    .test-link,\n"
			+ "    .test-link:visited{\n"
			+ "        color: #117;\n"
			+ "    }\n"
			+ "    .test-link:hover{\n"
			+ "        color: #22a;\n"
			+ "    }\n"
			+ "\n"
			+ "    .field-display {\n"
			+ "        display: flex;\n"
			+ "        flex-direction: row;\n"
			+ "    }\n"
			+ "\n"
			+ "    .field-value {\n"
			+ "        display: flex;\n"
			+ "        flex-direction: row;\n"
			+ "        flex-wrap: wrap;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-item {\n"
			+ "        display: flex;\n"
			+ "        flex-direction: column;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-row {\n"
			+ "        margin-top: 8px;\n"
			+ "        margin-right: -1px;\n"
			+ "        padding-bottom: 1px;\n"
			+ "\n"
			+ "        display: flex;\n"
			+ "        flex-direction: row;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-row:nth-child(1) {\n"
			+ "        border-top: 1px solid #aeaeae;\n"
			+ "        margin-top: 0px;\n"
			+ "    }\n"
			+ "    .event-row:last-child {\n"
			+ "        border-bottom: 1px solid #aeaeae;\n"
			+ "        padding-bottom: 0px;\n"
			+ "    }\n"
			+ "\n"
			+ "\n"
			+ "    .event-row:nth-child(1) > .field-display {\n"
			+ "        margin-top: -1px;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-action-summary {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "\n"
			+ "    span.event-action-summary:last-of-type {\n"
			+ "        display: flex;\n"
			+ "        order: -2;\n"
			+ "        flex: 1 0 100%;\n"
			+ "        margin-left: 10px;\n"
			+ "        margin-bottom: 20px;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-ts {\n"
			+ "        margin-left: 17px;\n"
			+ "        margin-bottom: 2px;\n"
			+ "        font-size: 1.1em;\n"
			+ "        font-family: monospace;\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-found > .field-name,\n"
			+ "    .name-parent > .field-name {\n"
			+ "        min-width: 4em;\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-iframe > .field-name,\n"
			+ "    .name-html-src > .field-name {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "    .name-iframe,\n"
			+ "    .name-iframe > .field-value,\n"
			+ "    .name-iframe > .field-value > iframe {\n"
			+ "        width:100%;\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-iframe > .field-value > iframe {\n"
			+ "        height: 60em;\n"
			+ "    }\n"
			+ "\n"
			+ "    #topAnchor {\n"
			+ "        position: absolute;\n"
			+ "        top: 0px;\n"
			+ "    }\n"
			+ "\n"
			+ "    /* ========= start exceptions style ========= */\n"
			+ "\n"
			+ "    .event-type-Error {\n"
			+ "        border-left: 5px solid #d11;\n"
			+ "        border-left: 5px solid #d11;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-type-Error > .event-row > .name-eval {\n"
			+ "        display:none;\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-stacktrace > .array-value > .array-item-display .field-value,\n"
			+ "    .name-stacktrace > .array-value > .array-item-display {\n"
			+ "        border: none;\n"
			+ "        padding: 1px;\n"
			+ "\n"
			+ "        display: flex;\n"
			+ "        flex-direction: row;\n"
			+ "        flex-wrap: wrap;\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-stacktrace > .field-value.array-value {\n"
			+ "        border: 1px solid #000;\n"
			+ "        margin-left: -1px;\n"
			+ "        margin-bottom: -1px;\n"
			+ "        padding: 4px;\n"
			+ "        background-color: #f9f9f9;\n"
			+ "\n"
			+ "        display: flex;\n"
			+ "        flex-direction: column;\n"
			+ "        flex-wrap: wrap;\n"
			+ "    }\n"
			+ "    .name-stacktrace {\n"
			+ "    }\n"
			+ "    .name-stacktrace > .field-name {\n"
			+ "        display:none;\n"
			+ "        width: 9em;\n"
			+ "    }\n"
			+ "    .name-stacktrace .name-method {\n"
			+ "      font-weight: bold;\n"
			+ "      color: #333;\n"
			+ "    }\n"
			+ "    .name-stacktrace .name-method > .field-value::before {\n"
			+ "        content: \".\";\n"
			+ "    }\n"
			+ "    .name-stacktrace .name-file > .field-value::before {\n"
			+ "        content: \"(\";\n"
			+ "    }\n"
			+ "    .name-stacktrace .name-line > .field-value::before {\n"
			+ "        content: \":\";\n"
			+ "    }\n"
			+ "    .name-stacktrace .name-line > .field-value::after {\n"
			+ "        content: \")\";\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-stacktrace::before,\n"
			+ "    .name-stacktrace > .array-value::before,\n"
			+ "    .name-stacktrace > .array-value > .array-item-display::after {\n"
			+ "        content: \"\";\n"
			+ "        clear: both;\n"
			+ "        display: table;\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-stacktrace > .array-value > .array-item-display .field-name {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "\n"
			+ "    .name-stacktrace > .array-value > .array-item-display .field-value {\n"
			+ "        border: none;\n"
			+ "        margin: 0px;\n"
			+ "    }\n"
			+ "    \n"
			+ "    .name-hl-method ~ .value-field > .field-value {\n"
			+ "        background: #d11;\n"
			+ "        color: #fff;\n"
			+ "    }\n"
			+ "    .name-hl-class ~ .value-field > .field-value {\n"
			+ "        background: #d05c5c;\n"
			+ "        color: #fff;\n"
			+ "    }\n"
			+ "    .name-hl-package ~ .value-field > .field-value {\n"
			+ "        background: #f2a2a2;\n"
			+ "    }\n"
			+ "    .name-hl-group ~ .value-field > .field-value {\n"
			+ "        background: #ffe6e6;\n"
			+ "    }\n"
			+ "    .name-hl-pmt ~ .value-field > .field-value {\n"
			+ "        color: #999;\n"
			+ "        font-size: 0.9em;    \n"
			+ "    }\n"
			+ "    .name-hl-system ~ .value-field > .field-value {\n"
			+ "        color: #b6b6b6;\n"
			+ "        font-size: 0.9em;    \n"
			+ "    }\n"
			+ "\n"
			+ "    [class*='name-hl-'] {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "\n"
			+ "    /* ========= end exceptions style ========= */\n"
			+ "\n"
			+ "    /* ========= start mail style ========= */\n"
			+ "\n"
			+ "    .array-field.name-source > .array-value {\n"
			+ "        border: 0px;\n"
			+ "    }\n"
			+ "\n"
			+ "    .array-field.name-source > .array-value > .array-item-display {\n"
			+ "        margin: 0px;\n"
			+ "        padding: 0px;\n"
			+ "        border: none;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-action-fetch-mail .name-actual, \n"
			+ "    .array-field.name-source .name-mail {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "\n"
			+ "    .array-field.name-source .value-field > .field-value {\n"
			+ "        padding: 7px;\n"
			+ "    }\n"
			+ "\n"
			+ "    .event-row > .name-source > .array-value > .array-item-display .field-value.object-value {\n"
			+ "        background: #f5f5f5;\n"
			+ "    }\n"
			+ "\n"
			+ "    .field-display.array-field.name-source {\n"
			+ "        margin-right: 1px;\n"
			+ "        background: #f5f5f5;\n"
			+ "    }\n"
			+ "\n"
			+ "    /* =========== end mail style ========= */\n"
			+ "\n"
			+ "    /* =========== start log style ========= */\n"
			+ "\n"
			+ "    .event-item.event-type-Log > .event-row > .name-eval,\n"
			+ "    .event-item.event-type-Log.event-action-log > .event-row > .name-op .name-action {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "\n"
			+ "    /* =========== end log style ========= */"
			+ "</style>\n"
			+ "</head>\n"
			+ "<body>\n"
			+ "<a id=\"topAnchor\" name=\"top\"></a>\n"
			+ "<div class=\"event-flex\">\n"
			+ "<div class=\"header-menu\">\n"
			+ "    <a id=\"topLink\" href=\"#top\">Go to Top</a>\n"
			+ "</div>";

}
