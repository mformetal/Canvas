package miles.scribble.ui;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpacingDecoration extends RecyclerView.ItemDecoration {
    
    private final int mSpace;

    public SpacingDecoration(int space) {
        this.mSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = mSpace;
        outRect.right = mSpace;
        outRect.bottom = mSpace;

        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = mSpace;
        }
    }
}
