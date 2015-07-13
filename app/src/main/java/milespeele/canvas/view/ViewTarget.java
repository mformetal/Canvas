package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import milespeele.canvas.util.Logger;

/**
 * Created by Miles Peele on 7/13/2015.
 */
public class ViewTarget extends ImageView implements Target {

    public ViewTarget(Context context) {
        super(context);
    }

    public ViewTarget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewTarget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewTarget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        setImageBitmap(bitmap);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        Logger.log("ON BITMAP FAILED");
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        Logger.log("ON PREPARE");
    }
}
