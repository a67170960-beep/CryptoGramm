package org.telegram.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CryptogramCaseStore;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

/** Local-only tools for text the user explicitly supplies. */
public class CryptogramOsintToolsActivity extends BaseFragment {
    private static final int MAX_VISIBLE_RESULTS = 100;
    private Context context;
    private LinearLayout content;
    private LinearLayout resultsContainer;
    private EditText sourceText;
    private JSONArray results = new JSONArray();

    public CryptogramOsintToolsActivity(int account) {
        setCurrentAccount(account);
    }

    @Override
    public View createView(Context ctx) {
        context = ctx;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.CryptogramOsintTools));
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

        addHeading(LocaleController.getString(R.string.CryptogramOsintTools));
        addParagraph(LocaleController.getString(R.string.CaseExtractDisclaimer));
        addSection(LocaleController.getString(R.string.CaseToolPasteHint));

        sourceText = new EditText(ctx);
        sourceText.setTextColor(Color.rgb(232, 238, 243));
        sourceText.setHintTextColor(Color.rgb(133, 148, 161));
        sourceText.setHint(LocaleController.getString(R.string.CaseToolPasteHint));
        sourceText.setTextSize(15);
        sourceText.setGravity(Gravity.TOP | Gravity.START);
        sourceText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        sourceText.setMinLines(6);
        sourceText.setMaxLines(12);
        sourceText.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(10), AndroidUtilities.dp(12), AndroidUtilities.dp(10));
        sourceText.setBackground(background(Color.rgb(25, 34, 43), Color.rgb(61, 77, 91)));
        content.addView(sourceText, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        addAction(R.string.CaseToolAnalyze, this::analyze);
        addAction(R.string.CaseToolClear, this::clear);

        addSection(LocaleController.getString(R.string.CaseToolResultsHeading));
        addParagraph(LocaleController.getString(R.string.CaseToolCopyHint));
        resultsContainer = new LinearLayout(ctx);
        resultsContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(resultsContainer, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        addAction(R.string.CryptogramCases, () -> presentFragment(new CryptogramCaseToolActivity(currentAccount)));
        fragmentView = scroll;
        return fragmentView;
    }

    @Override
    public void onFragmentDestroy() {
        if (sourceText != null) {
            sourceText.setText("");
        }
        results = new JSONArray();
        super.onFragmentDestroy();
    }

    private void analyze() {
        try {
            String text = sourceText == null ? "" : sourceText.getText().toString();
            if (text.length() > 200000) {
                Toast.makeText(context, LocaleController.getString(R.string.CaseToolInputTooLong), Toast.LENGTH_LONG).show();
                return;
            }
            results = CryptogramCaseStore.extractSelectedText(text);
            resultsContainer.removeAllViews();
            if (results.length() == 0) {
                addParagraph(resultsContainer, LocaleController.getString(R.string.CaseNoCandidates));
                return;
            }
            int visibleCount = Math.min(results.length(), MAX_VISIBLE_RESULTS);
            for (int i = 0; i < visibleCount; i++) {
                JSONObject item = results.optJSONObject(i);
                if (item != null) {
                    addResult(item.optString("type"), item.optString("value"));
                }
            }
            if (results.length() > visibleCount) {
                addParagraph(resultsContainer, String.format(java.util.Locale.getDefault(),
                        LocaleController.getString(R.string.CaseToolResultsTruncated), visibleCount, results.length()));
            }
        } catch (Exception e) {
            results = new JSONArray();
            resultsContainer.removeAllViews();
            addParagraph(resultsContainer, LocaleController.getString(R.string.CaseNoCandidates));
        }
    }

    private void addResult(String type, String value) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(10), AndroidUtilities.dp(12), AndroidUtilities.dp(10));
        row.setBackground(background(Color.rgb(29, 40, 51), Color.rgb(45, 61, 75)));

        TextView typeView = new TextView(context);
        typeView.setText(typeLabel(type));
        typeView.setTextColor(Color.rgb(151, 174, 190));
        typeView.setTextSize(12);
        row.addView(typeView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView valueView = new TextView(context);
        valueView.setText(value);
        valueView.setTextColor(Color.rgb(230, 236, 241));
        valueView.setTextSize(15);
        valueView.setPadding(0, AndroidUtilities.dp(3), 0, 0);
        row.addView(valueView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        row.setContentDescription(typeLabel(type) + ": " + value);
        row.setOnClickListener(view -> copy(value));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = AndroidUtilities.dp(6);
        resultsContainer.addView(row, params);
    }

    private String typeLabel(String type) {
        if ("phone".equals(type)) return LocaleController.getString(R.string.CaseToolPhone);
        if ("email".equals(type)) return LocaleController.getString(R.string.CaseTypeEmail);
        if ("url".equals(type)) return LocaleController.getString(R.string.CaseToolUrl);
        if ("username".equals(type)) return LocaleController.getString(R.string.CaseTypeUsername);
        if ("crypto_address".equals(type)) return LocaleController.getString(R.string.CaseToolCryptoAddress);
        return type;
    }

    private void copy(String value) {
        Object service = context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (service instanceof ClipboardManager) {
            ((ClipboardManager) service).setPrimaryClip(ClipData.newPlainText("OSINT candidate", value));
            Toast.makeText(context, LocaleController.getString(R.string.CaseToolCopied), Toast.LENGTH_SHORT).show();
        }
    }

    private void clear() {
        sourceText.setText("");
        results = new JSONArray();
        resultsContainer.removeAllViews();
    }

    private void addHeading(String value) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextColor(Color.rgb(232, 238, 243));
        view.setTextSize(21);
        view.setPadding(0, AndroidUtilities.dp(6), 0, AndroidUtilities.dp(8));
        content.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void addSection(String value) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextColor(Color.rgb(143, 161, 175));
        view.setTextSize(12);
        view.setPadding(0, AndroidUtilities.dp(16), 0, AndroidUtilities.dp(6));
        content.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void addParagraph(String value) {
        addParagraph(content, value);
    }

    private void addParagraph(LinearLayout parent, String value) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextColor(Color.rgb(165, 179, 191));
        view.setTextSize(14);
        view.setPadding(0, AndroidUtilities.dp(4), 0, AndroidUtilities.dp(8));
        parent.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
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
