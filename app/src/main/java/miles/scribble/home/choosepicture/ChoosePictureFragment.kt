package miles.scribble.home.choosepicture

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import miles.scribble.BuildConfig
import miles.scribble.R
import miles.scribble.home.choosepicture.transformation.GrayScaleTransformation
import miles.scribble.home.choosepicture.transformation.NoTransformation
import miles.scribble.ui.glide.GlideApp
import miles.scribble.util.RecyclerSpacingDecoration
import miles.scribble.util.extensions.lazyInflate

/**
 * Created from mbpeele on 8/18/17.
 */
class ChoosePictureFragment : Fragment() {

    val ARGUMENT_URI = BuildConfig.APPLICATION_ID + ".choosepicture.uri"

    private val picture by lazyInflate<ImageView>(R.id.picture)
    private val filtersRecycler by lazyInflate<RecyclerView>(R.id.filters_recycler)

    lateinit var uri : Uri

    companion object {
        fun newInstance(uri: Uri) : ChoosePictureFragment {
            return ChoosePictureFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARGUMENT_URI, uri)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_choose_picture, container!!, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        uri = arguments.getParcelable(ARGUMENT_URI)

        picture.setImageURI(uri)

        val transformations = listOf(
                NoTransformation(),
               GrayScaleTransformation())
        filtersRecycler.addItemDecoration(RecyclerSpacingDecoration(resources.getDimensionPixelSize(R.dimen.spacing_micro)))
        filtersRecycler.layoutManager = GridLayoutManager(activity, 4)
        filtersRecycler.adapter = ImageTransformationAdapter(transformations)
    }

    private fun transformCurrentImage(transformation: BitmapTransformation) {
        GlideApp.with(this)
                .load(uri)
                .transform(transformation)
                .into(picture)
    }

    private inner class ImageTransformationAdapter(
            private val transformations: List<BitmapTransformation>) : RecyclerView.Adapter<ImageTransformationAdapter.ImageTransformationViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageTransformationViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.view_transformed_image_holder, parent, false)
            return ImageTransformationViewHolder(view).apply {
                itemView.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        transformCurrentImage(transformations[adapterPosition])
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: ImageTransformationViewHolder, position: Int) {
            holder.bind(transformations[position])
        }

        override fun getItemCount() = transformations.size

        private inner class ImageTransformationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(transformation: BitmapTransformation) {
                GlideApp.with(this@ChoosePictureFragment)
                        .load(uri)
                        .transform(transformation)
                        .into(itemView.findViewById<ImageView>(R.id.filtered_image))
            }
        }
    }
}