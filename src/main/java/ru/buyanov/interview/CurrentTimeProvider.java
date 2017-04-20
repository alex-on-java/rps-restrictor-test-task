package ru.buyanov.interview;

/**
 * @author A.Buyanov 20.04.2017.
 */
public interface CurrentTimeProvider {
    long NANO_SECONDS_IN_SECOND = 1_000_000_000;

    default long nanoTime() {
        return System.nanoTime();
    }
}