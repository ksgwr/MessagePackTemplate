package jp.ksgwr.messagepack;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;

public class ObjectTemplateTest {

	public static class TestObj {
		Map<String, Object> mapObj;
		String longFieldNameValue;
	}

	/**
	 * Map<String, Object>がシリアライズ可能で、
	 * それ以外のフィールド名はシリアライズされないことを確認する
	 * @throws IOException MessagePackのException
	 */
	@Test
	public void simpleTest() throws IOException {
		MessagePack msgpack = new MessagePack();
		msgpack.register(Object.class, ObjectTemplate.getInstance());
		msgpack.register(TestObj.class);

		TestObj t = new TestObj();
		t.mapObj = new LinkedHashMap<>();

		t.mapObj.put("a", "aval");
		t.mapObj.put("b", 1);

		t.longFieldNameValue = "c";

		byte[] bytes = msgpack.write(t);

		Value value = msgpack.read(bytes);

		assertEquals("[{\"a\":\"aval\",\"b\":1},\"c\"]", value.toString());
	}
}
