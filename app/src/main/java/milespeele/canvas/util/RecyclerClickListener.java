package milespeele.canvas.util;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import milespeele.canvas.R;

public class RecyclerClickListener {

    private final RecyclerView mRecyclerView;
    private OnItemClickListener mOnItemClickListener;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                mOnItemClickListener.onItemClicked(mRecyclerView, holder, v);
            }
        }
    };

    private RecyclerView.OnChildAttachStateChangeListener mAttachListener
            = new RecyclerView.OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {
            if (mOnItemClickListener != null) {
                view.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {
            view.setOnClickListener(null);
        }
    };

    private RecyclerClickListener(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.setTag(R.id.recycler_click_listener, this);
        mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
    }

    public static RecyclerClickListener addTo(RecyclerView view) {
        RecyclerClickListener listener = (RecyclerClickListener) view.getTag(R.id.recycler_click_listener);
        if (listener == null) {
            listener = new RecyclerClickListener(view);
        }
        return listener;
    }

    public static RecyclerClickListener removeFrom(RecyclerView view) {
        RecyclerClickListener listener = (RecyclerClickListener) view.getTag(R.id.recycler_click_listener);
        if (listener != null) {
            listener.detach(view);
        }
        return listener;
    }

    public RecyclerClickListener setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
        return this;
    }

    private void detach(RecyclerView view) {
        view.removeOnChildAttachStateChangeListener(mAttachListener);
        view.setTag(R.id.recycler_click_listener, null);
    }

    public interface OnItemClickListener {

        void onItemClicked(RecyclerView recyclerView, RecyclerView.ViewHolder holder, View v);
    }
}
