package org.pagemodel.web.testers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.DefaultWebTestContext;
import org.pagemodel.web.LocatedWebElement;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.utils.PageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static org.mockito.Mockito.*;

public class WebElementTesterTest {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private WebTestContext context;
	private WebDriver driver;
	private WebElement element;
	private PageModel currentPage;
	private PageModel navPage;
	private Object returnObj;
	private WebElementTester tester;
	private TestEvaluator testEvaluator;

	static class TestPage extends PageModel.DefaultPageModel<TestPage> {
		public TestPage(WebTestContext testContext) {
			super(testContext);
		}
	}

	static class TestTestContext extends DefaultWebTestContext {
		public TestTestContext(WebDriver driver) {
			super(driver);
		}

		@Override
		public PageException createException(String message, Throwable cause) {
			return new PageException(this, false, message, cause);
		}
	}

	@Before
	public void setup(){
		driver = mock(WebDriver.class);
		context = new TestTestContext(driver);
		returnObj = new Object();
		currentPage = new TestPage(context);
		testEvaluator = new TestEvaluator.Now();
		tester = new WebElementTester(returnObj,
				ClickAction.make(() -> element, currentPage, testEvaluator),
				new TestEvaluator.Now());
	}

	private boolean assertException(Runnable test){
		log.info("Expecting exception:");
		try {
			test.run();
		}catch(Throwable t){
			return true;
		}
		Assert.fail("no exception caught");
		return false;
	}

	@Test
	public void callRef() {
		element = null;
		Assert.assertTrue(tester.callRef() instanceof LocatedWebElement);
		Assert.assertFalse(tester.callRef().hasElement());
		Assert.assertEquals(null, tester.callRef().getElement());
		assertException(() -> tester.exists());
		tester.notExists();

		Assert.assertEquals(null, new WebElementTester(returnObj,
				ClickAction.make(() -> {throw new Exception();}, currentPage, testEvaluator),
				new TestEvaluator.Now()).callRef().getElement());

		element = mock(WebElement.class);
		Assert.assertTrue(tester.callRef() instanceof LocatedWebElement);
		Assert.assertTrue(tester.callRef().hasElement());
		Assert.assertEquals(element, tester.callRef().getElement());
		tester.exists();
		assertException(() -> tester.notExists());
	}

	@Test
	public void getReturnObj() {
		Assert.assertEquals(returnObj, tester.getReturnObj());
	}

	@Test
	public void setReturnObj() {
		Object ret2 = new Object();
		tester.setReturnObj(ret2);
		Assert.assertEquals(ret2, tester.getReturnObj());
		tester.setReturnObj(null);
		Assert.assertEquals(null, tester.getReturnObj());
	}

	@Test
	public void exists() {
		element = null;
		assertException(() -> tester.exists());

		element = mock(WebElement.class);
		tester.exists();
	}

	@Test
	public void notExists() {
		element = null;
		tester.notExists();

		element = mock(WebElement.class);
		assertException(() -> tester.notExists());
	}

	@Test
	public void isSelected() {
		element = null;
		assertException(() -> tester.isSelected());

		element = mock(WebElement.class);
		when(element.isSelected()).thenReturn(true);
		tester.isSelected();

		element = mock(WebElement.class);
		when(element.isSelected()).thenReturn(false);
		assertException(() -> tester.isSelected());
	}

	@Test
	public void notSelected() {
		element = null;
		assertException(() -> tester.notSelected());

		element = mock(WebElement.class);
		when(element.isSelected()).thenReturn(false);
		tester.notSelected();

		element = mock(WebElement.class);
		when(element.isSelected()).thenReturn(true);
		assertException(() -> tester.notSelected());
	}

	@Test
	public void isFocused() {
		element = null;
		assertException(() -> tester.isFocused());

		element = mock(WebElement.class);
		WebDriver.TargetLocator targetLocator = mock(WebDriver.TargetLocator.class);
		when(driver.switchTo()).thenReturn(targetLocator);
		WebElement different = mock(WebElement.class);
		when(targetLocator.activeElement()).thenReturn(different);
		assertException(() -> tester.isFocused());

		element = mock(WebElement.class);
		targetLocator = mock(WebDriver.TargetLocator.class);
		when(driver.switchTo()).thenReturn(targetLocator);
		when(targetLocator.activeElement()).thenReturn(element);
		tester.isFocused();
	}

	@Test
	public void notFocused() {
		element = null;
		assertException(() -> tester.notFocused());

		element = mock(WebElement.class);
		WebDriver.TargetLocator targetLocator = mock(WebDriver.TargetLocator.class);
		when(driver.switchTo()).thenReturn(targetLocator);
		when(targetLocator.activeElement()).thenReturn(element);
		assertException(() -> tester.notFocused());

		element = mock(WebElement.class);
		targetLocator = mock(WebDriver.TargetLocator.class);
		when(driver.switchTo()).thenReturn(targetLocator);
		WebElement different = mock(WebElement.class);
		when(targetLocator.activeElement()).thenReturn(different);
		tester.notFocused();
	}

	@Test
	public void isEnabled() {
		element = null;
		assertException(() -> tester.isEnabled());

		element = mock(WebElement.class);
		when(element.isEnabled()).thenReturn(true);
		tester.isEnabled();

		element = mock(WebElement.class);
		when(element.isEnabled()).thenReturn(false);
		assertException(() -> tester.isEnabled());
	}

	@Test
	public void notEnabled() {
		element = null;
		assertException(() -> tester.notEnabled());

		element = mock(WebElement.class);
		when(element.isEnabled()).thenReturn(false);
		tester.notEnabled();

		element = mock(WebElement.class);
		when(element.isEnabled()).thenReturn(true);
		assertException(() -> tester.notEnabled());
	}

	@Test
	public void isDisplayed() {
		element = null;
		assertException(() -> tester.isDisplayed());

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(true);
		tester.isDisplayed();

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(false);
		assertException(() -> tester.isDisplayed());
	}

	@Test
	public void notDisplayed() {
		element = null;
		tester.notDisplayed();

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(false);
		tester.notDisplayed();

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(true);
		assertException(() -> tester.notDisplayed());
	}

	@Test
	public void isClickable() {
		element = null;
		assertException(() -> tester.isClickable());

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(false);
		when(element.isEnabled()).thenReturn(false);
		assertException(() -> tester.isClickable());

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(false);
		when(element.isEnabled()).thenReturn(true);
		assertException(() -> tester.isClickable());

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(false);
		assertException(() -> tester.isClickable());

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(true);
		tester.isClickable();
	}

	@Test
	public void notClickable() {
		element = null;
		assertException(() -> tester.notClickable());

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(false);
		when(element.isEnabled()).thenReturn(false);
		tester.notClickable();

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(false);
		when(element.isEnabled()).thenReturn(true);
		tester.notClickable();

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(false);
		tester.notClickable();

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(true);
		assertException(() -> tester.notClickable());

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(true);
		tester.isClickable();
	}

	@Test
	public void text() {
		element = null;
		tester.text().equals("");

		element = mock(WebElement.class);
		when(element.getText()).thenReturn("");
		tester.text().equals("");

		element = mock(WebElement.class);
		when(element.getText()).thenReturn(null);
		tester.text().equals(null);

		element = mock(WebElement.class);
		when(element.getText()).thenReturn("test string");
		tester.text().equals("test string");
	}

	@Test
	public void tagName() {
		element = null;
		tester.tagName().equals("");

		element = mock(WebElement.class);
		when(element.getTagName()).thenReturn("");
		tester.tagName().equals("");

		element = mock(WebElement.class);
		when(element.getTagName()).thenReturn(null);
		tester.tagName().equals(null);

		element = mock(WebElement.class);
		when(element.getTagName()).thenReturn("div");
		tester.tagName().equals("div");
	}

	@Test
	public void attribute() {
		element = null;
		tester.attribute("id").equals("");
		tester.attribute("").equals("");
		tester.attribute(null).equals("");

		element = mock(WebElement.class);
		when(element.getAttribute("id")).thenReturn("myId");
		tester.attribute("id").equals("myId");

		element = mock(WebElement.class);
		when(element.getAttribute("id")).thenReturn(null);
		tester.attribute("id").equals(null);

		element = mock(WebElement.class);
		when(element.getAttribute("id")).thenReturn("");
		tester.attribute("id").equals("");
	}

	@Test
	public void cssValue() {
		element = null;
		tester.cssValue("width").equals("");
		tester.cssValue("").equals("");
		tester.cssValue(null).equals("");

		element = mock(WebElement.class);
		when(element.getCssValue("display")).thenReturn("block");
		tester.cssValue("display").equals("block");

		element = mock(WebElement.class);
		when(element.getCssValue("display")).thenReturn(null);
		tester.cssValue("display").equals(null);

		element = mock(WebElement.class);
		when(element.getCssValue("display")).thenReturn("");
		tester.cssValue("display").equals("");
	}

	@Test
	public void size() {
		element = null;
		Assert.assertEquals(null, tester.size().callRef());

		element = mock(WebElement.class);
		Dimension dimension = mock(Dimension.class);
		when(element.getSize()).thenReturn(dimension);
		Assert.assertEquals(dimension, tester.size().callRef());
	}

	@Test
	public void location() {
		element = null;
		Assert.assertEquals(null, tester.size().callRef());

		element = mock(WebElement.class);
		Rectangle rectangle = mock(Rectangle.class);
		when(element.getRect()).thenReturn(rectangle);
		Assert.assertEquals(rectangle, tester.location().callRef());
	}

	@Test
	public void clearText() {
		element = null;
		assertException(() -> tester.clearText());

		element = mock(WebElement.class);
		tester.clearText();
		verify(element).clear();
	}

	@Test
	public void sendKeys() {
		element = null;
		assertException(() -> tester.sendKeys("test"));
		assertException(() -> tester.sendKeys(""));
		assertException(() -> tester.sendKeys((CharSequence) null));
		assertException(() -> tester.sendKeys());

		element = mock(WebElement.class);
		tester.sendKeys("test");
		verify(element).sendKeys("test");

		element = mock(WebElement.class);
		tester.sendKeys((CharSequence) null);
		verify(element).sendKeys((CharSequence)null);

		element = mock(WebElement.class);
		tester.sendKeys();
		verify(element).sendKeys();
	}

	@Test
	public void sendKeysAnd() {
		element = null;
		assertException(() -> tester.sendKeysAnd("test"));
		assertException(() -> tester.sendKeysAnd(""));
		assertException(() -> tester.sendKeysAnd((CharSequence) null));
		assertException(() -> tester.sendKeysAnd());

		element = mock(WebElement.class);
		tester.sendKeysAnd("test");
		verify(element).sendKeys("test");

		element = mock(WebElement.class);
		tester.sendKeysAnd((CharSequence) null);
		verify(element).sendKeys((CharSequence)null);

		element = mock(WebElement.class);
		tester.sendKeysAnd();
		verify(element).sendKeys();
	}

	@Test
	public void click() {
		element = null;
		assertException(() -> tester.click());

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(false);
		when(element.isEnabled()).thenReturn(false);
		assertException(() -> tester.click());
		verify(element, never()).click();

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(false);
		when(element.isEnabled()).thenReturn(true);
		assertException(() -> tester.click());
		verify(element, never()).click();

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(false);
		assertException(() -> tester.click());
		verify(element, never()).click();

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(true);
		mockDriverTimeouts();
		Object ret = tester.click();
		Assert.assertEquals(currentPage, ret);
		verify(element).click();
	}

	@Test
	public void clickAnd() {
		element = null;
		assertException(() -> tester.clickAnd());

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(false);
		when(element.isEnabled()).thenReturn(false);
		assertException(() -> tester.clickAnd());
		verify(element, never()).click();

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(false);
		when(element.isEnabled()).thenReturn(true);
		assertException(() -> tester.clickAnd());
		verify(element, never()).click();

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(false);
		assertException(() -> tester.clickAnd());
		verify(element, never()).click();

		element = mock(WebElement.class);
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(true);
		mockDriverTimeouts();
		WebActionTester<TestPage> ret = tester.clickAnd();
		Assert.assertEquals(returnObj, ret.noRedirect());
		verify(element).click();
	}

	public void mockDriverTimeouts(){
		WebDriver.Options manage = mock(WebDriver.Options.class);
		when(driver.manage()).thenReturn(manage);
		WebDriver.Timeouts timeouts = mock(WebDriver.Timeouts.class);
		when(manage.timeouts()).thenReturn(timeouts);
	}
}