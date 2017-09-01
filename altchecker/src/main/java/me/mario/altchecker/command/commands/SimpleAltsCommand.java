package me.mario.altchecker.command.commands;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import me.mario.altchecker.command.AltCommand;
import me.mario.altchecker.util.Util;
import me.mario.altchecker.util.alts.PlayerIPInformation;
import me.mario.altchecker.util.alts.PlayerInformation;
import me.mario.altchecker.util.database.Database;
import net.md_5.bungee.api.ChatColor;

public class SimpleAltsCommand extends AltCommand {

	@Override
	public String name() {
		return "salts";
	}

	@Override
	public String[] aliases() {
		return new String[] { };
	}

	@Override
	public String permission() {
		return "aip.simplealts";
	}

	@Override
	public String description() {
		return "Search for alts that are connected to a player [doesn't display IPs]";
	}

	@Override
	public String syntax() {
		return "/aip salts <name>";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + getHelp());
			return;
		}
			
		UUID uuid = Util.tryElse(() -> {
			return UUID.fromString(args[0]);	
		}, null, false);
		
		if(uuid == null)
			uuid = Util.tryElse(() -> {
				return Database.get().getUuid(args[0]);
			}, null, false);
		
		if(uuid == null) {
			sender.sendMessage(ChatColor.RED + args[0] + " doesn't exist or has never joined!");
			return;
		}
		
		sender.sendMessage(ChatColor.GOLD + "Loading alts for " + ChatColor.YELLOW + args[0] + ChatColor.GOLD + ". Please wait");
		
		int id = Database.get().getPlayerId(uuid);
		PlayerInformation pInfo = Database.get().getLoggedIps(id);
		Set<PlayerIPInformation> ips = pInfo.getIpInfo();
		Set<String> players = new HashSet<>();
		
		for(PlayerIPInformation info : ips) {
			for(PlayerInformation pi : Database.get().getPlayersUsingIp(info.getIp()))
				players.add(ChatColor.GREEN + pi.getName());
		}
		
		sender.sendMessage("  " + StringUtils.join(players, ChatColor.DARK_RED + ", "));
	}

	
	
}
