package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import milespeele.canvas.R;
import milespeele.canvas.util.TextUtils;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 9/2/15.
 */
public class ViewTypefaceButton extends Button {


    public ViewTypefaceButton(Context context) {
        super(context);
        init(null);
    }

    public ViewTypefaceButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ViewTypefaceButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewTypefaceButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attributeSet) {
        setTypeface(TextUtils.getStaticTypeFace(getContext(), "Roboto.ttf"));

        TypedArray typedArray = getResources().obtainAttributes(attributeSet, R.styleable.ViewTypefaceButton);
        int background = typedArray.getColor(R.styleable.ViewTypefaceButton_backgroundColor, Color.WHITE);
        if (background != Color.WHITE) {
            getBackground().setColorFilter(background, PorterDuff.Mode.SRC_ATOP);
        }
        typedArray.recycle();
    }

}
