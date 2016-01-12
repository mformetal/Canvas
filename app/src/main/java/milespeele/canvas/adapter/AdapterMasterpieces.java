package milespeele.canvas.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.parse.Masterpiece;
import milespeele.canvas.view.ViewAspectRatioImage;
import milespeele.canvas.view.ViewTypefaceTextView;

/**
 * Created by mbpeele on 1/11/16.
 */
public class AdapterMasterpieces extends RecyclerView.Adapter<AdapterMasterpieces.MasterpieceViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Masterpiece> mMasterpieces;
    private Picasso mPicasso;

    public AdapterMasterpieces(Context context, Picasso picasso) {
        mContext = context;
        mPicasso = picasso;
        mInflater = LayoutInflater.from(mContext);
        mMasterpieces = new ArrayList<>();
    }

    @Override
    public MasterpieceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_masterpieces_layout, parent, false);
        return new MasterpieceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MasterpieceViewHolder holder, int position) {
        Masterpiece masterpiece = mMasterpieces.get(position);

        ParseFile file = masterpiece.getImage();

        Glide.with(mContext)
                .load(file.getUrl())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mMasterpieces.size();
    }

    public void addMasterpiece(Masterpiece masterpiece) {
        mMasterpieces.add(masterpiece);
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
