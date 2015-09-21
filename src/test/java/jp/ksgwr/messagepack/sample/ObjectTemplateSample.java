package jp.ksgwr.messagepack.sample;

import static org.msgpack.template.Templates.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jp.ksgwr.messagepack.ObjectTemplate;

import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.type.Value;

import com.google.gson.Gson;

/**
 * MessagePack0.6とJSONの相互変換のテスト
 */
public class ObjectTemplateSample {

	/**
	 * テスト対象のPOJO
	 */
	public static class TestObj {
		public String str;
		public int i;
		public Child child;
		public Map<String, Object> children; //ObjectはJSONに入る型のみ, 設計が変わる可能性のあるフィールドの格納方式

		public String toString() {
			return "{str:"+str+",i:"+i+",child:"+child+",children:"+children.toString()+"}";
		}
	}

	/**
	 * テスト対象のPOJOの子クラス
	 */
	public static class Child {
		public String child;

		public String toString() {
			return "{"+child+"}";
		}
	}

	/**
	 * main program
	 * @param args 必要なし
	 * @throws Exception 恐らく発生しないexception
	 */
	public static void main(String[] args) throws Exception {

		//テスト対象のオブジェクト生成
		Child child = new Child();
		child.child = "hoge";

		TestObj obj = new TestObj();
		obj.str = "test";
		obj.i = 2;
		obj.child = child;
		obj.children = new HashMap<String, Object>();
		obj.children.put("foo", "bar");
		obj.children.put("piyo", ObjectTemplate.mapObject(child)); //Map<String,Object>には任意のオブジェクトは入れれないのでMapに変換
		obj.children.put("puyo", new String[]{"aaa"});

		//MessagePackの初期化、registerを使うので@Messageのアノテーションの使用はなし
		MessagePack msgpack = new MessagePack();
		msgpack.register(Object.class, ObjectTemplate.getInstance());
		msgpack.register(Child.class);
		msgpack.register(TestObj.class);

		//------------------------------------------------------
		//MessagePackのserialize
		byte[] bytes = msgpack.write(obj);

		//フィールド名がserializeされないことを確認
		System.out.println(bytes.length); //49
		System.out.println(Arrays.toString(bytes)); //[-108, -92, 116, 101, 115, 116, 2, -111, -92, 104, 111, 103, 101, -125, -93, 102, 111, 111, -93, 98, 97, 114, -92, 112, 105, 121, 111, -127, -91, 99, 104, 105, 108, 100, -92, 104, 111, 103, 101, -92, 112, 117, 121, 111, -111, -93, 97, 97, 97]

		Value value = msgpack.read(bytes);
		System.out.println(value); //["test",2,["hoge"],{"foo":"bar","piyo":{"child":"hoge"},"puyo":["aaa"]}]

		//MessagePackのdesirialzie
		TestObj to = msgpack.read(bytes, TestObj.class);
		System.out.println(to); //{str:test,i:2,child:{hoge},children:{foo=bar, piyo={child=hoge}, puyo=[aaa]}}

		//-----------------------------------------------------
		//Map変換後のフィールド名付きのserializeできることを確認
		bytes = msgpack.write(ObjectTemplate.mapObject(obj));

		//フィールド名が付くため圧縮効率は落ちる
		System.out.println(bytes.length); //76
		System.out.println(Arrays.toString(bytes)); //[-124, -93, 115, 116, 114, -92, 116, 101, 115, 116, -88, 99, 104, 105, 108, 100, 114, 101, 110, -125, -93, 102, 111, 111, -93, 98, 97, 114, -92, 112, 105, 121, 111, -127, -91, 99, 104, 105, 108, 100, -92, 104, 111, 103, 101, -92, 112, 117, 121, 111, -111, -93, 97, 97, 97, -95, 105, 2, -91, 99, 104, 105, 108, 100, -127, -91, 99, 104, 105, 108, 100, -92, 104, 111, 103, 101]

		value = msgpack.read(bytes);
		System.out.println(value); //{"str":"test","children":{"foo":"bar","piyo":{"child":"hoge"},"puyo":["aaa"]},"i":2,"child":{"child":"hoge"}}

		//MessagePackのdesirialzie, static importを使うことに注意
		Template<Map<String, Object>> mapTmpl = tMap(TString, ObjectTemplate.getInstance());
		Map<String, Object> mo = msgpack.read(bytes, mapTmpl);

		System.out.println(mo); //{str=test, i=2, children={foo=bar, piyo={child=hoge}, puyo=[aaa]}, child={child=hoge}}

		//-----------------------------------------------------
		//このオブジェクトがJSONでもserialize/desirializeできることを確認
		Gson gson = new Gson();

		//Map版も順番は違うが同じオブジェクトになることを確認
		String json = gson.toJson(to);
		String json2 = gson.toJson(mo);
		System.out.println(json); //{"str":"test","i":2,"child":{"child":"hoge"},"children":{"foo":"bar","piyo":{"child":"hoge"},"puyo":["aaa"]}}
		System.out.println(json2); //{"str":"test","i":2,"children":{"foo":"bar","piyo":{"child":"hoge"},"puyo":["aaa"]},"child":{"child":"hoge"}}

		TestObj to2 = gson.fromJson(json, TestObj.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> mo2 = (Map<String, Object>)gson.fromJson(json2, Map.class);

		System.out.println(to2); //{str:test,i:2,child:{hoge},children:{foo=bar, piyo={child=hoge}, puyo=[aaa]}}
		System.out.println(mo2); //{str=test, i=2.0, children={foo=bar, piyo={child=hoge}, puyo=[aaa]}, child={child=hoge}}
	}

}
