package me.mario.altchecker.command.commands;

import java.util.Set;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import me.mario.altchecker.command.AltCommand;
import me.mario.altchecker.util.Util;
import me.mario.altchecker.util.alts.PlayerIPInformation;
import me.mario.altchecker.util.alts.PlayerInformation;
import me.mario.altchecker.util.database.Database;
import net.md_5.bungee.api.ChatColor;

public class AltsCommand extends AltCommand {

	@Override
	public String name() {
		return "alts";
	}

	@Override
	public String[] aliases() {
		return new String[] { };
	}

	@Override
	public String permission() {
		return "aip.alts";
	}

	@Override
	public String description() {
		return "Search for alts that are connected to a player or an IP";
	}

	@Override
	public String syntax() {
		return "/aip alts <name>";
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
			uuid = Database.get().getUuid(args[0]);
		
		if(uuid == null) {
			sender.sendMessage(ChatColor.RED + args[0] + " doesn't exist or has never joined!");
			return;
		}
		
		sender.sendMessage(ChatColor.GOLD + "Loading alts for " + ChatColor.YELLOW + args[0] + ChatColor.GOLD + ". Please wait");
		
		int id = Database.get().getPlayerId(uuid);
		Set<PlayerIPInformation> ips = Database.get().getLoggedIps(id).getIpInfo();
		
		for(PlayerIPInformation info : ips) {
			sender.sendMessage("IP: " + info.getIp());
			for(PlayerInformation pi : Database.get().getPlayersUsingIp(info.getIp())) {
				sender.sendMessage(pi.getName() + " [" + pi.getIpInfo().iterator().next().getCount() + "]");
			}
		}
	}

	
	
}
