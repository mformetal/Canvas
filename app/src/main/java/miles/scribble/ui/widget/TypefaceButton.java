package miles.scribble.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

import me.grantland.widget.AutofitHelper;
import miles.scribble.R;
import miles.scribble.util.TextUtils;

/**
 * Created by mbpeele on 9/2/15.
 */
public class TypefaceButton extends Button {

    private Paint mPaint;
    private AutofitHelper mAutofitHelper;

    public TypefaceButton(Context context) {
        super(context);
        init(null);
    }

    public TypefaceButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TypefaceButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TypefaceButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attributeSet) {
        if (isInEditMode()) {
            return;
        }

        setTypeface(TextUtils.getStaticTypeFace(getContext()));

        TypedArray typedArray = getResources().obtainAttributes(attributeSet, R.styleable.TypefaceButton);
        int background = typedArray.getColor(R.styleable.TypefaceButton_backgroundColor, Color.WHITE);
        if (background != Color.WHITE) {
            getBackground().setColorFilter(background, PorterDuff.Mode.SRC_OVER);
        }
        if (typedArray.getBoolean(R.styleable.TypefaceButton_buttonAutofit, false)) {
            mAutofitHelper = AutofitHelper.create(this);
        }
        typedArray.recycle();
    }

    public String getTextAsString() {
        return getText().toString();
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    public Paint getExamplePaint() { return mPaint; }

}
