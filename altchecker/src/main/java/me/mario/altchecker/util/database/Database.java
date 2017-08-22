package me.mario.altchecker.util.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Setter;
import me.mario.altchecker.AltChecker;
import me.mario.altchecker.util.alts.PlayerIPInformation;
import me.mario.altchecker.util.alts.PlayerInformation;
import me.mario.altchecker.util.alts.PlayerInformation.PlayerInformationBuilder;

public class Database {

	@Setter
	private HikariDataSource dataSource;
	private static Database instance = new Database();

	private Connection currentConnection;

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
			this.currentConnection = dataSource.getConnection();
			return currentConnection;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Close our current connection
	 */
	public void closeCurrentConnection() {
		try {
			this.currentConnection.close();
			this.currentConnection = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close the instance of {@link HikariDataSource}
	 */
	public void shutdown() {
		dataSource.close();
	}

	/**
	 * For executing queries that won't return anything (insert, update, etc.)
	 * 
	 * @param query
	 *            Query to be executed
	 */
	public void execute(String query) {
		try {
			PreparedStatement statement = getConnection().prepareStatement(query);
			statement.executeUpdate();

			closeCurrentConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Query the DB for specific information
	 * 
	 * @param query
	 *            The query
	 * @param values
	 *            The values (mainly to prevent SQL injection)
	 * @return The result
	 */
	public <T> HashSet<T> search(String query, ResultSetConsumer<T> consumer, Object... values) {
		HashSet<T> data = new HashSet<>();
		try {
			Connection c = getConnection();
			PreparedStatement statement = c.prepareStatement(query);
			for (int i = 0; i < values.length; i++)
				statement.setObject(i + 1, values[i]);

			ResultSet set = statement.executeQuery();

			while (set.next())
				data.add(consumer.accept(set));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeCurrentConnection();
		}

		return data;
	}

	/**
	 * Insert new player in DB
	 * 
	 * @param uuid
	 * @param name
	 */
	public void insertNewPlayer(UUID uuid, String name) {
		execute("INSERT INTO `player` (`uuid`, `name`) VALUES ('" + uuid.toString() + "', '" + name + "');");
	}

	/**
	 * Insert new IP record into DB
	 * 
	 * @param playerId
	 *            Player ID
	 * @param ip
	 *            IP of player
	 */
	public void insertNewIpRecord(int playerId, String ip) {
		execute("INSERT INTO `player_ip` (`player_id`, `ip`, `count`, `last_join`) VALUES (" + playerId + ", '" + ip
				+ "', 1, '" + Timestamp.from(Instant.now()) + "');");
	}

	/**
	 * Get players' ID
	 * 
	 * @param uuid
	 *            UUID of player
	 * @return Unique ID for player
	 */
	public Integer getPlayerId(UUID uuid) {
		if (AltChecker.getInstance().getCachedPlayerIds().containsKey(uuid))
			return AltChecker.getInstance().getCachedPlayerIds().get(uuid);

		try {
			HashSet<Integer> ids = search("select id from player where uuid=?", (set) -> {
				try {
					return set.getInt(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			} , uuid.toString());

			if(ids.iterator().hasNext())
				return ids.iterator().next();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Update a players name in the database. Do this whenever they login
	 * 
	 * @param playerId
	 *            Player's unique ID
	 * @param name
	 *            The name to be updated
	 */
	public void updatePlayerName(int playerId, String name) {
		execute("update player set name='" + name + "' where id=" + playerId);
	}

	/**
	 * Get player's UUID from their name
	 * 
	 * @param name
	 *            Player's name
	 * @return Their UUID
	 */
	public UUID getUuid(String name) {
		UUID uuid = search("select uuid from player where name=?", (set) -> {
			try {
				return UUID.fromString(set.getString(1));
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		} , name).iterator().next();

		return uuid;
	}

	/**
	 * Gets the amount of times a player has logged onto a specific IP
	 * 
	 * @param playerId
	 *            ID of player
	 * @param ip
	 *            IP player is using
	 * @return # of times player has connected w/ that IP
	 */
	public Integer getLoginCount(int playerId, String ip) {
		Integer count = search("select count from player_ip where ip=? and player_id=?", (set) -> {
			try {
				return set.getInt(1);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		} , ip, playerId).iterator().next();

		return count;
	}

	/**
	 * Retrieve all Player information
	 * 
	 * @param playerId
	 *            ID of player
	 * @return Set containing all of their info
	 */
	public PlayerInformation getLoggedIps(int playerId) {
		HashSet<PlayerIPInformation> ipInfo = search("select * from player_ip where player_id=?", (set) -> {
			try {
				return buildIpObject(set);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		} , playerId);

		PlayerInformationBuilder info = search("select * from player where id=?", (set) -> {
			try {
				PlayerInformationBuilder builder = PlayerInformation.builder();
				builder.uuid(UUID.fromString(set.getString(2)));
				builder.name(set.getString(3));
				builder.firstJoin(set.getTimestamp(4));

				return builder;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		} , playerId).iterator().next();

		info.ipInfo(ipInfo);

		return info.build();
	}

	private PlayerIPInformation buildIpObject(ResultSet ips) {
		try {
			return PlayerIPInformation.builder().ip(ips.getString(3)).count(ips.getInt(4))
					.firstJoin(ips.getTimestamp(5)).lastJoin(ips.getTimestamp(6)).id(ips.getInt(1)).build();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Get the players who have used the same IP as the one specified
	 * 
	 * @param ip
	 *            IP to search for
	 * @return List of players who have logged into the IP
	 */
	public Set<PlayerInformation> getPlayersUsingIp(String ip) {
		Set<PlayerInformation> players = new HashSet<>();

		HashSet<PlayerIPInformation> info = search("select * from player_ip where ip=?", (set) -> {
			return buildIpObject(set);
		} , ip);

		for (PlayerIPInformation ipInfo : info) {
			HashSet<PlayerInformation> found = search("select * from player where id=?", (playerInfo) -> {
				try {
					return PlayerInformation.builder().firstJoin(playerInfo.getTimestamp(4))
							.uuid(UUID.fromString(playerInfo.getString(2))).name(playerInfo.getString(3))
							.ipInfo(Sets.newHashSet(info)).build();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			} , ipInfo.getId());

			players.addAll(found);
		}

		return players;
	}

	/**
	 * Increment amount of times player has connected to an IP
	 * 
	 * @param playerId
	 *            ID of player
	 * @param IP
	 *            IP of player
	 * @param newCount
	 *            New count that they've logged onto
	 */
	public void incrementLoginCount(int playerId, String ip, int newCount) {
		execute("update player_ip set count=" + newCount + ", last_join='" + Timestamp.from(Instant.now())
				+ "' where player_id=" + playerId + " and ip='" + ip + "'");
	}

	/**
	 * Check if a player has connected w/ a specific IP
	 * 
	 * @param playerId
	 *            ID of player
	 * @param ip
	 *            IP that player is connecting w/
	 * @return Whether or not player has connected w/ that IP
	 */
	public boolean ipExists(int playerId, String ip) {
		try {
			HashSet<Boolean> ids = search("select id from player_ip where player_id=? and ip=?", (set) -> { return true; } , playerId, ip);
			return ids.size() > 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
