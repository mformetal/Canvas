package miles.scribble.home.brushpicker

import android.annotation.SuppressLint
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
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import mformetal.kodi.android.KodiDialogFragment
import mformetal.kodi.core.Kodi
import mformetal.kodi.core.api.ScopeRegistry
import mformetal.kodi.core.api.builder.bind
import mformetal.kodi.core.api.builder.get
import mformetal.kodi.core.api.injection.register
import mformetal.kodi.core.api.scoped
import mformetal.kodi.core.provider.provider
import miles.dispatch.core.Dispatcher
import miles.dispatch.core.Dispatchers
import miles.scribble.R
import miles.scribble.home.HomeActivity
import miles.scribble.home.events.BrushPickerEvents
import miles.scribble.home.events.BrushPickerReducer
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.util.android.RecyclerSpacingDecoration
import miles.scribble.util.extensions.inflater
import miles.scribble.util.extensions.safeActivity

/**
 * Created from mbpeele on 7/29/17.
 */
class BrushPickerDialogFragment : KodiDialogFragment() {

    private lateinit var recycler : RecyclerView
    private lateinit var currentBrushView : BrushExampleView

    val dispatcher : Dispatcher<BrushPickerEvents, BrushPickerEvents> by injector.register()
    val viewModel : HomeViewModel by injector.register()

    lateinit var clickDisposable : Disposable

    override fun installModule(kodi: Kodi): ScopeRegistry {
        return kodi.scopeBuilder()
                .dependsOn(scoped<HomeActivity>())
                .build(scoped<BrushPickerDialogFragment>()) {
                    bind<Dispatcher<BrushPickerEvents, BrushPickerEvents>>() using provider {
                        Dispatchers.create(get(), BrushPickerReducer())
                    }
                }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val inflater = safeActivity.inflater()
        val view = inflater.inflate(R.layout.brush_picker_fragment, null, false).apply {
            recycler = findViewById(R.id.brush_picker_recycler)
            currentBrushView = findViewById(R.id.current_brush)
        }

        currentBrushView.paint = viewModel.state.paint

        val currentColor = viewModel.state.paint.color
        val currentWidth = 5f
        val brushes = listOf(
                Brush(getString(R.string.brush_normal), PaintStyles.normal(currentColor, currentWidth)),
                Brush(getString(R.string.brush_dots), PaintStyles.dots(currentColor, currentWidth)),
                Brush(getString(R.string.brush_emboss), PaintStyles.emboss(currentColor, currentWidth)),
                Brush(getString(R.string.brush_deboss), PaintStyles.deboss(currentColor, currentWidth)),
                Brush(getString(R.string.brush_shadow), PaintStyles.normalShadow(currentColor, currentWidth)),
                Brush(getString(R.string.brush_inner_shadow), PaintStyles.innerShadow(currentColor, currentWidth)),
                Brush(getString(R.string.brush_outer_shadow), PaintStyles.outerShadow(currentColor, currentWidth)),
                Brush(getString(R.string.brush_solid_shadow), PaintStyles.solidShadow(currentColor, currentWidth))
        )

        val clickListener = PublishSubject.create<Brush>()
        clickDisposable = clickListener.subscribe { brush ->
            currentBrushView.paint = brush.paint
        }
        recycler.layoutManager = GridLayoutManager(activity, 3)
        recycler.addItemDecoration(RecyclerSpacingDecoration(resources.getDimensionPixelSize(R.dimen.spacing_normal)))
        recycler.adapter = BrushPickerAdapter(inflater, brushes, clickListener)

        return AlertDialog.Builder(safeActivity)
                .setView(view)
                .setPositiveButton(android.R.string.ok, { dialogInterface, _ ->
                    dispatcher.dispatch(BrushPickerEvents.BrushChosen(currentBrushView.paint!!))
                    dialogInterface.dismiss()
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    @SuppressLint("MissingSuperCall")
    override fun onDestroy() {
        super.onDestroy()

        clickDisposable.dispose()
    }

    private class Brush(val name: String, val paint: Paint)

    private class BrushPickerAdapter(
            val inflater: LayoutInflater,
            val brushes: List<Brush>,
            val listener: PublishSubject<Brush>) : RecyclerView.Adapter<BrushPickerViewHolder>() {

        override fun onBindViewHolder(holder: BrushPickerViewHolder, position: Int) {
            holder.bind(brushes[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrushPickerViewHolder {
            return BrushPickerViewHolder(inflater.inflate(R.layout.vh_brush_example, parent, false)).apply {
                itemView.setOnClickListener {
                    val brush = brushes[adapterPosition]
                    listener.onNext(brush)
                }
            }
        }

        override fun getItemCount(): Int {
            return brushes.size
        }
    }

    private class BrushPickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val brushName: TextView = itemView.findViewById(R.id.brush_name)
        val brushExample: BrushExampleView = itemView.findViewById(R.id.brush_example)

        fun bind(brush: Brush) {
            brushName.text = brush.name
            brushExample.paint = brush.paint
        }
    }
}