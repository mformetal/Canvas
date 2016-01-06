package milespeele.canvas.adapter;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.grantland.widget.AutofitTextView;
import milespeele.canvas.R;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.TextUtils;
import milespeele.canvas.view.ViewTypefaceButton;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class AdapterBrushPicker extends RecyclerView.Adapter<AdapterBrushPicker.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewTypefaceButton button;

        public ViewHolder(View itemView) {
            super(itemView);
            button = (ViewTypefaceButton) itemView.findViewById(R.id.paint_example_layout_text);
        }
    }

    private List<PaintExample> dataList;

    public AdapterBrushPicker(List<PaintExample> list) {
        dataList = list;
    }

    @Override
    public AdapterBrushPicker.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_paint_example_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(AdapterBrushPicker.ViewHolder viewHolder, int i) {
        PaintExample example = dataList.get(i);

        ViewTypefaceButton textView = viewHolder.button;

        String paintName = example.getPaintName();
        Paint paint = example.getPaint();

        textView.setText(paintName);
        textView.setPaint(paint);

        if (!TextUtils.containsNewLine(paintName)) {
            textView.setSingleLine();
            textView.setMaxLines(1);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class PaintExample {

        private String which;
        private Paint paint;

        public PaintExample(String which, Paint paint) {
            this.which = which;
            this.paint = paint;
        }

        public String getPaintName() {
            return which;
        }

        public Paint getPaint() { return paint; }

    }
}