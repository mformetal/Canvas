package miles.scribble.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import miles.scribble.R;
import miles.scribble.data.adapter.BrushPickerAdapter;
import miles.scribble.ui.RecyclerClickListener;
import miles.scribble.ui.SpacingDecoration;
import miles.scribble.util.PaintStyles;

import java.util.ArrayList;


/**
 * Created by milespeele on 8/8/15.
 */
public class BrushPickerLayout extends LinearLayout implements RecyclerClickListener.OnItemClickListener {

    @BindView(R.id.fragment_brush_picker_view_example) BrushExample mainExample;
    @BindView(R.id.fragment_brush_picker_view_recycler) RecyclerView recycler;

    private Paint lastSelectedPaint;
    private int trueColor;

    public BrushPickerLayout(Context context) {
        super(context);
        init();
    }

    public BrushPickerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BrushPickerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BrushPickerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        Button example = (Button) v;
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
        ArrayList<BrushPickerAdapter.PaintExample> arrayList = new ArrayList<>();
        for (String res: myResArray) {
            String name = res.toUpperCase();

            arrayList.add(new BrushPickerAdapter.PaintExample(name,
                    PaintStyles.getStyleFromName(res, lastSelectedPaint.getColor())));
        }
        recycler.setAdapter(new BrushPickerAdapter(arrayList));
    }

    public Paint getPaint() {
        lastSelectedPaint.setColor(trueColor);
        return lastSelectedPaint;
    }
}