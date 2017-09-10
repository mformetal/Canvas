package miles.scribble.home.brushpicker

import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import miles.scribble.R
import miles.scribble.dagger.fragment.HasFragmentSubcomponentBuilders
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.ui.ViewModelDialogFragment
import miles.scribble.util.PaintStyles
import miles.scribble.util.extensions.inflater

/**
 * Created by mbpeele on 7/29/17.
 */
class BrushPickerDialogFragment : ViewModelDialogFragment<HomeViewModel>() {

    private lateinit var recycler : RecyclerView
    private lateinit var currentBrushView : BrushExampleView

    override fun inject(hasFragmentSubcomponentBuilders: HasFragmentSubcomponentBuilders): HomeViewModel {
        val builder = hasFragmentSubcomponentBuilders.getBuilder(BrushPickerDialogFragment::class.java)
        val componentBuilder = builder as BrushPickerComponent.Builder
        val component = componentBuilder.module(BrushPickerModule(this)).build()
        component.injectMembers(this)
        return component.viewModel()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val inflater = activity.inflater()
        val view = inflater.inflate(R.layout.brush_picker_fragment, null, false).apply {
            recycler = findViewById(R.id.brush_picker_recycler)
            currentBrushView = findViewById(R.id.current_brush)
        }

        currentBrushView.paint = viewModel.state.paint

        val currentColor = viewModel.state.paint.color
        val brushes = listOf(
                Brush("NORMAL", PaintStyles.normal(currentColor, 5f))
        )
        recycler.layoutManager = GridLayoutManager(activity, 3)
        recycler.adapter = BrushPickerAdapter(inflater, brushes)

        return AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(android.R.string.ok, { _, _ ->

                })
                .setNegativeButton(android.R.string.cancel, { _, _ ->

                })
                .create()
    }

    private class Brush(val name: String, val paint: Paint)

    private class BrushPickerAdapter(
            val inflater: LayoutInflater,
            val brushes: List<Brush>) : RecyclerView.Adapter<BrushPickerViewHolder>() {

        override fun onBindViewHolder(holder: BrushPickerViewHolder, position: Int) {
            holder.bind(brushes[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrushPickerViewHolder {
            return BrushPickerViewHolder(inflater.inflate(R.layout.brush_example, parent, false))
        }

        override fun getItemCount(): Int {
            return brushes.size
        }
    }

    private class BrushPickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val brushName: TextView = itemView.findViewById(R.id.brush_example)

        fun bind(brush: Brush) {
            brushName.text = brush.name
        }
    }
}