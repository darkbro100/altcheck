package me.mario.altchecker.command.commands;

import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import me.mario.altchecker.command.AltCommand;
import me.mario.altchecker.util.database.Database;
import net.md_5.bungee.api.ChatColor;

public class IpLookupCommand extends AltCommand {

	@Override
	public String name() {
		return "iplookup";
	}

	@Override
	public String[] aliases() {
		return new String[] { "ipl", "lookup" };
	}

	@Override
	public String permission() {
		return "aip.iplookup";
	}

	@Override
	public String description() {
		return "An IP based lookup only. Finds all accounts that have used an IP";
	}

	@Override
	public String syntax() {
		return "/aip iplookup <ip>";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + getHelp());
			return;
		}

		String ip = args[0];

		sender.sendMessage(ChatColor.GOLD + "Finding players using IP: " + ChatColor.YELLOW + ip);
		sender.sendMessage(StringUtils.join(Database.get().getPlayersUsingIp(ip).stream()
				.map(pi -> pi.getName() + " [" + pi.getIpInfo().iterator().next().getCount() + "]")
				.collect(Collectors.toSet()), ", "));
	}

}
