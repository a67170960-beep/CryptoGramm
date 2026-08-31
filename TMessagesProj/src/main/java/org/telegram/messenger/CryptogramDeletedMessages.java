/*
 * Хранение удалённых сообщений Cryptogram. Когда собеседник (или вы сами)
 * удаляет сообщение, оно не пропадает бесследно — перед реальным удалением
 * из локальной базы Telegram мы сохраняем его текст в свою собственную
 * папку на диске приложения ("Cryptogram/deleted_messages"), в простом
 * текстовом журнале, а не в полноценной базе данных — это проще и быстрее
 * встроить, но полностью решает исходную задачу "сообщение не пропадает".
 *
 * Пользователь сам управляет лимитом размера журнала и может его очистить —
 * см. настройки, экран "Удалённые сообщения".
 */
package org.telegram.messenger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CryptogramDeletedMessages {

    public static boolean enabled = false;
    // Лимит размера журнала в мегабайтах — пользователь настраивает сам.
    public static int maxSizeMb = 10;

    private static final String FOLDER_NAME = "Cryptogram";
    private static final String FILE_NAME = "deleted_messages.txt";

    public static void onMessageDeleted(long dialogId, long messageId, String senderName, String text, boolean isMedia) {
        if (!enabled || (text == null || text.isEmpty()) && !isMedia) {
            return;
        }
        Utilities.globalQueue.postRunnable(() -> {
            try {
                File file = getLogFile();
                if (file == null) {
                    return;
                }
                enforceSizeLimit(file);

                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                StringBuilder line = new StringBuilder();
                line.append("[").append(timestamp).append("] ");
                line.append("chat=").append(dialogId).append(" ");
                line.append("msg_id=").append(messageId).append(" ");
                if (senderName != null && !senderName.isEmpty()) {
                    line.append("from=").append(senderName.replace('\n', ' ')).append(" ");
                }
                if (isMedia) {
                    line.append(": [медиафайл удалён]");
                } else {
                    line.append(": ").append(text.replace('\n', ' '));
                }
                line.append('\n');

                try (FileWriter writer = new FileWriter(file, true)) {
                    writer.write(line.toString());
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
    }

    private static File getLogFile() {
        File externalDir = ApplicationLoader.applicationContext.getExternalFilesDir(null);
        if (externalDir == null) {
            return null;
        }
        File folder = new File(externalDir, FOLDER_NAME);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder, FILE_NAME);
    }

    // Если журнал превысил заданный пользователем лимит — обрезаем самую
    // старую часть файла (первую половину), чтобы не расти бесконечно.
    private static void enforceSizeLimit(File file) {
        try {
            if (!file.exists()) {
                return;
            }
            long maxBytes = (long) maxSizeMb * 1024 * 1024;
            if (file.length() <= maxBytes) {
                return;
            }
            byte[] content;
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                long keepFrom = file.length() / 2;
                raf.seek(keepFrom);
                content = new byte[(int) (file.length() - keepFrom)];
                raf.readFully(content);
            }
            // отрезаем до первой полной строки, чтобы не обрывать запись на середине
            int firstNewline = -1;
            for (int i = 0; i < content.length; i++) {
                if (content[i] == '\n') {
                    firstNewline = i;
                    break;
                }
            }
            String remaining = firstNewline >= 0
                    ? new String(content, firstNewline + 1, content.length - firstNewline - 1, StandardCharsets.UTF_8)
                    : new String(content, StandardCharsets.UTF_8);
            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write("[журнал обрезан из-за лимита размера]\n");
                writer.write(remaining);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static void clearLog() {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                File file = getLogFile();
                if (file != null && file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
    }

    public static long getLogSizeBytes() {
        File file = getLogFile();
        return file != null && file.exists() ? file.length() : 0;
    }

    public static String getLogPath() {
        File file = getLogFile();
        return file != null ? file.getAbsolutePath() : "";
    }
}
