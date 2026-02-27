package qc.mat.matcraftmod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ServerBlockingTracker {
	private static final Set<UUID> blockingPlayers = new HashSet<>();

	public static void setBlocking(UUID playerId, boolean blocking) {
		if (blocking) {
			blockingPlayers.add(playerId);
		} else {
			blockingPlayers.remove(playerId);
		}
	}

	public static boolean isBlocking(UUID playerId) {
		return blockingPlayers.contains(playerId);
	}

	public static void remove(UUID playerId) {
		blockingPlayers.remove(playerId);
	}
}
