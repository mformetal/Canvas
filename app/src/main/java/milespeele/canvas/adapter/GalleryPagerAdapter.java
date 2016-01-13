package milespeele.canvas.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import milespeele.canvas.R;
import milespeele.canvas.model.Sketch;
import milespeele.canvas.util.Logg;
import milespeele.canvas.view.ViewAspectRatioImage;
import milespeele.canvas.view.ViewTypefaceTextView;

/**
 * Created by mbpeele on 1/12/16.
 */
public class GalleryPagerAdapter extends PagerAdapter {

    private Activity mActivity;
    private LayoutInflater mInflater;
    private ArrayList<Sketch> mDataList;
    private PagerListener mListener;

    public GalleryPagerAdapter(Activity activity) {
        super();
        mActivity = activity;
        mInflater = LayoutInflater.from(activity);
        mDataList = new ArrayList<>();
        mListener = (PagerListener) activity;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        ViewGroup viewGroup = (ViewGroup) mInflater.inflate(R.layout.adapter_gallery_layout, collection, false);
        collection.addView(viewGroup);

        ViewAspectRatioImage imageView =
                (ViewAspectRatioImage) viewGroup.findViewById(R.id.adapter_gallery_image);
        ViewTypefaceTextView textView =
                (ViewTypefaceTextView) viewGroup.findViewById(R.id.adapter_gallery_title);

        Sketch sketch = mDataList.get(position);

        textView.setText(sketch.getTitle());

        Glide.with(mActivity)
                .fromBytes()
                .asBitmap()
                .animate(android.R.anim.fade_in)
                .load(sketch.getBytes())
                .listener(new RequestListener<byte[], Bitmap>() {
                    @Override
                    public boolean onException(Exception e, byte[] model, Target<Bitmap> target, boolean isFirstResource) {
                        Logg.log("GLIDE:", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, byte[] model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                mListener.onPaletteReady(palette);

                                Palette.Swatch swatch = palette.getVibrantSwatch();
                                if (swatch != null) {
                                    textView.animateTextColor(swatch.getTitleTextColor(), 350).start();
                                }
                            }
                        });
                        return false;
                    }
                })
                .into(imageView);

        return viewGroup;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mDataList.get(position).getTitle();
    }

    public void add(Sketch sketch) {
        mDataList.add(sketch);
        notifyDataSetChanged();
    }

    public interface PagerListener {

        void onPaletteReady(Palette palette);
    }
}
