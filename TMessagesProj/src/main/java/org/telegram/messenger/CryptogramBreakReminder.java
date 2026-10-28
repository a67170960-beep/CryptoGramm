package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.LaunchActivity;

import java.lang.ref.WeakReference;

/** Tracks foreground time in the main client and shows an optional local break reminder. */
public final class CryptogramBreakReminder {
    private static final String PREFS = "cryptogram_break_reminder";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_INTERVAL = "interval_minutes";
    private static final String KEY_SNOOZE = "snooze_minutes";
    private static final String KEY_MESSAGE = "message";
    private static final long BACKGROUND_RESET_MS = 15 * 60 * 1000L;

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Runnable reminderRunnable = CryptogramBreakReminder::showReminder;
    private static WeakReference<LaunchActivity> activityReference;
    private static boolean foreground;
    private static long foregroundStartedAt;
    private static long accumulatedForegroundMs;
    private static long lastBackgroundAt;
    private static long snoozeUntil;

    private CryptogramBreakReminder() {
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled(Context context) {
        return preferences(context).getBoolean(KEY_ENABLED, true);
    }

    public static void setEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static int getIntervalMinutes(Context context) {
        return preferences(context).getInt(KEY_INTERVAL, 60);
    }

    public static void setIntervalMinutes(Context context, int minutes) {
        preferences(context).edit().putInt(KEY_INTERVAL, Math.max(15, Math.min(240, minutes))).apply();
    }

    public static int getSnoozeMinutes(Context context) {
        return preferences(context).getInt(KEY_SNOOZE, 15);
    }

    public static void setSnoozeMinutes(Context context, int minutes) {
        preferences(context).edit().putInt(KEY_SNOOZE, Math.max(5, Math.min(60, minutes))).apply();
    }

    public static String getMessage(Context context) {
        return preferences(context).getString(KEY_MESSAGE, LocaleController.getString(R.string.CryptogramBreakDefaultMessage));
    }

    public static void setMessage(Context context, String message) {
        String clean = message == null ? "" : message.trim();
        if (clean.length() > 160) {
            clean = clean.substring(0, 160);
        }
        if (clean.isEmpty()) {
            preferences(context).edit().remove(KEY_MESSAGE).apply();
        } else {
            preferences(context).edit().putString(KEY_MESSAGE, clean).apply();
        }
    }

    public static void onActivityResumed(LaunchActivity activity) {
        long now = SystemClock.elapsedRealtime();
        if (lastBackgroundAt != 0 && now - lastBackgroundAt >= BACKGROUND_RESET_MS) {
            accumulatedForegroundMs = 0;
            snoozeUntil = 0;
        }
        activityReference = new WeakReference<>(activity);
        if (!foreground) {
            foregroundStartedAt = now;
            foreground = true;
        }
        schedule();
    }

    public static void onActivityPaused(LaunchActivity activity) {
        LaunchActivity current = activityReference == null ? null : activityReference.get();
        if (current != activity) {
            return;
        }
        long now = SystemClock.elapsedRealtime();
        if (foreground && isEnabled(activity)) {
            accumulatedForegroundMs += Math.max(0, now - foregroundStartedAt);
        }
        foreground = false;
        lastBackgroundAt = now;
        handler.removeCallbacks(reminderRunnable);
        activityReference = null;
    }

    /** Call after a setting changes so the next interval starts from the new configuration. */
    public static void onSettingsChanged(Context context) {
        handler.removeCallbacks(reminderRunnable);
        accumulatedForegroundMs = 0;
        snoozeUntil = 0;
        if (foreground && isEnabled(context)) {
            foregroundStartedAt = SystemClock.elapsedRealtime();
            schedule();
        }
    }

    public static void showTestReminder(LaunchActivity activity) {
        showBulletin(activity);
    }

    private static void schedule() {
        handler.removeCallbacks(reminderRunnable);
        LaunchActivity activity = activityReference == null ? null : activityReference.get();
        if (!foreground || activity == null || activity.isFinishing() || !isEnabled(activity)) {
            return;
        }
        long now = SystemClock.elapsedRealtime();
        long delay;
        if (snoozeUntil > now) {
            delay = snoozeUntil - now;
        } else {
            snoozeUntil = 0;
            long intervalMs = getIntervalMinutes(activity) * 60 * 1000L;
            long elapsed = accumulatedForegroundMs + Math.max(0, now - foregroundStartedAt);
            delay = intervalMs - elapsed;
        }
        handler.postDelayed(reminderRunnable, Math.max(500, delay));
    }

    private static void showReminder() {
        LaunchActivity activity = activityReference == null ? null : activityReference.get();
        if (!foreground || activity == null || activity.isFinishing() || !isEnabled(activity)) {
            return;
        }
        accumulatedForegroundMs = 0;
        foregroundStartedAt = SystemClock.elapsedRealtime();
        snoozeUntil = 0;
        showBulletin(activity);
        schedule();
    }

    private static void showBulletin(LaunchActivity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        activity.showBulletin(factory -> factory.createSimpleBulletin(
                R.raw.chats_infotip,
                getMessage(activity),
                LocaleController.getString(R.string.CryptogramBreakSnooze),
                Bulletin.DURATION_PROLONG,
                () -> snooze(activity)
        ));
    }

    private static void snooze(LaunchActivity activity) {
        LaunchActivity current = activityReference == null ? null : activityReference.get();
        if (!foreground || current != activity) {
            return;
        }
        accumulatedForegroundMs = 0;
        foregroundStartedAt = SystemClock.elapsedRealtime();
        snoozeUntil = foregroundStartedAt + getSnoozeMinutes(activity) * 60 * 1000L;
        schedule();
    }
}
