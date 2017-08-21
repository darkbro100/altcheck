package me.mario.altchecker.command.commands;

import java.text.DateFormat;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import me.mario.altchecker.command.AltCommand;
import me.mario.altchecker.util.Util;
import me.mario.altchecker.util.alts.PlayerIPInformation;
import me.mario.altchecker.util.alts.PlayerInformation;
import me.mario.altchecker.util.database.Database;
import mkremins.fanciful.FancyMessage;
import net.md_5.bungee.api.ChatColor;

public class InfoCommand extends AltCommand {

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
		return "aip.info";
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
		
		sender.sendMessage(ChatColor.GOLD + "Loading " + ChatColor.YELLOW + args[0] + ChatColor.GOLD + "'s information. Please wait");
		
		int id = Database.get().getPlayerId(uuid);
		PlayerInformation info = Database.get().getLoggedIps(id);
		
		sender.sendMessage(ChatColor.GRAY + "=== " + ChatColor.GREEN + info.getName() + ChatColor.GRAY + " ===");
		sender.sendMessage(ChatColor.YELLOW + "First join date: " + ChatColor.GOLD + DateFormat.getInstance().format(info.getFirstJoin()));
		sender.sendMessage(ChatColor.YELLOW + "Logged IPs " + ChatColor.GOLD + "(Hover for info)");
		
		for(PlayerIPInformation ipInfo : info.getIpInfo()) {
			FancyMessage fm = new FancyMessage("    - " + ChatColor.DARK_AQUA + ipInfo.getIp());
			fm.tooltip(ChatColor.YELLOW + "Login Count: " + ChatColor.GOLD + ipInfo.getCount(), ChatColor.YELLOW + "First Join: " + ChatColor.GOLD + DateFormat.getInstance().format(ipInfo.getFirstJoin()), ChatColor.YELLOW + "Last Join: " + ChatColor.GOLD + DateFormat.getInstance().format(ipInfo.getLastJoin()));
			fm.send(sender);
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
