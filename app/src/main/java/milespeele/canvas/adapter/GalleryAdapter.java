package milespeele.canvas.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.model.Sketch;
import milespeele.canvas.view.ViewAspectRatioImage;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MasterpieceViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Sketch> mDataList;

    public GalleryAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mDataList = new ArrayList<>();
    }

    @Override
    public MasterpieceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_masterpieces_layout, parent, false);
        return new MasterpieceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MasterpieceViewHolder holder, int position) {
        Sketch sketch = mDataList.get(position);

        Glide.with(mContext)
                .load(sketch.getBytes())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void addMasterpiece(Sketch masterpiece) {
        mDataList.add(masterpiece);
        notifyDataSetChanged();
    }

    final static class MasterpieceViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.adapter_masterpieces_image) ViewAspectRatioImage imageView;

        public MasterpieceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}