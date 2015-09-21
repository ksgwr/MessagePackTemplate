package jp.ksgwr.messagepack;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;


/**
 * serialize/desirialzie likes map object in messagepack
 * field name including
 *
 * @param <T> TargetClass
 */
public class MapObjectTemplate<T> extends AbstractTemplate<T> {

	/** target class */
	private Class<T> clazz;

	/** compress option */
	private boolean isCompress;

	/** compress index */
	private Map<Integer,Field> compressIndex;

	/**
	 * constructor
	 * @param clazz target class
	 */
	public MapObjectTemplate(Class<T> clazz) {
		this(clazz, false);
	}

	/**
	 * constructor
	 * @param clazz target class
	 * @param isCompress compress option
	 */
	public MapObjectTemplate(Class<T> clazz, boolean isCompress) {
		this.clazz = clazz;
		this.isCompress = isCompress;

		if (isCompress) {
			compressIndex = new TreeMap<Integer,Field>();
			for(Field field:clazz.getFields()) {
				int mod = field.getModifiers();
				if(!Modifier.isPublic(mod) || Modifier.isStatic(mod) || Modifier.isTransient(mod)) {
					continue;
				}
				CompressField cfield = field.getAnnotation(CompressField.class);
				if (cfield==null) {
					continue;
				}
				compressIndex.put(cfield.value(), field);
			}
		}
	}

	@Override
	public void write(Packer paramPacker, T paramT, boolean paramBoolean)
			throws IOException {
		if(paramT==null) {
			if(paramBoolean) {
				throw new MessageTypeException("Attempted to write null");
			}
			paramPacker.writeNil();
			return;
		} else {
			Field[] fields = paramT.getClass().getFields();
			paramPacker.writeMapBegin(fields.length);
			for(Field field:fields) {
				int mod = field.getModifiers();
				if(Modifier.isPublic(mod) && !Modifier.isStatic(mod) && !Modifier.isTransient(mod)) {
					try {
						CompressField cfield;
						if (isCompress && (cfield = field.getAnnotation(CompressField.class)) != null) {
							// if compress option is true, serialize field name to integer
							paramPacker.write(cfield.value());
						} else {
							paramPacker.write(field.getName());
						}
						paramPacker.write(field.get(paramT));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new MessageTypeException("Illegal Exception");
					}
				}
			}
			paramPacker.writeMapEnd(paramBoolean);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public T read(Unpacker paramUnpacker, T paramT,
			boolean paramBoolean) throws IOException {
		T obj = null;

		try {
			obj = clazz.newInstance();
			int size = paramUnpacker.readMapBegin();
			for(int i=0;i<size;i++) {
				try {
					Field field;
					if (isCompress && paramUnpacker.getNextType()==ValueType.INTEGER) {
						field = compressIndex.get(paramUnpacker.readInt());
						if (field == null) {
							// skip value, not exists its field
							paramUnpacker.skip();
							continue;
						}
					} else {
						field = clazz.getField(paramUnpacker.readString());
					}

					Object val = null;

					if (paramUnpacker.getNextType() == ValueType.NIL) {
						paramUnpacker.readNil();
						continue;
					}

					Type type = field.getGenericType();

					if(type instanceof Class) {
						val = paramUnpacker.read(field.getType());
					} else if(type instanceof ParameterizedType) {
						// List, Map etc...
						ParameterizedType pt = (ParameterizedType) type;
						Type[] types = pt.getActualTypeArguments();
						if(paramUnpacker.getNextType()==ValueType.MAP) {
							int len = paramUnpacker.readMapBegin();
							Map map = new LinkedHashMap();
							for(int j=0;j<len;j++) {
								Object key = paramUnpacker.read((Class)types[0]);
								Object value = paramUnpacker.read((Class)types[1]);
								map.put(key, value);
							}
							paramUnpacker.readMapEnd();
							val = map;
						} else if(paramUnpacker.getNextType()==ValueType.ARRAY){
							int len = paramUnpacker.readArrayBegin();
							List list = new ArrayList<>(len);
							for(int j=0;j<len;j++) {
								list.add(paramUnpacker.read((Class)types[0]));
							}
							paramUnpacker.readArrayEnd();
							val = list;
						}
					}
					field.set(obj, val);
				} catch (NoSuchFieldException | SecurityException e) {
					// skip value, not exists its field
					paramUnpacker.skip();
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new MessageTypeException("Instantiation, Illegal Exception");
		}

		return obj;
	}

}
