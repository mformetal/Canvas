package miles.scribble.data.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import miles.scribble.R;
import miles.scribble.data.model.Sketch;
import miles.scribble.ui.widget.AspectRatioImageView;
import miles.scribble.ui.widget.TypefaceTextView;
import miles.scribble.util.Logg;

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
