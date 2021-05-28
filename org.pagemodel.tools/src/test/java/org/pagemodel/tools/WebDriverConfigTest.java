package org.pagemodel.tools;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Map;

public class WebDriverConfigTest {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private WebDriverConfig webDriverConfig;

	@Before
	public void setup(){
	}

	@Test
	public void updateField() {
		webDriverConfig = WebDriverConfig.local("chrome");
		webDriverConfig.updateField("opts", obj -> obj
				.addValue("test1", "val1"));
		Assert.assertEquals("val1", ((Map) webDriverConfig.getCapabilities().asMap().get("opts")).get("test1"));
		webDriverConfig.updateField("opts", obj -> obj
				.addValue("test1", "val1-2")
				.addValue("test2", "val2"));
		Assert.assertEquals("val1-2", ((Map) webDriverConfig.getCapabilities().asMap().get("opts")).get("test1"));
		Assert.assertEquals("val2", ((Map) webDriverConfig.getCapabilities().asMap().get("opts")).get("test2"));
		webDriverConfig.setField("opts", "optsVal");
		Assert.assertEquals("optsVal",webDriverConfig.getCapabilities().asMap().get("opts"));
	}

}