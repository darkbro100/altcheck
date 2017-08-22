package me.mario.altchecker.util.database;

import java.sql.ResultSet;

/**
 * Simple consumer for handling data from ResultSets
 * @author Paul
 * @param <T> Generic type for what you want to return
 */
public interface ResultSetConsumer<T> {

	public T accept(ResultSet set);
	
}
