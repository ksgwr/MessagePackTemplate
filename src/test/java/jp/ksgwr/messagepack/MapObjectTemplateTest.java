package jp.ksgwr.messagepack;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;

public class MapObjectTemplateTest {

	public static class TestObj {
		public String a;
		public int b;
	}

	@Test
	public void simpleTest() throws IOException {
		MessagePack msgpack = new MessagePack();
		msgpack.register(TestObj.class, new MapObjectTemplate<TestObj>(TestObj.class));

		TestObj t = new TestObj();
		t.a = "aval";
		t.b = 1;

		byte[] bytes = msgpack.write(t);

		Value value = msgpack.read(bytes);

		assertEquals("{\"a\":\"aval\",\"b\":1}", value.toString());
	}
}
