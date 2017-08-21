package me.mario.altchecker.command.commands;

import org.bukkit.command.CommandSender;

import me.mario.altchecker.command.AltCommand;
import me.mario.altchecker.util.Util;
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
		return "/aip alts <name|ip>";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + getHelp());
			return;
		}
		
		String str = args[0];
		
		if(Util.isValidIPV4(str))
			sender.sendMessage(ChatColor.YELLOW + str + " is a valid IP");
		else
			sender.sendMessage("using str as name/uuid instead");
	}

	
	
}
