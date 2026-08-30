/*
 * "Часы в нике" Cryptogram: периодически переписывает имя/фамилию профиля,
 * подставляя туда текущее время в выбранном часовом поясе. Исходное имя
 * сохраняется отдельно и не теряется — при выключении функции оно
 * восстанавливается.
 *
 * ВАЖНО: слишком частая смена имени может временно попасть под FLOOD_WAIT
 * сервера Telegram (сервер сам сообщает, сколько ждать, если это произойдёт —
 * это нормальная защита от злоупотреблений, а не баг). Сервис аккуратно
 * обрабатывает такую ошибку и не долбит сервер повторно раньше времени,
 * указанного самим сервером.
 */
package org.telegram.messenger;

import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CryptogramClockName {

    public enum Position {
        FIRST_NAME_PREFIX,  // время + пробел + исходное имя
        FIRST_NAME_SUFFIX,  // исходное имя + пробел + время
        LAST_NAME_REPLACE   // фамилия целиком заменяется на время
    }

    public static boolean enabled = false;
    public static String timeZoneId = TimeZone.getDefault().getID();
    public static Position position = Position.LAST_NAME_REPLACE;
    public static int intervalMinutes = 1;
    public static String originalFirstName = "";
    public static String originalLastName = "";
    public static String timeFormatPattern = "HH:mm";

    private static final int currentAccount = UserConfig.selectedAccount;
    private static Runnable updateRunnable;
    private static boolean scheduledAfterFlood;

    public static void start() {
        if (!enabled) {
            return;
        }
        captureOriginalNameIfNeeded();
        scheduleNext(0);
    }

    public static void stop() {
        if (updateRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(updateRunnable);
            updateRunnable = null;
        }
    }

    public static void restoreOriginalName() {
        if (originalFirstName.isEmpty() && originalLastName.isEmpty()) {
            return;
        }
        sendNameUpdate(originalFirstName, originalLastName);
    }

    private static void captureOriginalNameIfNeeded() {
        if (!originalFirstName.isEmpty() || !originalLastName.isEmpty()) {
            return;
        }
        TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
        if (user != null) {
            originalFirstName = user.first_name != null ? user.first_name : "";
            originalLastName = user.last_name != null ? user.last_name : "";
        }
    }

    private static void scheduleNext(long delayMs) {
        stop();
        updateRunnable = () -> {
            if (!enabled) {
                return;
            }
            applyClockToName();
            scheduleNext(Math.max(1, intervalMinutes) * 60_000L);
        };
        AndroidUtilities.runOnUIThread(updateRunnable, delayMs);
    }

    private static void applyClockToName() {
        String time = formatCurrentTime();
        String newFirstName = originalFirstName;
        String newLastName = originalLastName;
        switch (position) {
            case FIRST_NAME_PREFIX:
                newFirstName = (time + " " + originalFirstName).trim();
                break;
            case FIRST_NAME_SUFFIX:
                newFirstName = (originalFirstName + " " + time).trim();
                break;
            case LAST_NAME_REPLACE:
            default:
                newLastName = time;
                break;
        }
        sendNameUpdate(newFirstName, newLastName);
    }

    private static String formatCurrentTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(timeFormatPattern, Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            return sdf.format(new Date());
        } catch (Exception e) {
            FileLog.e(e);
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
            return String.format(Locale.getDefault(), "%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        }
    }

    private static void sendNameUpdate(String firstName, String lastName) {
        TL_account.updateProfile req = new TL_account.updateProfile();
        req.flags |= 1;
        req.first_name = firstName != null ? firstName : "";
        req.flags |= 2;
        req.last_name = lastName != null ? lastName : "";
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            if (error != null) {
                // FLOOD_WAIT_<секунды> — сервер просит подождать именно столько,
                // прежде чем повторять этот же запрос. Уважаем это указание.
                if (error.text != null && error.text.startsWith("FLOOD_WAIT_")) {
                    try {
                        int seconds = Integer.parseInt(error.text.substring("FLOOD_WAIT_".length()));
                        scheduledAfterFlood = true;
                        AndroidUtilities.runOnUIThread(() -> {
                            if (enabled) {
                                scheduleNext(0);
                            }
                        }, (seconds + 1) * 1000L);
                    } catch (NumberFormatException ignored) {
                    }
                } else {
                    FileLog.e("CryptogramClockName: " + error.text);
                }
            } else if (response instanceof TLRPC.User) {
                UserConfig.getInstance(currentAccount).setCurrentUser((TLRPC.User) response);
                UserConfig.getInstance(currentAccount).saveConfig(true);
            }
        });
    }
}
