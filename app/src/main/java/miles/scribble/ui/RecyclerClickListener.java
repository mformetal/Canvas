package miles.scribble.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import miles.scribble.R;

public class RecyclerClickListener {

    private final RecyclerView mRecyclerView;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                try {
                    RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                    mOnItemClickListener.onItemClicked(mRecyclerView, holder.getAdapterPosition(), v);
                } catch (IllegalArgumentException e) {
                    mOnItemClickListener.onItemClicked(mRecyclerView, -1, v);
                }
            }
        }
    };

    private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mOnItemLongClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                return mOnItemLongClickListener.onItemLongClicked(mRecyclerView, holder.getAdapterPosition(), v);
            }
            return false;
        }
    };

    private RecyclerView.OnChildAttachStateChangeListener mAttachListener
            = new RecyclerView.OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {
            if (mOnItemClickListener != null) {
                view.setOnClickListener(mOnClickListener);

                if (view instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) view;
                    for (int i = 0; i < vg.getChildCount(); i++) {
                        vg.getChildAt(i).setOnClickListener(mOnClickListener);
                    }
                }
            }

            if (mOnItemLongClickListener != null) {
                view.setOnLongClickListener(mOnLongClickListener);

                if (view instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) view;
                    for (int i = 0; i < vg.getChildCount(); i++) {
                        vg.getChildAt(i).setOnLongClickListener(mOnLongClickListener);
                    }
                }
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {

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

    public RecyclerClickListener setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
        return this;
    }

    private void detach(RecyclerView view) {
        view.removeOnChildAttachStateChangeListener(mAttachListener);
        view.setTag(R.id.recycler_click_listener, null);
    }

    public interface OnItemClickListener {

        void onItemClicked(RecyclerView recyclerView, int position, View v);
    }

    public interface OnItemLongClickListener {

        boolean onItemLongClicked(RecyclerView recyclerView, int position, View v);
    }
}