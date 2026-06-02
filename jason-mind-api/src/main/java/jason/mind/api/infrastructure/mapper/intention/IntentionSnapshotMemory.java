package jason.mind.api.infrastructure.mapper.intention;

import jason.asSemantics.IntendedMeans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class IntentionSnapshotMemory {

    private static final IntentionSnapshotMemory INSTANCE = new IntentionSnapshotMemory();

    private final ConcurrentMap<String, ConcurrentMap<Integer, List<IntendedMeans>>> cache = new ConcurrentHashMap<>();

    private IntentionSnapshotMemory() {}

    public static IntentionSnapshotMemory getInstance() {
        return INSTANCE;
    }

    public void remember(String agentName, int intentionId, List<IntendedMeans> intendedMeans) {
        cache.computeIfAbsent(agentName, ignored -> new ConcurrentHashMap<>())
                .put(intentionId, List.copyOf(new ArrayList<>(intendedMeans)));
    }

    public List<IntendedMeans> recall(String agentName, int intentionId) {
        Map<Integer, List<IntendedMeans>> byIntention = cache.get(agentName);
        if (byIntention == null) {
            return List.of();
        }
        return byIntention.getOrDefault(intentionId, List.of());
    }

    public void clearAgent(String agentName) {
        cache.remove(agentName);
    }
}
