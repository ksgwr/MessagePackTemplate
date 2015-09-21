package jp.ksgwr.messagepack;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Compress Field Number Annotation for MessagePack
 * number value must set unique
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CompressField {
	/** number value (unique) */
	int value();
}
