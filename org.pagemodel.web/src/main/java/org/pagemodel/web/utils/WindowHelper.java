package org.pagemodel.web.utils;

import org.openqa.selenium.Dimension;
import org.pagemodel.web.WebTestContext;

public class WindowHelper {
	public static void storeBrowserOffset(WebTestContext context){
		if (context.containsKey("wtcHeightOffset") && context.containsKey("wtcWidthOffset")) {
			return;
		}
		refreshBrowserOffset(context);
	}

	public static void refreshBrowserOffset(WebTestContext context){
		try {
			if (context.containsKey("wtcHeightOffset")) {
				context.removeStored("wtcHeightOffset");
			} else if (context.containsKey("wtcWidthOffset")) {
				context.removeStored("wtcWidthOffset");
			}
			Dimension size = context.getDriver().manage().window().getSize();
			EmptyPage page = new EmptyPage(context);
			page.testPage().testViewHeight().transform(i -> size.getHeight() - i).storeValue("wtcHeightOffset");
			page.testPage().testViewWidth().transform(i -> size.getWidth() - i).storeValue("wtcWidthOffset");
		}catch (Throwable t){
			context.store("wtcHeightOffset", 0);
			context.store("wtcWidthOffset", 0);
		}
	}
}
