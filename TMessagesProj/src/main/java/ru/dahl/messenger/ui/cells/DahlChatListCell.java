package ru.dahl.messenger.ui.cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadioButton;
import org.telegram.ui.Components.RecyclerListView;

import ru.dahl.messenger.settings.DahlSettings;

public class DahlChatListCell extends RecyclerListView {

    private static class ListView extends FrameLayout {

        private RadioButton button;
        private int lines;
        private RectF rect = new RectF();
        private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        @StringRes
        private int titleRes;

        public ListView(Context context) {
            super(context);
            setWillNotDraw(false);
            textPaint.setTextSize(AndroidUtilities.dp(13));

            button = new RadioButton(context) {
                @Override
                public void invalidate() {
                    super.invalidate();
                    ListView.this.invalidate();
                }
            };
            button.setSize(AndroidUtilities.dp(20));
            addView(button, LayoutHelper.createFrame(22, 22, Gravity.RIGHT | Gravity.TOP, 0, 26, 10, 0));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int color = Theme.getColor(Theme.key_switchTrack);
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            button.setColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_radioBackgroundChecked));

            rect.set(AndroidUtilities.dp(1), AndroidUtilities.dp(1), getMeasuredWidth() - AndroidUtilities.dp(1), AndroidUtilities.dp(73));
            Theme.chat_instantViewRectPaint.setColor(Color.argb((int) (43 * button.getProgress()), r, g, b));
            canvas.drawRoundRect(rect, AndroidUtilities.dp(6), AndroidUtilities.dp(6), Theme.chat_instantViewRectPaint);

            rect.set(0, 0, getMeasuredWidth(), AndroidUtilities.dp(74));
            Theme.dialogs_onlineCirclePaint.setColor(Color.argb((int) (31 * (1.0f - button.getProgress())), r, g, b));
            canvas.drawRoundRect(rect, AndroidUtilities.dp(6), AndroidUtilities.dp(6), Theme.dialogs_onlineCirclePaint);

            String text = LocaleController.getString(titleRes);
            int width = (int) Math.ceil(textPaint.measureText(text));

            textPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            canvas.drawText(text, (getMeasuredWidth() - width) / 2f, AndroidUtilities.dp(96), textPaint);

            int lineCount = lines == 1 ? 3 : 2;
            for (int a = 0; a <= lineCount; a++) {
                int cy;
                if(lines != 1){
                    cy = AndroidUtilities.dp(a == 0 ? 21 : 53);
                }else if (a == 0) {
                    cy = AndroidUtilities.dp(15);
                }else if(a == 1){
                    cy = AndroidUtilities.dp(37);
                }else{
                    cy = AndroidUtilities.dp(59);
                }
                Theme.dialogs_onlineCirclePaint.setColor(Color.argb(a == 0 ? 204 : 90, r, g, b));
                float radius = AndroidUtilities.dp(lines == 1 ? 7 : 11);
                int startMargin = AndroidUtilities.dp(11);
                if(DahlSettings.getRectangularAvatars()){
                    rect.set(startMargin, cy - radius, startMargin + radius * 2, cy + radius);
                    canvas.drawRoundRect(rect, AndroidUtilities.dpf2(3), AndroidUtilities.dpf2(3), Theme.dialogs_onlineCirclePaint);
                }else {
                    canvas.drawCircle(startMargin + radius, cy, radius, Theme.dialogs_onlineCirclePaint);
                }

                for (int i = 0; i < lines; i++) {
                    Theme.dialogs_onlineCirclePaint.setColor(Color.argb(i == 0 ? 204 : 90, r, g, b));
                    if (lines == 3) {
                        rect.set(AndroidUtilities.dp(41), cy - AndroidUtilities.dp(8.3f - i * 7), getMeasuredWidth() - AndroidUtilities.dp(i == 0 ? 72 : 48), cy - AndroidUtilities.dp(8.3f - 3 - i * 7));
                        canvas.drawRoundRect(rect, AndroidUtilities.dpf2(1.5f), AndroidUtilities.dpf2(1.5f), Theme.dialogs_onlineCirclePaint);
                    } else if (lines == 2) {
                        rect.set(AndroidUtilities.dp(41), cy - AndroidUtilities.dp(7 - i * 10), getMeasuredWidth() - AndroidUtilities.dp(i == 0 ? 72 : 48), cy - AndroidUtilities.dp(7 - 4 - i * 10));
                        canvas.drawRoundRect(rect, AndroidUtilities.dp(2), AndroidUtilities.dp(2), Theme.dialogs_onlineCirclePaint);
                    } else {
                        rect.set(AndroidUtilities.dp(33), cy - AndroidUtilities.dp(2 - i * 10), getMeasuredWidth() - AndroidUtilities.dp(72), cy - AndroidUtilities.dp(2 - 4 - i * 10));
                        canvas.drawRoundRect(rect, AndroidUtilities.dpf2(1.5f), AndroidUtilities.dpf2(1.5f), Theme.dialogs_onlineCirclePaint);
                    }
                }
            }
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName(RadioButton.class.getName());
            info.setChecked(button.isChecked());
            info.setCheckable(true);
            info.setContentDescription(LocaleController.getString(titleRes));
        }

        void bind(int lines, boolean checked) {
            this.lines = lines;
            switch (lines) {
                case 1:
                    titleRes = R.string.ChatListSingleLine;
                    break;
                case 2:
                    titleRes = R.string.ChatListDefault;
                    break;
                case 3:
                    titleRes = R.string.ChatListExpanded;
                    break;
                default:
                    titleRes = R.string.ChatListDefault;
            }
            setContentDescription(LocaleController.getString(titleRes));
            button.setChecked(checked, true);
        }
    }

    private ListAdapter adapter;

    public DahlChatListCell(Context context) {
        super(context);
        setFocusable(false);
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        setItemAnimator(null);
        setLayoutAnimation(null);

        setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        adapter = new ListAdapter();
        setAdapter(adapter);
        int position = Math.max(0, Math.min(2, DahlSettings.getChatListLines() - 1));
        adapter.updateSelection(position);

        scrollToPosition(position);

        setOnItemClickListener((view, pos) -> {
            adapter.updateSelection(pos);
            didSelectChatType(pos + 1);
            postDelayed(() -> smoothScrollToPosition(pos), 100);
        });
    }

    protected void didSelectChatType(int lines) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(123), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getAdapter() != null) {
            ((ListAdapter) getAdapter()).updateItemSize(w / 2 - AndroidUtilities.dp(28), h);
        }
    }

    static class ListAdapter extends Adapter<ViewHolder> {

        private int selectedPosition = 0;
        private int itemWidth = 0;
        private int itemHeight = 0;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FrameLayout view = new FrameLayout(parent.getContext());
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(position + 1, position == selectedPosition, itemWidth, itemHeight);
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        void updateSelection(int position) {
            int oldPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
        }


        @SuppressLint("NotifyDataSetChanged")
        void updateItemSize(int width, int height) {
            if (itemWidth == width && itemHeight == height) return;

            itemWidth = width;
            itemHeight = height;
            notifyDataSetChanged();
        }
    }

    static class ViewHolder extends RecyclerListView.ViewHolder {

        ListView view;

        ViewHolder(FrameLayout itemView) {
            super(itemView);
            this.view = new ListView(itemView.getContext());
            itemView.addView(view, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.START));
        }

        void bind(int lines, boolean checked, int width, int height) {
            view.bind(lines, checked);
            int startPadding;
            int endPadding;
            if (lines == 1) {
                startPadding = AndroidUtilities.dp(21);
                endPadding = 0;
            } else if (lines == 3) {
                startPadding = 0;
                endPadding = AndroidUtilities.dp(21);
            } else {
                startPadding = AndroidUtilities.dp(10);
                endPadding = AndroidUtilities.dp(10);
            }
            itemView.setPaddingRelative(startPadding, AndroidUtilities.dp(10), endPadding, 0);

            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            if (layoutParams != null && (layoutParams.width != width || layoutParams.height != height)) {
                layoutParams.width = width;
                layoutParams.height = height;
                view.setLayoutParams(layoutParams);
                view.requestLayout();
            }
        }
    }
}
