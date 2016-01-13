package miles.canvas.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import miles.canvas.R;

/**
 * Created by mbpeele on 1/11/16.
 */
public class AspectRatioImageView extends ImageView {

    public static final int MEASUREMENT_WIDTH = 0;
    public static final int MEASUREMENT_HEIGHT = 1;

    private static final float DEFAULT_ASPECT_RATIO = 1f;
    private static final boolean DEFAULT_ASPECT_RATIO_ENABLED = false;
    private static final int DEFAULT_DOMINANT_MEASUREMENT = MEASUREMENT_WIDTH;

    private float aspectRatio;
    private boolean aspectRatioEnabled;
    private int dominantMeasurement;

    public AspectRatioImageView(Context context) {
        super(context);
        init(null);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray a = getContext().obtainStyledAttributes(attributeSet, R.styleable.AspectRatioImageView);
            aspectRatio = a.getFloat(R.styleable.AspectRatioImageView_aspectRatio, DEFAULT_ASPECT_RATIO);
            aspectRatioEnabled = a.getBoolean(R.styleable.AspectRatioImageView_aspectRatioEnabled,
                    DEFAULT_ASPECT_RATIO_ENABLED);
            dominantMeasurement = a.getInt(R.styleable.AspectRatioImageView_dominantMeasurement,
                    DEFAULT_DOMINANT_MEASUREMENT);
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!aspectRatioEnabled) return;

        int newWidth;
        int newHeight;
        switch (dominantMeasurement) {
            case MEASUREMENT_WIDTH:
                newWidth = getMeasuredWidth();
                newHeight = (int) (newWidth * aspectRatio);
                break;

            case MEASUREMENT_HEIGHT:
                newHeight = getMeasuredHeight();
                newWidth = (int) (newHeight * aspectRatio);
                break;

            default:
                throw new IllegalStateException("Unknown measurement with ID " + dominantMeasurement);
        }

        setMeasuredDimension(newWidth, newHeight);
    }

    /** Get the aspect ratio for this image view. */
    public float getAspectRatio() {
        return aspectRatio;
    }

    /** Set the aspect ratio for this image view. This will update the view instantly. */
    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        if (aspectRatioEnabled) {
            requestLayout();
        }
    }

    /** Get whether or not forcing the aspect ratio is enabled. */
    public boolean getAspectRatioEnabled() {
        return aspectRatioEnabled;
    }

    /** set whether or not forcing the aspect ratio is enabled. This will re-layout the view. */
    public void setAspectRatioEnabled(boolean aspectRatioEnabled) {
        this.aspectRatioEnabled = aspectRatioEnabled;
        requestLayout();
    }

    /** Get the dominant measurement for the aspect ratio. */
    public int getDominantMeasurement() {
        return dominantMeasurement;
    }

    /**
     * Set the dominant measurement for the aspect ratio.
     *
     * @see #MEASUREMENT_WIDTH
     * @see #MEASUREMENT_HEIGHT
     */
    public void setDominantMeasurement(int dominantMeasurement) {
        if (dominantMeasurement != MEASUREMENT_HEIGHT && dominantMeasurement != MEASUREMENT_WIDTH) {
            throw new IllegalArgumentException("Invalid measurement type.");
        }
        this.dominantMeasurement = dominantMeasurement;
        requestLayout();
    }
}
