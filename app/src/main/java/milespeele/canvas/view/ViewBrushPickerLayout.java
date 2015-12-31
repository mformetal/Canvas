package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.adapter.AdapterBrushPicker;
import milespeele.canvas.util.ItemClickSupport;
import milespeele.canvas.util.PaintStyles;
import milespeele.canvas.util.WrapContentLinearLayoutManager;


/**
 * Created by milespeele on 8/8/15.
 */
public class ViewBrushPickerLayout extends LinearLayout implements ItemClickSupport.OnItemClickListener {

    @Bind(R.id.fragment_brush_picker_view_example) ViewBrushExample mainExample;
    @Bind(R.id.fragment_brush_picker_view_recycler) RecyclerView recycler;

    private Paint lastSelectedPaint;

    public ViewBrushPickerLayout(Context context) {
        super(context);
        init();
    }

    public ViewBrushPickerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewBrushPickerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewBrushPickerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        lastSelectedPaint = new Paint();

        setOrientation(VERTICAL);
        setClipChildren(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        ItemClickSupport.addTo(recycler).setOnItemClickListener(this);
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        ViewBrushExample example = (ViewBrushExample) ((LinearLayout) v).getChildAt(1);

        Paint paint = example.getPaint();
        lastSelectedPaint.set(paint);
        mainExample.animatePaintChange(paint);
    }

    public void setPaint(Paint paint) {
        lastSelectedPaint.set(paint);
        mainExample.setInitialPaint(paint);
        createList(getContext());
    }

    private void createList(Context context) {
        String[] myResArray = context.getResources().getStringArray(R.array.paint_examples);
        ArrayList<AdapterBrushPicker.PaintExample> arrayList = new ArrayList<>();
        for (String res: myResArray) {
            arrayList.add(new AdapterBrushPicker.PaintExample(
                    res.substring(0,1).toUpperCase() + res.substring(1),
                    PaintStyles.getStyleFromName(res, lastSelectedPaint.getColor())));
        }
        recycler.setAdapter(new AdapterBrushPicker(arrayList));
    }

    public Paint getPaint() {
        return lastSelectedPaint;
    }
}