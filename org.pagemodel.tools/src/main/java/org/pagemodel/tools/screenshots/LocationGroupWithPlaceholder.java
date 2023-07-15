package org.pagemodel.tools.screenshots;

import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.PageModel;

import java.util.function.Consumer;

public class LocationGroupWithPlaceholder<P extends PageModel<? super P>> extends LocationGroup<P> {
	LocationGroup<P> base;

	public LocationGroupWithPlaceholder(LocationGroup<P> base) {
		super(base.page, base.storePlaceholderNames, base.annotationStyle);
		this.base = base;
		this.groupPrefix = base.groupPrefix;
		this.name = base.name;
		this.groupLocation = base.groupLocation;
		this.locations = base.locations;
		this.mergedBounds = base.mergedBounds;
		this.screenshotSetup = base.screenshotSetup;
		this.screenshotCleanup = base.screenshotCleanup;
		this.annotationSetup = base.annotationSetup;
		this.annotationCleanup = base.annotationCleanup;
		this.annotated = base.annotated;
		this.page = base.page;
		this.annotationStyle = base.annotationStyle;
		String name = locations.get(locations.size() - 1).name;
		if (!storePlaceholderNames.contains(name)) {
			storePlaceHolder(name);
		} else {
			TestEvaluator testEvaluator = page != null ? page.getEvaluator() : new TestEvaluator.Now();
			testEvaluator.logEvent(j -> j
					.addValue("Error", "Unable to auto-store Placeholder with duplicate name, use storePlaceholder to define new store key.")
					.addValue("name", name));
		}
	}

	public LocationGroup<P> usePlaceHolder(Consumer<LocationGroup<P>> save) {
		if (locations.size() == 0) {
			return this;
		}
		locations.get(locations.size() - 1).savePlaceHolder = save;
		return this;
	}

	public LocationGroup<P> storePlaceHolder(String name) {
		storePlaceholderNames.add(name);
		return usePlaceHolder(p -> page.getContext().store(name, p));
	}
}
