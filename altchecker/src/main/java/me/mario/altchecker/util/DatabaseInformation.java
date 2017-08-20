package me.mario.altchecker.util;

import org.bukkit.configuration.file.FileConfiguration;

import lombok.Getter;

@Getter
public class DatabaseInformation {

	private String host,username,password,database;
	private int port;
	
	public DatabaseInformation(FileConfiguration config) {
		this.host = config.getString("mysql-info.host");
		this.username = config.getString("mysql-info.user");
		this.password = config.getString("mysql-info.password");
		this.database = config.getString("mysql-info.database");
		this.port = config.getInt("mysql-info.port");
	}
	
}
