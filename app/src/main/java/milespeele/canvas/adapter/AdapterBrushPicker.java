package milespeele.canvas.adapter;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import milespeele.canvas.R;
import milespeele.canvas.view.ViewBrushExample;
import milespeele.canvas.view.ViewTypefaceTextView;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class AdapterBrushPicker extends RecyclerView.Adapter<AdapterBrushPicker.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewTypefaceTextView paintName;
        public ViewBrushExample paintExample;

        public ViewHolder(View itemView) {
            super(itemView);
            paintName = (ViewTypefaceTextView) itemView.findViewById(R.id.paint_example_layout_text);
            paintExample = (ViewBrushExample) itemView.findViewById(R.id.paint_example_layout_paint);
        }
    }

    private List<PaintExample> dataList;

    public AdapterBrushPicker(List<PaintExample> list) {
        dataList = list;
    }

    @Override
    public AdapterBrushPicker.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_paint_example_layout,
                        viewGroup,
                        false));
    }

    @Override
    public void onBindViewHolder(AdapterBrushPicker.ViewHolder viewHolder, int i) {
        PaintExample example = dataList.get(i);

        ViewTypefaceTextView textView = viewHolder.paintName;
        textView.setText(example.getPaintName());
        textView.setTextColor(example.getColorForText());

        ViewBrushExample paintExample = viewHolder.paintExample;
        paintExample.setInitialPaint(example.getPaint());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class PaintExample {

        private String which;
        private Paint paint;
        private int color;

        public PaintExample(String which, Paint paint) {
            this.which = which;
            this.paint = paint;
        }

        public String getPaintName() {
            return which;
        }

        public Paint getPaint() { return paint; }

        public int getColorForText() {
            return (color != 0) ? color : Color.WHITE;
        }

    }
}