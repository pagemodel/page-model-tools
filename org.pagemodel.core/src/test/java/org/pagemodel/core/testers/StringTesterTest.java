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
import java.util.UUID;

public class StringTesterTest {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private TestContext context;
	private String testString;
	private StringTester<?> tester;

	@Before
	public void setup(){
		context = new DefaultTestContext();
		tester = new StringTester<>(() -> testString, null, context, new TestEvaluator.Now());
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
		testString = null;
		Assert.assertEquals(null, tester.callRef());
		testString = "";
		Assert.assertEquals("", tester.callRef());
		String uuid = UUID.randomUUID().toString();
		testString = uuid;
		Assert.assertEquals(uuid, tester.callRef());
		Assert.assertTrue(uuid == tester.callRef());
		Assert.assertEquals("", new StringTester<>(() -> {throw new Exception();}, null, context, new TestEvaluator.Now()).callRef());
	}

	@Test
	public void contains() {
		testString = "test abcd 1234";
		tester.contains(" abc");
		tester.contains("4");
		tester.contains("test");
		tester.contains("test abcd 1234");
		tester.contains("");
		assertException(() -> tester.contains("test abcd 12345"));
		assertException(() -> tester.contains("ABCD"));
		assertException(() -> tester.contains("abcd1234"));
		assertException(() -> tester.contains("abcd 1234t"));
		assertException(() -> tester.contains(" test"));
		assertException(() -> tester.contains(null));

		testString = null;
		assertException(() -> tester.contains(null));
		assertException(() -> tester.contains(""));
		assertException(() -> tester.contains("test abcd 1234"));
		assertException(() -> tester.contains("5678"));
	}

	@Test
	public void notContains() {
		testString = "test abcd 1234";
		assertException(() -> tester.notContains(" abc"));
		assertException(() -> tester.notContains("4"));
		assertException(() -> tester.notContains("test"));
		assertException(() -> tester.notContains("test abcd 1234"));
		tester.notContains("f");
		tester.notContains("ABCD");
		tester.notContains("abcd1234");
		tester.notContains("abcd 1234t");
		tester.notContains(" test");
		assertException(() -> tester.notContains(null));

		testString = null;
		assertException(() -> tester.notContains(null));
		assertException(() -> tester.notContains(""));
		assertException(() -> tester.notContains("test abcd 1234"));
		assertException(() -> tester.notContains("5678"));
	}

	@Test
	public void testEquals() {
		testString = "test abcd 1234";
		tester.equals("test abcd 1234");
		tester.equals("test " + "abcd " + "1234");
		assertException(() -> tester.equals("test abcd 123"));
		assertException(() -> tester.equals("est abcd 1234"));
		assertException(() -> tester.equals(" test abcd 1234"));
		assertException(() -> tester.equals(null));

		testString = null;
		tester.equals(null);
		assertException(() -> tester.equals(""));
		assertException(() -> tester.equals("test abcd 1234"));

		testString = "";
		tester.equals("");
		assertException(() -> tester.equals(null));
		assertException(() -> tester.equals("test abcd 1234"));
	}

	@Test
	public void notEquals() {
		testString = "test abcd 1234";
		tester.notEquals("test abcd 123");
		tester.notEquals("est abcd 1234");
		tester.notEquals(" test abcd 1234");
		tester.notEquals(null);
		assertException(() -> tester.notEquals("test abcd 1234"));

		testString = null;
		assertException(() -> tester.notEquals(null));
		tester.notEquals("");
		tester.notEquals("test abcd 1234");

		testString = "";
		assertException(() -> tester.notEquals(""));
		tester.notEquals(null);
		tester.notEquals("test abcd 1234");
	}

	@Test
	public void matches() {
		testString = "test abcd 1234";
		tester.matches("test abcd 123.");
		tester.matches(".est abcd 1234");
		tester.matches(".*st ab.*");
		tester.matches("test abcd 1234");
		tester.matches(".*(test abcd 1234).*");
		tester.matches(".* abcde? [0-9]*");
		tester.matches(".*");
		assertException(() -> tester.matches(null));
		assertException(() -> tester.matches(""));
		assertException(() -> tester.matches(" test"));
		assertException(() -> tester.matches("test abcd 123"));
		assertException(() -> tester.matches("st ab.*"));
		assertException(() -> tester.matches(".test abcd 1234"));
		assertException(() -> tester.matches("bad match"));
		assertException(() -> tester.matches("bad [syntax"));

		testString = null;
		assertException(() -> tester.matches(null));
		assertException(() -> tester.matches(""));
		assertException(() -> tester.matches(".*"));
		assertException(() -> tester.matches("bad match"));
		assertException(() -> tester.matches("bad [syntax"));

		testString = "";
		tester.matches("");
		tester.matches(".*");
		assertException(() -> tester.matches(null));
		assertException(() -> tester.matches("."));
		assertException(() -> tester.matches("bad match"));
		assertException(() -> tester.matches("bad [syntax"));
	}

	@Test
	public void notMatches() {
		testString = "test abcd 1234";
		tester.notMatches("test abcd 123");
		tester.notMatches("est abcd 1234");
		tester.notMatches(".*st ab");
		tester.notMatches("test abcd 1234.");
		tester.notMatches("");
		tester.notMatches("bad match");
		assertException(() -> tester.notMatches(null));
		assertException(() -> tester.notMatches("test abcd 1234"));
		assertException(() -> tester.notMatches(".*"));
		assertException(() -> tester.notMatches("bad [syntax"));

		testString = null;
		assertException(() -> tester.notMatches(null));
		assertException(() -> tester.notMatches(""));
		assertException(() -> tester.notMatches(".*"));
		assertException(() -> tester.notMatches("bad match"));
		assertException(() -> tester.notMatches("bad [syntax"));

		testString = "";
		tester.notMatches(".");
		tester.notMatches("bad match");
		assertException(() -> tester.notMatches(null));
		assertException(() -> tester.notMatches(".*"));
		assertException(() -> tester.notMatches(""));
		assertException(() -> tester.notMatches("bad [syntax"));
	}

	@Test
	public void startsWith() {
		testString = "test abcd 1234";
		tester.startsWith("test a");
		tester.startsWith("test abcd 1234");
		tester.startsWith("t");
		tester.startsWith("");
		assertException(() -> tester.startsWith(null));
		assertException(() -> tester.startsWith("abcd 1234"));
		assertException(() -> tester.startsWith(".*"));

		testString = null;
		assertException(() -> tester.startsWith(null));
		assertException(() -> tester.startsWith(""));
		assertException(() -> tester.startsWith(".*"));

		testString = "";
		tester.startsWith("");
		assertException(() -> tester.startsWith(null));
		assertException(() -> tester.startsWith(".*"));
		assertException(() -> tester.startsWith(" "));
	}

	@Test
	public void notStartsWith() {
		testString = "test abcd 1234";
		assertException(() -> tester.notStartsWith("test a"));
		assertException(() -> tester.notStartsWith("test abcd 1234"));
		assertException(() -> tester.notStartsWith("t"));
		assertException(() -> tester.notStartsWith(""));
		assertException(() -> tester.notStartsWith(null));
		tester.notStartsWith("abcd 1234");
		tester.notStartsWith(".*");

		testString = null;
		assertException(() -> tester.notStartsWith(null));
		assertException(() -> tester.notStartsWith(""));
		assertException(() -> tester.notStartsWith(".*"));

		testString = "";
		tester.notStartsWith(".*");
		tester.notStartsWith(" ");
		assertException(() -> tester.notStartsWith(null));
		assertException(() -> tester.notStartsWith(""));
	}

	@Test
	public void endsWith() {
		testString = "test abcd 1234";
		tester.endsWith("cd 1234");
		tester.endsWith("test abcd 1234");
		tester.endsWith("4");
		tester.endsWith("");
		assertException(() -> tester.endsWith(null));
		assertException(() -> tester.endsWith("abcd 123"));
		assertException(() -> tester.endsWith(".*"));

		testString = null;
		assertException(() -> tester.endsWith(null));
		assertException(() -> tester.endsWith(""));
		assertException(() -> tester.endsWith(".*"));

		testString = "";
		tester.endsWith("");
		assertException(() -> tester.endsWith(null));
		assertException(() -> tester.endsWith(".*"));
		assertException(() -> tester.endsWith(" "));
	}

	@Test
	public void notEndsWith() {
		testString = "test abcd 1234";
		assertException(() -> tester.notEndsWith("1234"));
		assertException(() -> tester.notEndsWith("test abcd 1234"));
		assertException(() -> tester.notEndsWith("4"));
		assertException(() -> tester.notEndsWith(""));
		assertException(() -> tester.notEndsWith(null));
		tester.notEndsWith("test abcd");
		tester.notEndsWith(".*");
		tester.notEndsWith("12345");

		testString = null;
		assertException(() -> tester.notEndsWith(null));
		assertException(() -> tester.notEndsWith(""));
		assertException(() -> tester.notEndsWith(".*"));

		testString = "";
		tester.notEndsWith(".*");
		tester.notEndsWith(" ");
		assertException(() -> tester.notEndsWith(null));
		assertException(() -> tester.notEndsWith(""));
	}

	@Test
	public void isEmpty() {
		testString = "test abcd 1234";
		assertException(() -> tester.isEmpty());

		testString = null;
		tester.isEmpty();

		testString = "";
		tester.isEmpty();
	}

	@Test
	public void notEmpty() {
		testString = "test abcd 1234";
		tester.notEmpty();

		testString = null;
		assertException(() -> tester.notEmpty());

		testString = "";
		assertException(() -> tester.notEmpty());
	}

	@Test
	public void storeValue() {
		testString = "test abcd 1234";
		tester.storeValue("1");
		Assert.assertEquals("test abcd 1234", context.loadString("1"));
		assertException(() -> tester.storeValue(null));

		testString = null;
		assertException(() -> tester.storeValue("2"));
		assertException(() -> tester.storeValue(null));

		testString = "";
		tester.storeValue("3");
		Assert.assertEquals("", context.loadString("3"));
		assertException(() -> tester.storeValue(null));
	}

	@Test
	public void storeMatch() {
		testString = "test abcd 1234";

		tester.storeMatch("t1.1", "t.*t");
		Assert.assertEquals("test", context.loadString("t1.1"));

		tester.storeMatch("t1.2", "((test) (.*) .*)");
		Assert.assertEquals("test abcd 1234", context.loadString("t1.2"));

		tester.storeMatch("t1.3", ".*");
		Assert.assertEquals("test abcd 1234", context.loadString("t1.3"));

		tester.storeMatch("t1.4", "");
		Assert.assertEquals("", context.loadString("t1.4"));

		assertException(() -> tester.storeMatch("t1.5", "bad match"));
		assertException(() -> tester.storeMatch("t1.6", "bad [syntax"));
		assertException(() -> tester.storeMatch("t1.7", null));
		assertException(() -> tester.storeMatch(null, null));
		assertException(() -> tester.storeMatch(null, ".*"));


		tester.storeMatch("t1.a.g0", ".*",0);
		Assert.assertEquals("test abcd 1234", context.loadString("t1.a.g0"));

		assertException(() -> tester.storeMatch("t1.a.g1", ".*", 1));

		tester.storeMatch("t1.g0.2", "((st) (.*) ..)",0);
		Assert.assertEquals("st abcd 12", context.loadString("t1.g0.2"));

		tester.storeMatch("t1.g1", "((st) (.*) ..)",1);
		Assert.assertEquals("st abcd 12", context.loadString("t1.g1"));

		tester.storeMatch("t1.g2", "((st) (.*) ..)",2);
		Assert.assertEquals("st", context.loadString("t1.g2"));

		tester.storeMatch("t1.g3", "((st) (.*) ..)",3);
		Assert.assertEquals("abcd", context.loadString("t1.g3"));

		assertException(() -> tester.storeMatch("t1.g4", "((st) (.*) ..)", 4));
		assertException(() -> tester.storeMatch("t1.g-1", "((st) (.*) ..)", -1));

		testString = null;
		assertException(() -> tester.storeMatch("t2.1", null));
		assertException(() -> tester.storeMatch("t2.2", "bad match"));
		assertException(() -> tester.storeMatch("t2.3", "bad [syntax"));
		assertException(() -> tester.storeMatch("t2.4", ".*"));
		assertException(() -> tester.storeMatch("t2.5", ""));

		testString = "";
		assertException(() -> tester.storeMatch("t3.1", null));
		assertException(() -> tester.storeMatch("t3.2", "bad match"));
		assertException(() -> tester.storeMatch("t3.3", "bad [syntax"));

		tester.storeMatch("t3.3", ".*");
		Assert.assertEquals("", context.loadString("t3.3"));

		tester.storeMatch("t3.4", "");
		Assert.assertEquals("", context.loadString("t3.4"));
	}

	@Test
	public void testMatch() {
		testString = "test abcd 1234";
		Assert.assertEquals("test abcd 1234", tester.testMatch(".*").callRef());
		Assert.assertEquals("test", tester.testMatch("t.*t").callRef());
		Assert.assertEquals("abcd", tester.testMatch("[a-d]{4,4}").callRef());
		Assert.assertEquals("1234", tester.testMatch("[0-9]+").callRef());
		Assert.assertEquals("", tester.testMatch("").callRef());

		Assert.assertEquals("", tester.testMatch("bad match").callRef());
		Assert.assertEquals("", tester.testMatch("bad [syntax").callRef());
		Assert.assertEquals("", tester.testMatch(null).callRef());

		testString = null;
		Assert.assertEquals("", tester.testMatch("[0-9]+").callRef());
		Assert.assertEquals("", tester.testMatch("").callRef());
		Assert.assertEquals("", tester.testMatch("bad match").callRef());
		Assert.assertEquals("", tester.testMatch("bad [syntax").callRef());
		Assert.assertEquals("", tester.testMatch(null).callRef());

		testString = "";
		Assert.assertEquals("", tester.testMatch("[0-9]+").callRef());
		Assert.assertEquals("", tester.testMatch("").callRef());
		Assert.assertEquals("", tester.testMatch("bad match").callRef());
		Assert.assertEquals("", tester.testMatch("bad [syntax").callRef());
		Assert.assertEquals("", tester.testMatch(null).callRef());
	}

	@Test
	public void transform() {
		testString = "test abcd 1234";
		Assert.assertEquals("test abcd 1234", tester.transform(str -> str).callRef());
		Assert.assertEquals("TEST ABCD 1234", tester.transform(String::toUpperCase).callRef());
		Assert.assertEquals("abcd", tester.transform(str -> str.substring(5,9)).callRef());
		Assert.assertEquals("new string", tester.transform(str -> "new string").callRef());
		Assert.assertEquals("", tester.transform(str -> "").callRef());
		Assert.assertEquals(null, tester.transform(str -> null).callRef());
		Assert.assertEquals("", tester.transform(str -> {throw new Exception();}).callRef());

		testString = null;
		Assert.assertEquals(null, tester.transform(str -> str).callRef());
		Assert.assertEquals("", tester.transform(str -> {throw new Exception();}).callRef());

		testString = "";
		Assert.assertEquals("", tester.transform(str -> str).callRef());
		Assert.assertEquals("", tester.transform(str -> {throw new Exception();}).callRef());
	}

	@Test
	public void transformCompare() {
		testString = "test abcd 1234";
		Assert.assertEquals(4, (int)tester.transformCompare(str -> 4).callRef());
		Assert.assertEquals(2.6, tester.transformCompare(str -> 2.6).callRef(), 0);
		Assert.assertEquals(5.1f, tester.transformCompare(str -> 5.1f).callRef(), 0f);
		Assert.assertEquals(null, tester.transformCompare(str -> null).callRef());
		Assert.assertEquals(null, tester.transformCompare(str -> {throw new Exception();}).callRef());
		Assert.assertEquals("test abcd 1234", tester.transformCompare(str -> str).callRef());
		Date date = new Date();
		Assert.assertEquals(date, tester.transformCompare(str -> date).callRef());
		tester.transformCompare(str -> System.currentTimeMillis()).greaterThan(623l);

		testString = null;
		Assert.assertEquals(null, tester.transformCompare(str -> str).callRef());
		Assert.assertEquals(null, tester.transformCompare(str -> null).callRef());
		Assert.assertEquals(6, (int)tester.transformCompare(str -> 6).callRef());
		Assert.assertEquals(null, tester.transformCompare(str -> {throw new Exception();}).callRef());

		testString = "";
		Assert.assertEquals("", tester.transformCompare(str -> str).callRef());
		Assert.assertEquals(null, tester.transformCompare(str -> {throw new Exception();}).callRef());
	}

	@Test
	public void length() {
		testString = "test abcd 1234";
		Assert.assertEquals(14, (int)tester.length().callRef());
		tester.length().equals(14);

		testString = null;
		Assert.assertEquals(null, tester.length().callRef());

		testString = "";
		Assert.assertEquals(0, (int)tester.length().callRef());
		tester.length().equals(0);
	}

	@Test
	public void asInteger() {
		testString = "test abcd 1234";
		Assert.assertEquals(null, tester.asInteger().callRef());

		testString = null;
		Assert.assertEquals(null, tester.asInteger().callRef());

		testString = "";
		Assert.assertEquals(null, tester.asInteger().callRef());

		testString = "10";
		Assert.assertEquals(10, (int)tester.asInteger().callRef());
		tester.asInteger().equals(10);

		testString = "0";
		Assert.assertEquals(0, (int)tester.asInteger().callRef());
		tester.asInteger().equals(0);

		testString = "-0";
		Assert.assertEquals(0, (int)tester.asInteger().callRef());
		tester.asInteger().equals(0);

		testString = "-45";
		Assert.assertEquals(-45, (int)tester.asInteger().callRef());
		tester.asInteger().equals(-45);

		testString = "6.2";
		Assert.assertEquals(null, tester.asInteger().callRef());
	}

	@Test
	public void asDate() {
		testString = "test abcd 1234";
		Assert.assertEquals(null, tester.asDate().callRef());

		testString = null;
		Assert.assertEquals(null, tester.asDate().callRef());

		testString = "";
		Assert.assertEquals(null, tester.asDate().callRef());

		Date date = new Date(0);
		testString = date.toString();
		Assert.assertEquals(date, tester.asDate().callRef());
		tester.asDate().equals(date);
		tester.asDate().lessThan(new Date());
	}
}