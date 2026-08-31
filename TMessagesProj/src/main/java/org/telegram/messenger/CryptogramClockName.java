/*
 * "Часы в нике" Cryptogram: периодически переписывает имя/фамилию профиля,
 * подставляя туда текущее время в выбранном часовом поясе. Исходное имя
 * сохраняется отдельно и не теряется — при выключении функции оно
 * восстанавливается.
 *
 * Работает только пока приложение открыто в памяти (обычный таймер, без
 * фоновой службы) — при полном закрытии приложения обновление времени
 * останавливается и возобновится при следующем запуске.
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

    // Шаблон свободного текста: место, куда вставляется время, отмечается
    // токеном [time]. Например "Алина [time] дура" даст "Алина 06:42 дура".
    // Если токена в шаблоне нет — время просто добавляется в конец.
    public static final String TIME_TOKEN = "[time]";

    public enum TargetField {
        FIRST_NAME,
        LAST_NAME
    }

    public static boolean enabled = false;
    public static String timeZoneId = TimeZone.getDefault().getID();
    public static TargetField targetField = TargetField.LAST_NAME;
    public static String template = TIME_TOKEN;
    public static int intervalMinutes = 1;
    public static String originalFirstName = "";
    public static String originalLastName = "";
    public static String timeFormatPattern = "HH:mm";

    private static final int currentAccount = UserConfig.selectedAccount;
    private static Runnable updateRunnable;

    public static void start() {
        if (!enabled) {
            return;
        }
        captureOriginalNameIfNeeded();
        scheduleAtNextMinuteBoundary();
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

    // Cryptogram: вместо простого "подождать N минут от текущего момента"
    // (что со временем накапливает отставание — каждая отправка занимает
    // какое-то время на сеть) — вычисляем точный момент ближайшей границы
    // интервала (например, ровно 06:43:00.000) и планируем именно на неё.
    private static void scheduleAtNextMinuteBoundary() {
        stop();
        int intervalMs = Math.max(1, intervalMinutes) * 60_000;
        long now = System.currentTimeMillis();
        long delay = intervalMs - (now % intervalMs);
        updateRunnable = () -> {
            if (!enabled) {
                return;
            }
            applyClockToName();
            scheduleAtNextMinuteBoundary();
        };
        AndroidUtilities.runOnUIThread(updateRunnable, delay);
    }

    private static void applyClockToName() {
        String time = formatCurrentTime();
        String textWithTime = template != null && template.contains(TIME_TOKEN)
                ? template.replace(TIME_TOKEN, time)
                : (template == null || template.isEmpty() ? time : template + " " + time);

        String newFirstName = originalFirstName;
        String newLastName = originalLastName;
        if (targetField == TargetField.FIRST_NAME) {
            newFirstName = textWithTime;
        } else {
            newLastName = textWithTime;
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
                        AndroidUtilities.runOnUIThread(() -> {
                            if (enabled) {
                                scheduleAtNextMinuteBoundary();
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
