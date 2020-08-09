package db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import db.annotation.DbField;
import db.annotation.DbId;
import db.annotation.DbLink;

public class DatabaseUtils {
	
	public static boolean isPersistable(Class<?> clazz) {
		for(Class<?> interfaces : clazz.getInterfaces()) {
			if (interfaces.getName().endsWith("Persistable")) {
				return true;
			}
		}
		return false;
	}
	
	public static void checkIfPersistable(Field f) {
		if (!isPersistable(f.getType())) {
			throw new IllegalStateException("Field object don't implement Persistable");
		}
	}
	
	public static boolean isDbId(Field f) {
		return isAnnotation(f, DbId.class);
	}
	
	public static boolean isDbField(Field f) {
		return isAnnotation(f, DbField.class);
	}
	
	public static String getDbField(Field f) {
		if (!isDbField(f)) {
			return null;
		}
		return f.getAnnotation(DbField.class).value();
	}
	
	public static boolean isDbLink(Field f) {
		return isAnnotation(f, DbLink.class);
	}
	
	public static String getDbLink(Field f) {
		if (!isDbLink(f)) {
			return null;
		}
		String val = f.getAnnotation(DbLink.class).value();
		if ("#PROPERTIES#".equals(val)) {
			val = DatabaseProperties.get("dblink");
		}
		return val;
	}
	
	public static Field getIdField(Class<?> clazz) {
		if (!isPersistable(clazz)) {
			return null;
		}
		for(Field f : clazz.getFields()) {
			if (isDbField(f) && isDbId(f)) {
				return f;
			}
		}
		return null;
	}
	
	public static <T extends Persistable> Persistable getDbLinkObject(Field f, T obj) {
		if (!isDbLink(f)) {
			return null;
		}
		checkIfPersistable(f);
		try {
			return (Persistable) f.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}
	
	public static DatabaseTable<?> getDatabaseTable(Field f) {
		if (!(isDbLink(f) && isPersistable(f.getType()))) {
			return null;
		}
		String pckg = getDbLink(f);
		String[] split = f.getType().getName().split("\\.");
		String className = split[split.length - 1];
		return getDatabaseTable(pckg, className);
	}
	
	public static DatabaseTable<?> getDatabaseTable(String pckg, String className) {
		Class<?> clazz;
		try {
			clazz = Class.forName(pckg + "." + className + "Table");
			return (DatabaseTable<?>) clazz.getMethod("getInstance").invoke(null);
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Unable to load " + className + "Table in the package " + pckg, e);
		}
	}
	
	private static boolean isAnnotation(Field f, Class<? extends Annotation> clazz) {
		return f.isAnnotationPresent(clazz);
	}

}
