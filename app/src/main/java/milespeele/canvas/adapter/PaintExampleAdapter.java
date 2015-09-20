package milespeele.canvas.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import milespeele.canvas.R;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.pojo.PojoPaintExample;
import milespeele.canvas.view.ViewBrushLayoutPaintExample;
import milespeele.canvas.view.ViewTypefaceTextView;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class PaintExampleAdapter extends RecyclerView.Adapter<PaintExampleAdapter.ViewHolder> {

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
    private Context cxt;

    public PaintExampleAdapter(Context context, List<PojoPaintExample> list) {
        dataList = list;
        cxt = context;
    }

    @Override
    public PaintExampleAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.paint_example_layout,
                        viewGroup,
                        false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PaintExampleAdapter.ViewHolder viewHolder, int i) {
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

    public List<PojoPaintExample> getDataList() { return dataList; }

    public PojoPaintExample getPojoAtPosition(int ndx) { return dataList.get(ndx); }
}
