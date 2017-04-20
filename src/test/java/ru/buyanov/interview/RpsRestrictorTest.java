package ru.buyanov.interview;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.stream.IntStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static ru.buyanov.interview.CurrentTimeProvider.NANO_SECONDS_IN_SECOND;

/**
 * @author A.Buyanov 20.04.2017.
 */
public class RpsRestrictorTest {
    private RpsRestrictor service;
    private CurrentTimeProvider timeProvider;
    private static final long INITIAL_TIME = 1000;
    public static final int MAX_RPS = 10;

    @Before
    public void init() {
        timeProvider = Mockito.mock(CurrentTimeProvider.class);
        when(timeProvider.nanoTime()).thenReturn(INITIAL_TIME);
        service = new RpsRestrictor(timeProvider);
    }

    @Test
    public void testAllowed_zeroRps() {
        assertFalse("Should be false for zero rps", service.allowed(0));
    }

    @Test
    public void testAllowed_overflowMax() {
        IntStream.rangeClosed(1, MAX_RPS).forEach(i -> service.allowed(MAX_RPS));
        assertFalse("Should restrict if there are more than MAX_RPS", service.allowed(MAX_RPS));
    }

    @Test
    public void testAllowed_overflowMaxAndCheckPlusOne() {
        IntStream.rangeClosed(1, MAX_RPS).forEach(i -> service.allowed(MAX_RPS));
        assertTrue("Should not restrict for MAX_RPS + 1 after MAX_RPS was reached", service.allowed(MAX_RPS + 1));
    }

    @Test
    public void testAllowed_callMaxThanWaitMoreThanSecond() throws InterruptedException {
        IntStream.rangeClosed(1, MAX_RPS).forEach(i -> service.allowed(MAX_RPS));
        when(timeProvider.nanoTime()).thenReturn(INITIAL_TIME + NANO_SECONDS_IN_SECOND + 1);
        IntStream.rangeClosed(1, MAX_RPS - 1).forEach(i -> service.allowed(MAX_RPS));
        assertTrue("Should allow maximum after a second", service.allowed(MAX_RPS));
    }

    @Test
    public void testAllowed_callMuchMoreTimesThenWaitMoreThanOneSecond() {
        IntStream.rangeClosed(1, MAX_RPS * 10).forEach(i -> service.allowed(MAX_RPS));
        when(timeProvider.nanoTime()).thenReturn(INITIAL_TIME + NANO_SECONDS_IN_SECOND + 1);
        IntStream.rangeClosed(1, MAX_RPS - 1).forEach(i -> service.allowed(MAX_RPS));
        assertTrue("Should allow maximum after a second even in case of massive calls before", service.allowed(MAX_RPS));
    }

    @Test
    public void testAllowed_callMaxThanWaitLessThanSecond() throws InterruptedException {
        IntStream.rangeClosed(1, MAX_RPS).forEach(i -> service.allowed(MAX_RPS));
        when(timeProvider.nanoTime()).thenReturn(INITIAL_TIME + NANO_SECONDS_IN_SECOND - 1);
        assertFalse("Should not allow after less than a second", service.allowed(MAX_RPS));
    }

    @Test
    public void testAllowed_peakAtTheEndOfFirstSecond() throws InterruptedException {
        service.allowed(MAX_RPS);
        when(timeProvider.nanoTime()).thenReturn(INITIAL_TIME + NANO_SECONDS_IN_SECOND - 1);
        IntStream.rangeClosed(2, MAX_RPS).forEach(i -> service.allowed(MAX_RPS));
        when(timeProvider.nanoTime()).thenReturn(INITIAL_TIME + NANO_SECONDS_IN_SECOND + 1);
        service.allowed(MAX_RPS);
        assertFalse("Should not allow at the beginning of the 2nd second", service.allowed(MAX_RPS));

    }
}