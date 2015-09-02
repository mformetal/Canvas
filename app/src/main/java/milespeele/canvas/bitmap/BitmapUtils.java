package milespeele.canvas.bitmap;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

import rx.Observable;

/**
 * Created by mbpeele on 9/2/15.
 */
public class BitmapUtils {

    public static Observable<byte[]> compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return Observable.just(stream.toByteArray());
    }
}
