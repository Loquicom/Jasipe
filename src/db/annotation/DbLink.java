package db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DbLink {
	
	/**
	 * Le nom du package dans lequel se trouve la class Table lié à l'entité qu'il faut importer
	 * La valeur par défaut #PROPERTIES# doit être remplacer par la valeur dans le fichier de properties
	 * @return
	 */
	public String value() default "#PROPERTIES#";
	
}
