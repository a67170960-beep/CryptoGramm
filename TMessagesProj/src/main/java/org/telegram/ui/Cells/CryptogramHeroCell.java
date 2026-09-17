package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.SharedConfig;

/**
 * Branded visual header for Cryptogram settings. It is deliberately drawn in
 * code so it follows the current theme without adding another XML layout.
 */
public class CryptogramHeroCell extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final boolean appearanceMode;

    public CryptogramHeroCell(Context context, boolean appearanceMode) {
        super(context);
        this.appearanceMode = appearanceMode;
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void refresh() {
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = AndroidUtilities.dp(appearanceMode ? 178 : 154);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int padding = AndroidUtilities.dp(16);
        float radius = AndroidUtilities.dp(24);

        rect.set(padding, AndroidUtilities.dp(10), width - padding, getHeight() - AndroidUtilities.dp(10));
        paint.setShader(new LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                new int[]{Color.rgb(36, 44, 92), Color.rgb(31, 131, 153), Color.rgb(118, 47, 145)},
                null, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, radius, radius, paint);
        paint.setShader(null);

        paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        paint.setTextSize(AndroidUtilities.dp(12));
        paint.setColor(0xBFFFFFFF);
        canvas.drawText(appearanceMode ? "CRYPTOGRAM  /  UI LAB" : "CRYPTOGRAM  /  CONTROL CENTER",
                rect.left + AndroidUtilities.dp(18), rect.top + AndroidUtilities.dp(25), paint);

        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        paint.setTextSize(AndroidUtilities.dp(21));
        paint.setColor(Color.WHITE);
        canvas.drawText(appearanceMode ? "Собери свой интерфейс" : "Приватность без компромиссов",
                rect.left + AndroidUtilities.dp(18), rect.top + AndroidUtilities.dp(53), paint);

        paint.setTextSize(AndroidUtilities.dp(12));
        paint.setColor(0xD9FFFFFF);
        canvas.drawText(appearanceMode
                        ? "Живой предпросмотр • пресеты • детали"
                        : "Ghost Mode • автоматизация • внешний вид",
                rect.left + AndroidUtilities.dp(18), rect.top + AndroidUtilities.dp(74), paint);

        int chipTop = appearanceMode ? 94 : 98;
        drawChip(canvas, rect.left + AndroidUtilities.dp(18), chipTop,
                "✦  " + SharedConfig.getCryptogramUiPresetName(), 0x35FFFFFF);
        drawChip(canvas, rect.left + AndroidUtilities.dp(126), chipTop,
                appearanceMode
                        ? "◒  " + SharedConfig.getCryptogramCheckStyleName()
                        : (SharedConfig.ghostMode ? "ON  Ghost" : "OFF  Ghost"),
                appearanceMode ? 0x2DFFFFFF : (SharedConfig.ghostMode ? 0x5539E58C : 0x30FFFFFF));

        if (appearanceMode) {
            drawChip(canvas, rect.left + AndroidUtilities.dp(18), 130,
                    "◉  Пузырь " + SharedConfig.messageBubbleRoundness + "%", 0x2DFFFFFF);
            drawChip(canvas, rect.left + AndroidUtilities.dp(146), 130,
                    SharedConfig.cryptogramSnowEnabled ? "❄  Snow" : "☼  Clean", 0x2DFFFFFF);

            paint.setColor(0xBFFFFFFF);
            paint.setTextSize(AndroidUtilities.dp(11));
            canvas.drawText("Нажми пресет ниже — и весь стиль изменится сразу",
                    rect.left + AndroidUtilities.dp(18), rect.bottom - AndroidUtilities.dp(16), paint);
        }
    }

    private void drawChip(Canvas canvas, float left, int top, String text, int color) {
        paint.setColor(color);
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(AndroidUtilities.dp(11));
        float textWidth = paint.measureText(text);
        rect.set(left, AndroidUtilities.dp(top), left + textWidth + AndroidUtilities.dp(22),
                AndroidUtilities.dp(top + 24));
        canvas.drawRoundRect(rect, AndroidUtilities.dp(12), AndroidUtilities.dp(12), paint);
        paint.setColor(0xF2FFFFFF);
        canvas.drawText(text, left + AndroidUtilities.dp(11), AndroidUtilities.dp(top + 16), paint);
    }
}