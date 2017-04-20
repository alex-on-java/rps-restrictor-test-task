package ru.buyanov.interview;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static ru.buyanov.interview.CurrentTimeProvider.NANO_SECONDS_IN_SECOND;

/**
 * @author A.Buyanov 20.04.2017.
 */
public class RpsRestrictor {
    private final CurrentTimeProvider timeProvider;
    private final Queue<Long> queue = new ConcurrentLinkedQueue<>();


    public RpsRestrictor(CurrentTimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Check if there are less requests per second for now
     * @param maxRps number of maximum requests per second
     * @return {@code true} if there are less RPS then {@code maxRps}, else
     *         {@code false}
     */
    public boolean allowed(int maxRps) {
        long current = timeProvider.nanoTime();
        if (queue.size() < maxRps) {
            queue.add(current);
            return true;
        }
        Long peek = Optional.ofNullable(queue.peek()).orElse(0L);
        if (peek + NANO_SECONDS_IN_SECOND < current) {
            queue.add(current);
            queue.poll();
            return true;
        }
        return false;
    }
}