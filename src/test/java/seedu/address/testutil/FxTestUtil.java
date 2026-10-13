package seedu.address.testutil;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javafx.application.Platform;

/**
 * Runs GUI test actions on one shared JavaFX toolkit without shutting it down between tests.
 * Requires matching native libraries and a graphical session (for example, Xvfb on Linux).
 */
public final class FxTestUtil {

    private static final Object STARTUP_LOCK = new Object();
    private static final int TIMEOUT_SECONDS = 30;

    // All callers share the startup result, including a native-runtime or display failure.
    private static CompletableFuture<Void> startup;

    private FxTestUtil() {}

    /**
     * Executes and awaits an action on the FX thread, or executes inline when already on that thread.
     * Propagates the original assertion/error/runtime exception to the calling test. Interruptions
     * preserve the caller's interrupt flag; an interrupted or timed-out queued action is canceled.
     * An action already running cannot be rolled back. Tests must close their own windows and must
     * never call {@code Platform.exit()} because the toolkit cannot be restarted in the same JVM.
     *
     * @param action Non-null GUI operation or assertions.
     */
    public static void runAndWait(Runnable action) {
        requireNonNull(action);
        if (Platform.isFxApplicationThread()) {
            Platform.setImplicitExit(false);
            action.run();
            return;
        }

        awaitStartup();
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        try {
            await(task);
        } finally {
            if (!task.isDone()) {
                task.cancel(false);
            }
        }
    }

    /**
     * Starts the toolkit at most once and also supports a toolkit started by another test runner.
     * The startup callback never takes the lock, and callers wait outside it to avoid FX deadlocks.
     */
    private static void awaitStartup() {
        CompletableFuture<Void> result;
        synchronized (STARTUP_LOCK) {
            if (startup == null) {
                startup = new CompletableFuture<>();
                CompletableFuture<Void> startupResult = startup;
                Runnable ready = () -> {
                    Platform.setImplicitExit(false);
                    startupResult.complete(null);
                };
                try {
                    Platform.startup(ready);
                } catch (IllegalStateException e) {
                    // Platform.startup rejects an already initialized toolkit; reuse its event queue.
                    Platform.runLater(ready);
                } catch (RuntimeException | Error e) {
                    startupResult.completeExceptionally(e);
                }
            }
            result = startup;
        }
        await(result);
    }

    /**
     * Waits for a real completion signal instead of sleeping or silently skipping GUI failures.
     */
    private static void await(Future<?> result) {
        try {
            result.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while awaiting the JavaFX test action", e);
        } catch (TimeoutException e) {
            throw new AssertionError("JavaFX test action did not complete within " + TIMEOUT_SECONDS + " seconds", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new AssertionError("JavaFX test action failed", cause);
        }
    }
}
