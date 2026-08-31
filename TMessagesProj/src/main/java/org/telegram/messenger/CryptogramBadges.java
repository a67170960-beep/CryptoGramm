/*
 * Часть Cryptogram: собственная система бейджей "официальный разработчик /
 * официальный ресурс / официальный канал", не связанная с настоящей верификацией
 * Telegram. Список ID хранится не в самом приложении, а в отдельном файле в
 * репозитории на GitHub — так его можно менять без пересборки APK. Каждый
 * запущенный клиент Cryptogram периодически скачивает этот файл сам и у всех
 * пользователей появляется одна и та же картина.
 */
package org.telegram.messenger;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class CryptogramBadges {

    // Прямая ссылка на "сырой" файл в репозитории — правишь его на GitHub в
    // браузере (карандаш → правка JSON → commit), и все клиенты подхватят
    // изменения при следующей проверке, без обновления самого приложения.
    private static final String LIST_URL =
            "https://raw.githubusercontent.com/a67170960-beep/CryptoGramm/main/official_ids/list.json";

    // Не проверяем сервер чаще одного раза в час — экономим трафик и не
    // создаём лишнюю нагрузку на GitHub.
    private static final long CHECK_INTERVAL_MS = 60 * 60 * 1000L;

    public enum BadgeType {
        NONE,
        DEVELOPER,        // "Официальный разработчик Cryptogram"
        OFFICIAL_RESOURCE, // "Официальный ресурс"
        OFFICIAL_CHANNEL  // "Официальный канал"
    }

    private static volatile Set<Long> developerIds = new HashSet<>();
    private static volatile Set<Long> officialResourceIds = new HashSet<>();
    private static volatile Set<Long> officialChannelIds = new HashSet<>();
    private static volatile boolean loadedOnce = false;
    private static volatile long lastCheckTime = 0;

    // Твой личный user_id — только этот аккаунт видит в настройках экран
    // управления бейджами. Больше никто не может открыть этот экран, даже
    // зная о его существовании: проверка идёт по числовому ID аккаунта.
    public static final long ADMIN_USER_ID = 6444684762L;

    public static Set<Long> getDeveloperIdsSnapshot() {
        return new HashSet<>(developerIds);
    }

    public static Set<Long> getResourceIdsSnapshot() {
        return new HashSet<>(officialResourceIds);
    }

    public static Set<Long> getChannelIdsSnapshot() {
        return new HashSet<>(officialChannelIds);
    }

    public static boolean isAdmin(long userId) {
        return userId == ADMIN_USER_ID;
    }

    public static BadgeType getBadge(long userId) {
        if (developerIds.contains(userId)) {
            return BadgeType.DEVELOPER;
        }
        if (officialResourceIds.contains(userId)) {
            return BadgeType.OFFICIAL_RESOURCE;
        }
        if (officialChannelIds.contains(userId)) {
            return BadgeType.OFFICIAL_CHANNEL;
        }
        return BadgeType.NONE;
    }

    // Вызывать один раз при старте приложения и дальше — по мере надобности
    // перед показом профиля/списка чатов. Сама функция сама решает, нужно ли
    // реально идти в сеть (не чаще раза в час) или можно использовать
    // уже загруженные ранее данные.
    public static void checkForUpdates() {
        long now = System.currentTimeMillis();
        if (loadedOnce && (now - lastCheckTime) < CHECK_INTERVAL_MS) {
            return;
        }
        lastCheckTime = now;
        performFetch();
    }

    // Игнорирует часовой лимит и скачивает список немедленно — используется
    // из админ-панели, когда только что изменили список на GitHub и не хотят
    // ждать до часа, чтобы увидеть результат у себя же.
    public static void forceUpdateNow() {
        lastCheckTime = System.currentTimeMillis();
        performFetch();
    }

    private static void performFetch() {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                URL url = new URL(LIST_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestMethod("GET");

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                connection.disconnect();

                if (!TextUtils.isEmpty(sb.toString())) {
                    parseAndApply(sb.toString());
                }
            } catch (Exception e) {
                // Тихо игнорируем сетевые ошибки — бейджи не критичны для работы
                // приложения, при следующей успешной проверке всё обновится.
                FileLog.e(e);
            }
        });
    }

    private static void parseAndApply(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            Set<Long> newDeveloper = extractIds(obj, "developer");
            Set<Long> newResource = extractIds(obj, "official_resource");
            Set<Long> newChannel = extractIds(obj, "official_channel");

            developerIds = newDeveloper;
            officialResourceIds = newResource;
            officialChannelIds = newChannel;
            loadedOnce = true;
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private static Set<Long> extractIds(JSONObject obj, String key) {
        Set<Long> result = new HashSet<>();
        JSONArray arr = obj.optJSONArray(key);
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                result.add(arr.optLong(i));
            }
        }
        return result;
    }
}
