package me.mario.altchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Getter;
import me.mario.altchecker.command.AltCommandManager;
import me.mario.altchecker.util.StaticListener;
import me.mario.altchecker.util.database.Database;

@Getter
public class AltChecker extends JavaPlugin {

	@Getter
	private static AltChecker instance;

	private Map<UUID, Integer> cachedPlayerIds;
	
	private File hikariFile;
	
	@Override
	public void onEnable() {
		instance = this;
		cachedPlayerIds = new HashMap<>();
		hikariFile = new File(getDataFolder(), "hikari.properties");
		
		if(!hikariFile.exists())
			createPropertiesFile();
		
		//Establish HikariCP. Make sure everything runs fine
		HikariDataSource dataSource = loadPropertiesFile();
		try {
			dataSource.getConnection();
			Bukkit.getLogger().info("Connected to database!");
		} catch (SQLException e1) {
			//If connection cannot be established, shut down plugin as it becomes obsolete
			e1.printStackTrace();
			Bukkit.getLogger().info("Couldn't establish database connection! Shutting down plugin!");
			getServer().getPluginManager().disablePlugin(this);
		}
		
		// Update Data Source in our Database class
		Database.get().setDataSource(dataSource);
		
		try {
			createPlayerTable();
			createPlayerIpTable();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not setup DB information! Disabling plugin");
			Bukkit.getPluginManager().disablePlugin(this);
		}
		
		registerListeners();
		registerCommands();
		
		Bukkit.getScheduler().runTaskTimer(this, () -> {
			cachedPlayerIds.clear();
			Bukkit.getLogger().info("Cleared cached player counts");
		}, 20L * 300L, 20L * 300L);
	}
	
	@Override
	public void onDisable() {
		Database.get().shutdown();
		instance = null;
	}
	
	private void registerListeners() {
		Reflections reflections = new Reflections("me.mario.altchecker");    
		PluginManager pm = getServer().getPluginManager();
		
		Set<Class<?>> clazzes = reflections.getTypesAnnotatedWith(StaticListener.class);
		for(Class<?> clazz : clazzes) {
			try {
				pm.registerEvents((Listener) clazz.newInstance(), this);
				Bukkit.getLogger().info("Registered StaticListener " + clazz);
			} catch (Exception e) {
				System.err.println("Failed to create StaticListener for " + clazz);
				e.printStackTrace();
			}
		}
	}
	
	private void createPropertiesFile() {
		this.hikariFile = new File(getDataFolder(), "hikari.properties");
		try {
			hikariFile.createNewFile();
			
			Properties properties = new Properties();
			properties.setProperty("username", "user");
			properties.setProperty("password", "pass");
			properties.setProperty("jdbcUrl", "jdbc:mysql://127.0.0.1/mario");
			properties.setProperty("driverClassName", "com.mysql.jdbc.Driver");
			properties.store(new FileOutputStream(hikariFile), "HikariCP Database Information");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private HikariDataSource loadPropertiesFile() {
		try {
			Properties properties = new Properties();
			
			properties.load(new FileInputStream(hikariFile));
			HikariConfig hkC = new HikariConfig(properties);
			HikariDataSource ds = new HikariDataSource(hkC);
			
			return ds;
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void registerCommands() {
		getCommand("aip").setExecutor(new AltCommandManager());
	}

	private void createPlayerTable() {
		Database.get().execute(
				"CREATE TABLE IF NOT EXISTS `player` (   `id` INT NOT NULL AUTO_INCREMENT,  "
				+ "`uuid` VARCHAR(40) NOT NULL, "
				+ "`name` VARCHAR(16) NOT NULL, "
				+ "`join_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`),  UNIQUE INDEX `id` (`id`))");
	}

	private void createPlayerIpTable() {
		Database.get()
				.execute("CREATE TABLE IF NOT EXISTS `player_ip` ( `id` INT NOT NULL AUTO_INCREMENT, "
						+ "`player_id` INT NOT NULL, "
						+ "`ip` VARCHAR(100) NOT NULL," + " `count` INT NOT NULL, "
						+ "`first_join` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  "
						+ "`last_join` TIMESTAMP NOT NULL, PRIMARY KEY (`id`)," + "UNIQUE INDEX `id` (`id`))");
	}

}
