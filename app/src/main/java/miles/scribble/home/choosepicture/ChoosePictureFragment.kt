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
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.util.FixedPreloadSizeProvider
import miles.scribble.R
import miles.scribble.ui.glide.GlideApp
import miles.scribble.util.RecyclerSpacingDecoration
import miles.scribble.util.extensions.lazyInflate

/**
 * Created from mbpeele on 8/18/17.
 */
class ChoosePictureFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    private lateinit var adapter : ImageLoadingAdapter
    private val toolbar by lazyInflate<Toolbar>(R.id.choose_picture_toolbar)
    private val chosenPicture by lazyInflate<ImageView>(R.id.chosen_picture)
    private val recycler by lazyInflate<RecyclerView>(R.id.choose_picture_recycler)

    companion object {
        fun newInstance() : ChoosePictureFragment {
            return ChoosePictureFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.supportLoaderManager.initLoader(0, null, this)

        return inflater.inflate(R.layout.choose_picture, container!!, false)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        if (!cursor.isClosed) {
            cursor.moveToFirst()
            val defaultUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)).toLong())
            setChosenPicture(defaultUri, chosenPicture)

            val uris = mutableListOf<Uri>()
            uris.add(defaultUri)
            while (cursor.moveToNext()) {
                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)).toLong())
                uris.add(uri)
            }

            cursor.close()

            val width = view!!.measuredWidth / 4
            val preLoadSizeProvider = FixedPreloadSizeProvider<Uri>(width, width)
            val preLoadModelProvider = object : ListPreloader.PreloadModelProvider<Uri> {
                override fun getPreloadRequestBuilder(item: Uri): RequestBuilder<*> {
                    return GlideApp.with(this@ChoosePictureFragment)
                            .load(item)
                            .centerCrop()
                            .override(width, width)
                            .transition(DrawableTransitionOptions.withCrossFade())
                }

                override fun getPreloadItems(position: Int): MutableList<Uri> {
                    return mutableListOf(uris[position])
                }
            }
            val preloader = RecyclerViewPreloader<Uri>(
                    Glide.with(this), preLoadModelProvider, preLoadSizeProvider, 8)

            adapter = ImageLoadingAdapter(uris, LayoutInflater.from(activity), width, width)
            adapter.setHasStableIds(true)

            recycler.addItemDecoration(RecyclerSpacingDecoration(resources.getDimensionPixelSize(R.dimen.spacing_micro)))
            recycler.addOnScrollListener(preloader)
            recycler.layoutManager = GridLayoutManager(activity, 4)
            recycler.adapter = adapter
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns._ID)
        return CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }

    private fun setChosenPicture(uri: Uri, imageView: ImageView) {
        GlideApp.with(this@ChoosePictureFragment)
                .load(uri)
                .centerCrop()
                .into(imageView)
    }

    private inner class ImageLoadingAdapter(private var uris: List<Uri>,
                                            private val inflater: LayoutInflater,
                                            private val width: Int,
                                            private val height: Int) : RecyclerView.Adapter<ImageLoadingAdapter.ImageViewHolder>() {

        override fun getItemId(position: Int): Long {
            return ContentUris.parseId(uris[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = inflater.inflate(R.layout.choose_picture_adapter_view_holder, parent, false)
            return ImageViewHolder(view).apply {
                itemView.setOnClickListener {
                    val uri = uris[adapterPosition]
                    setChosenPicture(uri, chosenPicture)
                }
            }
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val uri = uris[position]
            holder.bind(uri)
        }

        override fun getItemCount() = uris.size

        private inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(uri: Uri) {
                GlideApp.with(this@ChoosePictureFragment)
                        .load(uri)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .override(width, height)
                        .into(itemView.findViewById(R.id.image))
            }
        }
    }
}