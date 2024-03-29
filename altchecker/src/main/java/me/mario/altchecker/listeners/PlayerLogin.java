package me.mario.altchecker.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import me.mario.altchecker.AltChecker;
import me.mario.altchecker.util.StaticListener;
import me.mario.altchecker.util.database.Database;

@StaticListener
public class PlayerLogin implements Listener {

	@EventHandler
	public void onLogin(AsyncPlayerPreLoginEvent event) {
		String ip = event.getAddress().getHostAddress();
		UUID uuid = event.getUniqueId();
		String name = event.getName();

		Integer id = null;

		if (AltChecker.getInstance().getCachedPlayerIds().containsKey(uuid))
			id = AltChecker.getInstance().getCachedPlayerIds().get(uuid);
		else
			id = Database.get().getPlayerId(uuid);

		if (id == null) {
			Database.get().insertNewPlayer(uuid, name);
			id = Database.get().getPlayerId(uuid);
		}
		
		Database.get().updatePlayerName(id, name); //Update player's name in DB
		AltChecker.getInstance().getCachedPlayerIds().put(uuid, id); //Cache their ID in case for further use

		if (!Database.get().ipExists(id, ip))
			Database.get().insertNewIpRecord(id, ip); //insert new IP record if they've never connected w/ this ip 
		else {
			int newCount = Database.get().getLoginCount(id, ip) + 1;
			Database.get().incrementLoginCount(id, ip, newCount); //increment their login count + update last login date
		}
	}

}
