package org.pagemodel.tools.screenshots;

import org.openqa.selenium.Rectangle;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.ThrowingConsumer;
import org.pagemodel.core.utils.ThrowingFunction;
import org.pagemodel.tools.ExtendedTestContext;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.testers.HasPageBounds;
import org.pagemodel.web.testers.ImageAnnotator;
import org.pagemodel.web.testers.PageBoundsHelper;
import org.pagemodel.web.testers.RectangleTester;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.pagemodel.web.utils.RectangleUtils.*;

public class LocationGroup<P extends PageModel<? super P>> {
	protected ExtendedTestContext context;
	protected P page;
	protected String groupPrefix;
	protected String name;
	protected ThrowingFunction<P, HasPageBounds,?> groupLocation;
	protected List<NamedLocation<P>> locations;
	protected RectangleTester<P> mergedBounds;

	protected ThrowingConsumer<P,?> screenshotSetup;
	protected ThrowingConsumer<P,?> screenshotCleanup;
	protected ThrowingConsumer<P,?> annotationSetup;
	protected ThrowingConsumer<P,?> annotationCleanup;
	protected List<String> storePlaceholderNames;

	protected boolean annotated = true;
	protected boolean placeholder = false;
	protected AnnotationStyle annotationStyle;

	public LocationGroup(P page) {
		this(page, new ArrayList<>(), null);
	}

	protected LocationGroup(P page, List<String> storePlaceholderNames, AnnotationStyle style) {
		this.groupPrefix = "";
		this.name = "";
		this.locations = new ArrayList<>();
		this.page = page;
		this.storePlaceholderNames = storePlaceholderNames;
		this.annotationStyle = style != null ? style : new AnnotationStyle()
				.font(new Font("mono", Font.BOLD, 22))
				.color(Color.WHITE)
				.backgroundColor(Color.RED)
				.strokeSize(2)
				.imagePadding(10)
				.locationPadding(4);
	}

	public LocationGroup<P> log(){
		TestEvaluator testEvaluator = page != null ? page.getEvaluator() : new TestEvaluator.Now();
		testEvaluator.logEvent(TestEvaluator.TEST_LOG, "LocationGroup", j -> j
				.addValue("order", this.getGroupPrefix())
				.addValue("name", this.getName())
				.addValue("placeholder", placeholder)
				.addValue("annotated", this.annotated)
				.addValue("children", this.locations.size())
		);
		return this;
	}

	public LocationGroup<P> withGroupStyle(ThrowingFunction<AnnotationStyle, AnnotationStyle,?> updateStyle){
		annotationStyle = ThrowingFunction.unchecked(updateStyle).apply(annotationStyle);
		return this;
	}

	public LocationGroup<P> withLocationStyle(ThrowingFunction<AnnotationStyle, AnnotationStyle,?> updateStyle){
		if(locations.size() == 0){
			return this;
		}
		NamedLocation<P> loc = locations.get(locations.size()-1);
		AnnotationStyle locStyle = loc.annotationStyle;
		if(locStyle == null){
			locStyle = new AnnotationStyle(annotationStyle);
		}
		loc.withStyle(ThrowingFunction.unchecked(updateStyle).apply(locStyle));
		return this;
	}

	public LocationGroup<P> withGroupPrefix(String groupPrefix) {
		this.groupPrefix = groupPrefix;
		return this;
	}

	public String getGroupPrefix() {
		return groupPrefix;
	}

	public LocationGroup<P> withName(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return name;
	}

	public LocationGroup<P> withLocation(ThrowingFunction<P, HasPageBounds, ?> groupLocation) {
		this.groupLocation = groupLocation;
		return this;
	}

	public LocationGroup<P> annotated() {
		this.annotated = true;
		return this;
	}

	public LocationGroup<P> notAnnotated() {
		this.annotated = false;
		return this;
	}

	public LocationGroup<P> add(String name, ThrowingFunction<P, HasPageBounds, ?> getBounds) {
		locations.add(new NamedLocation<P>(name, getBounds, this));
		return this;
	}

	public LocationGroupWithPlaceholder<P> addPlaceholder(String name, ThrowingFunction<P, HasPageBounds, ?> getBounds) {
		LocationGroup<P> newGroup = new LocationGroup<P>(page, storePlaceholderNames, new AnnotationStyle(annotationStyle)).withLocation(getBounds).notAnnotated();
		locations.add(new NamedLocation<P>(name, newGroup, this).placeholder().notAnnotated());
		return new LocationGroupWithPlaceholder<>(this);
	}

	public LocationGroupWithPlaceholder<P> addPlaceholder(String name) {
		LocationGroup<P> newGroup = new LocationGroup<P>(page, storePlaceholderNames, new AnnotationStyle(annotationStyle)).notAnnotated();
		locations.add(new NamedLocation<P>(name, newGroup, this).placeholder().notAnnotated());
		return new LocationGroupWithPlaceholder<>(this);
	}

	public LocationGroupWithPlaceholder<P> addTextPlaceholder(String name) {
		LocationGroup<P> newGroup = new LocationGroup<P>(page, storePlaceholderNames, new AnnotationStyle(annotationStyle)).notAnnotated();
		locations.add(new NamedLocation<P>(name, newGroup, this).placeholder().notAnnotated());
		return new LocationGroupWithPlaceholder<>(this);
	}

	public <R extends PageModel<? super R>> NavLocationBuilder<P,R> addNavigation(String name, ThrowingFunction<P,R,?> navTo) {
		NavLocation<P,R> navLocation = new NavLocation<P, R>(name, this);
		locations.add(navLocation);
		NavLocationBuilder<P,R> builder = new NavLocationBuilder<>(navTo, navLocation, page, storePlaceholderNames, new AnnotationStyle(annotationStyle));
		return builder;
	}

	public LocationGroup<P> addGroup(String name, Consumer<LocationGroup<P>> group) {
		LocationGroup<P> newGroup = new LocationGroup<>(page, storePlaceholderNames, new AnnotationStyle(annotationStyle));
		group.accept(newGroup);
		locations.add(new NamedLocation<P>(name, newGroup, this));
		return this;
	}

	public LocationGroupWithPlaceholder<P> addGroupPlaceholder(String name, Consumer<LocationGroup<P>> group) {
		LocationGroup<P> newGroup = new LocationGroup<>(page, storePlaceholderNames, new AnnotationStyle(annotationStyle));
		group.accept(newGroup);
		locations.add(new NamedLocation<P>(name, newGroup, this).placeholder());
		return new LocationGroupWithPlaceholder<>(this);
	}

	public LocationGroup<P> addIndividual(String name, ThrowingFunction<P, HasPageBounds, ?> getBounds) {
		locations.add(new NamedLocation<P>(name, getBounds, this).notAnnotated());
		return this;
	}

	public LocationGroupWithPlaceholder<P> addIndividualPlaceholder(String name, ThrowingFunction<P, HasPageBounds, ?> getBounds) {
		LocationGroup<P> newGroup = new LocationGroup<P>(page, storePlaceholderNames, new AnnotationStyle(annotationStyle)).withLocation(getBounds).notAnnotated();
		locations.add(new NamedLocation<P>(name, newGroup, this).placeholder().notAnnotated());
		return new LocationGroupWithPlaceholder<>(this);
	}

	public LocationGroup<P> addIndividualGroup(String name, Consumer<LocationGroup<P>> group) {
		LocationGroup<P> newGroup = new LocationGroup<>(page, storePlaceholderNames, new AnnotationStyle(annotationStyle));
		group.accept(newGroup);
		locations.add(new NamedLocation<P>(name, newGroup, this).notAnnotated());
		return this;
	}

	public LocationGroupWithPlaceholder<P> addIndividualGroupPlaceholder(String name, Consumer<LocationGroup<P>> group) {
		LocationGroup<P> newGroup = new LocationGroup<>(page, storePlaceholderNames, new AnnotationStyle(annotationStyle));
		group.accept(newGroup);
		locations.add(new NamedLocation<P>(name, newGroup, this).placeholder().notAnnotated());
		return new LocationGroupWithPlaceholder<>(this);
	}

	public LocationGroup<P> apply(ThrowingConsumer<LocationGroup<P>,?> group) {
		ThrowingConsumer.unchecked(group).accept(this);
		return this;
	}

	protected List<NamedLocation<P>> getLocations() {
		return locations;
	}

	protected int getLocationCount(){
		return locations.size();
	}

	private <R> void setupGraphics(ImageAnnotator<R> i) {
		i.setColor(annotationStyle.color)
				.setBackground(annotationStyle.backgroundColor)
				.setFont(annotationStyle.font)
				.setStroke(new BasicStroke(annotationStyle.strokeSize));
	}

	public RectangleTester<P> getMergedBounds(P page) {
		boolean failed = false;
		RectangleTester<P> mergedBounds = this.mergedBounds;
		if (mergedBounds == null) {
			try{
				if(groupLocation != null){
					mergedBounds = PageBoundsHelper.getTester(ThrowingFunction.unchecked(groupLocation).apply(page), page);
				}
			}catch (Throwable t){
				failed = true;
				mergedBounds = null;
			}
			if(annotated) {
				for (NamedLocation<P> loc : locations) {
					if (!loc.getAnnotated() || loc.placeholder) {
						continue;
					}
					if(loc.getLocationGroup() != null && NavLocation.class.isAssignableFrom(loc.getClass())){
						continue;
					}
					try {
						if (mergedBounds == null) {
							mergedBounds = loc.getBounds(page);
						} else {
							mergedBounds = mergedBounds.extend(loc::getPageBounds);
						}
					} catch (Throwable t) {
						failed = true;
					}
				}
			}
		}
		if(failed){
			this.mergedBounds = null;
		}else{
			this.mergedBounds = mergedBounds;
		}
		if(mergedBounds == null){
			return null;
		}
		return mergedBounds;
	}

	private void resetMergedBounds(){
		this.mergedBounds = null;
		for(NamedLocation<P> loc : locations){
			if(loc.getLocationGroup() != null){
				loc.getLocationGroup().resetMergedBounds();
			}
		}
	}

	public P takeScreenshot() {
		if(screenshotSetup != null){
			ThrowingConsumer.unchecked(screenshotSetup).accept(page);
		}
		if (annotationSetup != null) {
			ThrowingConsumer.unchecked(annotationSetup).accept(page);
			resetMergedBounds();
		}

		RectangleTester<P> bounds = getMergedBounds(page);
		if(bounds == null){
			return page;
		}
		bounds.pad(annotationStyle.imagePadding).takeScreenshot(groupPrefix + ".0.0." + name);

		if(annotated) {
			ImageAnnotator<P> img = getMergedBounds(page).pad(annotationStyle.imagePadding).editScreenshot().paint(this::setupGraphics);
			int count = 1;
			for (int i = 0; i < locations.size(); i++) {
				NamedLocation<P> loc = locations.get(i);
				if(loc.getAnnotated()) {
					try {
						if(!loc.placeholder && !NavLocation.class.isAssignableFrom(loc.getClass())) {
							img = img.paint(label("" + count, loc.getAnnotationStyle(), loc));
						}
						count++;
					}catch (Throwable t){
						throw loc.createCause("Annotate Screenshot", t);
					}
				}
			}
			img.save(groupPrefix + ".0.1." + name + "Labeled");
		}
		if (annotationCleanup != null) {
			ThrowingConsumer.unchecked(annotationCleanup).accept(page);
			resetMergedBounds();
		}
		int count = 0;
		for (int i = 0; i < locations.size(); i++) {
			NamedLocation<P> loc = locations.get(i);
//			if(loc.getAnnotated()) {
			try {
				takeLocationScreenshot(count, loc, page);
			}catch (Throwable t){
				throw loc.createCause("Annotate Screenshot", t);
			}
				count++;
//			}
		}
//		for (int i = 0; i < locations.size(); i++) {
//			NamedLocationBase<P> loc = locations.get(i);
//			if(!loc.getAnnotated()) {
//				takeLocationScreenshot(count, loc, page);
//				count++;
//			}
//		}
		if(screenshotCleanup != null){
			ThrowingConsumer.unchecked(screenshotCleanup).accept(page);
		}
		return page;
	}

	protected <R extends PageModel<? super R>> void takeLocationScreenshot(int i, NamedLocation<P> location, P page){
		if(location.screenshotSetup != null && !location.placeholder){
			ThrowingConsumer.unchecked(location.screenshotSetup).accept(page);
		}
		if(NavLocation.class.isAssignableFrom(location.getClass())){
			NavLocation<P,R> nav = (NavLocation<P, R>) location;
			R navPage = ThrowingFunction.unchecked(nav.navAction).apply(page);
			doNavScreenshot(i, nav, navPage);
			if(nav.navCleanup != null) {
				page = ThrowingFunction.unchecked(nav.navCleanup).apply(navPage);
			}
		}else {
			doLocationScreenshot(i, location);
		}
		if(location.screenshotCleanup != null && !location.placeholder){
			ThrowingConsumer.unchecked(location.screenshotCleanup).accept(page);
		}
	}

	protected void doLocationScreenshot(int i, NamedLocation<P> location){
		String locPrefix = groupPrefix + "." + (i + 1);
		if(location.isGroup()){
			location.getLocationGroup().withGroupPrefix(locPrefix).withName(location.name);
			if(!location.placeholder) {
				location.getLocationGroup().takeScreenshot();
			}else if(location.savePlaceHolder != null){
				LocationGroup<P> group = location.getLocationGroup();
				group.placeholder = false;
				location.savePlaceHolder.accept(location.getLocationGroup());
			}
		}else {
			if(!location.placeholder) {
				location.getBounds(page).pad(location.getAnnotationStyle().imagePadding)
						.takeScreenshot(groupPrefix + "." + (i + 1) + ".0.0." + location.name);
			}
		}
	}

	protected <R extends PageModel<? super R>> void doNavScreenshot(int i, NavLocation<P,R> location, R page){
		String locPrefix = groupPrefix + "." + (i + 1);
		location.setPage(page);
		location.inner.withGroupPrefix(locPrefix).withName(location.name);
		if(!location.placeholder) {
			location.inner.takeScreenshot();
		}else if(location.savePlaceHolder != null){
//			LocationGroup<R> group = location.inner;
//			group.placeholder = false;
//			location.savePlaceHolder.accept((LocationGroup) group);
		}
	}

	public LocationGroup<P> withGroupSetup(ThrowingConsumer<P,?> setup){
		this.screenshotSetup = setup;
		return this;
	}

	public LocationGroup<P> withGroupCleanup(ThrowingConsumer<P,?> cleanup){
		this.screenshotCleanup = cleanup;
		return this;
	}

	public LocationGroup<P> withAnnotationSetup(ThrowingConsumer<P,?> setup){
		this.annotationSetup = setup;
		return this;
	}

	public LocationGroup<P> withAnnotationCleanup(ThrowingConsumer<P,?> setup){
		this.annotationCleanup = setup;
		return this;
	}

	public LocationGroup<P> withLocationSetup(ThrowingConsumer<P,?> setup){
		if(locations.size() == 0){
//			throw new IllegalArgumentException("No Locations added");
			return this;
		}
		NamedLocation<P> location = locations.get(locations.size() - 1);
		location.screenshotSetup = setup;
		return this;
	}

	public LocationGroup<P> withLocationCleanup(ThrowingConsumer<P,?> cleanup){
		if(locations.size() == 0){
			return this;
//			throw new IllegalArgumentException("No Locations added");
		}
		NamedLocation<P> location = locations.get(locations.size() - 1);
		location.screenshotCleanup = cleanup;
		return this;
	}

	private <R extends PageModel<? super R>> ThrowingConsumer<ImageAnnotator<R>, ?> label(String label, AnnotationStyle style, NamedLocation<P> location) {
		double height[] = new double[2];
		double width[] = new double[2];
		double widthPad[] = new double[1];
		int arcSize[] = new int[1];
		TestEvaluator testEvaluator = page != null ? page.getEvaluator() : new TestEvaluator.Now();
		testEvaluator.logEvent(TestEvaluator.TEST_EXECUTE, "Annotate", j -> j
				.addValue("group", this.getGroupPrefix() + "." + this.getName())
				.addValue("location", location.name)
				.doAdd(AnnotationStyle.logJson(style)));
		ThrowingFunction<R,RectangleTester<? super R>, ?> locBounds = page -> page
				.testPage().testLocation(p -> location.getPageBounds((P)p))
				.pad(style.locationPadding);
		ThrowingFunction<R,RectangleTester<? super R>, ?> labelBounds = p -> ThrowingFunction.unchecked(locBounds).apply((R)p)
				.translate(style.labelMargin[TOP] + style.strokeSize, style.labelMargin[LEFT] + style.strokeSize);
		return img -> img
				.useGraphics(graphics -> {
					FontMetrics metrics = graphics.getFontMetrics(style.font);
					Rectangle2D bounds = metrics.getStringBounds(label, graphics);
					height[0] = bounds.getHeight() + style.labelPad[TOP] + style.labelPad[BOTTOM];
					height[1] = Math.max(0, bounds.getHeight() - metrics.getAscent());
					width[0] = bounds.getWidth() + style.labelPad[LEFT] + style.labelPad[RIGHT] + 4;
					width[1] = metrics.getMaxAdvance();
					widthPad[0] = (Math.max(width[0], height[0]) - width[0]) / 2;
					arcSize[0] = style.labelRadiusPx != null ? style.labelRadiusPx : (int)(height[0] * style.labelRadius);
				})
				.setFont(style.font)
				.setStroke(new BasicStroke(style.strokeSize))
				.setColor(style.backgroundColor)
				// Locaton Outline
				.drawRoundedRect(locBounds, style.locationRadiusPx, style.locationRadiusPx)

				// Label Background
				.fillRoundedRect(page -> page.testPage().testLocation(p -> location.getPageBounds((P)p))
						.topLeft().asRectangle((int)(width[0] + 2*widthPad[0]), (int)height[0]), arcSize[0], arcSize[0])
				// Label Text
				.setColor(style.color)
				.drawText(label, page -> page.testPage().testLocation(p -> location.getPageBounds((P)p))
						.topLeft().translate((int)(widthPad[0]) + style.labelPad[LEFT] + 2, (int)(height[0] - height[1] - 1 - style.labelPad[TOP])))
				.setColor(style.backgroundColor);
	}

	public static class NamedLocation<P extends PageModel<? super P>> {
		protected LocationGroup<P> parentGroup;
		protected ThrowingFunction<P, HasPageBounds, ?> pageBounds;
		protected LocationGroup<P> locationGroup;
		protected String name;
		protected boolean annotated = true;
		protected boolean placeholder = false;
		protected ThrowingConsumer<P,?> screenshotSetup;
		protected ThrowingConsumer<P,?> screenshotCleanup;
		protected StackTraceElement[] createTrace;
		protected Consumer<LocationGroup<P>> savePlaceHolder = null;
		protected AnnotationStyle annotationStyle = null;

		public NamedLocation(String name, ThrowingFunction<P, HasPageBounds, ?> pageBounds, LocationGroup<P> parentGroup) {
			this.name = name;
			this.locationGroup = null;
			this.pageBounds = pageBounds;
			this.createTrace = Thread.currentThread().getStackTrace();
			this.parentGroup = parentGroup;
		}

		public NamedLocation(String name, LocationGroup<P> locationGroup, LocationGroup<P> parentGroup) {
			this.name = name;
			this.locationGroup = locationGroup;
			this.pageBounds = null;
			this.createTrace = Thread.currentThread().getStackTrace();
			this.parentGroup = parentGroup;
		}

		protected RuntimeException createCause(String message, Throwable cause){
			StackTraceElement[] trace = new StackTraceElement[this.createTrace.length-3];
			System.arraycopy(this.createTrace, 3, trace, 0, this.createTrace.length-3);
			message = message.length() == 0 ? message : " " + message;
			RuntimeException t = new RuntimeException("NamedLocation:[" + name + "]" + message, cause);
			t.setStackTrace(trace);
			return t;
		}

		public NamedLocation<P> withStyle(AnnotationStyle style){
			annotationStyle = style;
			if(locationGroup != null){
				locationGroup.annotationStyle.set(style);
			}
			return this;
		}

		public NamedLocation<P> placeholder(){
			this.placeholder = true;
			if(locationGroup != null){
				locationGroup.placeholder = true;
			}
			return this;
		}

		public NamedLocation<P> notPlaceholder(){
			this.placeholder = false;
			if(locationGroup != null){
				locationGroup.placeholder = false;
			}
			return this;
		}

		public NamedLocation<P> annotated(){
			this.annotated = true;
			return this;
		}

		public NamedLocation<P> notAnnotated(){
			this.annotated = false;
			return this;
		}

		public boolean getAnnotated() {
			return annotated;
		}

		public String getName() {
			return name;
		}

		protected HasPageBounds getPageBounds(P page) {
			try {
				if (pageBounds != null) {
					return ThrowingFunction.unchecked(pageBounds).apply(page);
				}
				return locationGroup.getMergedBounds(page);
			}catch(RuntimeException t){
				throw createCause("pageBounds", t);
			}
		}

		protected RectangleTester<P> getBounds(P page) {
			try {
				if (pageBounds != null) {
					return PageBoundsHelper.getTester(ThrowingFunction.unchecked(pageBounds).apply(page), page);
				}
				return PageBoundsHelper.getTester(getPageBounds(page), page);
			}catch(RuntimeException t){
				throw createCause("pageBounds", t);
			}
		}

		protected LocationGroup<P> getLocationGroup(){
			return locationGroup;
		}

		public boolean isGroup(){
			return locationGroup != null;
		}

		public AnnotationStyle getAnnotationStyle(){
			if(annotationStyle == null){
				annotationStyle = new AnnotationStyle(parentGroup.annotationStyle);
			}
			return annotationStyle;
		}
	}

	protected class NavLocation<P extends PageModel<? super P>, R extends PageModel<? super R>> extends NamedLocation<P> {
		protected ThrowingFunction<P,R,?> navAction;
		protected ThrowingFunction<R,P,?> navCleanup;
		protected LocationGroup<R> inner;

		public NavLocation(String name, LocationGroup<P> parentGroup) {
			super(name, (LocationGroup<P>) null, parentGroup);
			this.notAnnotated();
		}

		public void setNavAction(ThrowingFunction<P, R, ?> navAction) {
			this.navAction = navAction;
		}

		public void setNavCleanup(ThrowingFunction<R, P, ?> navCleanup) {
			this.navCleanup = navCleanup;
		}

		public void setInner(LocationGroup<R> inner) {
			this.inner = inner;
		}

		public void setPage(R page) {
			setPage(page, inner);
		}

		private <Q extends PageModel<? super Q>> void setPage(R page, LocationGroup<R> group) {
			group.page = page;
			for(NamedLocation<R> loc : group.locations){
				if(loc.locationGroup != null) {
					setPage(page, loc.locationGroup);
				}else if(NavLocation.class.isAssignableFrom(loc.getClass())){
				}
			}
		}
	}

	public class NavLocationBuilder<P extends  PageModel<? super P>, R extends  PageModel<? super R>> {
		protected NavLocation<P,R> location;
		protected LocationGroup<R> newGroup;

		public NavLocationBuilder(ThrowingFunction<P,R,?> navTo, NavLocation<P,R> location, P page, List<String> storePlaceholderNames, AnnotationStyle style){
			this.location = location;
			this.newGroup = new LocationGroup<>(null, storePlaceholderNames, style);
			location.setNavAction(navTo);
		}

		public NavLocationReturn<P,R> withGroup(Consumer<LocationGroup<R>> group){
			group.accept(newGroup);
			location.setInner(newGroup);
			return new NavLocationReturn<>(location);
		}

		public LocationGroup<P> withGroupIncludingNavReturn(Consumer<LocationGroup<R>> group){
			group.accept(newGroup);
			location.setInner(newGroup);
			return location.parentGroup;
		}
	}

	public class NavLocationReturn<P extends  PageModel<? super P>, R extends  PageModel<? super R>> {
		protected NavLocation<P,R> location;

		public NavLocationReturn(NavLocation<P,R> location){
			this.location = location;
		}

		public LocationGroup<P> navigateReturn(ThrowingFunction<R,P,?> navFrom){
			location.setNavCleanup(navFrom);
			return location.parentGroup;
		}
	}

}