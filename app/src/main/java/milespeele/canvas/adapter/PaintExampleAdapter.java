package milespeele.canvas.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import milespeele.canvas.R;
import milespeele.canvas.pojo.PaintExample;
import milespeele.canvas.view.ViewBrushPickerPaintExample;
import milespeele.canvas.view.ViewTypefaceTextView;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class PaintExampleAdapter extends RecyclerView.Adapter<PaintExampleAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewTypefaceTextView paintName;
        public ViewBrushPickerPaintExample paintExample;

        public ViewHolder(View itemView) {
            super(itemView);

            paintName = (ViewTypefaceTextView) itemView.findViewById(R.id.paint_example_layout_text);
            paintExample = (ViewBrushPickerPaintExample) itemView.findViewById(R.id.paint_example_layout_paint);
        }
    }

    @Override
    public PaintExampleAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.paint_example_layout, viewGroup, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(PaintExampleAdapter.ViewHolder viewHolder, int i) {
//        // Get the data model based on position
//        PaintExample contact = mContacts.get(position);
//
//        // Set item views based on the data model
//        TextView textView = viewHolder.nameTextView;
//        textView.setText(contact.getName());
//
//        Button button = viewHolder.messageButton;
//
//        if (contact.isOnline()) {
//            button.setText("Message");
//            button.setEnabled(true);
//        }
//        else {
//            button.setText("Offline");
//            button.setEnabled(false);
//        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
