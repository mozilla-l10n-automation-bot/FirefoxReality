package org.mozilla.vrbrowser.ui.widgets.dialogs;

import android.content.Context;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mozilla.geckoview.GeckoSession;
import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.ui.views.UITextButton;
import org.mozilla.vrbrowser.ui.widgets.UIWidget;
import org.mozilla.vrbrowser.ui.widgets.WidgetManagerDelegate;
import org.mozilla.vrbrowser.ui.widgets.WidgetPlacement;
import org.mozilla.vrbrowser.utils.StringUtils;
import org.mozilla.vrbrowser.utils.ViewUtils;

import java.util.ArrayList;

import static android.view.Gravity.CENTER_VERTICAL;

public class SelectionActionWidget extends UIWidget implements WidgetManagerDelegate.FocusChangeListener {
    public interface Delegate {
        void onAction(String action);
        void onDismiss();
    }

    private Delegate mDelegate;
    private Point mPosition;
    private LinearLayout mContainer;
    private int mMinButtonWidth;

    public SelectionActionWidget(Context aContext) {
        super(aContext);
        initialize();
    }

    private void initialize() {
        inflate(getContext(), R.layout.selection_action_menu, this);
        mContainer = findViewById(R.id.selectionMenuContainer);
        mMinButtonWidth = WidgetPlacement.pixelDimension(getContext(), R.dimen.autocompletion_widget_min_item_width);
        mBackHandler = () -> {
            onDismiss();
        };
    }

    @Override
    protected void initializeWidgetPlacement(WidgetPlacement aPlacement) {
        aPlacement.width =  WidgetPlacement.dpDimension(getContext(), R.dimen.context_menu_row_width);
        aPlacement.height = WidgetPlacement.dpDimension(getContext(), R.dimen.context_menu_row_height);
        aPlacement.parentAnchorX = 0.5f;
        aPlacement.parentAnchorY = 0.5f;
        aPlacement.anchorX = 0.5f;
        aPlacement.anchorY = 0.5f;
        aPlacement.translationX = 0.0f;
        aPlacement.translationY = 0.0f;
        aPlacement.translationZ = WidgetPlacement.unitFromMeters(getContext(), R.dimen.context_menu_z_distance);
        aPlacement.visible = false;
    }

    public void setDelegate(Delegate aDelegate) {
        mDelegate = aDelegate;
    }

    @Override
    public void show(@ShowFlags int aShowFlags) {
        mWidgetManager.addFocusChangeListener(this);
        mWidgetPlacement.setSizeFromMeasure(getContext(), this);
        if (mPosition != null) {
            mWidgetPlacement.parentAnchorX = 0.0f;
            mWidgetPlacement.parentAnchorY = 1.0f;
            mWidgetPlacement.translationX = mPosition.x * WidgetPlacement.worldToWindowRatio(getContext());
            mWidgetPlacement.translationY = -mPosition.y * WidgetPlacement.worldToWindowRatio(getContext());
            mWidgetPlacement.translationY += mWidgetPlacement.height * 0.5f;
        }
        super.show(aShowFlags);
    }

    @Override
    public void hide(@HideFlags int aHideFlags) {
        super.hide(aHideFlags);
        mWidgetManager.removeFocusChangeListener(this);
    }

    @Override
    protected void onDismiss() {
        if (mDelegate != null) {
            mDelegate.onDismiss();
        }
    }

    public void setSelectionRect(@Nullable RectF aRect) {
        if (aRect != null) {
            mPosition = new Point((int) aRect.centerX(), (int) aRect.top);
        } else {
            mPosition = null;
        }
    }

    public void setActions(@NonNull String[] actions) {
        mContainer.removeAllViews();
        ArrayList<UITextButton> buttons = new ArrayList<>();

        if (StringUtils.contains(actions, GeckoSession.SelectionActionDelegate.ACTION_CUT)) {
            buttons.add(createButton(R.string.context_menu_cut_text, GeckoSession.SelectionActionDelegate.ACTION_CUT, this::handleAction));
        }
        if (StringUtils.contains(actions, GeckoSession.SelectionActionDelegate.ACTION_COPY)) {
            buttons.add(createButton(R.string.context_menu_copy_text, GeckoSession.SelectionActionDelegate.ACTION_COPY, this::handleAction));
        }
        if (StringUtils.contains(actions, GeckoSession.SelectionActionDelegate.ACTION_PASTE)) {
            buttons.add(createButton(R.string.context_menu_paste_text, GeckoSession.SelectionActionDelegate.ACTION_PASTE, this::handleAction));
        }
        if (StringUtils.contains(actions, GeckoSession.SelectionActionDelegate.ACTION_SELECT_ALL)) {
            buttons.add(createButton(R.string.context_menu_select_all_text, GeckoSession.SelectionActionDelegate.ACTION_SELECT_ALL, this::handleAction));
        }
        if (StringUtils.contains(actions, GeckoSession.SelectionActionDelegate.ACTION_UNSELECT)) {
            buttons.add(createButton(R.string.context_menu_unselect, GeckoSession.SelectionActionDelegate.ACTION_UNSELECT, this::handleAction));
        }

        for (int i = 0; i < buttons.size(); ++i) {
            mContainer.addView(buttons.get(i));
            if (i < buttons.size() - 1) {
                mContainer.addView(createSeparator());
            }

            int backgroundId = R.drawable.selection_menu_button;
            if (buttons.size() == 1) {
                backgroundId = R.drawable.selection_menu_button_single;
            } else if (i == 0) {
                backgroundId = R.drawable.selection_menu_button_first;
            } else if (i == buttons.size() - 1) {
                backgroundId = R.drawable.selection_menu_button_last;
            }
            buttons.get(i).setBackgroundDrawable(getContext().getDrawable(backgroundId));
        }
    }

    private UITextButton createButton(int aStringId, String aAction, OnClickListener aHandler) {
        UITextButton button = new UITextButton(getContext(), null, R.attr.selectionActionButtonStyle);
        button.setBackground(getContext().getDrawable(R.drawable.autocompletion_item_background));
        if (aHandler != null) {
            button.setOnClickListener(aHandler);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        button.setMinWidth(mMinButtonWidth);
        params.gravity = CENTER_VERTICAL;
        button.setLayoutParams(params);
        button.setTag(aAction);
        button.setText(getContext().getString(aStringId));

        return button;
    }

    private View createSeparator() {
        View view = new View(getContext());
        float density = getContext().getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(1.5f * density), (int)(40.0f * density));
        params.gravity = CENTER_VERTICAL;
        view.setLayoutParams(params);
        view.setBackground(getContext().getDrawable(R.drawable.separator_background));
        return view;
    }

    private void handleAction(View sender) {
        if (mDelegate != null) {
            mDelegate.onAction((String)sender.getTag());
        }
    }

    // WidgetManagerDelegate.FocusChangeListener

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        if (!ViewUtils.isChildrenOf(getChildAt(0), newFocus)) {
            onDismiss();
        }
    }
}