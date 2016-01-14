package miles.canvas.data.adapter;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import miles.canvas.R;
import miles.canvas.data.Sketch;
import miles.canvas.ui.widget.AspectRatioImageView;
import miles.canvas.ui.widget.TypefaceTextView;
import miles.canvas.util.Logg;
import miles.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 1/12/16.
 */
public class GalleryPagerAdapter extends PagerAdapter {

    private Activity mActivity;
    private LayoutInflater mInflater;
    private ArrayList<Sketch> mDataList;

    public GalleryPagerAdapter(Activity activity) {
        super();
        mActivity = activity;
        mInflater = LayoutInflater.from(activity);
        mDataList = new ArrayList<>();
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        ViewGroup viewGroup =
                (ViewGroup) mInflater.inflate(R.layout.adapter_gallery_layout, collection, false);
        collection.addView(viewGroup);

        Sketch sketch = mDataList.get(position);

        AspectRatioImageView imageView =
                (AspectRatioImageView) viewGroup.findViewById(R.id.adapter_gallery_image);
        TypefaceTextView textView =
                (TypefaceTextView) viewGroup.findViewById(R.id.adapter_gallery_title);

        textView.setText(sketch.getTitle());

        Glide.with(mActivity)
                .fromBytes()
                .asBitmap()
                .animate(android.R.anim.fade_in)
                .load(sketch.getBytes())
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

    public Sketch get(int pos) {
        return mDataList.get(pos);
    }

    public void remove(Sketch sketch) {
        mDataList.remove(sketch);
    }

    public Sketch remove(int pos) {
        return mDataList.remove(pos);
    }
}
