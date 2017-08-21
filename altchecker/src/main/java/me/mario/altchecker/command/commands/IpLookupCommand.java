package me.mario.altchecker.command.commands;

import java.text.DateFormat;
import java.util.Set;

import org.bukkit.command.CommandSender;

import me.mario.altchecker.command.AltCommand;
import me.mario.altchecker.util.alts.PlayerIPInformation;
import me.mario.altchecker.util.alts.PlayerInformation;
import me.mario.altchecker.util.database.Database;
import mkremins.fanciful.FancyMessage;
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
		
		Set<PlayerInformation> players = Database.get().getPlayersUsingIp(ip);
		
		if(players.isEmpty()) {
			sender.sendMessage(ChatColor.DARK_GRAY + "None");
			return;
		}
		
		int i = 0;
		FancyMessage fm = new FancyMessage("");
		
		for(PlayerInformation pi : players) {
			i++;
			PlayerIPInformation ipInfo = pi.getIpInfo().iterator().next();
			
			fm.then(ChatColor.GREEN + pi.getName()).tooltip(ChatColor.GOLD + "First Join: " + ChatColor.YELLOW + DateFormat.getInstance().format(ipInfo.getFirstJoin()), ChatColor.GOLD + "Last Join: " + ChatColor.YELLOW + DateFormat.getInstance().format(ipInfo.getLastJoin()), ChatColor.GOLD + "Count: " + ChatColor.YELLOW + ipInfo.getCount());
			if(i <= players.size() - 1)
				fm.then(ChatColor.RESET + ", ");
		}
		
		fm.send(sender);
	}

}
