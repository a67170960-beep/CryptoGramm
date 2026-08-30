/*
 * Исходный код Cryptogram для Android — форка Telegram для Android.
 * Распространяется по лицензии GNU GPL v. 2 или более поздней.
 * Копию лицензии вы должны были получить вместе с этим архивом (см. LICENSE).
 */

package org.telegram.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.CryptogramBadges;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class CryptogramSettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    @SuppressWarnings("FieldCanBeLocal")
    private LinearLayoutManager layoutManager;
    private ListAdapter listAdapter;

    private int rowCount;
    private int ghostModeHeaderRow;
    private int ghostModeRow;
    private int ghostModeInfoRow;
    private int ghostModeTypingRow;
    private int ghostModeTypingInfoRow;
    private int ghostModeReadStatusRow;
    private int ghostModeReadStatusInfoRow;
    private int groupReadReceiptsRow;
    private int groupReadReceiptsInfoRow;

    private int extraHeaderRow;
    private int disableAutoplayVideoRow;
    private int disableAutoSaveMediaRow;
    private int hideLastSeenDateRow;
    private int disableLinkPreviewGenerationRow;
    private int compactChatListRow;
    private int extraInfoRow;

    private int sectionsHeaderRow;
    private int uiSettingsRow;
    private int autoReplyRow;
    private int clockNameRow;
    private int readAllRow;

    private int aboutHeaderRow;
    private int aboutDeveloperRow;
    private int aboutChannelRow;
    private int aboutReleasesRow;
    private int aboutInfoRow;

    private int adminHeaderRow;
    private int adminRow;

    private void updateRows() {
        rowCount = 0;
        ghostModeHeaderRow = rowCount++;
        ghostModeRow = rowCount++;
        ghostModeInfoRow = rowCount++;
        ghostModeTypingRow = rowCount++;
        ghostModeTypingInfoRow = rowCount++;
        ghostModeReadStatusRow = rowCount++;
        ghostModeReadStatusInfoRow = rowCount++;
        groupReadReceiptsRow = rowCount++;
        groupReadReceiptsInfoRow = rowCount++;

        extraHeaderRow = rowCount++;
        disableAutoplayVideoRow = rowCount++;
        disableAutoSaveMediaRow = rowCount++;
        hideLastSeenDateRow = rowCount++;
        disableLinkPreviewGenerationRow = rowCount++;
        compactChatListRow = rowCount++;
        extraInfoRow = rowCount++;

        sectionsHeaderRow = rowCount++;
        uiSettingsRow = rowCount++;
        autoReplyRow = rowCount++;
        clockNameRow = rowCount++;
        readAllRow = rowCount++;

        aboutHeaderRow = rowCount++;
        aboutDeveloperRow = rowCount++;
        aboutChannelRow = rowCount++;
        aboutReleasesRow = rowCount++;
        aboutInfoRow = rowCount++;

        if (CryptogramBadges.isAdmin(UserConfig.getInstance(currentAccount).clientUserId)) {
            adminHeaderRow = rowCount++;
            adminRow = rowCount++;
        } else {
            adminHeaderRow = -1;
            adminRow = -1;
        }
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
        actionBar.setTitle(LocaleController.getString(R.string.CryptogramSettings));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        if (parentLayout != null && parentLayout.isRightLayout()) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_close);
        }

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        actionBar.setAdaptiveBackground(listView);

        listView.setOnItemClickListener((view, position) -> {
            if (position == ghostModeRow) {
                SharedConfig.toggleGhostMode();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.ghostMode);
                }
            } else if (position == ghostModeTypingRow) {
                SharedConfig.toggleGhostModeTyping();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.ghostModeTyping);
                }
            } else if (position == ghostModeReadStatusRow) {
                SharedConfig.toggleGhostModeReadStatus();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.ghostModeReadStatus);
                }
            } else if (position == groupReadReceiptsRow) {
                SharedConfig.toggleGhostModeGroupReadReceipts();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.ghostModeGroupReadReceipts);
                }
            } else if (position == disableAutoplayVideoRow) {
                SharedConfig.toggleDisableAutoplayVideo();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.disableAutoplayVideo);
                }
            } else if (position == disableAutoSaveMediaRow) {
                SharedConfig.toggleDisableAutoSaveMedia();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.disableAutoSaveMedia);
                }
            } else if (position == hideLastSeenDateRow) {
                SharedConfig.toggleHideLastSeenDate();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.hideLastSeenDate);
                }
            } else if (position == disableLinkPreviewGenerationRow) {
                SharedConfig.toggleDisableLinkPreviewGeneration();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.disableLinkPreviewGeneration);
                }
            } else if (position == compactChatListRow) {
                SharedConfig.toggleCompactChatList();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.compactChatList);
                }
            } else if (position == uiSettingsRow) {
                presentFragment(new CryptogramUISettingsActivity());
            } else if (position == autoReplyRow) {
                presentFragment(new CryptogramAutoReplyActivity());
            } else if (position == clockNameRow) {
                presentFragment(new CryptogramClockNameActivity());
            } else if (position == readAllRow) {
                showReadAllDialog();
            } else if (position == aboutDeveloperRow) {
                Browser.openUrl(getParentActivity(), "https://t.me/crypto5312");
            } else if (position == aboutChannelRow) {
                Browser.openUrl(getParentActivity(), "https://t.me/Cryptogram_offcial");
            } else if (position == aboutReleasesRow) {
                Browser.openUrl(getParentActivity(), "https://t.me/Cryptogram_Releases");
            } else if (position == adminRow) {
                presentFragment(new CryptogramAdminActivity());
            }
        });

        return fragmentView;
    }

    private void showReadAllDialog() {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        String[] items = {
                "Прочитать всё",
                "Только личные чаты",
                "Только группы",
                "Только каналы",
                "Только чаты с ботами"
        };
        org.telegram.ui.ActionBar.AlertDialog.Builder builder = new org.telegram.ui.ActionBar.AlertDialog.Builder(context);
        builder.setTitle("Отметить как прочитанное");
        builder.setItems(items, (dialogInterface, which) -> {
            if (which == 0) {
                getMessagesStorage().readAllDialogs(-1);
            } else {
                readAllDialogsFiltered(which);
            }
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    // which: 1 = личные чаты, 2 = группы, 3 = каналы, 4 = боты
    private void readAllDialogsFiltered(int which) {
        java.util.ArrayList<org.telegram.tgnet.TLRPC.Dialog> dialogs = getMessagesController().getAllDialogs();
        for (org.telegram.tgnet.TLRPC.Dialog dialog : dialogs) {
            if (dialog == null || dialog.unread_count <= 0) {
                continue;
            }
            long dialogId = dialog.id;
            if (org.telegram.messenger.DialogObject.isEncryptedDialog(dialogId)) {
                continue;
            }
            boolean isChatOrChannel = org.telegram.messenger.DialogObject.isChatDialog(dialogId);
            boolean matches = false;
            if (which == 1 && !isChatOrChannel) {
                org.telegram.tgnet.TLRPC.User user = getMessagesController().getUser(dialogId);
                matches = user != null && !user.bot;
            } else if (which == 4 && !isChatOrChannel) {
                org.telegram.tgnet.TLRPC.User user = getMessagesController().getUser(dialogId);
                matches = user != null && user.bot;
            } else if ((which == 2 || which == 3) && isChatOrChannel) {
                org.telegram.tgnet.TLRPC.Chat chat = getMessagesController().getChat(-dialogId);
                if (chat != null) {
                    boolean isChannel = org.telegram.messenger.ChatObject.isChannel(chat) && !chat.megagroup;
                    matches = (which == 3) == isChannel;
                }
            }
            if (matches) {
                getMessagesController().markDialogAsRead(dialogId, dialog.top_message, 0, dialog.last_message_date, false, 0, dialog.unread_count, true, 0);
            }
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == ghostModeRow || position == ghostModeTypingRow || position == ghostModeReadStatusRow || position == groupReadReceiptsRow
                    || position == disableAutoplayVideoRow || position == disableAutoSaveMediaRow || position == hideLastSeenDateRow
                    || position == disableLinkPreviewGenerationRow || position == compactChatListRow
                    || position == uiSettingsRow || position == autoReplyRow || position == clockNameRow || position == readAllRow
                    || position == aboutDeveloperRow || position == aboutChannelRow || position == aboutReleasesRow
                    || position == adminRow;
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
                case 3:
                    view = new TextCell(mContext);
                    break;
                default:
                    view = new ShadowSectionCell(mContext);
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
                    if (position == ghostModeHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.CryptogramGhostModeHeader));
                    } else if (position == extraHeaderRow) {
                        headerCell.setText("Дополнительно");
                    } else if (position == sectionsHeaderRow) {
                        headerCell.setText("Разделы");
                    } else if (position == aboutHeaderRow) {
                        headerCell.setText("О Cryptogram");
                    } else if (position == adminHeaderRow) {
                        headerCell.setText("Администрирование");
                    }
                    break;
                }
                case 1: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    if (position == ghostModeRow) {
                        checkCell.setTextAndCheck(LocaleController.getString(R.string.CryptogramGhostMode), SharedConfig.ghostMode, true);
                    } else if (position == ghostModeTypingRow) {
                        checkCell.setTextAndCheck(LocaleController.getString(R.string.CryptogramGhostModeTyping), SharedConfig.ghostModeTyping, true);
                    } else if (position == ghostModeReadStatusRow) {
                        checkCell.setTextAndCheck(LocaleController.getString(R.string.CryptogramGhostModeReadStatus), SharedConfig.ghostModeReadStatus, true);
                    } else if (position == groupReadReceiptsRow) {
                        checkCell.setTextAndCheck(LocaleController.getString(R.string.CryptogramDisableReadReceiptsInGroups), SharedConfig.ghostModeGroupReadReceipts, true);
                    } else if (position == disableAutoplayVideoRow) {
                        checkCell.setTextAndCheck("Отключить автовоспроизведение видео и GIF", SharedConfig.disableAutoplayVideo, true);
                    } else if (position == disableAutoSaveMediaRow) {
                        checkCell.setTextAndCheck("Не сохранять медиа в галерею автоматически", SharedConfig.disableAutoSaveMedia, true);
                    } else if (position == hideLastSeenDateRow) {
                        checkCell.setTextAndCheck("Скрыть точную дату последнего захода", SharedConfig.hideLastSeenDate, true);
                    } else if (position == disableLinkPreviewGenerationRow) {
                        checkCell.setTextAndCheck("Не генерировать превью ссылок", SharedConfig.disableLinkPreviewGeneration, true);
                    } else if (position == compactChatListRow) {
                        checkCell.setTextAndCheck("Компактный список чатов", SharedConfig.compactChatList, false);
                    }
                    break;
                }
                case 2: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == ghostModeInfoRow) {
                        cell.setText(LocaleController.getString(R.string.CryptogramGhostModeInfo));
                    } else if (position == ghostModeTypingInfoRow) {
                        cell.setText(LocaleController.getString(R.string.CryptogramGhostModeTypingInfo));
                    } else if (position == ghostModeReadStatusInfoRow) {
                        cell.setText(LocaleController.getString(R.string.CryptogramGhostModeReadStatusInfo));
                    } else if (position == groupReadReceiptsInfoRow) {
                        cell.setText(LocaleController.getString(R.string.CryptogramDisableReadReceiptsInGroupsInfo));
                    } else if (position == extraInfoRow) {
                        cell.setText("Дополнительные функции Cryptogram, не связанные с Режимом Призрака.");
                    } else if (position == aboutInfoRow) {
                        cell.setText("Официальные ресурсы форка Cryptogram. Подписывайтесь на канал с релизами, чтобы не пропустить новые версии.");
                    }
                    break;
                }
                case 3: {
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == uiSettingsRow) {
                        textCell.setText("Внешний вид (UI)", true);
                    } else if (position == autoReplyRow) {
                        textCell.setText("Автоответчик", true);
                    } else if (position == clockNameRow) {
                        textCell.setText("Время в нике", true);
                    } else if (position == readAllRow) {
                        textCell.setText("Прочитать всё", true);
                    } else if (position == aboutDeveloperRow) {
                        textCell.setTextAndValue("Официальный разработчик", "@crypto5312", true);
                    } else if (position == aboutChannelRow) {
                        textCell.setTextAndValue("Официальный канал", "@Cryptogram_offcial", true);
                    } else if (position == aboutReleasesRow) {
                        textCell.setTextAndValue("Релизы", "@Cryptogram_Releases", true);
                    } else if (position == adminRow) {
                        textCell.setText("Панель администратора", false);
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == ghostModeHeaderRow || position == extraHeaderRow || position == sectionsHeaderRow || position == aboutHeaderRow || position == adminHeaderRow) {
                return 0;
            } else if (position == ghostModeRow || position == ghostModeTypingRow || position == ghostModeReadStatusRow || position == groupReadReceiptsRow
                    || position == disableAutoplayVideoRow || position == disableAutoSaveMediaRow || position == hideLastSeenDateRow
                    || position == disableLinkPreviewGenerationRow || position == compactChatListRow) {
                return 1;
            } else if (position == ghostModeInfoRow || position == ghostModeTypingInfoRow || position == ghostModeReadStatusInfoRow
                    || position == groupReadReceiptsInfoRow || position == extraInfoRow || position == aboutInfoRow) {
                return 2;
            } else if (position == uiSettingsRow || position == autoReplyRow || position == clockNameRow || position == readAllRow
                    || position == aboutDeveloperRow || position == aboutChannelRow || position == aboutReleasesRow
                    || position == adminRow) {
                return 3;
            }
            return 4;
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        return new ArrayList<>();
    }
}
