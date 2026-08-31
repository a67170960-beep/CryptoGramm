package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.CryptogramBadges;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CryptogramAdminActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private final Set<Long> developerIds = new HashSet<>();
    private final Set<Long> resourceIds = new HashSet<>();
    private final Set<Long> channelIds = new HashSet<>();

    private int rowCount;
    private int headerRow;
    private int developerHeaderRow;
    private int developerListStart;
    private int developerAddRow;
    private int resourceHeaderRow;
    private int resourceListStart;
    private int resourceAddRow;
    private int channelHeaderRow;
    private int channelListStart;
    private int channelAddRow;
    private int applyRow;
    private int infoRow;

    private int statsHeaderRow;
    private int statsRow;

    private int modulesHeaderRow;
    private int autoReplyToggleRow;
    private int autoForwardToggleRow;
    private int modulesInfoRow;

    private int toolsHeaderRow;
    private int forceUpdateRow;
    private int deletedMessagesStatsRow;
    private int toolsInfoRow;

    private final ArrayList<Long> developerListOrder = new ArrayList<>();
    private final ArrayList<Long> resourceListOrder = new ArrayList<>();
    private final ArrayList<Long> channelListOrder = new ArrayList<>();

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        if (!CryptogramBadges.isAdmin(UserConfig.getInstance(currentAccount).clientUserId)) {
            return false;
        }
        developerIds.addAll(CryptogramBadges.getDeveloperIdsSnapshot());
        resourceIds.addAll(CryptogramBadges.getResourceIdsSnapshot());
        channelIds.addAll(CryptogramBadges.getChannelIdsSnapshot());
        updateRows();
        return true;
    }

    private void updateRows() {
        rowCount = 0;
        headerRow = rowCount++;

        developerHeaderRow = rowCount++;
        developerListOrder.clear();
        developerListOrder.addAll(developerIds);
        developerListStart = rowCount;
        rowCount += developerListOrder.size();
        developerAddRow = rowCount++;

        resourceHeaderRow = rowCount++;
        resourceListOrder.clear();
        resourceListOrder.addAll(resourceIds);
        resourceListStart = rowCount;
        rowCount += resourceListOrder.size();
        resourceAddRow = rowCount++;

        channelHeaderRow = rowCount++;
        channelListOrder.clear();
        channelListOrder.addAll(channelIds);
        channelListStart = rowCount;
        rowCount += channelListOrder.size();
        channelAddRow = rowCount++;

        applyRow = rowCount++;
        infoRow = rowCount++;

        statsHeaderRow = rowCount++;
        statsRow = rowCount++;

        modulesHeaderRow = rowCount++;
        autoReplyToggleRow = rowCount++;
        autoForwardToggleRow = rowCount++;
        modulesInfoRow = rowCount++;

        toolsHeaderRow = rowCount++;
        forceUpdateRow = rowCount++;
        deletedMessagesStatsRow = rowCount++;
        toolsInfoRow = rowCount++;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Cryptogram Admin");
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
            if (position == developerAddRow) {
                showAddDialog(developerIds);
            } else if (position == resourceAddRow) {
                showAddDialog(resourceIds);
            } else if (position == channelAddRow) {
                showAddDialog(channelIds);
            } else if (position == applyRow) {
                openGithubWithChanges();
            } else if (position == autoReplyToggleRow) {
                org.telegram.messenger.CryptogramAutoReply.enabled = !org.telegram.messenger.CryptogramAutoReply.enabled;
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(org.telegram.messenger.CryptogramAutoReply.enabled);
                }
            } else if (position == autoForwardToggleRow) {
                org.telegram.messenger.CryptogramAutoForward.enabled = !org.telegram.messenger.CryptogramAutoForward.enabled;
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(org.telegram.messenger.CryptogramAutoForward.enabled);
                }
            } else if (position == forceUpdateRow) {
                org.telegram.messenger.CryptogramBadges.forceUpdateNow();
                if (getParentActivity() != null) {
                    org.telegram.ui.Components.BulletinFactory.of(this).createSimpleBulletin(R.raw.chats_infotip, "Список верификации обновляется...").show();
                }
            } else if (position >= developerListStart && position < developerListStart + developerListOrder.size()) {
                showRemoveDialog(developerIds, developerListOrder.get(position - developerListStart));
            } else if (position >= resourceListStart && position < resourceListStart + resourceListOrder.size()) {
                showRemoveDialog(resourceIds, resourceListOrder.get(position - resourceListStart));
            } else if (position >= channelListStart && position < channelListStart + channelListOrder.size()) {
                showRemoveDialog(channelIds, channelListOrder.get(position - channelListStart));
            }
        });

        return fragmentView;
    }

    private void showAddDialog(Set<Long> targetSet) {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        EditTextBoldCursor editText = new EditTextBoldCursor(context);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setTextSize(18);
        editText.setPadding(dp(16), dp(8), dp(16), dp(8));
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        editText.setHintText("User ID / Chat ID");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Добавить ID");
        builder.setView(layout);
        builder.setPositiveButton("Добавить", (dialogInterface, i) -> {
            String text = editText.getText().toString().trim();
            try {
                long id = Long.parseLong(text);
                targetSet.add(id);
                updateRows();
                listAdapter.notifyDataSetChanged();
            } catch (NumberFormatException ignored) {
            }
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void showRemoveDialog(Set<Long> targetSet, long id) {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Убрать " + id + "?");
        builder.setPositiveButton("Убрать", (dialogInterface, i) -> {
            targetSet.remove(id);
            updateRows();
            listAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void openGithubWithChanges() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("developer", new JSONArray(new ArrayList<>(developerIds)));
            obj.put("official_resource", new JSONArray(new ArrayList<>(resourceIds)));
            obj.put("official_channel", new JSONArray(new ArrayList<>(channelIds)));
            String jsonText = obj.toString(2);

            String base64 = Base64.encodeToString(jsonText.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            String encoded = URLEncoder.encode(base64, "UTF-8");

            String url = "https://github.com/a67170960-beep/CryptoGramm/edit/main/official_ids/list.json"
                    + "?value=" + encoded;

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ApplicationLoader.applicationContext.startActivity(intent);
        } catch (Exception ignored) {
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
            return position == developerAddRow || position == resourceAddRow || position == channelAddRow
                    || position == applyRow || position == autoReplyToggleRow || position == autoForwardToggleRow
                    || position == forceUpdateRow
                    || (position >= developerListStart && position < developerListStart + developerListOrder.size())
                    || (position >= resourceListStart && position < resourceListStart + resourceListOrder.size())
                    || (position >= channelListStart && position < channelListStart + channelListOrder.size());
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
                    view = new TextCell(mContext);
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    break;
                default:
                    view = new TextInfoPrivacyCell(mContext);
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
                    if (position == headerRow) {
                        headerCell.setText("Панель администратора Cryptogram");
                    } else if (position == developerHeaderRow) {
                        headerCell.setText("Официальные разработчики");
                    } else if (position == resourceHeaderRow) {
                        headerCell.setText("Официальные ресурсы");
                    } else if (position == channelHeaderRow) {
                        headerCell.setText("Официальные каналы");
                    } else if (position == statsHeaderRow) {
                        headerCell.setText("Статистика");
                    } else if (position == modulesHeaderRow) {
                        headerCell.setText("Модули автоматизации");
                    } else if (position == toolsHeaderRow) {
                        headerCell.setText("Инструменты");
                    }
                    break;
                }
                case 1: {
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == developerAddRow || position == resourceAddRow || position == channelAddRow) {
                        textCell.setText("Добавить ID", false);
                    } else if (position == applyRow) {
                        textCell.setText("Применить изменения на GitHub", false);
                    } else if (position == statsRow) {
                        int total = developerIds.size() + resourceIds.size() + channelIds.size();
                        textCell.setTextAndValue("Всего верифицированных", String.valueOf(total), false);
                    } else if (position >= developerListStart && position < developerListStart + developerListOrder.size()) {
                        textCell.setText(String.valueOf(developerListOrder.get(position - developerListStart)), false);
                    } else if (position >= resourceListStart && position < resourceListStart + resourceListOrder.size()) {
                        textCell.setText(String.valueOf(resourceListOrder.get(position - resourceListStart)), false);
                    } else if (position >= channelListStart && position < channelListStart + channelListOrder.size()) {
                        textCell.setText(String.valueOf(channelListOrder.get(position - channelListStart)), false);
                    } else if (position == forceUpdateRow) {
                        textCell.setText("Обновить список верификации сейчас", true);
                    } else if (position == deletedMessagesStatsRow) {
                        long bytes = org.telegram.messenger.CryptogramDeletedMessages.getLogSizeBytes();
                        String sizeText = bytes < 1024 * 1024
                                ? String.format(java.util.Locale.getDefault(), "%.1f КБ", bytes / 1024f)
                                : String.format(java.util.Locale.getDefault(), "%.2f МБ", bytes / (1024f * 1024f));
                        textCell.setTextAndValue("Размер журнала удалённых сообщений", sizeText, false);
                    }
                    break;
                }
                case 2: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == infoRow) {
                        cell.setText("Изменения применяются только после подтверждения на GitHub в открывшейся вкладке браузера.");
                    } else if (position == modulesInfoRow) {
                        cell.setText("Включение/выключение автоответчика и автопересылки для этого устройства. Подробные настройки — в Настройках Cryptogram.");
                    } else if (position == toolsInfoRow) {
                        cell.setText("Список верификации обычно обновляется раз в час автоматически — эта кнопка форсирует проверку немедленно.");
                    }
                    break;
                }
                case 3: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    if (position == autoReplyToggleRow) {
                        checkCell.setTextAndCheck("Автоответчик активен", org.telegram.messenger.CryptogramAutoReply.enabled, true);
                    } else if (position == autoForwardToggleRow) {
                        checkCell.setTextAndCheck("Автопересылка активна", org.telegram.messenger.CryptogramAutoForward.enabled, false);
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == headerRow || position == developerHeaderRow || position == resourceHeaderRow || position == channelHeaderRow
                    || position == statsHeaderRow || position == modulesHeaderRow || position == toolsHeaderRow) {
                return 0;
            } else if (position == infoRow || position == modulesInfoRow || position == toolsInfoRow) {
                return 2;
            } else if (position == autoReplyToggleRow || position == autoForwardToggleRow) {
                return 3;
            }
            return 1;
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        return new ArrayList<>();
    }
}
