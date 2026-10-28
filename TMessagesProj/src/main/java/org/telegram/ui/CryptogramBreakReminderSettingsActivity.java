package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CryptogramBreakReminder;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

public class CryptogramBreakReminderSettingsActivity extends BaseFragment {
    private Context context;
    private LinearLayout content;

    public CryptogramBreakReminderSettingsActivity(int account) {
        setCurrentAccount(account);
    }

    @Override
    public View createView(Context ctx) {
        context = ctx;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.CryptogramBreakTitle));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        ScrollView scroll = new ScrollView(ctx);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(20, 27, 34));
        content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(24));
        scroll.addView(content, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        fragmentView = scroll;
        render();
        return fragmentView;
    }

    private void render() {
        content.removeAllViews();
        addHeading(LocaleController.getString(R.string.CryptogramBreakTitle));
        addParagraph(LocaleController.getString(R.string.CryptogramBreakInfo));

        String enabled = LocaleController.getString(CryptogramBreakReminder.isEnabled(context) ? R.string.CryptogramBreakOn : R.string.CryptogramBreakOff);
        addSetting(R.string.CryptogramBreakEnabled, enabled, () -> {
            CryptogramBreakReminder.setEnabled(context, !CryptogramBreakReminder.isEnabled(context));
            changed();
        });

        addSetting(R.string.CryptogramBreakInterval, minutes(CryptogramBreakReminder.getIntervalMinutes(context)), this::chooseInterval);
        addSetting(R.string.CryptogramBreakSnoozeInterval, minutes(CryptogramBreakReminder.getSnoozeMinutes(context)), this::chooseSnooze);
        addSetting(R.string.CryptogramBreakMessage, CryptogramBreakReminder.getMessage(context), this::editMessage);
        addAction(R.string.CryptogramBreakTest, this::testReminder);
        addAction(R.string.CryptogramNotificationSettings, this::openNotifications);
    }

    private void changed() {
        CryptogramBreakReminder.onSettingsChanged(context);
        render();
    }

    private String minutes(int value) {
        return String.format(java.util.Locale.getDefault(), LocaleController.getString(R.string.CryptogramBreakMinutesFormat), value);
    }

    private void chooseInterval() {
        int[] values = new int[] { 30, 45, 60, 90, 120 };
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = minutes(values[i]);
        }
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CryptogramBreakInterval))
                .setItems(labels, (dialog, which) -> {
                    CryptogramBreakReminder.setIntervalMinutes(context, values[which]);
                    changed();
                })
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .show();
    }

    private void chooseSnooze() {
        int[] values = new int[] { 5, 10, 15, 20, 30 };
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = minutes(values[i]);
        }
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CryptogramBreakSnoozeInterval))
                .setItems(labels, (dialog, which) -> {
                    CryptogramBreakReminder.setSnoozeMinutes(context, values[which]);
                    changed();
                })
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .show();
    }

    private void editMessage() {
        EditText editor = new EditText(context);
        editor.setText(CryptogramBreakReminder.getMessage(context));
        editor.setTextSize(16);
        editor.setSingleLine(false);
        editor.setMinLines(2);
        editor.setMaxLines(4);
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editor.setFilters(new InputFilter[] { new InputFilter.LengthFilter(160) });
        editor.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(10), AndroidUtilities.dp(16), AndroidUtilities.dp(10));
        new android.app.AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.CryptogramBreakMessage))
                .setView(editor)
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .setNeutralButton(LocaleController.getString(R.string.CryptogramBreakResetMessage), (dialog, which) -> {
                    CryptogramBreakReminder.setMessage(context, "");
                    changed();
                })
                .setPositiveButton(LocaleController.getString(R.string.Done), (dialog, which) -> {
                    CryptogramBreakReminder.setMessage(context, editor.getText().toString());
                    changed();
                })
                .show();
    }

    private void testReminder() {
        Activity activity = getParentActivity();
        if (activity instanceof LaunchActivity) {
            CryptogramBreakReminder.showTestReminder((LaunchActivity) activity);
        }
    }

    private void openNotifications() {
        NotificationsSettingsActivity activity = new NotificationsSettingsActivity();
        activity.setCurrentAccount(currentAccount);
        presentFragment(activity);
    }

    private void addHeading(String value) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextColor(Color.rgb(232, 238, 243));
        view.setTextSize(21);
        view.setPadding(0, AndroidUtilities.dp(6), 0, AndroidUtilities.dp(8));
        content.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void addParagraph(String value) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextColor(Color.rgb(165, 179, 191));
        view.setTextSize(14);
        view.setPadding(0, AndroidUtilities.dp(4), 0, AndroidUtilities.dp(10));
        content.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void addSetting(int label, String value, Runnable action) {
        TextView row = new TextView(context);
        row.setText(LocaleController.getString(label) + "\n" + value);
        row.setTextColor(Color.rgb(225, 233, 239));
        row.setTextSize(15);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(12), AndroidUtilities.dp(14), AndroidUtilities.dp(12));
        row.setBackground(background(Color.rgb(29, 40, 51), Color.rgb(45, 61, 75)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = AndroidUtilities.dp(7);
        content.addView(row, params);
        row.setOnClickListener(view -> action.run());
    }

    private void addAction(int label, Runnable action) {
        TextView button = new TextView(context);
        button.setText(LocaleController.getString(label));
        button.setTextColor(Color.rgb(225, 233, 239));
        button.setTextSize(15);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(12), AndroidUtilities.dp(14), AndroidUtilities.dp(12));
        button.setBackground(background(Color.rgb(39, 51, 63), Color.rgb(58, 74, 88)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = AndroidUtilities.dp(7);
        content.addView(button, params);
        button.setOnClickListener(view -> action.run());
    }

    private GradientDrawable background(int fill, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(AndroidUtilities.dp(7));
        drawable.setStroke(AndroidUtilities.dp(1), stroke);
        return drawable;
    }
}
