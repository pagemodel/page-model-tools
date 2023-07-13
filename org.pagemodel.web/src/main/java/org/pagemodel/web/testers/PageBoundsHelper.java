package org.pagemodel.web.testers;

import org.openqa.selenium.Rectangle;

public class PageBoundsHelper {
	public static Rectangle getBounds(HasPageBounds pageBounds){
		return pageBounds.getBounds();
	}
}
