/*
 * Функция автоматизации Cryptogram: автоматическая пересылка входящих
 * сообщений, содержащих заданное ключевое слово, в другой чат (например,
 * себе в "Избранное" или в отдельный чат-архив). Подписывается на то же
 * уведомление о новых сообщениях, что и автоответчик, но работает
 * независимо от него.
 */
package org.telegram.messenger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class CryptogramAutoForward implements NotificationCenter.NotificationCenterDelegate {

    private static volatile CryptogramAutoForward instance;
    private final int currentAccount;

    public static boolean enabled = false;
    // ключевое слово (нижний регистр) -> dialogId, куда пересылать сообщения с этим словом
    public static final HashMap<String, Long> forwardRules = new HashMap<>();

    private CryptogramAutoForward(int account) {
        currentAccount = account;
    }

    public static CryptogramAutoForward getInstance(int account) {
        if (instance == null) {
            synchronized (CryptogramAutoForward.class) {
                if (instance == null) {
                    instance = new CryptogramAutoForward(account);
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
        if (id != NotificationCenter.didReceiveNewMessages || !enabled || forwardRules.isEmpty()) {
            return;
        }
        try {
            ArrayList<MessageObject> messages = (ArrayList<MessageObject>) args[1];
            boolean scheduled = (Boolean) args[2];
            if (scheduled || messages == null || messages.isEmpty()) {
                return;
            }
            for (MessageObject message : messages) {
                if (message == null || message.isOut() || message.messageOwner == null || message.messageOwner.message == null) {
                    continue;
                }
                String text = message.messageOwner.message.toLowerCase(Locale.getDefault());
                for (String keyword : forwardRules.keySet()) {
                    if (!keyword.isEmpty() && text.contains(keyword.toLowerCase(Locale.getDefault()))) {
                        Long destination = forwardRules.get(keyword);
                        if (destination != null) {
                            AndroidUtilities.runOnUIThread(() ->
                                    SendMessagesHelper.getInstance(account).processForwardFromMyName(message, destination, 0, 0, null));
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }
}
