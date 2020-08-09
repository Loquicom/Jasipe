package db;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SQLQueryBuilder {
	
	private static final int SELECT_QUERY = 0;
	private static final int INSERT_QUERY = 1;
	private static final int UPDATE_QUERY = 2;
	private static final int DELETE_QUERY = 3;
	
	private int queryType;
	private String table;
	private Map<String, Object> data = new LinkedHashMap<>();
	private String idKey;
	
	private SQLQueryBuilder(int queryType, String table) {
		this.queryType = queryType;
		this.table = table;
	}
	
	public SQLQueryBuilder add(String fieldName) {
		return add(fieldName, null);
	}
	
	public SQLQueryBuilder add(String fieldName, Object value) {
		if (data.containsKey(fieldName)) {
			data.replace(fieldName, value);
		} else {
			data.put(fieldName, value);
		}
		return this;
	}
	
	public SQLQueryBuilder addId(String idName, Object value) {
		add(idName, value);
		idKey = idName;
		return this;
	}
	
	public SQLQueryBuilder remove(String fieldName) {
		if (data.containsKey(fieldName)) {
			data.remove(fieldName);
		}
		return this;
	}
	
	public List<String> listField() {
		List<String> result = new ArrayList<>();
		data.forEach((key, val) -> result.add(key));
		return result;
	}
	
	public String getSQL() {
		switch(queryType) {
			case SELECT_QUERY:
				return select();
			case INSERT_QUERY:
				return insert();
			case UPDATE_QUERY:
				return update();
			case DELETE_QUERY:
				return delete();
			default:
				return null;
		}
	}
	
	public String getSQL(String append) {
		return getSQL() + " " + append;
	}
	
	public List<Object> getParams() {
		List<Object> result = new ArrayList<>();
		data.forEach((key, val) -> {
			if (idKey != null) {
				if (!idKey.equals(key)) {
					result.add(val);
				}
			} else {
				result.add(val);
			}
		});
		if (idKey != null) {
			result.add(data.get(idKey));
		}
		return result;
	}
	
	public String toSQL() {
		return getSQL();
	}
	
	@Override
	public String toString() {
		return getSQL();
	}
	
	public static SQLQueryBuilder selectQuery(String table) {
		return new SQLQueryBuilder(SELECT_QUERY, table);
	}
	
	public static SQLQueryBuilder insertQuery(String table) {
		return new SQLQueryBuilder(INSERT_QUERY, table);
	}
	
	public static SQLQueryBuilder updateQuery(String table) {
		return new SQLQueryBuilder(UPDATE_QUERY, table);
	}
	
	public static SQLQueryBuilder deleteQuery(String table) {
		return new SQLQueryBuilder(DELETE_QUERY, table);
	}
	
	private String select() {
		StringBuilder sql = new StringBuilder();
		sql.append("Select * From ");
		sql.append(table);
		sql.append(" Where 1=1");
		data.forEach((key, val) -> {
			sql.append(" And ");
			sql.append(key);
			sql.append(" = ?");
		});
		return sql.toString();
	}
	
	private String insert() {
		StringBuilder sql = new StringBuilder();
		StringBuilder val = new StringBuilder();
		boolean first = true;
		sql.append("Insert into ");
		sql.append(table);
		sql.append("(");
		for(Entry<String, Object> entry : data.entrySet()) {
			if (!first) {
				sql.append(",");
				val.append(",");
			}
			sql.append(entry.getKey());
			val.append("?");
			first = false;
		}
		sql.append(") Values(");
		sql.append(val);
		sql.append(")");
		return sql.toString();
	}
	
	private String update() {
		// Si pas d'id indiqué
		if (idKey == null) {
			return null;
		}
		StringBuilder sql = new StringBuilder();
		boolean first = true;
		sql.append("Update ");
		sql.append(table);
		sql.append(" Set ");
		for(Entry<String, Object> entry : data.entrySet()) {
			// On ajoute pas la clef
			if(idKey.equals(entry.getKey())) {
				continue;
			}
			if (!first) {
				sql.append(",");
			}
			sql.append(entry.getKey());
			sql.append(" = ?");
			first = false;
		}
		sql.append(" Where ");
		sql.append(idKey);
		sql.append(" = ?");
		return sql.toString();
	}
	
	private String delete() {
		// Si pas d'id indiqué
		if (idKey == null) {
			return null;
		}
		StringBuilder sql = new StringBuilder();
		sql.append("Delete From ");
		sql.append(table);
		sql.append(" Where 1=1");
		data.forEach((key, val) -> {
			if (!idKey.equals(key)) {
				sql.append(" And ");
				sql.append(key);
				sql.append(" = ?");
			}
		});
		sql.append(" And ");
		sql.append(idKey);
		sql.append(" = ?");
		return sql.toString();
	}

}
