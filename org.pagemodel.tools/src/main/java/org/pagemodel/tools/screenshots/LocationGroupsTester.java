package org.pagemodel.tools.screenshots;

import org.pagemodel.core.utils.ThrowingConsumer;
import org.pagemodel.web.PageModel;

import java.util.ArrayList;
import java.util.List;

public class LocationGroupsTester<P extends PageModel<? super P>> {
	protected String orderPrefix;
	protected int startOrder;
	protected P page;
	protected List<LocationGroup<P>> locations;

	public LocationGroupsTester(String orderPrefix, int startOrder, P page, List<LocationGroup<P>> locations){
		this.orderPrefix = orderPrefix == null || orderPrefix.trim().isEmpty() ? null : orderPrefix;
		this.startOrder = startOrder;
		this.page = page;
		this.locations = locations == null ? new ArrayList<>() : locations;
	}

	public LocationGroupsTester<P> addGroup(String name, ThrowingConsumer<LocationGroup<P>,?> group){
		LocationGroup<P> lg = new LocationGroup<>(page);
		lg.withName(name);
		int i = startOrder + locations.size();
		lg.withGroupPrefix(orderPrefix == null ? ""+i : orderPrefix + "." + i);
		ThrowingConsumer.unchecked(group).accept(lg);
		locations.add(lg);
		return this;
	}

	public P store(String key){
		page.getContext().store(key, this.locations);
		return page;
	}

	public P returnToPage(){
		return page;
	}

	public LocationGroupsTester<P> each(ThrowingConsumer<LocationGroup<P>,?> action){
		for(LocationGroup<P> location : locations) {
			ThrowingConsumer.unchecked(action).accept(location);
		}
		return this;
	}
}
