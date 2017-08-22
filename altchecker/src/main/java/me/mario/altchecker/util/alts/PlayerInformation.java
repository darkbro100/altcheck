package me.mario.altchecker.util.alts;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class PlayerInformation {

	private String name;
	private UUID uuid;
	private Timestamp firstJoin;
	private Set<PlayerIPInformation> ipInfo;
	private int id;
	
}
