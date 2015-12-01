package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

import milespeele.canvas.R;
import milespeele.canvas.adapter.AdapterPojoPaintExamples;
import milespeele.canvas.util.PaintStyles;
import milespeele.canvas.pojo.PojoPaintExample;
import milespeele.canvas.util.ItemClickSupport;
import milespeele.canvas.util.WrapContentLinearLayoutManager;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class ViewPaintExamplesRecycler extends RecyclerView implements ItemClickSupport.OnItemClickListener {

    private int color;

    private AdapterPojoPaintExamples adapter;

    public ViewPaintExamplesRecycler(Context context) {
        super(context);
        init();
    }

    public ViewPaintExamplesRecycler(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewPaintExamplesRecycler(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setHasFixedSize(true);
        createList(getContext());
        setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        ItemClickSupport.addTo(this).setOnItemClickListener(this);
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        ViewBrushPickerPaintExample example = (ViewBrushPickerPaintExample) ((LinearLayout) v).getChildAt(1);

        ((ViewBrushPickerLayout) getParent()).changeExamplePaint(example.getExamplePaint());
    }

    private void createList(Context context) {
        String[] myResArray = context.getResources().getStringArray(R.array.paint_examples);
        ArrayList<PojoPaintExample> arrayList = new ArrayList<>();
        for (String res: myResArray) {
            arrayList.add(new PojoPaintExample(res.substring(0,1).toUpperCase() + res.substring(1),
                    PaintStyles.getStyleFromName(res, getColor())));
        }
        setAdapter((adapter = new AdapterPojoPaintExamples(arrayList)));
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return (color != 0) ? color : Color.WHITE;
    }
}
