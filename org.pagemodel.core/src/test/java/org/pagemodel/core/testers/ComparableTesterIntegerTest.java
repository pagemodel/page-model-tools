package org.pagemodel.core.testers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pagemodel.core.DefaultTestContext;
import org.pagemodel.core.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Date;

public class ComparableTesterIntegerTest {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private TestContext context;
	private Integer integer;
	private ComparableTester<Integer,?> tester;
	private Object returnObj;

	@Before
	public void setup(){
		returnObj = new Object();
		context = new DefaultTestContext();
		tester = new ComparableTester<>(() -> integer, returnObj, context, new TestEvaluator.Now());
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
		integer = null;
		Assert.assertEquals(null, tester.callRef());

		integer = 0;
		Assert.assertEquals(0, (int)tester.callRef());
		integer = Integer.MAX_VALUE;
		Assert.assertEquals(Integer.MAX_VALUE, (int)tester.callRef());
		integer = Integer.MIN_VALUE;
		Assert.assertEquals(Integer.MIN_VALUE, (int)tester.callRef());

		Assert.assertEquals(null, new ComparableTester<Integer,Object>(() -> {throw new Exception();}, null, context, new TestEvaluator.Now()).callRef());
	}

	@Test
	public void testEquals() {
		integer = null;
		tester.equals(null);
		assertException(() -> tester.equals(0));
		assertException(() -> tester.equals(1));
		assertException(() -> tester.equals(-1));

		integer = 0;
		tester.equals(0);
		tester.equals(-0);
		assertException(() -> tester.equals(1));
		assertException(() -> tester.equals(-1));
		assertException(() -> tester.equals(null));

		integer = 42;
		tester.equals(42);
		assertException(() -> tester.equals(null));
		assertException(() -> tester.equals(-42));
	}

	@Test
	public void notEquals() {
		integer = null;
		tester.notEquals(0);
		tester.notEquals(42);
		tester.notEquals(-1);
		assertException(() -> tester.notEquals(null));

		integer = 0;
		tester.notEquals(null);
		tester.notEquals(42);
		tester.notEquals(-1);
		assertException(() -> tester.notEquals(0));

		integer = 42;
		tester.notEquals(null);
		tester.notEquals(0);
		tester.notEquals(-1);
		assertException(() -> tester.notEquals(42));
	}

	@Test
	public void greaterThan() {
		integer = null;
		assertException(() -> tester.greaterThan(Integer.MIN_VALUE));
		assertException(() -> tester.greaterThan(0));
		assertException(() -> tester.greaterThan(null));

		integer = 0;
		tester.greaterThan(Integer.MIN_VALUE);
		tester.greaterThan(-1);
		assertException(() -> tester.greaterThan(0));
		assertException(() -> tester.greaterThan(1));
		assertException(() -> tester.greaterThan(Integer.MAX_VALUE));
		assertException(() -> tester.greaterThan(null));

		integer = 42;
		tester.greaterThan(Integer.MIN_VALUE);
		tester.greaterThan(-1);
		tester.greaterThan(0);
		tester.greaterThan(41);
		assertException(() -> tester.greaterThan(42));
		assertException(() -> tester.greaterThan(424));
		assertException(() -> tester.greaterThan(Integer.MAX_VALUE));
		assertException(() -> tester.greaterThan(null));
	}

	@Test
	public void notGreaterThan() {
		integer = null;
		assertException(() -> tester.notGreaterThan(Integer.MIN_VALUE));
		assertException(() -> tester.notGreaterThan(0));
		assertException(() -> tester.notGreaterThan(null));

		integer = 0;
		assertException(() -> tester.notGreaterThan(Integer.MIN_VALUE));
		assertException(() -> tester.notGreaterThan(-1));
		tester.notGreaterThan(0);
		tester.notGreaterThan(Integer.MAX_VALUE);
		assertException(() -> tester.notGreaterThan(null));

		integer = 42;
		assertException(() -> tester.notGreaterThan(Integer.MIN_VALUE));
		assertException(() -> tester.notGreaterThan(-1));
		assertException(() -> tester.notGreaterThan(0));
		assertException(() -> tester.notGreaterThan(41));
		tester.notGreaterThan(42);
		tester.notGreaterThan(Integer.MAX_VALUE);
		assertException(() -> tester.notGreaterThan(null));
	}

	@Test
	public void lessThan() {
		integer = null;
		assertException(() -> tester.lessThan(Integer.MIN_VALUE));
		assertException(() -> tester.lessThan(0));
		assertException(() -> tester.lessThan(null));

		integer = 0;
		assertException(() -> tester.lessThan(Integer.MIN_VALUE));
		assertException(() -> tester.lessThan(-1));
		assertException(() -> tester.lessThan(0));
		tester.lessThan(1);
		tester.lessThan(Integer.MAX_VALUE);
		assertException(() -> tester.lessThan(null));

		integer = 42;
		assertException(() -> tester.lessThan(Integer.MIN_VALUE));
		assertException(() -> tester.lessThan(-1));
		assertException(() -> tester.lessThan(0));
		assertException(() -> tester.lessThan(42));
		tester.lessThan(43);
		tester.lessThan(Integer.MAX_VALUE);
		assertException(() -> tester.lessThan(null));
	}

	@Test
	public void notLessThan() {
		integer = null;
		assertException(() -> tester.notLessThan(Integer.MIN_VALUE));
		assertException(() -> tester.notLessThan(0));
		assertException(() -> tester.notLessThan(null));

		integer = 0;
		tester.notLessThan(Integer.MIN_VALUE);
		tester.notLessThan(-1);
		tester.notLessThan(0);
		assertException(() -> tester.notLessThan(1));
		assertException(() -> tester.notLessThan(Integer.MAX_VALUE));
		assertException(() -> tester.notLessThan(null));

		integer = 42;
		tester.notLessThan(Integer.MIN_VALUE);
		tester.notLessThan(-1);
		tester.notLessThan(0);
		tester.notLessThan(42);
		assertException(() -> tester.notLessThan(43));
		assertException(() -> tester.notLessThan(424));
		assertException(() -> tester.notLessThan(Integer.MAX_VALUE));
		assertException(() -> tester.notLessThan(null));
	}

	@Test
	public void asString() {
		integer = null;
		Assert.assertEquals("", tester.asString().callRef());

		integer = -42;
		Assert.assertEquals("-42", tester.asString().callRef());

		integer = -0;
		Assert.assertEquals("0", tester.asString().callRef());

		integer = 0;
		Assert.assertEquals("0", tester.asString().callRef());

		integer = +0;
		Assert.assertEquals("0", tester.asString().callRef());

		integer = 54356;
		Assert.assertEquals("54356", tester.asString().callRef());
	}

	@Test
	public void transform() {
		integer = null;
		Assert.assertEquals(null, tester.transform(x -> {throw new RuntimeException();}).callRef());
		Assert.assertEquals(7, (int)tester.transform(x -> 7).callRef());
		Assert.assertEquals(null, tester.transform(x -> null).callRef());
		Assert.assertEquals("", tester.transform(x -> "").callRef());
		Date date = new Date();
		Assert.assertEquals(date, tester.transform(x -> date).callRef());

		integer = 5;
		Assert.assertEquals(null, tester.transform(x -> {throw new RuntimeException();}).callRef());
		Assert.assertEquals(null, tester.transform(x -> null).callRef());
		Assert.assertEquals(25, (int)tester.transform(x -> x*x).callRef());
		Assert.assertEquals(25.5, tester.transform(x -> x*x+0.5).callRef(), 0.0);
		Assert.assertEquals("25", tester.transform(x -> new Integer(x*x).toString()).callRef());
	}

	@Test
	public void storeValue() {
		integer = -4325;
		tester.storeValue("1");
		Assert.assertEquals(-4325, (int)context.load("1"));
		assertException(() -> tester.storeValue(null));

		integer = null;
		assertException(() -> tester.storeValue("2"));
		assertException(() -> tester.storeValue(null));
	}
}