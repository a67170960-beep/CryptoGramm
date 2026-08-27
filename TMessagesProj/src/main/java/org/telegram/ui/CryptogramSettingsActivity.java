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
            } else if (position == adminRow) {
                presentFragment(new CryptogramAdminActivity());
            }
        });

        return fragmentView;
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
                    }
                    break;
                }
                case 3: {
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == adminRow) {
                        textCell.setText("Панель администратора", false);
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == ghostModeHeaderRow || position == extraHeaderRow || position == adminHeaderRow) {
                return 0;
            } else if (position == ghostModeRow || position == ghostModeTypingRow || position == ghostModeReadStatusRow || position == groupReadReceiptsRow
                    || position == disableAutoplayVideoRow || position == disableAutoSaveMediaRow || position == hideLastSeenDateRow
                    || position == disableLinkPreviewGenerationRow || position == compactChatListRow) {
                return 1;
            } else if (position == ghostModeInfoRow || position == ghostModeTypingInfoRow || position == ghostModeReadStatusInfoRow
                    || position == groupReadReceiptsInfoRow || position == extraInfoRow) {
                return 2;
            } else if (position == adminRow) {
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
