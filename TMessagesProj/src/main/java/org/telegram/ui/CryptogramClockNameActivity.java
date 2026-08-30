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

import org.telegram.messenger.CryptogramClockName;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.RadioButtonCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CryptogramClockNameActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    // Часовые пояса на выбор — основные страны/города СНГ и популярные мировые.
    // ID должны быть валидными идентификаторами java.util.TimeZone.
    private static final LinkedHashMap<String, String> TIMEZONES = new LinkedHashMap<>();
    static {
        TIMEZONES.put("Europe/Moscow", "Москва (UTC+3)");
        TIMEZONES.put("Europe/Kiev", "Киев (UTC+2)");
        TIMEZONES.put("Europe/Minsk", "Минск (UTC+3)");
        TIMEZONES.put("Asia/Almaty", "Алматы, Казахстан (UTC+6)");
        TIMEZONES.put("Asia/Tashkent", "Ташкент, Узбекистан (UTC+5)");
        TIMEZONES.put("Asia/Baku", "Баку, Азербайджан (UTC+4)");
        TIMEZONES.put("Asia/Yerevan", "Ереван, Армения (UTC+4)");
        TIMEZONES.put("Asia/Tbilisi", "Тбилиси, Грузия (UTC+4)");
        TIMEZONES.put("Asia/Bishkek", "Бишкек, Киргизия (UTC+6)");
        TIMEZONES.put("Asia/Dushanbe", "Душанбе, Таджикистан (UTC+5)");
        TIMEZONES.put("Asia/Ashgabat", "Ашхабад, Туркменистан (UTC+5)");
        TIMEZONES.put("Europe/Chisinau", "Кишинёв, Молдова (UTC+2)");
        TIMEZONES.put("Europe/London", "Лондон (UTC+0/1)");
        TIMEZONES.put("Europe/Berlin", "Берлин (UTC+1/2)");
        TIMEZONES.put("America/New_York", "Нью-Йорк (UTC-5/4)");
        TIMEZONES.put("Asia/Dubai", "Дубай, ОАЭ (UTC+4)");
        TIMEZONES.put("Asia/Istanbul", "Стамбул, Турция (UTC+3)");
        TIMEZONES.put("Asia/Shanghai", "Пекин/Шанхай (UTC+8)");
    }

    private int rowCount;
    private int enabledRow;
    private int infoRow;

    private int positionHeaderRow;
    private int positionPrefixRow;
    private int positionSuffixRow;
    private int positionLastNameRow;
    private int positionInfoRow;

    private int timezoneHeaderRow;
    private int timezoneRow;

    private int intervalHeaderRow;
    private int intervalRow;
    private int intervalInfoRow;

    private void updateRows() {
        rowCount = 0;
        enabledRow = rowCount++;
        infoRow = rowCount++;

        positionHeaderRow = rowCount++;
        positionPrefixRow = rowCount++;
        positionSuffixRow = rowCount++;
        positionLastNameRow = rowCount++;
        positionInfoRow = rowCount++;

        timezoneHeaderRow = rowCount++;
        timezoneRow = rowCount++;

        intervalHeaderRow = rowCount++;
        intervalRow = rowCount++;
        intervalInfoRow = rowCount++;
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
        actionBar.setTitle("Время в нике");
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
                boolean newValue = !CryptogramClockName.enabled;
                CryptogramClockName.enabled = newValue;
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(newValue);
                }
                if (newValue) {
                    CryptogramClockName.start();
                } else {
                    CryptogramClockName.stop();
                    CryptogramClockName.restoreOriginalName();
                }
            } else if (position == positionPrefixRow) {
                CryptogramClockName.position = CryptogramClockName.Position.FIRST_NAME_PREFIX;
                listAdapter.notifyItemRangeChanged(positionPrefixRow, 3);
            } else if (position == positionSuffixRow) {
                CryptogramClockName.position = CryptogramClockName.Position.FIRST_NAME_SUFFIX;
                listAdapter.notifyItemRangeChanged(positionPrefixRow, 3);
            } else if (position == positionLastNameRow) {
                CryptogramClockName.position = CryptogramClockName.Position.LAST_NAME_REPLACE;
                listAdapter.notifyItemRangeChanged(positionPrefixRow, 3);
            } else if (position == timezoneRow) {
                showTimezonePicker();
            } else if (position == intervalRow) {
                showIntervalDialog();
            }
        });

        return fragmentView;
    }

    private void showTimezonePicker() {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        ArrayList<String> ids = new ArrayList<>(TIMEZONES.keySet());
        ArrayList<String> titles = new ArrayList<>(TIMEZONES.values());
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Выберите часовой пояс");
        builder.setItems(titles.toArray(new CharSequence[0]), (dialogInterface, which) -> {
            CryptogramClockName.timeZoneId = ids.get(which);
            listAdapter.notifyItemChanged(timezoneRow);
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void showIntervalDialog() {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        EditTextBoldCursor editText = new EditTextBoldCursor(context);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setText(String.valueOf(CryptogramClockName.intervalMinutes));
        editText.setTextSize(16);
        editText.setPadding(dp(16), dp(8), dp(16), dp(8));
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Интервал обновления (мин.)");
        builder.setMessage("Слишком частое обновление (каждую минуту) может привести к временной блокировке смены имени сервером Telegram (FLOOD_WAIT). Функция сама подождёт нужное время, если это произойдёт.");
        builder.setView(layout);
        builder.setPositiveButton("Сохранить", (dialogInterface, i) -> {
            try {
                int value = Math.max(1, Integer.parseInt(editText.getText().toString().trim()));
                CryptogramClockName.intervalMinutes = value;
                if (CryptogramClockName.enabled) {
                    CryptogramClockName.start();
                }
                listAdapter.notifyItemChanged(intervalRow);
            } catch (NumberFormatException ignored) {
            }
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
            return position == enabledRow || position == positionPrefixRow || position == positionSuffixRow
                    || position == positionLastNameRow || position == timezoneRow || position == intervalRow;
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
                    view = new RadioButtonCell(mContext);
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
                    if (position == positionHeaderRow) {
                        headerCell.setText("Куда вставлять время");
                    } else if (position == timezoneHeaderRow) {
                        headerCell.setText("Часовой пояс");
                    } else if (position == intervalHeaderRow) {
                        headerCell.setText("Интервал обновления");
                    }
                    break;
                }
                case 1: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    checkCell.setTextAndCheck("Включить время в нике", CryptogramClockName.enabled, false);
                    break;
                }
                case 2: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == infoRow) {
                        cell.setText("Периодически меняет ваше имя или фамилию, подставляя туда текущее время. Исходное имя сохраняется и восстанавливается при выключении.");
                    } else if (position == positionInfoRow) {
                        cell.setText("Выберите, заменять ли фамилию целиком на время, или добавлять время перед/после имени.");
                    } else if (position == intervalInfoRow) {
                        cell.setText("Минимум 1 минута. Слишком частое обновление может временно ограничиваться сервером Telegram.");
                    }
                    break;
                }
                case 3: {
                    RadioButtonCell radioCell = (RadioButtonCell) holder.itemView;
                    if (position == positionPrefixRow) {
                        radioCell.setTextAndValue("Время + Имя", "", true, CryptogramClockName.position == CryptogramClockName.Position.FIRST_NAME_PREFIX);
                    } else if (position == positionSuffixRow) {
                        radioCell.setTextAndValue("Имя + Время", "", true, CryptogramClockName.position == CryptogramClockName.Position.FIRST_NAME_SUFFIX);
                    } else if (position == positionLastNameRow) {
                        radioCell.setTextAndValue("Заменить фамилию", "", false, CryptogramClockName.position == CryptogramClockName.Position.LAST_NAME_REPLACE);
                    }
                    break;
                }
                default: {
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == timezoneRow) {
                        String label = TIMEZONES.containsKey(CryptogramClockName.timeZoneId)
                                ? TIMEZONES.get(CryptogramClockName.timeZoneId)
                                : CryptogramClockName.timeZoneId;
                        textCell.setTextAndValue("Часовой пояс", label, false);
                    } else if (position == intervalRow) {
                        textCell.setTextAndValue("Интервал", CryptogramClockName.intervalMinutes + " мин.", false);
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == positionHeaderRow || position == timezoneHeaderRow || position == intervalHeaderRow) {
                return 0;
            } else if (position == enabledRow) {
                return 1;
            } else if (position == infoRow || position == positionInfoRow || position == intervalInfoRow) {
                return 2;
            } else if (position == positionPrefixRow || position == positionSuffixRow || position == positionLastNameRow) {
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
