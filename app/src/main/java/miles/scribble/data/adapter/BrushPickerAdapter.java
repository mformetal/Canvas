package miles.scribble.data.adapter;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import miles.scribble.R;

import java.util.List;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class BrushPickerAdapter extends RecyclerView.Adapter<BrushPickerAdapter.BrushViewHolder> {

    public final static class BrushViewHolder extends RecyclerView.ViewHolder {
        public Button button;

        public BrushViewHolder(View itemView) {
            super(itemView);
            button = (Button) itemView.findViewById(R.id.paint_example_layout_text);
        }
    }

    private List<PaintExample> dataList;

    public BrushPickerAdapter(List<PaintExample> list) {
        dataList = list;
    }

    @Override
    public BrushViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new BrushViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_paint_example_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(BrushViewHolder viewHolder, int i) {
        PaintExample example = dataList.get(i);

        Button textView = viewHolder.button;

        String paintName = example.getPaintName();
        textView.setText(paintName);
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