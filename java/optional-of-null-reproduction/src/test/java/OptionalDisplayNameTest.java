import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OptionalDisplayNameTest {
    @Test
    void nullWithOfFailsBeforeTheFallbackRuns() {
        String name = null;
        AtomicInteger fallbackCalls = new AtomicInteger();

        var failure = assertThrows(NullPointerException.class,
                () -> OptionalDisplayName.usingOf(name, () -> {
                    fallbackCalls.incrementAndGet();
                    return "guest";
                }));

        assertEquals(0, fallbackCalls.get());
        System.out.printf("of input=null result=%s fallbackCalls=%d%n",
                failure.getClass().getSimpleName(), fallbackCalls.get());
    }

    @Test
    void theSameNullWithOfNullableRunsTheFallbackOnce() {
        String name = null;
        AtomicInteger fallbackCalls = new AtomicInteger();

        String result = OptionalDisplayName.usingOfNullable(name, () -> {
            fallbackCalls.incrementAndGet();
            return "guest";
        });

        assertEquals("guest", result);
        assertEquals(1, fallbackCalls.get());
        System.out.printf("ofNullable input=null result=%s fallbackCalls=%d%n",
                result, fallbackCalls.get());
    }

    @Test
    void aPresentNameWithOfDoesNotRunTheFallback() {
        String name = "佐藤";
        AtomicInteger fallbackCalls = new AtomicInteger();

        String result = OptionalDisplayName.usingOf(name, () -> {
            fallbackCalls.incrementAndGet();
            return "guest";
        });

        assertEquals("佐藤", result);
        assertEquals(0, fallbackCalls.get());
        System.out.printf("of input=佐藤 result=%s fallbackCalls=%d%n",
                result, fallbackCalls.get());
    }

    @Test
    void theSamePresentNameWithOfNullableDoesNotRunTheFallback() {
        String name = "佐藤";
        AtomicInteger fallbackCalls = new AtomicInteger();

        String result = OptionalDisplayName.usingOfNullable(name, () -> {
            fallbackCalls.incrementAndGet();
            return "guest";
        });

        assertEquals("佐藤", result);
        assertEquals(0, fallbackCalls.get());
        System.out.printf("ofNullable input=佐藤 result=%s fallbackCalls=%d%n",
                result, fallbackCalls.get());
    }
}
