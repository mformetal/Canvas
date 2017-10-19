package miles.scribble.util.android

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

class RecyclerSpacingDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        outRect.left = spacing
        outRect.right = spacing
        outRect.bottom = spacing

        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            val spanCount = layoutManager.spanCount
            val adapterPosition = parent.getChildAdapterPosition(view)
            if (adapterPosition < spanCount) {
                outRect.top = spacing
            }
        } else {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = spacing
            }
        }
    }
}