package org.pagemodel.core.utils.json;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonObjectBuilderTest {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private JsonObjectBuilder builder;

	@Before
	public void setup(){
		builder = new JsonObjectBuilder();
	}

	@Test
	public void addValue() {
		Assert.assertEquals(null, builder.toMap().get("value"));
		Assert.assertEquals(null, builder.toMap().get(null));
		Assert.assertEquals(0, builder.toMap().size());

		builder.addValue(null, null);
		Assert.assertEquals(null, builder.toMap().get(null));
		Assert.assertEquals(0, builder.toMap().size());

		builder.addValue(null, "value");
		Assert.assertEquals(null, builder.toMap().get(null));
		Assert.assertEquals(0, builder.toMap().size());

		builder.addValue("value", null);
		Assert.assertEquals(null, builder.toMap().get("value"));
		Assert.assertEquals(1, builder.toMap().size());

		builder.addValue("value", null);
		Assert.assertEquals(null, builder.toMap().get("value"));
		Assert.assertEquals(1, builder.toMap().size());

		builder.addValue("value", "val");
		Assert.assertEquals("val", builder.toMap().get("value"));
		Assert.assertEquals(1, builder.toMap().size());

		builder.addValue("value", null);
		Assert.assertEquals(null, builder.toMap().get("value"));
		Assert.assertEquals(1, builder.toMap().size());

		builder.addValue("value2", "val2");
		Assert.assertEquals("val2", builder.toMap().get("value2"));
		Assert.assertEquals(null, builder.toMap().get("value"));
		Assert.assertEquals(2, builder.toMap().size());

		builder.addValue("value2", "val2-2");
		Assert.assertEquals("val2-2", builder.toMap().get("value2"));
		Assert.assertEquals(null, builder.toMap().get("value"));
		Assert.assertEquals(2, builder.toMap().size());

		builder.addValue("value", "val-1");
		Assert.assertEquals("val-1", builder.toMap().get("value"));
		Assert.assertEquals("val2-2", builder.toMap().get("value2"));
		Assert.assertEquals(2, builder.toMap().size());

		Map<String,Object> obj = builder.toMap();
		builder.addValue("value3", obj);
		Assert.assertEquals(obj, builder.toMap().get("value3"));
		Assert.assertEquals("val-1", builder.toMap().get("value"));
		Assert.assertEquals("val2-2", builder.toMap().get("value2"));
		Assert.assertEquals(3, builder.toMap().size());

		builder.addValue("", null);
		Assert.assertEquals(null, builder.toMap().get(""));
		Assert.assertEquals(4, builder.toMap().size());

		builder.addValue("", "");
		Assert.assertEquals("", builder.toMap().get(""));
		Assert.assertEquals(4, builder.toMap().size());
	}

	@Test
	public void addObject() {
		builder.addObject(null, null);
		Assert.assertEquals(0, builder.toMap().size());

		builder.addObject(null, obj -> obj.addValue("value", "val"));
		Assert.assertEquals(0, builder.toMap().size());

		builder.addObject("obj", null);
		Assert.assertEquals(0, builder.toMap().size());

		builder.addObject("obj", obj -> obj.addValue("value", "val"));
		Assert.assertEquals(1, builder.toMap().size());
		Assert.assertEquals("val", ((Map)builder.toMap().get("obj")).get("value"));

		builder.addObject("obj", obj -> obj.addValue("value2", "val2"));
		Assert.assertEquals(1, builder.toMap().size());
		Assert.assertEquals(null, ((Map)builder.toMap().get("obj")).get("value"));
		Assert.assertEquals("val2", ((Map)builder.toMap().get("obj")).get("value2"));

		builder.addObject("obj", null);
		Assert.assertEquals(1, builder.toMap().size());
		Assert.assertEquals(null, ((Map)builder.toMap().get("obj")).get("value"));
		Assert.assertEquals("val2", ((Map)builder.toMap().get("obj")).get("value2"));

		builder.addObject("obj", obj -> {});
		Assert.assertEquals(1, builder.toMap().size());
		Assert.assertEquals(0, ((Map)builder.toMap().get("obj")).size());

		builder.addObject("obj2", obj -> obj.addValue("value", "val"));
		Assert.assertEquals(2, builder.toMap().size());
		Assert.assertEquals(0, ((Map)builder.toMap().get("obj")).size());
		Assert.assertEquals(1, ((Map)builder.toMap().get("obj2")).size());
	}

	@Test
	public void addArray() {
		builder.addArray(null, null);
		Assert.assertEquals(0, builder.toMap().size());

		builder.addArray(null, arr -> arr.addValue("val"));
		Assert.assertEquals(0, builder.toMap().size());

		builder.addArray("arr", null);
		Assert.assertEquals(0, builder.toMap().size());

		builder.addArray("arr", arr -> arr.addValue("val"));
		Assert.assertEquals(1, builder.toMap().size());
		Assert.assertEquals(1, ((List)builder.toMap().get("arr")).size());
		Assert.assertEquals("val", ((List)builder.toMap().get("arr")).get(0));

		builder.addArray("arr", arr -> {});
		Assert.assertEquals(1, builder.toMap().size());
		Assert.assertEquals(0, ((List)builder.toMap().get("arr")).size());

		builder.addArray("arr2", arr -> arr.addValue("val2"));
		Assert.assertEquals(2, builder.toMap().size());
		Assert.assertEquals(1, ((List)builder.toMap().get("arr2")).size());
		Assert.assertEquals("val2", ((List)builder.toMap().get("arr2")).get(0));
		Assert.assertEquals(0, ((List)builder.toMap().get("arr")).size());
	}

	@Test
	public void merge() {
		builder.merge(null);
		Assert.assertEquals(0, builder.toMap().size());

		builder.merge(Collections.emptyMap());
		Assert.assertEquals(0, builder.toMap().size());

		Map<String,Object> obj = new HashMap<>();
		obj.put("value", "val1");

		builder.merge(obj);
		Assert.assertEquals(1, builder.toMap().size());
		Assert.assertEquals("val1", builder.toMap().get("value"));

		obj.put("value", "val2");
		builder.merge(obj);
		Assert.assertEquals(1, builder.toMap().size());
		Assert.assertEquals("val2", builder.toMap().get("value"));

		Map<String,Object> obj2 = new HashMap<>();
		obj2.put("value1", obj);
		obj2.put("value2", "val-2");
		builder.merge(obj2);
		Assert.assertEquals(3, builder.toMap().size());
		Assert.assertEquals("val2", builder.toMap().get("value"));
		Assert.assertEquals(obj, builder.toMap().get("value1"));
		Assert.assertEquals("val-2", builder.toMap().get("value2"));
	}

	@Test
	public void doAdd() {
		builder.doAdd(null);
		Assert.assertEquals(0, builder.toMap().size());

		builder.doAdd(obj -> {});
		Assert.assertEquals(0, builder.toMap().size());

		builder.doAdd(obj -> obj.addValue("value", "val"));
		Assert.assertEquals(1, builder.toMap().size());
		Assert.assertEquals("val", builder.toMap().get("value"));

		builder.doAdd(obj -> obj.addValue("value", "val2"));
		Assert.assertEquals(1, builder.toMap().size());
		Assert.assertEquals("val2", builder.toMap().get("value"));

		builder.doAdd(obj -> obj.addValue("value2", "val-2"));
		Assert.assertEquals(2, builder.toMap().size());
		Assert.assertEquals("val-2", builder.toMap().get("value2"));
		Assert.assertEquals("val2", builder.toMap().get("value"));
	}
}