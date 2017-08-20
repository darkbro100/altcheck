package me.mario.altchecker.command;

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
			Set<AltCommand> availableCommands = getAvailableCommands(sender);
			if(availableCommands.size() > 0) {
				sender.sendMessage(ChatColor.RED + "Sub command not found! List of available commands:");
				for(AltCommand ac : availableCommands) 
					sender.sendMessage(ChatColor.AQUA + "/" + label + " " + ac.name());
				
				return true;
			}
		}
		
		return true;
	}
	
	private Set<AltCommand> getAvailableCommands(CommandSender sender) {
		return subCommands.stream().filter(c -> sender.hasPermission(c.permission())).collect(Collectors.toSet());
	}
}
