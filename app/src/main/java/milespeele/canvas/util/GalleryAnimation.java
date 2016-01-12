package milespeele.canvas.util;

import com.bumptech.glide.request.animation.GlideAnimation;

/**
 * Created by mbpeele on 1/12/16.
 */
public class GalleryAnimation implements com.bumptech.glide.request.animation.GlideAnimation {

    @Override
    public boolean animate(Object current, ViewAdapter adapter) {
        return true;
    }
}
