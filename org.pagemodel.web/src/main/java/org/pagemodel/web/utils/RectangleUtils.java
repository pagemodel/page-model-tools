package org.pagemodel.web.utils;

import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.json.JsonObjectBuilder;

import java.util.function.Consumer;

public class RectangleUtils {
	public static Rectangle merge(Rectangle a, Rectangle b){
		if(a == null){
			return b;
		}else if(b == null){
			return a;
		}
		int xmin = Math.min(a.x, b.x);
		int xmax = Math.max(a.x + a.width, b.x + b.width);
		int ymin = Math.min(a.y, b.y);
		int ymax = Math.max(a.y + a.height, b.y + b.height);
		return new Rectangle(xmin, ymin, ymax - ymin, xmax - xmin);
	}

	public static Consumer<JsonObjectBuilder> rectangleJson(Rectangle a){
		if(a == null){
			return json -> json
					.addValue("x", "null")
					.addValue("y", "null");
		}
		return json -> json
				.addValue("x", a.x)
				.addValue("y", a.y)
				.addValue("width", a.width)
				.addValue("height", a.height);
	}

	public static Consumer<JsonObjectBuilder> pointJson(Point a){
		if(a == null){
			return json -> json
					.addValue("x", "null")
					.addValue("y", "null");
		}
		return json -> json
				.addValue("x", a.x)
				.addValue("y", a.y);
	}

	public static Rectangle pad(Rectangle rect, int top, int right, int bottom, int left) {
		return pad(rect, top, right, bottom, left, null);
	}

	public static Rectangle pad(Rectangle rect, int top, int right, int bottom, int left, TestEvaluator evaluator) {
		if(rect == null){
			return null;
		}
		int top2 = Math.min(rect.x, top);
		int left2 = Math.min(rect.y, left);
		Rectangle newRect = new Rectangle(rect.x - left2, rect.y - top2, rect.height + top2 + bottom, rect.width + left2 + right);
		if(evaluator != null) {
			evaluator.logEvent(TestEvaluator.TEST_BUILD,
					"pad rectangle", op -> op
							.addValue("x", rect.x)
							.addValue("y", rect.y)
							.addValue("width", rect.width)
							.addValue("height", rect.height)
							.addValue("top", top2)
							.addValue("right", right)
							.addValue("bottom", bottom)
							.addValue("left", left2)
							.addObject("new", RectangleUtils.rectangleJson(newRect)));
		}
		return newRect;
	}
}
