package ru.buyanov.interview;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.buyanov.interview.CurrentTimeProvider.NANO_SECONDS_IN_SECOND;

/**
 * @author A.Buyanov 20.04.2017.
 */
public class RpsRestrictor {
    public static final int MAXIMUM_FOR_ALLOWED_RPS = 100_000;
    private final CurrentTimeProvider timeProvider;
    private final Queue<Long> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger lastIndex = new AtomicInteger();
    private final long[] cyclicBuffer = new long[MAXIMUM_FOR_ALLOWED_RPS];


    public RpsRestrictor(CurrentTimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Check if there are less requests per second for now
     * Implementation is not thread safe
     * @param maxRps number of maximum requests per second
     * @return {@code true} if there are less RPS then {@code maxRps}, else
     *         {@code false}
     */
    public boolean allowed(int maxRps) {
        if (maxRps == 0)
            return false;
        maxRps = maxRps <= MAXIMUM_FOR_ALLOWED_RPS ? maxRps : MAXIMUM_FOR_ALLOWED_RPS;
        long current = timeProvider.nanoTime();
        int diff = lastIndex.get() - maxRps + 1;
        int oldestIndex = diff > 0 ? diff : diff + cyclicBuffer.length - 1;
        if (current > cyclicBuffer[oldestIndex] + NANO_SECONDS_IN_SECOND) {
            cyclicBuffer[lastIndex.incrementAndGet()] = current;
            return true;
        }
        return false;
    }
}