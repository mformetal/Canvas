package miles.scribble.home.choosepicture

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import miles.scribble.R
import miles.scribble.ui.glide.GlideApp
import miles.scribble.util.extensions.lazyInflate




/**
 * Created from mbpeele on 8/18/17.
 */
class ChoosePictureFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    private val toolbar by lazyInflate<Toolbar>(R.id.choose_picture_toolbar)
    private val recycler by lazyInflate<RecyclerView>(R.id.choose_picture_recycler)

    companion object {
        fun newInstance() : ChoosePictureFragment {
            return ChoosePictureFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.choose_picture, container!!, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity.supportLoaderManager.initLoader(0, null, this)

        recycler.layoutManager = GridLayoutManager(activity, 4)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        recycler.adapter = ImageLoadingAdapter(data,
                resources.getDimensionPixelSize(R.dimen.spacing_micro),
                LayoutInflater.from(activity))
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns._ID)
        return CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) { }

    private inner class ImageLoadingAdapter(private val cursor: Cursor,
                                            private val margin: Int,
                                            private val inflater: LayoutInflater) : RecyclerView.Adapter<ImageLoadingAdapter.ImageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = inflater.inflate(R.layout.viewholder_wrap_content_image, parent, false)
            val layoutParams = view.layoutParams as GridLayoutManager.LayoutParams
            layoutParams.height = (parent.measuredHeight / 4) - margin
            layoutParams.width = (parent.measuredWidth / 4) - margin
            view.layoutParams = layoutParams
            return ImageViewHolder(view).apply {
                itemView.setOnClickListener {

                }
            }
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            cursor.moveToPosition(position)

            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)).toLong())
            holder.bind(uri)
        }

        override fun getItemCount() = cursor.count

        private inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(uri: Uri) {
                GlideApp.with(this@ChoosePictureFragment)
                        .load(uri)
                        .into(itemView.findViewById(R.id.image))
            }
        }
    }
}