package org.pagemodel.tools.screenshots;

import org.pagemodel.core.utils.json.JsonObjectBuilder;
import org.pagemodel.web.utils.RectangleUtils;

import java.awt.*;
import java.util.function.Consumer;

public class AnnotationStyle {
	public final static String FONT_FAMILY = "Serif";
	public final static int FONT_STYLE = Font.BOLD;
	public final static int FONT_SIZE = 22;
	public final static int STROKE_SIZE = 2;
	public final static int IMAGE_PAD = 10;
	public final static int LOCATION_PAD = 4;
	public final static int LOCATION_RADIUS = 10;
	public final static Color COLOR = Color.WHITE;
	public final static Color BACKGROUND_COLOR = Color.RED;
	public final static int LABEL_PAD = 2;
	public final static double LABEL_RADIUS = 1.0;

	public Font font = new Font(FONT_FAMILY, FONT_STYLE, FONT_SIZE);
	public int strokeSize = STROKE_SIZE;
	public int[] imagePadding = RectangleUtils.getPads(IMAGE_PAD);
	public int[] locationPadding = RectangleUtils.getPads(LOCATION_PAD);
	public int locationRadiusPx = LOCATION_RADIUS;
	public Color color = COLOR;
	public Color backgroundColor = BACKGROUND_COLOR;
	public int[] labelPad = RectangleUtils.getPads(LABEL_PAD);
	public int[] labelMargin = RectangleUtils.getPads(LABEL_PAD);
	public double labelRadius = LABEL_RADIUS;
	public Integer labelRadiusPx = null;

	public AnnotationStyle() {}

	public AnnotationStyle(AnnotationStyle o) {
		this.font = o.font;
		this.strokeSize = o.strokeSize;
		this.imagePadding = o.imagePadding;
		this.locationPadding = o.locationPadding;
		this.locationRadiusPx = o.locationRadiusPx;
		this.color = o.color;
		this.backgroundColor = o.backgroundColor;
		this.labelPad = o.labelPad;
		this.labelMargin = o.labelMargin;
		this.labelRadius = o.labelRadius;
		this.labelRadiusPx = o.labelRadiusPx;
	}

	public AnnotationStyle font(Font font) {
		this.font = font;
		return this;
	}

	public AnnotationStyle fontFamily(String family) {
		this.font = new Font(family, font.getStyle(), font.getSize());
		return this;
	}

	public AnnotationStyle fontStyle(int style) {
		this.font = new Font(font.getFamily(), style, font.getSize());
		return this;
	}

	public AnnotationStyle fontSize(int size) {
		this.font = new Font(font.getFamily(), font.getStyle(), size);
		return this;
	}

	public AnnotationStyle color(Color color) {
		this.color = color;
		return this;
	}

	public AnnotationStyle backgroundColor(Color color) {
		this.backgroundColor = color;
		return this;
	}

	public AnnotationStyle strokeSize(int size) {
		this.strokeSize = size;
		return this;
	}

	public AnnotationStyle imagePadding(int...padding) {
		this.imagePadding = RectangleUtils.getPads(padding);
		return this;
	}

	public AnnotationStyle locationPadding(int...padding) {
		this.locationPadding = RectangleUtils.getPads(padding);
		return this;
	}

	public AnnotationStyle locationRadiusPx(int px) {
		this.locationRadiusPx = px;
		return this;
	}

	public AnnotationStyle labelPadding(int...padding) {
		this.labelPad = RectangleUtils.getPads(padding);
		return this;
	}

	public AnnotationStyle labelMargin(int...margin) {
		this.labelMargin = RectangleUtils.getPads(margin);
		return this;
	}

	public AnnotationStyle labelRadius(double pct) {
		this.labelRadius = pct;
		this.labelRadiusPx = null;
		return this;
	}

	public AnnotationStyle labelRadiusPx(int px) {
		this.labelRadiusPx = px;
		return this;
	}

	protected AnnotationStyle set(AnnotationStyle o) {
		this.font = o.font;
		this.strokeSize = o.strokeSize;
		this.imagePadding = o.imagePadding;
		this.locationPadding = o.locationPadding;
		this.locationRadiusPx = o.locationRadiusPx;
		this.color = o.color;
		this.backgroundColor = o.backgroundColor;
		this.labelMargin = o.labelMargin;
		this.labelPad = o.labelPad;
		this.labelRadius = o.labelRadius;
		this.labelRadiusPx = o.labelRadiusPx;
		return this;
	}

	public static Consumer<JsonObjectBuilder> logJson(AnnotationStyle g){
		return j -> j
				.addObject("font", f -> f
						.addValue("size", g.font.getSize())
						.addValue("family", g.font.getFamily())
						.addValue("style", g.font.getStyle()))
				.addValue("stroke", g.strokeSize)
				.addValue("image padding", g.imagePadding)
				.addValue("location padding", g.locationPadding)
				.addValue("location radius", g.locationRadiusPx)
				.addValue("color", g.color)
				.addValue("background color", g.backgroundColor)
				.addValue("label padding", g.labelPad)
				.addValue("label margin", g.labelMargin)
				.addValue("label radius", g.labelRadiusPx != null ? g.labelRadiusPx + "px" : g.labelRadius + "%");
	}
}
