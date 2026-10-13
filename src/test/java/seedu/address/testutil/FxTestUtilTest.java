package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Verifies toolkit sharing, FX execution, synchronous completion and failure propagation.
 */
@TestMethodOrder(OrderAnnotation.class)
public class FxTestUtilTest {

    @Test
    @Order(1)
    public void runAndWait_concurrentCalls_shareToolkit() throws Exception {
        AtomicInteger completed = new AtomicInteger();
        AtomicReference<Thread> fxThread = new AtomicReference<>();
        CountDownLatch ready = new CountDownLatch(4);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            List<Future<?>> callers = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                callers.add(executor.submit(() -> {
                    ready.countDown();
                    assertTrue(start.await(10, TimeUnit.SECONDS));
                    FxTestUtil.runAndWait(() -> {
                        assertTrue(Platform.isFxApplicationThread());
                        fxThread.compareAndSet(null, Thread.currentThread());
                        assertSame(fxThread.get(), Thread.currentThread());
                        completed.incrementAndGet();
                    });
                    return null;
                }));
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            for (Future<?> caller : callers) {
                caller.get(45, TimeUnit.SECONDS);
            }
        }
        assertEquals(4, completed.get());
    }

    @Test
    public void runAndWait_nestedCall_executesInline() {
        FxTestUtil.runAndWait(() -> {
            Thread outerThread = Thread.currentThread();
            AtomicInteger completed = new AtomicInteger();
            FxTestUtil.runAndWait(() -> {
                assertSame(outerThread, Thread.currentThread());
                completed.incrementAndGet();
            });
            assertEquals(1, completed.get());
        });
    }

    @Test
    public void runAndWait_callbackFailures_propagatesOriginalAndRemainsUsable() {
        AssertionError assertion = new AssertionError("original assertion");
        assertSame(assertion, assertThrows(AssertionError.class, () -> FxTestUtil.runAndWait(() -> {
            throw assertion;
        })));
        IllegalArgumentException exception = new IllegalArgumentException("original exception");
        assertSame(exception, assertThrows(IllegalArgumentException.class, () -> FxTestUtil.runAndWait(() -> {
            throw exception;
        })));
        FxTestUtil.runAndWait(() -> assertTrue(Platform.isFxApplicationThread()));
    }

    @Test
    public void runAndWait_inlineFailure_propagatesOriginal() {
        FxTestUtil.runAndWait(() -> {
            AssertionError assertion = new AssertionError("inline assertion");
            assertSame(assertion, assertThrows(AssertionError.class, () -> FxTestUtil.runAndWait(() -> {
                throw assertion;
            })));
        });
    }

    @Test
    public void runAndWait_lastWindowClosed_toolkitRemainsUsable() {
        AtomicReference<Thread> fxThread = new AtomicReference<>();
        FxTestUtil.runAndWait(() -> {
            fxThread.set(Thread.currentThread());
            assertFalse(Platform.isImplicitExit());
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(new Label("Toolkit lifecycle test")));
                stage.show();
            } finally {
                stage.close();
            }
        });
        FxTestUtil.runAndWait(() -> assertSame(fxThread.get(), Thread.currentThread()));
    }

    @Test
    public void runAndWait_nullAction_rejectsNull() {
        assertThrows(NullPointerException.class, () -> FxTestUtil.runAndWait(null));
    }

    @Test
    public void runAndWait_interruptedCaller_preservesFlagAndCancelsQueuedAction() throws Exception {
        FxTestUtil.runAndWait(() -> assertTrue(Platform.isFxApplicationThread()));
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger executed = new AtomicInteger();
        FutureTask<Void> blocker = new FutureTask<>(() -> {
            entered.countDown();
            assertTrue(release.await(10, TimeUnit.SECONDS));
            return null;
        });
        Platform.runLater(blocker);
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            executor.submit(() -> {
                Thread.currentThread().interrupt();
                try {
                    assertThrows(AssertionError.class, () -> FxTestUtil.runAndWait(executed::incrementAndGet));
                    assertTrue(Thread.currentThread().isInterrupted());
                } finally {
                    Thread.interrupted();
                }
            }).get(10, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            blocker.get(10, TimeUnit.SECONDS);
        }
        FxTestUtil.runAndWait(() -> assertEquals(0, executed.get()));
    }
}
