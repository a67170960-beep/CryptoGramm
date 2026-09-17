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

import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.CryptogramHeroCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SeekBarView;

import java.util.ArrayList;

public class CryptogramUISettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int rowCount;
    private int heroRow;
    private int presetRow;
    private int resetRow;

    private int blurHeaderRow;
    private int blurEnabledRow;
    private int blurIntensityRow;
    private int blurInfoRow;

    private int animHeaderRow;
    private int animEnabledRow;
    private int animSpeedRow;

    private int bubbleHeaderRow;
    private int roundedBubblesRow;
    private int bubbleRoundnessRow;

    private int miscHeaderRow;
    private int vibrationRow;
    private int uiSoundsRow;
    private int largeAvatarsRow;
    private int hideNavLabelsRow;
    private int compactChatListRow;
    private int snowEffectRow;
    private int timeFormatRow;
    private int timeSecondsRow;
    private int timeColorRow;
    private int timeSizeRow;
    private int checkStyleRow;
    private int miscInfoRow;

    private void updateRows() {
        rowCount = 0;
        heroRow = rowCount++;
        presetRow = rowCount++;
        resetRow = rowCount++;

        blurHeaderRow = rowCount++;
        blurEnabledRow = rowCount++;
        blurIntensityRow = rowCount++;
        blurInfoRow = rowCount++;

        animHeaderRow = rowCount++;
        animEnabledRow = rowCount++;
        animSpeedRow = rowCount++;

        bubbleHeaderRow = rowCount++;
        roundedBubblesRow = rowCount++;
        bubbleRoundnessRow = rowCount++;

        miscHeaderRow = rowCount++;
        vibrationRow = rowCount++;
        uiSoundsRow = rowCount++;
        largeAvatarsRow = rowCount++;
        hideNavLabelsRow = rowCount++;
        compactChatListRow = rowCount++;
        snowEffectRow = rowCount++;
        timeFormatRow = rowCount++;
        timeSecondsRow = rowCount++;
        timeColorRow = rowCount++;
        timeSizeRow = rowCount++;
        checkStyleRow = rowCount++;
        miscInfoRow = rowCount++;
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
        actionBar.setTitle("Внешний вид Cryptogram");
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
            if (position == presetRow) {
                SharedConfig.cycleCryptogramUiPreset();
                reloadThemeResources();
                listAdapter.notifyDataSetChanged();
            } else if (position == resetRow) {
                SharedConfig.resetCryptogramAppearance();
                reloadThemeResources();
                listAdapter.notifyDataSetChanged();
            } else if (position == blurEnabledRow) {
                SharedConfig.toggleChatListBlur();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.chatListBlurEnabled);
                }
            } else if (position == animEnabledRow) {
                SharedConfig.toggleAnimationsEnabled();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.cryptogramAnimationsEnabled);
                }
            } else if (position == roundedBubblesRow) {
                SharedConfig.toggleRoundedBubbles();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.roundedBubblesEnabled);
                }
            } else if (position == vibrationRow) {
                SharedConfig.toggleVibrationOnMessage();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.vibrationOnMessageEnabled);
                }
            } else if (position == uiSoundsRow) {
                SharedConfig.toggleUiSounds();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.uiSoundsEnabled);
                }
            } else if (position == largeAvatarsRow) {
                SharedConfig.toggleLargeAvatarsInChatList();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.largeAvatarsInChatList);
                }
            } else if (position == hideNavLabelsRow) {
                SharedConfig.toggleHideNavigationBarLabels();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.hideNavigationBarLabels);
                }
            } else if (position == compactChatListRow) {
                SharedConfig.toggleCompactChatList();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.compactChatList);
                }
            } else if (position == snowEffectRow) {
                SharedConfig.toggleCryptogramSnow();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.cryptogramSnowEnabled);
                }
            } else if (position == timeFormatRow) {
                SharedConfig.cycleCryptogramTimeFormat();
                reloadThemeResources();
                listAdapter.notifyItemChanged(position);
            } else if (position == timeSecondsRow) {
                SharedConfig.toggleCryptogramTimeSeconds();
                reloadThemeResources();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.cryptogramTimeShowSeconds);
                }
            } else if (position == timeColorRow) {
                SharedConfig.toggleCryptogramTimeColor();
                reloadThemeResources();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.cryptogramTimeColorEnabled);
                }
            } else if (position == checkStyleRow) {
                SharedConfig.cycleCryptogramCheckStyle();
                reloadThemeResources();
                listAdapter.notifyItemChanged(position);
            }
            if (position != heroRow) {
                listAdapter.notifyItemChanged(heroRow);
            }
        });

        return fragmentView;
    }

    private void reloadThemeResources() {
        if (getParentActivity() != null) {
            Theme.reloadAllResources(getParentActivity());
        }
    }

    private class SliderCell extends FrameLayout {
        private final SeekBarView seekBarView;
        private final android.widget.TextView titleView;
        private boolean isSpeed;

        SliderCell(Context context) {
            super(context);
            titleView = new android.widget.TextView(context);
            titleView.setTextSize(15);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 21, 8, 21, 0));

            seekBarView = new SeekBarView(context);
            addView(seekBarView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 40, Gravity.TOP | Gravity.LEFT, 6, 32, 6, 0));
            setWillNotDraw(false);
        }

        void bindBlur() {
            isSpeed = false;
            titleView.setText("Интенсивность размытия: " + SharedConfig.chatListBlurIntensity + "%");
            seekBarView.setProgress(SharedConfig.chatListBlurIntensity / 100f);
            seekBarView.setDelegate((stop, progress) -> {
                SharedConfig.setChatListBlurIntensity(Math.round(progress * 100));
                titleView.setText("Интенсивность размытия: " + SharedConfig.chatListBlurIntensity + "%");
            });
        }

        void bindSpeed() {
            isSpeed = true;
            titleView.setText("Скорость анимаций: " + SharedConfig.animationSpeedPercent + "%");
            seekBarView.setProgress((SharedConfig.animationSpeedPercent - 50) / 150f);
            seekBarView.setDelegate((stop, progress) -> {
                SharedConfig.setAnimationSpeedPercent(50 + Math.round(progress * 150));
                titleView.setText("Скорость анимаций: " + SharedConfig.animationSpeedPercent + "%");
            });
        }

        void bindRoundness() {
            titleView.setText("Скругление пузырей сообщений: " + SharedConfig.messageBubbleRoundness + "%");
            seekBarView.setProgress(SharedConfig.messageBubbleRoundness / 100f);
            seekBarView.setDelegate((stop, progress) -> {
                SharedConfig.setMessageBubbleRoundness(Math.round(progress * 100));
                titleView.setText("Скругление пузырей сообщений: " + SharedConfig.messageBubbleRoundness + "%");
            });
        }

        void bindTimeSize() {
            titleView.setText("Размер времени: " + SharedConfig.cryptogramTimeTextSize + "dp");
            seekBarView.setProgress((SharedConfig.cryptogramTimeTextSize - 10) / 8f);
            seekBarView.setDelegate((stop, progress) -> {
                SharedConfig.setCryptogramTimeTextSize(10 + Math.round(progress * 8));
                titleView.setText("Размер времени: " + SharedConfig.cryptogramTimeTextSize + "dp");
            });
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(org.telegram.messenger.AndroidUtilities.dp(74), MeasureSpec.EXACTLY));
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
            return position == presetRow || position == resetRow
                    || position == blurEnabledRow || position == animEnabledRow || position == roundedBubblesRow
                    || position == vibrationRow || position == uiSoundsRow || position == largeAvatarsRow
                    || position == hideNavLabelsRow || position == compactChatListRow || position == snowEffectRow
                    || position == timeFormatRow || position == timeSecondsRow || position == timeColorRow
                    || position == checkStyleRow;
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
                    view = new SliderCell(mContext);
                    break;
                case 4:
                    view = new TextCell(mContext);
                    break;
                case 5:
                    view = new CryptogramHeroCell(mContext, true);
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
                case 5: {
                    ((CryptogramHeroCell) holder.itemView).refresh();
                    break;
                }
                case 0: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == blurHeaderRow) {
                        headerCell.setText("Размытие интерфейса");
                    } else if (position == animHeaderRow) {
                        headerCell.setText("Анимации");
                    } else if (position == bubbleHeaderRow) {
                        headerCell.setText("Сообщения");
                    } else if (position == miscHeaderRow) {
                        headerCell.setText("Прочее");
                    }
                    break;
                }
                case 1: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    if (position == blurEnabledRow) {
                        checkCell.setTextAndCheck("Размытие при прокрутке списка чатов", SharedConfig.chatListBlurEnabled, true);
                    } else if (position == animEnabledRow) {
                        checkCell.setTextAndCheck("Включить анимации интерфейса", SharedConfig.cryptogramAnimationsEnabled, true);
                    } else if (position == roundedBubblesRow) {
                        checkCell.setTextAndCheck("Скруглённые пузыри сообщений", SharedConfig.roundedBubblesEnabled, true);
                    } else if (position == vibrationRow) {
                        checkCell.setTextAndCheck("Вибрация при отправке/получении", SharedConfig.vibrationOnMessageEnabled, true);
                    } else if (position == uiSoundsRow) {
                        checkCell.setTextAndCheck("Звуки интерфейса", SharedConfig.uiSoundsEnabled, true);
                    } else if (position == largeAvatarsRow) {
                        checkCell.setTextAndCheck("Крупные аватары в списке чатов", SharedConfig.largeAvatarsInChatList, true);
                    } else if (position == hideNavLabelsRow) {
                        checkCell.setTextAndCheck("Скрыть подписи нижней панели", SharedConfig.hideNavigationBarLabels, true);
                    } else if (position == compactChatListRow) {
                        checkCell.setTextAndCheck("Компактный список чатов", SharedConfig.compactChatList, true);
                    } else if (position == snowEffectRow) {
                        checkCell.setTextAndCheck("Снег в чате круглый год ❄️", SharedConfig.cryptogramSnowEnabled, false);
                    } else if (position == timeFormatRow) {
                        checkCell.setTextAndCheck("Формат времени: " + SharedConfig.getCryptogramTimeFormatName(), SharedConfig.cryptogramTimeFormat != 0, true);
                    } else if (position == timeSecondsRow) {
                        checkCell.setTextAndCheck("Показывать секунды", SharedConfig.cryptogramTimeShowSeconds, true);
                    } else if (position == timeColorRow) {
                        checkCell.setTextAndCheck("Акцентный цвет времени", SharedConfig.cryptogramTimeColorEnabled, true);
                    } else if (position == checkStyleRow) {
                        checkCell.setTextAndCheck("Стиль галочек: " + SharedConfig.getCryptogramCheckStyleName(), SharedConfig.cryptogramCheckStyle != 0, false);
                    }
                    break;
                }
                case 2: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == blurInfoRow) {
                        cell.setText("Применяется к фону при прокрутке списка чатов.");
                    } else if (position == miscInfoRow) {
                        cell.setText("Время, галочки и другие параметры применяются сразу после перерисовки чата.");
                    }
                    break;
                }
                case 3: {
                    SliderCell cell = (SliderCell) holder.itemView;
                    if (position == blurIntensityRow) {
                        cell.bindBlur();
                    } else if (position == animSpeedRow) {
                        cell.bindSpeed();
                    } else if (position == bubbleRoundnessRow) {
                        cell.bindRoundness();
                    } else if (position == timeSizeRow) {
                        cell.bindTimeSize();
                    }
                    break;
                }
                case 4: {
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == presetRow) {
                        textCell.setTextAndValue("Пресет оформления", SharedConfig.getCryptogramUiPresetName(), true);
                    } else if (position == resetRow) {
                        textCell.setTextAndIcon("Сбросить оформление", R.drawable.msg_delete_filled, false);
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == heroRow) {
                return 5;
            } else if (position == presetRow || position == resetRow) {
                return 4;
            } else if (position == blurHeaderRow || position == animHeaderRow || position == bubbleHeaderRow || position == miscHeaderRow) {
                return 0;
            } else if (position == blurEnabledRow || position == animEnabledRow || position == roundedBubblesRow
                    || position == vibrationRow || position == uiSoundsRow || position == largeAvatarsRow
                    || position == hideNavLabelsRow || position == compactChatListRow || position == snowEffectRow
                    || position == timeFormatRow || position == timeSecondsRow || position == timeColorRow
                    || position == checkStyleRow) {
                return 1;
            } else if (position == blurInfoRow || position == miscInfoRow) {
                return 2;
            } else if (position == blurIntensityRow || position == animSpeedRow || position == bubbleRoundnessRow || position == timeSizeRow) {
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
