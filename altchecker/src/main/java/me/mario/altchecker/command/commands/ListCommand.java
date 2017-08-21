package me.mario.altchecker.command.commands;

import java.text.DateFormat;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import me.mario.altchecker.command.AltCommand;
import me.mario.altchecker.util.Util;
import me.mario.altchecker.util.alts.PlayerIPInformation;
import me.mario.altchecker.util.alts.PlayerInformation;
import me.mario.altchecker.util.database.Database;
import net.md_5.bungee.api.ChatColor;

public class ListCommand extends AltCommand {

	@Override
	public String name() {
		return "info";
	}

	@Override
	public String[] aliases() {
		return new String[] { "i" };
	}

	@Override
	public String permission() {
		return "aip.list";
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
		
		sender.sendMessage(ChatColor.GOLD + "Loading " + args[0] + "'s information. Please wait");
		
		int id = Database.get().getPlayerId(uuid);
		PlayerInformation info = Database.get().getLoggedIps(id);
		
		sender.sendMessage(ChatColor.DARK_RED + info.getName() + "'s information (" + info.getUuid().toString() + ")");
		sender.sendMessage(ChatColor.YELLOW + "First join date: " + ChatColor.GOLD + DateFormat.getInstance().format(info.getFirstJoin()));
		
		for(PlayerIPInformation ipInfo : info.getIpInfo()) {
			sender.sendMessage("");
			sender.sendMessage("IP: " + ipInfo.getIp());
			sender.sendMessage("Login Count: " + ipInfo.getCount());
			sender.sendMessage("First Join: " + DateFormat.getInstance().format(ipInfo.getFirstJoin()));
			sender.sendMessage("Last Join: " + DateFormat.getInstance().format(ipInfo.getLastJoin()));
		}
	}

	@Override
	public String description() {
		return "View player information (join dates, IPs, etc.)";
	}

	@Override
	public String syntax() {
		return "/aip info <player>";
	}

}
