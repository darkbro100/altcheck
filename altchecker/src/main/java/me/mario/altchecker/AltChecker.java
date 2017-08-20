package me.mario.altchecker;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import lombok.Getter;
import me.mario.altchecker.util.Database;
import me.mario.altchecker.util.DatabaseInformation;
import me.mario.altchecker.util.StaticListener;

@Getter
public class AltChecker extends JavaPlugin {

	/**
	 * https://pastebin.com/mnxjyY8r (table queries)
	 */

	@Getter
	private static AltChecker instance;

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();

		Database.get().setInfo(new DatabaseInformation(getConfig()));

		try {
			createPlayerTable();
			createPlayerIpTable();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not setup DB information! Disabling plugin");
			Bukkit.getPluginManager().disablePlugin(this);
		}
		
		registerListeners();
	}
	
	@Override
	public void onDisable() {
		Database.get().closeConnection();
		instance = null;
	}
	
	private void registerListeners() {
		Reflections reflections = new Reflections("me.mario.altchecker");    
		PluginManager pm = getServer().getPluginManager();
		
		Set<Class<?>> clazzes = reflections.getTypesAnnotatedWith(StaticListener.class);
		for(Class<?> clazz : clazzes) {
			try {
				pm.registerEvents((Listener) clazz.newInstance(), this);
				System.out.println("Registered StaticListener " + clazz);
			} catch (Exception e) {
				System.err.println("Failed to create StaticListener for " + clazz);
				e.printStackTrace();
			}
		}
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
