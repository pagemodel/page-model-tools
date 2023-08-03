package org.pagemodel.web.testers;

import org.openqa.selenium.Rectangle;
import org.pagemodel.web.PageModel;

public class PageBoundsHelper {
	public static Rectangle getBounds(HasPageBounds pageBounds){
		return pageBounds.getBounds();
	}

	public static <R extends PageModel<? super R>> RectangleTester<R> getTester(HasPageBounds pageBounds, R page){
			return new RectangleTester<>(() -> getBounds(pageBounds), page, page.getContext(), page.getEvaluator());
	}
}
