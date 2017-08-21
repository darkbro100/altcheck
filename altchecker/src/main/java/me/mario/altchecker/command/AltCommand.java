package me.mario.altchecker.command;

import org.bukkit.command.CommandSender;

/**
 * Simple interface for commands
 * @author Paul
 */
public abstract class AltCommand {

	public abstract String name();
	public abstract String[] aliases();
	public abstract String permission();
	public abstract String description();
	public abstract String syntax();
	public abstract void run(CommandSender sender, String[] args);
	
	public String getHelp() {
		return syntax() + " - " + description();
	}
	
}
