package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import milespeele.canvas.R;
import milespeele.canvas.adapter.AdapterPojoPaintExamples;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.pojo.PojoPaintExample;
import milespeele.canvas.util.ItemClickSupport;
import milespeele.canvas.util.WrapContentLinearLayoutManager;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class ViewPaintExamplesRecycler extends RecyclerView implements ItemClickSupport.OnItemClickListener {

    private AdapterPojoPaintExamples adapter;

    private int color;

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
        ViewBrushLayoutPaintExampleLayout layout = (ViewBrushLayoutPaintExampleLayout) v;

        adapter.getPojoAtPosition(position).setColorForText(getResources().getColor(R.color.spirit_gold));

        ((ViewBrushLayout) getParent()).changeExamplePaint(layout.getPaintFromExample());

        List<PojoPaintExample> list = adapter.getDataList();
        for (int i = 0; i < list.size(); i++) {
            if (i != position) {
                PojoPaintExample example = list.get(i);
                example.setColorForText(Color.WHITE);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void createList(Context context) {
        String[] myResArray = context.getResources().getStringArray(R.array.paint_examples);
        ArrayList<PojoPaintExample> arrayList = new ArrayList<>();
        for (String res: myResArray) {
            arrayList.add(new PojoPaintExample(res.substring(0,1).toUpperCase() + res.substring(1),
                    PaintStyles.getStyleFromName(res, getColor())));
        }
        adapter = new AdapterPojoPaintExamples(arrayList);
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(adapter);
        alphaInAnimationAdapter.setFirstOnly(false);
        setAdapter(alphaInAnimationAdapter);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return (color != 0) ? color : Color.WHITE;
    }
}
