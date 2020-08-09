package db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import db.Persistable;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DbTable {
	
	public String name();
	
	public Class<? extends Persistable> entity();

}
