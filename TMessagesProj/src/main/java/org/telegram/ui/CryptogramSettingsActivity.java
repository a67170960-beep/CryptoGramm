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

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
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
            return position == ghostModeRow || position == ghostModeTypingRow || position == ghostModeReadStatusRow || position == groupReadReceiptsRow;
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
                        checkCell.setTextAndCheck(LocaleController.getString(R.string.CryptogramDisableReadReceiptsInGroups), SharedConfig.ghostModeGroupReadReceipts, false);
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
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == ghostModeHeaderRow) {
                return 0;
            } else if (position == ghostModeRow || position == ghostModeTypingRow || position == ghostModeReadStatusRow || position == groupReadReceiptsRow) {
                return 1;
            } else if (position == ghostModeInfoRow || position == ghostModeTypingInfoRow || position == ghostModeReadStatusInfoRow || position == groupReadReceiptsInfoRow) {
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
