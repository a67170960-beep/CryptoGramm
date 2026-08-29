/*
 * Реальные UI-эффекты Cryptogram: вибрация и звук при отправке/получении
 * сообщений. Подписывается на события через NotificationCenter, не трогая
 * основной код отправки/получения сообщений.
 */
package org.telegram.messenger;

import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class CryptogramUIEffects implements NotificationCenter.NotificationCenterDelegate {

    private static volatile CryptogramUIEffects instance;
    private final int currentAccount;

    private CryptogramUIEffects(int account) {
        currentAccount = account;
    }

    public static CryptogramUIEffects getInstance(int account) {
        if (instance == null) {
            synchronized (CryptogramUIEffects.class) {
                if (instance == null) {
                    instance = new CryptogramUIEffects(account);
                }
            }
        }
        return instance;
    }

    public void start() {
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.messageReceivedByServer);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didReceiveNewMessages);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        try {
            if (id == NotificationCenter.messageReceivedByServer) {
                if (SharedConfig.vibrationOnMessageEnabled) {
                    doVibrate(20);
                }
                if (SharedConfig.uiSoundsEnabled) {
                    playTone(true);
                }
            } else if (id == NotificationCenter.didReceiveNewMessages) {
                boolean scheduled = args.length > 2 && Boolean.TRUE.equals(args[2]);
                if (scheduled) {
                    return;
                }
                if (SharedConfig.vibrationOnMessageEnabled) {
                    doVibrate(35);
                }
                if (SharedConfig.uiSoundsEnabled) {
                    playTone(false);
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void doVibrate(long durationMs) {
        try {
            Vibrator vibrator = AndroidUtilities.getVibrator();
            if (vibrator == null || !vibrator.hasVibrator()) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(durationMs);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void playTone(boolean outgoing) {
        try {
            android.media.ToneGenerator toneGenerator = new android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_NOTIFICATION, 60);
            toneGenerator.startTone(outgoing ? android.media.ToneGenerator.TONE_PROP_BEEP : android.media.ToneGenerator.TONE_PROP_BEEP2, 80);
            AndroidUtilities.runOnUIThread(toneGenerator::release, 200);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }
}
