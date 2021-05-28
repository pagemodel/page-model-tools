package org.pagemodel.core.utils;

import org.pagemodel.core.utils.json.OutputFilter;

public class HtmlUtils {
	public static String htmlDataUri(String html){
		String str = OutputFilter.mask(html)
				.replace("%", "%25")
				.replace("&", "%26")
				.replace("#", "%23")
				.replace("\"", "%22")
				.replace("'", "%27");
		return "data:text/html;charset=UTF-8," + str;
	}

	public static String htmlIframeUri(String html){
			String outerHtml =
					"<html><head><title>view html</title></head><body>"
							+ "<iframe width='100%%' height='100%%' sandbox src='%s'></iframe>"
							+ "</body></html>";
			String innerHtmlURI = htmlDataUri(html);
			String pageHtml = String.format(outerHtml, innerHtmlURI);
			return htmlDataUri(pageHtml);
		}
	}
