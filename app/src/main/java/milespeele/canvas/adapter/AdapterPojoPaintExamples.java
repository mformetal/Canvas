package milespeele.canvas.adapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.List;

import milespeele.canvas.R;
import milespeele.canvas.pojo.PojoPaintExample;
import milespeele.canvas.view.ViewBrushLayoutPaintExample;
import milespeele.canvas.view.ViewTypefaceTextView;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class AdapterPojoPaintExamples extends RecyclerView.Adapter<AdapterPojoPaintExamples.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewTypefaceTextView paintName;
        public ViewBrushLayoutPaintExample paintExample;

        public ViewHolder(View itemView) {
            super(itemView);

            paintName = (ViewTypefaceTextView) itemView.findViewById(R.id.paint_example_layout_text);
            paintExample = (ViewBrushLayoutPaintExample) itemView.findViewById(R.id.paint_example_layout_paint);
        }
    }

    private List<PojoPaintExample> dataList;
    private int mDuration = 300;
    private Interpolator mInterpolator = new LinearInterpolator();
    private int mLastPosition = -1;
    private boolean isFirstOnly = true;

    public AdapterPojoPaintExamples(List<PojoPaintExample> list) {
        dataList = list;
    }

    @Override
    public AdapterPojoPaintExamples.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.paint_example_layout,
                        viewGroup,
                        false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AdapterPojoPaintExamples.ViewHolder viewHolder, int i) {
        PojoPaintExample example = dataList.get(i);

        ViewTypefaceTextView textView = viewHolder.paintName;
        textView.setText(example.getPaintName());
        textView.setTextColor(example.getColorForText());

        ViewBrushLayoutPaintExample paintExample = viewHolder.paintExample;
        paintExample.setPaint(example.getPaint());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateAllTextViewColors(int posNotToUpdate) {
        for (int i = 0; i < dataList.size(); i++) {
            if (i != posNotToUpdate) {
                dataList.get(i).setColorForText(Color.WHITE);
            }
        }
        notifyDataSetChanged();
    }

    public void updateTextViewColorAtPosition(int position, int color) {
        dataList.get(position).setColorForText(color);
    }
}
