package me.mario.altchecker.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import lombok.Setter;

public class Database {

	@Setter
	private DatabaseInformation info;
	private static Database instance = new Database();

	private Connection connection;

	private Database() {
	}

	public static Database get() {
		return instance;
	}

	/**
	 * @return A new opened connection
	 */
	public Connection getConnection() {
		try {

			if (connection != null && !connection.isClosed())
				return connection;

			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + info.getHost() + ":" + info.getPort() + "/" + info.getDatabase()
					+ "?user=" + info.getUsername() + "&password=" + info.getPassword());

			return connection;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Close current SQL connection
	 */
	public void closeConnection() {
		try {
			if(connection != null && !connection.isClosed())
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * For executing queries that won't return anything (insert, update, etc.)
	 * @param query Query to be executed
	 */
	public void execute(String query) {
		try {
			getConnection();
			PreparedStatement statement = connection.prepareStatement(query);
			statement.executeUpdate();
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			closeConnection();
		}
	}
	
	/**
	 * Query the DB for specific information
	 * @param query The query
	 * @param values The values (mainly to prevent SQL injection)
	 * @return The result
	 */
	public ResultSet search(String query, Object... values) {
		ResultSet toReturn = null;
		
		try {
			getConnection();
			
			PreparedStatement statement = connection.prepareStatement(query);
			
			for(int i = 0; i < values.length; i++)
				statement.setObject(i+1, values[i]);
			
			toReturn = statement.executeQuery();
		} catch(Exception e) {
			e.printStackTrace();
			closeConnection();
		}
		
		return toReturn;
	}
	
	/**
	 * Insert new player in DB
	 * @param uuid
	 * @param name
	 */
	public void insertNewPlayer(UUID uuid, String name) {
		execute("INSERT INTO `" + info.getDatabase() + "`.`player` (`uuid`, `name`) VALUES ('" + uuid.toString() + "', '" + name + "');");
	}
	
	/**
	 * Insert new IP record into DB
	 * @param playerId Player ID
	 * @param ip IP of player
	 */
	public void insertNewIpRecord(int playerId, String ip) {
		execute("INSERT INTO `player_ip` (`player_id`, `ip`, `count`, `last_join`) VALUES (" + playerId + ", '" + ip + "', 1, '" + Timestamp.from(Instant.now()) + "');");
	}
	
	/**
	 * Get players' ID
	 * @param uuid UUID of player
	 * @return Unique ID for player
	 */
	public Integer getPlayerId(UUID uuid) {
		Integer id = null;
		try {
			ResultSet set = search("select id from player where uuid=?", uuid.toString());
			if(set.next())
				id = set.getInt(1);
			
			set.close();
		}catch(Exception e) {
			e.printStackTrace();
			return id;
		}
		
		return id;
	}
	
	/**
	 * Gets the amount of times a player has logged onto a specific IP
	 * @param playerId ID of player
	 * @param ip IP player is using
	 * @return # of times player has connected w/ that IP
	 */
	public Integer getLoginCount(int playerId, String ip) {
		Integer count = null;
		try {
			ResultSet set = search("select count from player_ip where ip=? and player_id=?", ip, playerId);
			if(set.next())
				count = set.getInt(1);
			
			set.close();
		}catch(Exception e) {
			e.printStackTrace();
			return count;
		}
		
		return count;
	}
	
	/**
	 * Increment amount of times player has connected to an IP
	 * @param playerId ID of player
	 * @param IP IP of player
	 * @param newCount New count that they've logged onto
	 */
	public void incrementLoginCount(int playerId, String ip, int newCount) {
		execute("update player_ip set count=" + newCount + ", last_join='" + Timestamp.from(Instant.now()) + "' where player_id=" + playerId + " and ip='" + ip + "'");
	}
	
	/**
	 * Check if a player has connected w/ a specific IP
	 * @param playerId ID of player
	 * @param ip IP that player is connecting w/
	 * @return Whether or not player has connected w/ that IP
	 */
	public boolean ipExists(int playerId, String ip) {
		boolean b = false;
		
		try {
			ResultSet set = search("select id from player_ip where player_id=? and ip=?", playerId, ip);
			b = set.next();
			
			set.close();
		}catch(Exception e) {
			e.printStackTrace();
			return b;
		}
		
		return b;
	}

}
