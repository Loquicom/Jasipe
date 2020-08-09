package db.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import db.DatabaseTable;
import db.DatabaseUtils;
import db.annotation.DbTable;

public class DatabaseMapper {
	
	public static <T> ResultSetMapper<T> objectMapper(DbTable dbTable) {
		return rs -> {
			try {
				T obj = (T) dbTable.entity().getConstructor().newInstance();
				if (!rs.next()) {
					return null;
				}
				for(Field field : dbTable.entity().getFields()) {
					String dbfield;
					if((dbfield = DatabaseUtils.getDbField(field)) == null) {
						continue;
					}
					// Recup valeur dans le resultat
					Object value = rs.getObject(dbfield);
					// Si lien avec une autre entité
					String link = DatabaseUtils.getDbLink(field);
					if(value != null && link != null) {
						DatabaseUtils.checkIfPersistable(field);
						Long otherId = (Long) value;
						if(otherId == null || otherId == 0) {
							value = null;
						} else {
							DatabaseTable table = DatabaseUtils.getDatabaseTable(field);
							value = table.getById(otherId);
						}
					}
					field.set(obj, value);
				}
				return obj;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | SQLException e) {
				throw new IllegalStateException("Unable to map value to the entity", e);
			}
		};
	}
	
	public static <T> ResultSetMapper<List<T>> listMapper(DbTable dbTable) {
		return rs -> {
			try {
				List<T> list = new ArrayList<>();
				while(rs.next()) {
					T obj = (T) dbTable.entity().getConstructor().newInstance();
					for(Field field : dbTable.entity().getFields()) {
						String dbfield;
						if((dbfield = DatabaseUtils.getDbField(field)) == null) {
							continue;
						}
						// Recup valeur dans le resultat
						Object value = rs.getObject(dbfield);
						// Si lien avec une autre entité
						String link = DatabaseUtils.getDbLink(field);
						if(value != null && link != null) {
							DatabaseUtils.checkIfPersistable(field);
							Long otherId = (Long) value;
							if(otherId == null || otherId == 0) {
								value = null;
							} else {
								DatabaseTable table = DatabaseUtils.getDatabaseTable(field);
								value = table.getById(otherId);
							}
						}
						field.set(obj, value);
					}
					list.add(obj);
				}
				return list;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | SQLException e) {
				throw new IllegalStateException("Unable to map value to the entity");
			}
		};
	}

}
