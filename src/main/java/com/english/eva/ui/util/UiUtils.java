package com.english.eva.ui.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingWorker;

public final class UiUtils {

    private UiUtils() {}

    public static <T> void runInBackground(Supplier<T> task, Consumer<T> onDone) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return task.get();
            }

            @Override
            protected void done() {
                try {
                    onDone.accept(get());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }
}
