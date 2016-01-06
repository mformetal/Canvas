package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.adapter.AdapterBrushPicker;
import milespeele.canvas.util.RecyclerClickListener;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.PaintStyles;
import milespeele.canvas.util.SpacingDecoration;
import milespeele.canvas.util.TextUtils;


/**
 * Created by milespeele on 8/8/15.
 */
public class ViewBrushPickerLayout extends LinearLayout implements RecyclerClickListener.OnItemClickListener {

    @Bind(R.id.fragment_brush_picker_view_example) ViewBrushExample mainExample;
    @Bind(R.id.fragment_brush_picker_view_recycler) RecyclerView recycler;

    private Paint lastSelectedPaint;
    private int trueColor;

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
        setOrientation(VERTICAL);
        setClipChildren(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new StaggeredGridLayoutManager(3, VERTICAL));
        recycler.addItemDecoration(new SpacingDecoration(
                getResources().getDimensionPixelOffset(R.dimen.brush_recycler_spacing)));
        RecyclerClickListener.addTo(recycler).setOnItemClickListener(this);
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        ViewTypefaceButton example = (ViewTypefaceButton) v;

        Paint paint = example.getExamplePaint();
        lastSelectedPaint.set(paint);
        mainExample.animatePaintChange(paint);
    }

    public void setPaint(Paint paint) {
        if (lastSelectedPaint == null) {
            trueColor = paint.getColor();
            lastSelectedPaint = new Paint();
        }

        paint.setColor(Color.WHITE);
        lastSelectedPaint.set(paint);
        mainExample.setInitialPaint(paint);
        createList(getContext());
    }

    private void createList(Context context) {
        String[] myResArray = context.getResources().getStringArray(R.array.paint_examples);
        ArrayList<AdapterBrushPicker.PaintExample> arrayList = new ArrayList<>();
        for (String res: myResArray) {
            String name = res;
            if (TextUtils.containsCapital(name)) {
                String[] array = name.split("(?=\\p{Upper})");
                String first = array[0], second = array[1];
                name = TextUtils.capitalizeFirst(first) + "\n" + TextUtils.capitalizeFirst(second);
            } else {
                name = name.toUpperCase();
            }
            arrayList.add(new AdapterBrushPicker.PaintExample(name,
                    PaintStyles.getStyleFromName(res, lastSelectedPaint.getColor())));
        }
        recycler.setAdapter(new AdapterBrushPicker(arrayList));
    }

    public Paint getPaint() {
        lastSelectedPaint.setColor(trueColor);
        return lastSelectedPaint;
    }
}