package milespeele.canvas.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import milespeele.canvas.R;
import milespeele.canvas.pojo.PojoPaintExample;
import milespeele.canvas.view.ViewBrushPickerPaintExample;
import milespeele.canvas.view.ViewTypefaceTextView;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class AdapterPojoPaintExamples extends RecyclerView.Adapter<AdapterPojoPaintExamples.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewTypefaceTextView paintName;
        public ViewBrushPickerPaintExample paintExample;

        public ViewHolder(View itemView) {
            super(itemView);
            paintName = (ViewTypefaceTextView) itemView.findViewById(R.id.paint_example_layout_text);
            paintExample = (ViewBrushPickerPaintExample) itemView.findViewById(R.id.paint_example_layout_paint);
        }
    }

    private List<PojoPaintExample> dataList;

    public AdapterPojoPaintExamples(List<PojoPaintExample> list) {
        dataList = list;
    }

    @Override
    public AdapterPojoPaintExamples.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_paint_example_layout,
                        viewGroup,
                        false));
    }

    @Override
    public void onBindViewHolder(AdapterPojoPaintExamples.ViewHolder viewHolder, int i) {
        PojoPaintExample example = dataList.get(i);

        ViewTypefaceTextView textView = viewHolder.paintName;
        textView.setText(example.getPaintName());
        textView.setTextColor(example.getColorForText());

        ViewBrushPickerPaintExample paintExample = viewHolder.paintExample;
        paintExample.setPaint(example.getPaint());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

}
