package org.pagemodel.web.utils;

import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.json.JsonObjectBuilder;

import java.util.function.Consumer;

public class RectangleUtils {
	public static int TOP = 0;
	public static int RIGHT = 1;
	public static int BOTTOM = 2;
	public static int LEFT = 3;

	private static Rectangle merge(Rectangle a, Rectangle b, boolean setWidth, boolean setHeight){
		if(a == null){
			return b;
		}else if(b == null){
			return a;
		}
		int xmin = setWidth ? Math.min(a.x, b.x) : a.x;
		int xmax = Math.max(a.x + a.width, b.x + b.width);
		int ymin = setHeight ? Math.min(a.y, b.y) : a.y;
		int ymax = Math.max(a.y + a.height, b.y + b.height);
		int width = setWidth ? xmax - xmin : a.width;
		int height = setHeight ? ymax - ymin : a.height;
		return new Rectangle(xmin, ymin, height, width);
	}

	public static Rectangle merge(Rectangle a, Rectangle b){
		return merge(a, b, true, true);
	}

	public static Rectangle mergeHeight(Rectangle a, Rectangle b){
		return merge(a, b, false, true);
	}

	public static Rectangle mergeWidth(Rectangle a, Rectangle b){
		return merge(a, b, true, false);
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

	public static Rectangle pad(Rectangle rect, int...padding) {
		return pad(null, rect, padding);
	}

	public static Rectangle pad(TestEvaluator evaluator, Rectangle rect, int...padding) {
		if(rect == null){
			return null;
		}
		if(padding.length == 0){
			return rect;
		}
		int[] pads = getPads(padding);
		int top2 = Math.min(rect.x, pads[TOP]);
		int left2 = Math.min(rect.y, pads[LEFT]);
		Rectangle newRect = new Rectangle(rect.x - left2, rect.y - top2, rect.height + top2 + pads[BOTTOM], rect.width + left2 + pads[RIGHT]);
		if(evaluator != null) {
			evaluator.logEvent(TestEvaluator.TEST_BUILD,
					"pad rectangle", op -> op
							.addValue("x", rect.x)
							.addValue("y", rect.y)
							.addValue("width", rect.width)
							.addValue("height", rect.height)
							.addValue("top", top2)
							.addValue("right", pads[RIGHT])
							.addValue("bottom", pads[BOTTOM])
							.addValue("left", left2)
							.addObject("new", RectangleUtils.rectangleJson(newRect)));
		}
		return newRect;
	}

	public static int[] getPads(int...pads){
		if(pads.length == 0) {
			return new int[]{0, 0, 0, 0};
		} else if(pads.length == 1) {
			return new int[]{pads[0], pads[0], pads[0], pads[0]};
		} else if(pads.length == 2) {
			return new int[]{pads[0], pads[1], pads[0], pads[1]};
		} else if(pads.length == 3) {
			return new int[]{pads[0], pads[1], pads[2], pads[1]};
		} else {
			return new int[]{pads[0], pads[1], pads[2], pads[3]};
		}
	}
}
