package tool;

import java.util.concurrent.atomic.AtomicBoolean;

public class Debounce {
    private final AtomicBoolean updating = new AtomicBoolean(false);
    private final long waitTime;

    public Debounce(long waitTime) {
        this.waitTime = waitTime;
    }

    public void debounce(Runnable action) {
        if (updating.compareAndSet(false, true)) {
            new Thread(() -> {
                try {
                    Thread.sleep(waitTime);
                    if (updating.get()) {
                        action.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    updating.set(false);
                }
            }).start();
        }
    }
}