package jp.ksgwr.messagepack.sample;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ksgwr.messagepack.CompressField;
import jp.ksgwr.messagepack.MapObjectTemplate;

import org.msgpack.MessagePack;
import org.msgpack.type.Value;

import com.google.gson.Gson;


/**
 * MessagePackをフィールド名有りでシリアライズするテスト
 *
 */
public class MapObjectTemplateSample {

	/**
	 * テスト対象のPOJO
	 */
	public static class TestObj {
		@CompressField(1)
		public String str;

		@CompressField(2)
		public int i;

		//@CompressField(3) //一部をわざと圧縮しないでおく
		public String[] strary;

		@CompressField(4)
		public List<String> strlist;

		@CompressField(5)
		public Map<String,Boolean> boolmap;

		@CompressField(6)
		public Child child;
	}

	/**
	 * TestObjの仕様変更版
	 *
	 */
	public static class AdvTestObj {
		public String str;

		public int i;

		public boolean additionalField;
	}

	/**
	 * テスト対象のPOJOの子クラス
	 */
	public static class Child {

		@CompressField(1)
		public String child;
	}

	/**
	 * main program
	 * @param args 必要なし
	 * @throws Exception 恐らく発生しないexception
	 */
	public static void main(String[] args) throws Exception {
		TestObj obj = new TestObj();
		obj.str = "test";
		obj.i = 2;
		obj.strary = new String[]{"aa"};
		obj.strlist = new ArrayList<String>();
		obj.strlist.add("bb");
		obj.boolmap = new HashMap<String,Boolean>();
		obj.boolmap.put("hoge", false);

		Child child = new Child();
		child.child = "hoge";

		obj.child = child;

		// Map形式でシリアライズする
		MessagePack msgpack = new MessagePack();
		msgpack.register(Child.class, new MapObjectTemplate<Child>(Child.class));
		msgpack.register(TestObj.class, new MapObjectTemplate<TestObj>(TestObj.class));

		byte[] bytes = msgpack.write(obj);

		System.out.println(bytes.length); // 69
		System.out.println(Arrays.toString(bytes)); // [-122, -93, 115, 116, 114, -92, 116, 101, 115, 116, -95, 105, 2, -90, 115, 116, 114, 97, 114, 121, -111, -94, 97, 97, -89, 115, 116, 114, 108, 105, 115, 116, -111, -94, 98, 98, -89, 98, 111, 111, 108, 109, 97, 112, -127, -92, 104, 111, 103, 101, -62, -91, 99, 104, 105, 108, 100, -127, -91, 99, 104, 105, 108, 100, -92, 104, 111, 103, 101]

		Value value = msgpack.read(bytes);
		System.out.println(value); // {"str":"test","i":2,"strary":["aa"],"strlist":["bb"],"boolmap":{"hoge":false},"child":{"child":"hoge"}}

		TestObj to = msgpack.read(bytes, TestObj.class);
		System.out.println(new Gson().toJson(to)); // {"str":"test","i":2,"strary":["aa"],"strlist":["bb"],"boolmap":{"hoge":false},"child":{"child":"hoge"}}


		// 通常との圧縮率の比較
		msgpack = new MessagePack();
		msgpack.register(Child.class);
		msgpack.register(TestObj.class);

		bytes = msgpack.write(obj);

		System.out.println(bytes.length); // 28
		System.out.println(Arrays.toString(bytes)); // [-106, -92, 116, 101, 115, 116, 2, -111, -94, 97, 97, -111, -94, 98, 98, -127, -92, 104, 111, 103, 101, -62, -111, -92, 104, 111, 103, 101]

		value = msgpack.read(bytes);
		System.out.println(value); // ["test",2,["aa"],["bb"],{"hoge":false},["hoge"]]


		// フィールド名をアノテーションで圧縮する場合
		msgpack = new MessagePack();

		msgpack.register(Child.class, new MapObjectTemplate<Child>(Child.class, true));
		msgpack.register(TestObj.class, new MapObjectTemplate<TestObj>(TestObj.class, true));

		bytes = msgpack.write(obj);

		System.out.println(bytes.length); // 41
		System.out.println(Arrays.toString(bytes)); // [-122, 1, -92, 116, 101, 115, 116, 2, 2, -90, 115, 116, 114, 97, 114, 121, -111, -94, 97, 97, 4, -111, -94, 98, 98, 5, -127, -92, 104, 111, 103, 101, -62, 6, -127, 1, -92, 104, 111, 103, 101]

		value = msgpack.read(bytes);
		System.out.println(value); // {1:"test",2:2,"strary":["aa"],4:["bb"],5:{"hoge":false},6:{1:"hoge"}}


		// 仕様変更があるオブジェクトが来た場合、不要なフィールドは無視する
		AdvTestObj obj2 = new AdvTestObj();
		obj2.str = "test";
		obj2.i = 2;
		obj2.additionalField = true;

		msgpack = new MessagePack();

		msgpack.register(Child.class, new MapObjectTemplate<Child>(Child.class, true));
		msgpack.register(AdvTestObj.class, new MapObjectTemplate<AdvTestObj>(AdvTestObj.class));
		msgpack.register(TestObj.class, new MapObjectTemplate<TestObj>(TestObj.class));

		bytes = msgpack.write(obj2);

		System.out.println(bytes.length);
		System.out.println(Arrays.toString(bytes));

		value = msgpack.read(bytes);
		System.out.println(value);

		TestObj obj3 = msgpack.read(bytes, TestObj.class);
		System.out.println(new Gson().toJson(obj3));
	}
}
