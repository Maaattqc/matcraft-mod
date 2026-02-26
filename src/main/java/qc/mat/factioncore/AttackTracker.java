package qc.mat.factioncore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AttackTracker {
	private static final long MIN_ATTACK_INTERVAL_MS = 100;
	private static final Map<UUID, Long> lastAttackTime = new ConcurrentHashMap<>();

	public static boolean tryAttack(UUID uuid) {
		long now = System.currentTimeMillis();
		Long last = lastAttackTime.get(uuid);
		if (last != null && (now - last) < MIN_ATTACK_INTERVAL_MS) {
			return false;
		}
		lastAttackTime.put(uuid, now);
		return true;
	}

	public static void removePlayer(UUID uuid) {
		lastAttackTime.remove(uuid);
	}
}
