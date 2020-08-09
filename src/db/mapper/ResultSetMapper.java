package db.mapper;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetMapper<T> {

	public T map(ResultSet data);
	
}
