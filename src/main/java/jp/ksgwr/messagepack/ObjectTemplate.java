package jp.ksgwr.messagepack;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;


/**
 * serialize/desirialize POJO Object MessagePack Template for using Map(String, Object)
 */
public class ObjectTemplate extends AbstractTemplate<Object> {

	/** Singletonのinstance */
	private static final ObjectTemplate INSTANCE = new ObjectTemplate();

	/** private constructor */
	private ObjectTemplate() {
	}

	/**
	 * get singleton instance
	 * @return ObjectTemplate instance
	 */
	public static ObjectTemplate getInstance() {
		return INSTANCE;
	}

	/**
	 * POJO object to Map(String, Object)
	 * @param obj pojo object
	 * @return map object
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> mapObject(Object obj) {
		Map<String, Object> map;
		if(obj instanceof Map) {
			map = (Map<String, Object>)obj;
		} else {
			map = new HashMap<String, Object>();
			for (Field field : obj.getClass().getFields()) {
				int mod = field.getModifiers();
				if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)
						&& !Modifier.isTransient(mod)) {
					try {
						Object val = field.get(obj);
						if (!(val instanceof Number || val instanceof Boolean || val instanceof String)) {
							val = mapObject(val);
						}
						map.put(field.getName(), val);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						// not reachable
						throw new RuntimeException(e);
					}
				}
			}
		}
		return map;
	}

	@Override
	public void write(Packer paramPacker, Object paramT, boolean paramBoolean)
			throws IOException {
		if(paramT==null) {
			if(paramBoolean) {
				throw new MessageTypeException("Attempted to write null");
			}
			paramPacker.writeNil();
			return;
		} else {
			paramPacker.write(paramT);
		}

	}

	@Override
	public Object read(Unpacker paramUnpacker, Object paramT,
			boolean paramBoolean) throws IOException {
		if (!paramBoolean && paramUnpacker.trySkipNil()) {
			return null;
		}
		Object obj = readObject(paramUnpacker);
		return obj;
	}

	/**
	 * check MessagePack Type, and unpack MessagePack
	 * @param paramUnpacker unpacker
	 * @return object
	 * @throws IOException exception
	 */
	private Object readObject(Unpacker paramUnpacker) throws IOException {
		Object obj = null;
		int size;
		switch(paramUnpacker.getNextType()) {
		case NIL:
			paramUnpacker.readNil();
			obj = null;
			break;
		case ARRAY :
			// don't know what type of List, read as List<Object>
			size = paramUnpacker.readArrayBegin();
			ArrayList<Object> list = new ArrayList<>(size);
			for(int i=0;i<size;i++) {
				Object val = readObject(paramUnpacker);
				list.add(val);
			}
			paramUnpacker.readArrayEnd();
			obj = list;
			break;
		case BOOLEAN :
			obj = paramUnpacker.readBoolean();
			break;
		case FLOAT :
			obj = paramUnpacker.readFloat();
			break;
		case INTEGER :
			obj = paramUnpacker.readInt();
			break;
		case MAP :
			size = paramUnpacker.readMapBegin();
			HashMap<String,Object> map = new HashMap<>();
			for(int i=0;i<size;i++) {
				String key = paramUnpacker.readString();
				Object val = readObject(paramUnpacker);
				map.put(key, val);
			}
			paramUnpacker.readMapEnd();
			obj = map;
			break;
		case RAW :
			obj = paramUnpacker.readString();
			break;
		default:
			break;
		}
		return obj;
	}

}
