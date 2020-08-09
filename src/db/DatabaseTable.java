package db;

import java.util.List;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import db.annotation.DbTable;
import db.mapper.DatabaseMapper;

public abstract class DatabaseTable<T extends Persistable> {
	
	private static final Logger LOGGER = Logger.getLogger(DatabaseTable.class.getName());
	
	private Map<Long, T> cacheMap = new HashMap<>();
	
	public T get(T obj) {
		return getById(obj.getId());
	}
	
	public Optional<T> find(T obj) {
		return findById(obj.getId());
	}
	
	public T getById(long id) {
		if (cacheMap.containsKey(id)) {
			return cacheMap.get(id);
		}
		Optional<T> opt = getFromDbById(id);
		if(opt.isPresent()) {
			return cache(opt.get());
		}
		return null;
	}
	
	public Optional<T> findById(long id) {
		T obj = getById(id);
		if (obj == null) {
			return Optional.empty();
		}
		return Optional.of(obj);
	}
	
	public List<T> getByField(String fieldname, Object value) {
		List<T> list = getFromDbByField(fieldname, value);
		return cache(list);
	}
	
	public List<T> getWhere(List<String> fields, List<Object> values) {
		List<T> list = getWhereFromDb(fields, values);
		return cache(list);
	}
	
	public List<T> getAll() {
		List<T> list = getAllFromDb();
		return cache(list);
	}
	
	public T refresh(long id) throws DatabaseException {
		if (!DatabaseProperties.getBool("cache")) {
			throw new DatabaseException("Cache is not enabled, can't refresh");
		}
		if (!cacheMap.containsKey(id)) {
			throw new DatabaseException("Entity is not load, can't refresh");
		}
		Optional<T> optObj = getFromDbById(id);
		if(!optObj.isPresent()) {
			throw new DatabaseException("Unable to find entity in the database");
		}
		return cache(optObj.get());
	}
	
	public T refresh(T obj) throws DatabaseException {
		return refresh(obj.getId());
	}
	
	public T save(T obj) {
		try {
			// Si le cache est activé on se base dessus
			if (DatabaseProperties.getBool("cache")) {
				if (cacheMap.containsKey(obj.getId())) {
					update(obj);
				} else {
					insert(obj);
				}
			} 
			// Sinon on regarde en base
			else {
				if (findById(obj.getId()).isPresent()) {
					update(obj);
				} else {
					insert(obj);
				}
			}
			return cache(obj);
		} catch (DatabaseException e) {
			LOGGER.severe(e.getMessage());
			return null;
		}
	}
	
	public boolean del(long id) {
		try {
			// Suppr de la base
			delete(id);
			// Retire du cache
			remove(id);
			// TOut est ok
			return true;
		} catch (DatabaseException e) {
			LOGGER.severe(e.getMessage());
			return false;
		}
		
	}
	
	public boolean del(T obj) {
		return del(obj.getId());
	}
	
	protected T cache(T obj) {
		// Si l'objet à un id invalide
		if (obj.getId() <= 0) {
			return obj;
		}
		// Si le cache est actif
		if (DatabaseProperties.getBool("cache")) {
			if (cacheMap.containsKey(obj.getId())) {
				cacheMap.replace(obj.getId(), obj);
			} else {
				cacheMap.put(obj.getId(), obj);
			}
		}
		//System.out.println("Cache size: " + cacheMap.size());
		//cacheMap.forEach((key, val) -> System.out.println("Cache: " + key));
		return obj;
	}
	
	protected List<T> cache(List<T> list) {
		list.forEach(elt -> cache(elt));
		return list;
	}
	
	private void remove(long id) {
		if (cacheMap.containsKey(id)) {
			cacheMap.remove(id);
		}
	}
	
	private DbTable getDbTableAnnotation() {
		Class<?> clazz = this.getClass();
		// Recupération info anotation DbTable
		if (!clazz.isAnnotationPresent(DbTable.class)) {
			throw new IllegalStateException("Unable ton find DbTable annotation");
		}
		return clazz.getAnnotation(DbTable.class);
	}
	
	private Optional<T> getFromDbById(long id) {
		DbTable dbTable = getDbTableAnnotation();
		// Recupère l'id
		Field idField = DatabaseUtils.getIdField(dbTable.entity());
		if (idField == null) {
			throw new IllegalStateException("Unable to find id field");
		}
		// Requete sql
		SQLQueryBuilder sql = SQLQueryBuilder.selectQuery(dbTable.name());
		sql.add(DatabaseUtils.getDbField(idField), id);
		Optional<T> result = Database.query(sql.toString(), sql.getParams(), DatabaseMapper.objectMapper(dbTable));
		// Return
		return result;
	}
	
	private List<T> getFromDbByField(String fieldname, Object value) {
		DbTable dbTable = getDbTableAnnotation();
		// Requete sql
		SQLQueryBuilder sql = SQLQueryBuilder.selectQuery(dbTable.name());
		sql.add(fieldname, value);
		Optional<List<T>> result = Database.query(sql.toString(), sql.getParams(), DatabaseMapper.listMapper(dbTable));
		// Return
		if(result.isPresent()) {
			return result.get();
		}
		return new ArrayList<>();
	}
	
	private List<T> getWhereFromDb(List<String> where, List<Object> params) {
		DbTable dbTable = getDbTableAnnotation();
		// Requete sql
		SQLQueryBuilder sql = SQLQueryBuilder.selectQuery(dbTable.name());
		where.forEach(elt -> sql.add(elt));
		// Execution requete
		Optional<List<T>> result = Database.query(sql.toString(), params, DatabaseMapper.listMapper(dbTable));
		// Return
		if(result.isPresent()) {
			return result.get();
		}
		return new ArrayList<>();
	}
	
	private List<T> getAllFromDb() {
		DbTable dbTable = getDbTableAnnotation();
		// Requete sql
		SQLQueryBuilder sql = SQLQueryBuilder.selectQuery(dbTable.name());
		Optional<List<T>> result = Database.query(sql.toString(), DatabaseMapper.listMapper(dbTable));
		// Return
		if(result.isPresent()) {
			return result.get();
		}
		return new ArrayList<>();	
	}
	
	private void insert(T obj) throws DatabaseException {
		DbTable dbTable = getDbTableAnnotation();
		// Récupération du champ id
		Field id = DatabaseUtils.getIdField(obj.getClass());
		if (id == null) {
			throw new IllegalStateException("Unable to find id field");
		}
		try {
			// Création requete SQL
			SQLQueryBuilder sql = SQLQueryBuilder.insertQuery(dbTable.name());
			for(Field field : obj.getClass().getFields()) {
				String dbField;
				if ((dbField = DatabaseUtils.getDbField(field)) == null || DatabaseUtils.isDbId(field)) {
					continue;
				}
				Object value = field.get(obj);
				// Si c'est un lien vers une autre entité
				if (value != null && DatabaseUtils.isDbLink(field)) {
					// Recup class gestion de la table pour sauvegarder l'objet avant de l'ajouter à la requete
					Persistable link = DatabaseUtils.getDbLinkObject(field, obj);
					DatabaseTable dt = DatabaseUtils.getDatabaseTable(field);
					dt.save(link);
					value = link.getId();
				}
				sql.add(dbField, value);
			}
			// Execution de la requete
			String dbIdName = DatabaseUtils.getDbField(id);
			String attrIdName = id.getName();
			Map<String, Object> newId = Database.insert(sql.toString(), sql.getParams(), new String[]{dbIdName});
			if (newId.isEmpty()) {
				throw new DatabaseException("Unable to save data");
			}
			// Récupération de l'id
			Object val = newId.get(dbIdName);
			if (val instanceof BigDecimal) {
				BigDecimal b = (BigDecimal) val;
				obj.getClass().getField(attrIdName).set(obj, b.longValue());
			} else {
				obj.getClass().getField(attrIdName).set(obj, val);
			}
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new IllegalStateException("Unable to save data", e);
		}
	}
	
	private void update(T obj) throws DatabaseException {
		DbTable dbTable = getDbTableAnnotation();
		// Récupération du champ id
		Field id = DatabaseUtils.getIdField(obj.getClass());
		if (id == null) {
			throw new IllegalStateException("Unable to find id field");
		}
		try {
			// Création requete SQL
			SQLQueryBuilder sql = SQLQueryBuilder.updateQuery(dbTable.name());
			for(Field field : obj.getClass().getFields()) {
				String dbField;
				if ((dbField = DatabaseUtils.getDbField(field)) == null || DatabaseUtils.isDbId(field)) {
					continue;
				}
				Object value = field.get(obj);
				// Si c'est un lien vers une autre entité
				if (value != null && DatabaseUtils.isDbLink(field)) {
					// Recup class gestion de la table pour sauvegarder l'objet avant de l'ajouter à la requete
					Persistable link = DatabaseUtils.getDbLinkObject(field, obj);
					DatabaseTable dt = DatabaseUtils.getDatabaseTable(field);
					dt.save(link);
					value = link.getId();
				}
				sql.add(dbField, value);
			}
			// Ajoute l'id
			sql.addId(DatabaseUtils.getDbField(id), id.get(obj));
			// Execution de la requete
			if(!Database.execute(sql.toString(), sql.getParams())) {
				throw new DatabaseException("Unable to save data");
			}
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			throw new IllegalStateException("Unable to save data", e);
		}
	}
	
	private void delete(long id) throws DatabaseException {
		DbTable dbTable = getDbTableAnnotation();
		// Récupération du champ id
		Field idField = DatabaseUtils.getIdField(dbTable.entity());
		if (idField == null) {
			throw new IllegalStateException("Unable to find id field");
		}
		// Création requete SQL
		SQLQueryBuilder sql = SQLQueryBuilder.deleteQuery(dbTable.name());
		sql.addId(DatabaseUtils.getDbField(idField), id);
		// Execution de la requete
		if (!Database.execute(sql.toString(), sql.getParams())) {
			throw new DatabaseException("Unable to delete data");
		}
	}

}
