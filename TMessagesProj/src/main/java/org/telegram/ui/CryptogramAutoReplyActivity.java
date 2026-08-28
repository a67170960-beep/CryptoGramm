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

import org.telegram.messenger.CryptogramAutoReply;
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
import java.util.Map;

public class CryptogramAutoReplyActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int rowCount;
    private int enabledRow;
    private int infoRow;
    private int replyToPrivateRow;
    private int replyToGroupsRow;
    private int defaultTextRow;
    private int delayRow;
    private int keywordsHeaderRow;
    private int addKeywordRow;
    private int keywordsListStart;
    private int keywordsInfoRow;

    private final ArrayList<String> keywordOrder = new ArrayList<>();

    private void updateRows() {
        rowCount = 0;
        enabledRow = rowCount++;
        infoRow = rowCount++;
        replyToPrivateRow = rowCount++;
        replyToGroupsRow = rowCount++;
        defaultTextRow = rowCount++;
        delayRow = rowCount++;

        keywordsHeaderRow = rowCount++;
        addKeywordRow = rowCount++;
        keywordOrder.clear();
        keywordOrder.addAll(CryptogramAutoReply.keywordReplies.keySet());
        keywordsListStart = rowCount;
        rowCount += keywordOrder.size();
        keywordsInfoRow = rowCount++;
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
        actionBar.setTitle("Автоответчик");
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
                CryptogramAutoReply.enabled = !CryptogramAutoReply.enabled;
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CryptogramAutoReply.enabled);
                }
            } else if (position == replyToPrivateRow) {
                CryptogramAutoReply.replyToPrivate = !CryptogramAutoReply.replyToPrivate;
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CryptogramAutoReply.replyToPrivate);
                }
            } else if (position == replyToGroupsRow) {
                CryptogramAutoReply.replyToGroups = !CryptogramAutoReply.replyToGroups;
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CryptogramAutoReply.replyToGroups);
                }
            } else if (position == defaultTextRow) {
                showEditTextDialog("Текст ответа по умолчанию", CryptogramAutoReply.defaultReplyText, text -> {
                    CryptogramAutoReply.defaultReplyText = text;
                    listAdapter.notifyItemChanged(defaultTextRow);
                });
            } else if (position == delayRow) {
                showEditNumberDialog("Задержка перед ответом (сек.)", String.valueOf(CryptogramAutoReply.delaySeconds), value -> {
                    try {
                        CryptogramAutoReply.delaySeconds = Math.max(0, Integer.parseInt(value));
                    } catch (NumberFormatException ignored) {
                    }
                    listAdapter.notifyItemChanged(delayRow);
                });
            } else if (position == addKeywordRow) {
                showAddKeywordDialog();
            } else if (position >= keywordsListStart && position < keywordsListStart + keywordOrder.size()) {
                String keyword = keywordOrder.get(position - keywordsListStart);
                showRemoveKeywordDialog(keyword);
            }
        });

        return fragmentView;
    }

    private interface TextResultCallback {
        void onResult(String text);
    }

    private void showEditTextDialog(String title, String currentValue, TextResultCallback callback) {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        EditTextBoldCursor editText = new EditTextBoldCursor(context);
        editText.setText(currentValue);
        editText.setTextSize(16);
        editText.setPadding(dp(16), dp(8), dp(16), dp(8));
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(layout);
        builder.setPositiveButton("Сохранить", (dialogInterface, i) -> callback.onResult(editText.getText().toString()));
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void showEditNumberDialog(String title, String currentValue, TextResultCallback callback) {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        EditTextBoldCursor editText = new EditTextBoldCursor(context);
        editText.setText(currentValue);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setTextSize(16);
        editText.setPadding(dp(16), dp(8), dp(16), dp(8));
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(layout);
        builder.setPositiveButton("Сохранить", (dialogInterface, i) -> callback.onResult(editText.getText().toString()));
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void showAddKeywordDialog() {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        EditTextBoldCursor keywordEdit = new EditTextBoldCursor(context);
        keywordEdit.setHintText("Ключевое слово");
        keywordEdit.setTextSize(16);
        keywordEdit.setPadding(dp(16), dp(8), dp(16), dp(8));
        keywordEdit.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));

        EditTextBoldCursor replyEdit = new EditTextBoldCursor(context);
        replyEdit.setHintText("Ответ на это слово");
        replyEdit.setTextSize(16);
        replyEdit.setPadding(dp(16), dp(8), dp(16), dp(8));
        replyEdit.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(keywordEdit, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        layout.addView(replyEdit, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Ответ по ключевому слову");
        builder.setView(layout);
        builder.setPositiveButton("Добавить", (dialogInterface, i) -> {
            String keyword = keywordEdit.getText().toString().trim();
            String reply = replyEdit.getText().toString().trim();
            if (!keyword.isEmpty() && !reply.isEmpty()) {
                CryptogramAutoReply.keywordReplies.put(keyword, reply);
                updateRows();
                listAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void showRemoveKeywordDialog(String keyword) {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Удалить ключевое слово \"" + keyword + "\"?");
        builder.setPositiveButton("Удалить", (dialogInterface, i) -> {
            CryptogramAutoReply.keywordReplies.remove(keyword);
            updateRows();
            listAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == enabledRow || position == replyToPrivateRow || position == replyToGroupsRow
                    || position == defaultTextRow || position == delayRow || position == addKeywordRow
                    || (position >= keywordsListStart && position < keywordsListStart + keywordOrder.size());
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new HeaderCell(mContext);
                    break;
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
                case 0: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == keywordsHeaderRow) {
                        headerCell.setText("Ответы по ключевым словам");
                    }
                    break;
                }
                case 1: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    if (position == enabledRow) {
                        checkCell.setTextAndCheck("Включить автоответчик", CryptogramAutoReply.enabled, true);
                    } else if (position == replyToPrivateRow) {
                        checkCell.setTextAndCheck("Отвечать в личных чатах", CryptogramAutoReply.replyToPrivate, true);
                    } else if (position == replyToGroupsRow) {
                        checkCell.setTextAndCheck("Отвечать в группах", CryptogramAutoReply.replyToGroups, false);
                    }
                    break;
                }
                case 2: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == infoRow) {
                        cell.setText("Автоматически отвечает на входящие сообщения. Ответ по ключевому слову имеет приоритет над ответом по умолчанию.");
                    } else if (position == keywordsInfoRow) {
                        cell.setText("Если текст входящего сообщения содержит одно из ключевых слов — будет отправлен соответствующий ответ вместо ответа по умолчанию.");
                    }
                    break;
                }
                default: {
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == defaultTextRow) {
                        textCell.setTextAndValue("Ответ по умолчанию", CryptogramAutoReply.defaultReplyText, true);
                    } else if (position == delayRow) {
                        textCell.setTextAndValue("Задержка перед ответом", CryptogramAutoReply.delaySeconds + " сек.", true);
                    } else if (position == addKeywordRow) {
                        textCell.setText("Добавить ключевое слово", keywordOrder.isEmpty());
                    } else if (position >= keywordsListStart && position < keywordsListStart + keywordOrder.size()) {
                        String keyword = keywordOrder.get(position - keywordsListStart);
                        boolean isLast = position == keywordsListStart + keywordOrder.size() - 1;
                        textCell.setTextAndValue(keyword, CryptogramAutoReply.keywordReplies.get(keyword), !isLast);
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == keywordsHeaderRow) {
                return 0;
            } else if (position == enabledRow || position == replyToPrivateRow || position == replyToGroupsRow) {
                return 1;
            } else if (position == infoRow || position == keywordsInfoRow) {
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
