/*
 * Автоответчик Cryptogram. Подписывается на уведомление о новых входящих
 * сообщениях (didReceiveNewMessages) и не изменяет основной код
 * MessagesController — вся логика живёт отдельно, в этом классе.
 */
package org.telegram.messenger;

import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class CryptogramAutoReply implements NotificationCenter.NotificationCenterDelegate {

    private static volatile CryptogramAutoReply instance;
    private final int currentAccount;

    public static boolean enabled = false;
    public static boolean replyToPrivate = true;
    public static boolean replyToGroups = false;
    public static String defaultReplyText = "Сейчас недоступен, отвечу позже.";
    public static int delaySeconds = 2;
    // ключевое слово (в нижнем регистре) -> текст ответа
    public static final HashMap<String, String> keywordReplies = new HashMap<>();
    // dialogId -> персональный ответ для конкретного пользователя/чата
    public static final HashMap<Long, String> perDialogReplies = new HashMap<>();
    // Несколько вариантов ответа по умолчанию — если список не пуст,
    // используется случайный вариант вместо defaultReplyText.
    public static final ArrayList<String> randomReplyVariants = new ArrayList<>();

    // Расписание работы: если includeSchedule=true, автоответчик срабатывает
    // только в указанном диапазоне часов (по 24-часовому времени устройства).
    public static boolean scheduleEnabled = false;
    public static int scheduleStartHour = 0;
    public static int scheduleEndHour = 24;

    // Не отвечаем повторно одному и тому же диалогу чаще, чем раз в 2 минуты,
    // чтобы не заспамить собеседника при активной переписке.
    private static final long COOLDOWN_MS = 2 * 60 * 1000L;
    private final HashMap<Long, Long> lastReplyTime = new HashMap<>();

    private CryptogramAutoReply(int account) {
        currentAccount = account;
    }

    public static CryptogramAutoReply getInstance(int account) {
        if (instance == null) {
            synchronized (CryptogramAutoReply.class) {
                if (instance == null) {
                    instance = new CryptogramAutoReply(account);
                }
            }
        }
        return instance;
    }

    public void start() {
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didReceiveNewMessages);
    }

    public void stop() {
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didReceiveNewMessages);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id != NotificationCenter.didReceiveNewMessages || !enabled) {
            return;
        }
        try {
            long dialogId = (Long) args[0];
            ArrayList<MessageObject> messages = (ArrayList<MessageObject>) args[1];
            boolean scheduled = (Boolean) args[2];
            if (scheduled || messages == null || messages.isEmpty()) {
                return;
            }
            if (UserObject.isUserSelf(MessagesController.getInstance(account).getUser(dialogId)) && dialogId > 0) {
                // не отвечаем сами себе в "Избранном"
                return;
            }
            boolean isGroupOrChannel = DialogObject.isChatDialog(dialogId);
            if (isGroupOrChannel && !replyToGroups) {
                return;
            }
            if (!isGroupOrChannel && !replyToPrivate) {
                return;
            }
            if (scheduleEnabled && !isWithinSchedule()) {
                return;
            }

            MessageObject lastMessage = messages.get(messages.size() - 1);
            if (lastMessage == null || lastMessage.isOut()) {
                return;
            }

            long now = System.currentTimeMillis();
            Long last = lastReplyTime.get(dialogId);
            if (last != null && (now - last) < COOLDOWN_MS) {
                return;
            }

            String replyText = resolveReplyText(dialogId, lastMessage);
            if (replyText == null || replyText.isEmpty()) {
                return;
            }

            lastReplyTime.put(dialogId, now);

            Utilities.stageQueue.postRunnable(() -> {
                try {
                    Thread.sleep(Math.max(0, delaySeconds) * 1000L);
                } catch (InterruptedException ignored) {
                }
                AndroidUtilities.runOnUIThread(() -> {
                    SendMessagesHelper.SendMessageParams params = SendMessagesHelper.SendMessageParams.of(replyText, dialogId);
                    SendMessagesHelper.getInstance(account).sendMessage(params);
                });
            });
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private boolean isWithinSchedule() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (scheduleStartHour <= scheduleEndHour) {
            return hour >= scheduleStartHour && hour < scheduleEndHour;
        }
        // диапазон через полночь, например 22-6
        return hour >= scheduleStartHour || hour < scheduleEndHour;
    }

    private String resolveReplyText(long dialogId, MessageObject message) {
        String perDialog = perDialogReplies.get(dialogId);
        if (perDialog != null && !perDialog.isEmpty()) {
            return perDialog;
        }
        String text = message.messageOwner != null && message.messageOwner.message != null
                ? message.messageOwner.message.toLowerCase(Locale.getDefault())
                : "";
        for (String keyword : keywordReplies.keySet()) {
            if (!keyword.isEmpty() && text.contains(keyword.toLowerCase(Locale.getDefault()))) {
                return keywordReplies.get(keyword);
            }
        }
        if (!randomReplyVariants.isEmpty()) {
            int index = Utilities.random.nextInt(randomReplyVariants.size());
            return randomReplyVariants.get(index);
        }
        return defaultReplyText;
    }
}
