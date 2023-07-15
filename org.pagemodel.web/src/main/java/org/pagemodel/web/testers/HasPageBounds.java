package org.pagemodel.web.testers;

import org.openqa.selenium.Rectangle;

public abstract class HasPageBounds {
	protected abstract Rectangle getBounds();
	protected abstract String getName();
}
