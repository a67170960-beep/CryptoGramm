/*
 * Cryptogram local appearance-profile parser.
 * The input is declarative JSON, never executable code.
 */
package org.telegram.messenger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Applies JSON appearance profiles through a strict, versioned allowlist.
 * This class intentionally has no access to chats, accounts, networking, or sending APIs.
 */
public final class CryptogramStyleScript {
    private static final int MAX_SCRIPT_LENGTH = 8192;
    private static final int MAX_SETTINGS = 15;
    private static final String FORMAT = "cryptogram-style";

    private CryptogramStyleScript() {
    }

    /** Validates the complete profile before changing any setting, then applies it locally. */
    public static String apply(String source) throws JSONException {
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Вставь JSON-сценарий оформления.");
        }
        if (source.length() > MAX_SCRIPT_LENGTH) {
            throw new IllegalArgumentException("Сценарий слишком большой: максимум 8192 символа.");
        }

        JSONObject root = new JSONObject(source);
        ensureAllowedKeys(root, "format", "version", "name", "settings");
        if (!FORMAT.equals(readString(root, "format"))) {
            throw new IllegalArgumentException("Неизвестный формат сценария.");
        }
        readInteger(root, "version", 1, 1);

        String name = "Мой стиль";
        if (root.has("name")) {
            Object rawName = root.get("name");
            if (!(rawName instanceof String) || ((String) rawName).trim().isEmpty() || ((String) rawName).length() > 48) {
                throw new IllegalArgumentException("Имя должно быть непустой строкой до 48 символов.");
            }
            name = ((String) rawName).trim();
        }

        Object rawSettings = root.get("settings");
        if (!(rawSettings instanceof JSONObject)) {
            throw new IllegalArgumentException("Поле settings должно быть объектом JSON.");
        }
        JSONObject settings = (JSONObject) rawSettings;
        if (settings.length() < 1 || settings.length() > MAX_SETTINGS) {
            throw new IllegalArgumentException("Добавь от 1 до 15 параметров оформления.");
        }
        validateSettings(settings);
        applySettings(settings);
        return name;
    }

    private static void ensureAllowedKeys(JSONObject object, String... allowed) throws JSONException {
        Iterator<String> iterator = object.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            boolean found = false;
            for (String allowedKey : allowed) {
                if (allowedKey.equals(key)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Обнаружено неподдерживаемое поле. Проверь документацию сценариев.");
            }
        }
    }

    private static void validateSettings(JSONObject settings) throws JSONException {
        Iterator<String> iterator = settings.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            switch (key) {
                case "chatListBlurEnabled":
                case "animationsEnabled":
                case "roundedBubblesEnabled":
                case "compactChatList":
                case "largeAvatarsInChatList":
                case "hideNavigationBarLabels":
                case "showTimeSeconds":
                case "timeAccentColorEnabled":
                case "snowEnabled":
                    requireBoolean(settings.get(key));
                    break;
                case "chatListBlurIntensity":
                case "messageBubbleRoundness":
                    requireInteger(settings.get(key), 0, 100);
                    break;
                case "animationSpeedPercent":
                    requireInteger(settings.get(key), 50, 200);
                    break;
                case "timeFormat":
                    requireInteger(settings.get(key), 0, 2);
                    break;
                case "timeTextSize":
                    requireInteger(settings.get(key), 10, 18);
                    break;
                case "checkStyle":
                    requireInteger(settings.get(key), 0, 3);
                    break;
                default:
                    throw new IllegalArgumentException("Обнаружен неподдерживаемый параметр оформления.");
            }
        }
    }

    private static boolean requireBoolean(Object value) {
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("Для параметра оформления нужно значение true или false.");
        }
        return (Boolean) value;
    }

    private static int requireInteger(Object value, int min, int max) {
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Для параметра оформления нужно целое число.");
        }
        double number = ((Number) value).doubleValue();
        if (number != Math.rint(number) || number < min || number > max) {
            throw new IllegalArgumentException("Числовое значение вне допустимого диапазона.");
        }
        return (int) number;
    }

    private static String readString(JSONObject object, String key) throws JSONException {
        Object value = object.get(key);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Поле format должно быть строкой.");
        }
        return (String) value;
    }

    private static int readInteger(JSONObject object, String key, int min, int max) throws JSONException {
        return requireInteger(object.get(key), min, max);
    }

    private static void applySettings(JSONObject settings) throws JSONException {
        if (settings.has("chatListBlurEnabled")) {
            setBoolean(SharedConfig.chatListBlurEnabled, settings.getBoolean("chatListBlurEnabled"), SharedConfig::toggleChatListBlur);
        }
        if (settings.has("chatListBlurIntensity")) {
            SharedConfig.setChatListBlurIntensity(settings.getInt("chatListBlurIntensity"));
        }
        if (settings.has("animationsEnabled")) {
            setBoolean(SharedConfig.cryptogramAnimationsEnabled, settings.getBoolean("animationsEnabled"), SharedConfig::toggleAnimationsEnabled);
        }
        if (settings.has("animationSpeedPercent")) {
            SharedConfig.setAnimationSpeedPercent(settings.getInt("animationSpeedPercent"));
        }
        if (settings.has("roundedBubblesEnabled")) {
            setBoolean(SharedConfig.roundedBubblesEnabled, settings.getBoolean("roundedBubblesEnabled"), SharedConfig::toggleRoundedBubbles);
        }
        if (settings.has("messageBubbleRoundness")) {
            SharedConfig.setMessageBubbleRoundness(settings.getInt("messageBubbleRoundness"));
        }
        if (settings.has("compactChatList")) {
            setBoolean(SharedConfig.compactChatList, settings.getBoolean("compactChatList"), SharedConfig::toggleCompactChatList);
        }
        if (settings.has("largeAvatarsInChatList")) {
            setBoolean(SharedConfig.largeAvatarsInChatList, settings.getBoolean("largeAvatarsInChatList"), SharedConfig::toggleLargeAvatarsInChatList);
        }
        if (settings.has("hideNavigationBarLabels")) {
            setBoolean(SharedConfig.hideNavigationBarLabels, settings.getBoolean("hideNavigationBarLabels"), SharedConfig::toggleHideNavigationBarLabels);
        }
        if (settings.has("timeFormat")) {
            setTimeFormat(settings.getInt("timeFormat"));
        }
        if (settings.has("showTimeSeconds")) {
            setBoolean(SharedConfig.cryptogramTimeShowSeconds, settings.getBoolean("showTimeSeconds"), SharedConfig::toggleCryptogramTimeSeconds);
        }
        if (settings.has("timeAccentColorEnabled")) {
            setBoolean(SharedConfig.cryptogramTimeColorEnabled, settings.getBoolean("timeAccentColorEnabled"), SharedConfig::toggleCryptogramTimeColor);
        }
        if (settings.has("timeTextSize")) {
            SharedConfig.setCryptogramTimeTextSize(settings.getInt("timeTextSize"));
        }
        if (settings.has("checkStyle")) {
            setCheckStyle(settings.getInt("checkStyle"));
        }
        if (settings.has("snowEnabled")) {
            setBoolean(SharedConfig.cryptogramSnowEnabled, settings.getBoolean("snowEnabled"), SharedConfig::toggleCryptogramSnow);
        }

        // A user-applied profile is shown as a custom profile instead of a built-in preset.
        SharedConfig.cryptogramUiPreset = -1;
        MessagesController.getGlobalMainSettings().edit().putInt("cryptogramUiPreset", -1).apply();
    }

    private static void setBoolean(boolean currentValue, boolean targetValue, Runnable toggle) {
        if (currentValue != targetValue) {
            toggle.run();
        }
    }

    private static void setTimeFormat(int target) {
        for (int i = 0; i < 3 && SharedConfig.cryptogramTimeFormat != target; i++) {
            SharedConfig.cycleCryptogramTimeFormat();
        }
    }

    private static void setCheckStyle(int target) {
        for (int i = 0; i < 4 && SharedConfig.cryptogramCheckStyle != target; i++) {
            SharedConfig.cycleCryptogramCheckStyle();
        }
    }

    public static String getExampleScript() {
        return "{\n"
                + "  \"format\": \"cryptogram-style\",\n"
                + "  \"version\": 1,\n"
                + "  \"name\": \"Мой стиль\",\n"
                + "  \"settings\": {\n"
                + "    \"chatListBlurEnabled\": true,\n"
                + "    \"chatListBlurIntensity\": 72,\n"
                + "    \"animationsEnabled\": true,\n"
                + "    \"animationSpeedPercent\": 110,\n"
                + "    \"roundedBubblesEnabled\": true,\n"
                + "    \"messageBubbleRoundness\": 80,\n"
                + "    \"compactChatList\": false,\n"
                + "    \"largeAvatarsInChatList\": true,\n"
                + "    \"hideNavigationBarLabels\": false,\n"
                + "    \"timeFormat\": 1,\n"
                + "    \"showTimeSeconds\": false,\n"
                + "    \"timeAccentColorEnabled\": true,\n"
                + "    \"timeTextSize\": 13,\n"
                + "    \"checkStyle\": 2,\n"
                + "    \"snowEnabled\": false\n"
                + "  }\n"
                + "}";
    }

    public static String getDocumentation() {
        return "JSON-сценарии меняют только локальные настройки внешнего вида. Это не JavaScript: код не запускается.\n\n"
                + "Как применить: измени пример в редакторе, вставь свой JSON и нажми «Применить».\n\n"
                + "Корневые поля: format должен быть cryptogram-style, version — 1, name — необязательное имя до 48 символов, settings — объект параметров.\n\n"
                + "Допустимые параметры settings:\n"
                + "• chatListBlurEnabled — true/false; chatListBlurIntensity — 0–100.\n"
                + "• animationsEnabled — true/false; animationSpeedPercent — 50–200.\n"
                + "• roundedBubblesEnabled — true/false; messageBubbleRoundness — 0–100.\n"
                + "• compactChatList, largeAvatarsInChatList, hideNavigationBarLabels — true/false.\n"
                + "• timeFormat — 0 системный, 1 формат 24 часа, 2 формат 12 часов.\n"
                + "• showTimeSeconds, timeAccentColorEnabled, snowEnabled — true/false.\n"
                + "• timeTextSize — 10–18; checkStyle — 0–3.\n\n"
                + "Неизвестные ключи, неверные типы и значения вне диапазона отклоняются до применения. Профиль хранится только в настройках приложения; сценарии не читают чаты, не отправляют сообщения, не управляют аккаунтами, не меняют статус онлайн и не используют сеть.";
    }
}
