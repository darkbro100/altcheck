package me.mario.altchecker.util.alts;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class PlayerIPInformation {

	private String ip;
	private int count;
	private Timestamp firstJoin;
	private Timestamp lastJoin;
	private int id;
	private int playerId;
	
}
