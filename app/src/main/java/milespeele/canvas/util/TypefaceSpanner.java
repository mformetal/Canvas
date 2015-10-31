package milespeele.canvas.util;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.util.LruCache;

/**
 * Created by mbpeele on 10/27/15.
 */
public class TypefaceSpanner extends MetricAffectingSpan {

    private Typeface mTypeface;

    public TypefaceSpanner(Context context, String typefaceName) {
        mTypeface = TextUtils.getStaticTypeFace(context, typefaceName);
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTypeface(mTypeface);
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(mTypeface);
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
}