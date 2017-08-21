package me.mario.altchecker.command;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.reflections.Reflections;

import net.md_5.bungee.api.ChatColor;

public class AltCommandManager implements CommandExecutor {

	private static final Set<AltCommand> subCommands;
	
	static {
			subCommands = new Reflections("me.mario.altchecker.command.commands").getSubTypesOf(AltCommand.class).stream().map(c -> {
				try {
					return c.newInstance();
				}catch(Exception e) {
					System.out.println("Could not setup command: " + c);
					return null;
				}
			}).collect(Collectors.toSet());
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0) {
			sendHelp(sender, label);
			return true;
		}
		
		AltCommand found = subCommands.stream().filter(s -> s.name().equalsIgnoreCase(args[0]) && sender.hasPermission(s.permission())).findFirst().orElse(null);
		if(found == null)
			sendHelp(sender, label);
		else
			found.run(sender, Arrays.copyOfRange(args, 1, args.length));
		
		return true;
	}
	
	private Set<AltCommand> getAvailableCommands(CommandSender sender) {
		return subCommands.stream().filter(c -> sender.hasPermission(c.permission())).collect(Collectors.toSet());
	}
	
	private void sendHelp(CommandSender sender, String label) {
		Set<AltCommand> availableCommands = getAvailableCommands(sender);
		if(availableCommands.size() > 0) {
			sender.sendMessage(ChatColor.RED + "Sub command not found! List of available commands:");
			for(AltCommand ac : availableCommands) 
				sender.sendMessage(ChatColor.AQUA + "/" + label + " " + ac.name());
		}
	}
}
