package org.pagemodel.core.utils.json;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsonArrayBuilderTest {
	private JsonArrayBuilder builder;

	@Before
	public void setup(){
		builder = new JsonArrayBuilder();
	}

	@Test
	public void addValue() {
		Assert.assertEquals(0, builder.toList().size());

		builder.addValue(null);
		Assert.assertEquals(1, builder.toList().size());
		Assert.assertEquals(null, builder.toList().get(0));

		builder.addValue("");
		Assert.assertEquals(2, builder.toList().size());
		Assert.assertEquals("", builder.toList().get(1));
		Assert.assertEquals(null, builder.toList().get(0));

		builder.addValue("value");
		Assert.assertEquals(3, builder.toList().size());
		Assert.assertEquals("value", builder.toList().get(2));
		Assert.assertEquals("", builder.toList().get(1));
		Assert.assertEquals(null, builder.toList().get(0));
	}

	@Test
	public void addObject() {
		Assert.assertEquals(0, builder.toList().size());

		builder.addObject(null);
		Assert.assertEquals(0, builder.toList().size());

		builder.addObject(obj -> {});
		Assert.assertEquals(1, builder.toList().size());
		Assert.assertEquals(0, ((Map)builder.toList().get(0)).size());

		builder.addObject(obj -> obj.addValue("value", "val"));
		Assert.assertEquals(2, builder.toList().size());
		Assert.assertEquals(1, ((Map)builder.toList().get(1)).size());
		Assert.assertEquals("val", ((Map)builder.toList().get(1)).get("value"));
		Assert.assertEquals(0, ((Map)builder.toList().get(0)).size());
	}

	@Test
	public void addArray() {
		Assert.assertEquals(0, builder.toList().size());

		builder.addArray(null);
		Assert.assertEquals(0, builder.toList().size());

		builder.addArray(arr -> {});
		Assert.assertEquals(1, builder.toList().size());
		Assert.assertEquals(0, ((List)builder.toList().get(0)).size());

		builder.addArray(arr -> arr.addValue("val"));
		Assert.assertEquals(2, builder.toList().size());
		Assert.assertEquals(1, ((List)builder.toList().get(1)).size());
		Assert.assertEquals("val", ((List)builder.toList().get(1)).get(0));
		Assert.assertEquals(0, ((List)builder.toList().get(0)).size());
	}

	@Test
	public void merge() {
		Assert.assertEquals(0, builder.toList().size());

		builder.merge(null);
		Assert.assertEquals(0, builder.toList().size());

		builder.merge(Arrays.asList("val", 0, "", null));
		Assert.assertEquals(4, builder.toList().size());
		Assert.assertEquals("val", builder.toList().get(0));
		Assert.assertEquals(0, builder.toList().get(1));
		Assert.assertEquals("", builder.toList().get(2));
		Assert.assertEquals(null, builder.toList().get(3));

		builder.merge(Arrays.asList("val2", 4));
		Assert.assertEquals(6, builder.toList().size());
		Assert.assertEquals("val", builder.toList().get(0));
		Assert.assertEquals(0, builder.toList().get(1));
		Assert.assertEquals("", builder.toList().get(2));
		Assert.assertEquals(null, builder.toList().get(3));
		Assert.assertEquals("val2", builder.toList().get(4));
		Assert.assertEquals(4, builder.toList().get(5));
	}

	@Test
	public void doAdd() {
		Assert.assertEquals(0, builder.toList().size());

		builder.doAdd(null);
		Assert.assertEquals(0, builder.toList().size());

		builder.doAdd(arr -> {});
		Assert.assertEquals(0, builder.toList().size());

		builder.doAdd(arr -> arr.addValue("val"));
		Assert.assertEquals(1, builder.toList().size());
		Assert.assertEquals("val", builder.toList().get(0));

		builder.doAdd(arr -> arr.addValue("val"));
		Assert.assertEquals(2, builder.toList().size());
		Assert.assertEquals("val", builder.toList().get(1));
		Assert.assertEquals("val", builder.toList().get(0));

		builder.doAdd(arr -> arr.addValue(null).addObject(obj -> {}));
		Assert.assertEquals(4, builder.toList().size());
		Assert.assertEquals(0, ((Map)builder.toList().get(3)).size());
		Assert.assertEquals(null, builder.toList().get(2));
		Assert.assertEquals("val", builder.toList().get(1));
		Assert.assertEquals("val", builder.toList().get(0));
	}
}