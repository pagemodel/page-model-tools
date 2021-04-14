package org.pagemodel.core.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class UniqueTest {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	int uniqCount = 1000;

	@Test
	public void shortString() {
		List<String> vals = new ArrayList<>(uniqCount);
		for(int i=0; i<uniqCount; i++){
			String uniq = Unique.shortString();
			Assert.assertFalse(vals.contains(uniq));
			vals.add(uniq);
			Assert.assertEquals(12, uniq.length());
		}
	}

	@Test
	public void string() {
		List<String> vals = new ArrayList<>(uniqCount);
		for(int i=0; i<uniqCount; i++){
			String uniq = Unique.string("test: %s pattern %s");
			Assert.assertFalse(vals.contains(uniq));
			vals.add(uniq);
			Assert.assertTrue(uniq.matches("^test: [0-9a-z]{12,12} pattern [0-9a-z]{12,12}$"));
		}
		assertException(() -> Unique.string(null));
		Assert.assertTrue(Unique.string("%s").matches("^[0-9a-z]{12,12}$"));
		Assert.assertTrue(Unique.string("string").matches("^string [0-9a-z]{12,12}$"));
		Assert.assertTrue(Unique.string("string-%s").matches("^string-[0-9a-z]{12,12}$"));
		Assert.assertTrue(Unique.string("%s-string").matches("^[0-9a-z]{12,12}-string$"));

		String uniq = Unique.string("%s%s");
		Assert.assertTrue(uniq.matches("^[0-9a-z]{12,12}[0-9a-z]{12,12}$"));
		Assert.assertFalse(uniq.endsWith(uniq.substring(0,12)));

		uniq = Unique.string("%s-%s");
		Assert.assertTrue(uniq.matches("^[0-9a-z]{12,12}-[0-9a-z]{12,12}$"));
		Assert.assertFalse(uniq.endsWith(uniq.substring(0,12)));

	}

//	@Test
//	public void longString() {
//	}
//
//	@Test
//	public void uuid() {
//	}
//
//	@Test
//	public void number() {
//	}

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
}