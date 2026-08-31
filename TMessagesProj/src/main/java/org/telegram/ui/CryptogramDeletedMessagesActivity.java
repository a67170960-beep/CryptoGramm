/*
 * Исходный код Cryptogram для Android — форка Telegram для Android.
 * Распространяется по лицензии GNU GPL v. 2 или более поздней.
 * Копию лицензии вы должны были получить вместе с этим архивом (см. LICENSE).
 */

package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.CryptogramDeletedMessages;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Locale;

public class CryptogramDeletedMessagesActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int rowCount;
    private int enabledRow;
    private int infoRow;
    private int limitRow;
    private int sizeRow;
    private int clearRow;
    private int clearInfoRow;

    private void updateRows() {
        rowCount = 0;
        enabledRow = rowCount++;
        infoRow = rowCount++;
        limitRow = rowCount++;
        sizeRow = rowCount++;
        clearRow = rowCount++;
        clearInfoRow = rowCount++;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRows();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Удалённые сообщения");
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener((view, position) -> {
            if (position == enabledRow) {
                CryptogramDeletedMessages.enabled = !CryptogramDeletedMessages.enabled;
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CryptogramDeletedMessages.enabled);
                }
            } else if (position == limitRow) {
                showLimitDialog();
            } else if (position == clearRow) {
                showClearConfirmDialog();
            }
        });

        return fragmentView;
    }

    private void showLimitDialog() {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        EditTextBoldCursor editText = new EditTextBoldCursor(context);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setText(String.valueOf(CryptogramDeletedMessages.maxSizeMb));
        editText.setTextSize(16);
        editText.setPadding(dp(16), dp(8), dp(16), dp(8));
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Максимальный размер журнала (МБ)");
        builder.setView(layout);
        builder.setPositiveButton("Сохранить", (dialogInterface, i) -> {
            try {
                int value = Math.max(1, Integer.parseInt(editText.getText().toString().trim()));
                CryptogramDeletedMessages.maxSizeMb = value;
                listAdapter.notifyItemChanged(limitRow);
            } catch (NumberFormatException ignored) {
            }
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void showClearConfirmDialog() {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Очистить журнал?");
        builder.setMessage("Все сохранённые удалённые сообщения будут стёрты безвозвратно.");
        builder.setPositiveButton("Очистить", (dialogInterface, i) -> {
            CryptogramDeletedMessages.clearLog();
            listAdapter.notifyItemChanged(sizeRow);
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " Б";
        } else if (bytes < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.1f КБ", bytes / 1024f);
        } else {
            return String.format(Locale.getDefault(), "%.2f МБ", bytes / (1024f * 1024f));
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == enabledRow || position == limitRow || position == clearRow;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 1:
                    view = new TextCheckCell(mContext);
                    break;
                case 2:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
                default:
                    view = new TextCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 1: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    checkCell.setTextAndCheck("Сохранять удалённые сообщения", CryptogramDeletedMessages.enabled, true);
                    break;
                }
                case 2: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == infoRow) {
                        cell.setText("Когда сообщение удаляется (вами или собеседником), его текст сохраняется в файл на вашем устройстве, в папке \"Cryptogram\".");
                    } else if (position == clearInfoRow) {
                        cell.setText("Файл журнала: " + CryptogramDeletedMessages.getLogPath());
                    }
                    break;
                }
                default: {
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == limitRow) {
                        textCell.setTextAndValue("Лимит размера", CryptogramDeletedMessages.maxSizeMb + " МБ", true);
                    } else if (position == sizeRow) {
                        textCell.setTextAndValue("Текущий размер", formatSize(CryptogramDeletedMessages.getLogSizeBytes()), true);
                    } else if (position == clearRow) {
                        textCell.setText("Очистить журнал", false);
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == enabledRow) {
                return 1;
            } else if (position == infoRow || position == clearInfoRow) {
                return 2;
            }
            return 3;
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        return new ArrayList<>();
    }
}
