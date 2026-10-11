package com.dread.util;

import java.util.ArrayList;
import java.util.List;

/** Tiny tick scheduler used to sequence scary events. */
public final class Scheduler {
    private record Task(long runAt, Runnable action) {}

    private static final List<Task> TASKS = new ArrayList<>();
    private static long now = 0;

    private Scheduler() {}

    public static long now() {
        return now;
    }

    public static void later(int ticks, Runnable action) {
        TASKS.add(new Task(now + Math.max(0, ticks), action));
    }

    public static void tick() {
        now++;
        if (TASKS.isEmpty()) return;
        List<Task> due = new ArrayList<>();
        TASKS.removeIf(t -> {
            if (t.runAt() <= now) {
                due.add(t);
                return true;
            }
            return false;
        });
        for (Task t : due) {
            try {
                t.action().run();
            } catch (Exception ignored) {
                // a failed scare should never crash the server
            }
        }
    }

    public static void clear() {
        TASKS.clear();
    }
}
