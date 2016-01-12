package milespeele.canvas.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.activity.ActivitySketch;
import milespeele.canvas.model.Sketch;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;
import milespeele.canvas.view.ViewAspectRatioImage;
import milespeele.canvas.view.ViewTypefaceTextView;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.SketchViewHolder> {

    private Activity mContext;
    private LayoutInflater mInflater;
    private ArrayList<Sketch> mDataList;

    public GalleryAdapter(Activity context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mDataList = new ArrayList<>();
    }

    @Override
    public SketchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_masterpieces_layout, parent, false);
        return new SketchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SketchViewHolder holder, int position) {
        Sketch sketch = mDataList.get(position);

        ViewAspectRatioImage imageView = holder.imageView;
        ViewTypefaceTextView textView = holder.textView;

//        imageView.setOnClickListener(v -> ActivitySketch.newIntent(mContext, sketch, imageView));

        textView.setText(sketch.getTitle());

        Glide.with(mContext)
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
                        if (!holder.hasLoaded) {
                            Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch vibrant = palette.getDarkMutedSwatch();
                                    if (vibrant != null) {
                                        ViewUtils.animateBackground(textView, 350, vibrant.getRgb()).start();
                                        textView.animateTextColor(vibrant.getTitleTextColor(), 350).start();
                                        holder.hasLoaded = true;
                                    }
                                }
                            });
                        }
                        return false;
                    }
                })
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void addMasterpiece(Sketch masterpiece) {
        mDataList.add(masterpiece);
        notifyDataSetChanged();
    }

    public Sketch get(int pos) {
        if (pos >= 0 && pos <= getItemCount() - 1) {
            return mDataList.get(pos);
        }

        return null;
    }

    public final static class SketchViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.adapter_masterpieces_image) ViewAspectRatioImage imageView;
        @Bind(R.id.adapter_masterpieces_title) ViewTypefaceTextView textView;
        private boolean hasLoaded = false;

        public SketchViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public View getImageView() { return imageView; }
        public View getTextView() { return textView; }
    }
}