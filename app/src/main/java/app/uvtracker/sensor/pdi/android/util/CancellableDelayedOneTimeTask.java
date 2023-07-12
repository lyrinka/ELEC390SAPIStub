package app.uvtracker.sensor.pdi.android.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

public class CancellableDelayedOneTimeTask implements Runnable {

    @NonNull
    private final Runnable runnable;

    private boolean cancelled;

    public CancellableDelayedOneTimeTask(@NonNull Runnable runnable) {
        this.runnable = runnable;
        this.cancelled = false;
    }

    public boolean isCancelledOrDone() {
        return this.cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public CancellableDelayedOneTimeTask post(int delayms) {
        return this.post(Looper.getMainLooper(), delayms);
    }

    public CancellableDelayedOneTimeTask post(@NonNull Looper looper, int delayms) {
        (new Handler(looper)).postDelayed(this, delayms);
        return this;
    }

    @Override
    public void run() {
        if(this.cancelled) return;
        this.runnable.run();
        this.cancelled = true;
    }

}
