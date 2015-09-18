package milespeele.canvas.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import milespeele.canvas.R;
import milespeele.canvas.adapter.PaintExampleAdapter;
import milespeele.canvas.pojo.PaintExample;
import milespeele.canvas.util.ItemClickSupport;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.WrapContentLinearLayoutManager;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class ViewPaintExamplesRecycler extends RecyclerView implements ItemClickSupport.OnItemClickListener {

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
        setClipChildren(false);
        createList(getContext());
        setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        ItemClickSupport.addTo(this).setOnItemClickListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        ((ViewBrushLayoutPaintExampleLayout) getChildAt(0)).highlight();
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        ViewBrushLayoutPaintExampleLayout layout = (ViewBrushLayoutPaintExampleLayout) v;

        if (!layout.isHasColorChangedToGold()) {
            layout.highlight();

            ((ViewBrushLayout) getParent()).changeExamplePaint(layout.getPaintFromExample());

            for (int i = 0; i < getChildCount(); i++) {
                View ndx = getChildAt(i);
                if (ndx != layout) {
                    ((ViewBrushLayoutPaintExampleLayout) ndx).dehighlight();
                }
            }
        }
    }

    public void changeChildViewPaintThickness(float thickness) {
        for (int i = 0; i < getChildCount(); i++) {
            View ndx = getChildAt(i);
            ((ViewBrushLayoutPaintExampleLayout) ndx).changeExamplePaintViewThickness(thickness);
        }
    }

    public void createList(Context context) {
        String[] myResArray = context.getResources().getStringArray(R.array.paint_examples);
        ArrayList<PaintExample> arrayList = new ArrayList<>();
        for (String res: myResArray) {
            arrayList.add(new PaintExample(res));
        }
        setAdapter(new PaintExampleAdapter(context, arrayList));
    }
}
