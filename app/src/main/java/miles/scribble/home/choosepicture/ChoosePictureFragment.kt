package miles.scribble.home.choosepicture

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import mformetal.kodi.android.KodiFragment
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
import miles.scribble.home.events.HomeActivityEvents
import miles.scribble.home.events.HomeActivityEventsReducer
import miles.scribble.ui.glide.GlideApp
import miles.scribble.util.android.RecyclerSpacingDecoration
import miles.scribble.util.extensions.lazyInflate

/**
 * Created from mbpeele on 8/18/17.
 */
class ChoosePictureFragment : KodiFragment(), LoaderManager.LoaderCallbacks<Cursor> {

    private lateinit var adapter : ImageLoadingAdapter
    private val choosePicture by lazyInflate<ImageView>(R.id.choose_picture)
    private val chosenPicture by lazyInflate<ImageView>(R.id.chosen_picture)
    private val recycler by lazyInflate<RecyclerView>(R.id.choose_picture_recycler)
    private var chosenUri : Uri? = null

    private val dispatcher : Dispatcher<HomeActivityEvents, HomeActivityEvents> by injector.register()

    companion object {
        fun newInstance() : ChoosePictureFragment {
            return ChoosePictureFragment()
        }
    }

    override fun installModule(kodi: Kodi): ScopeRegistry {
        return kodi.scopeBuilder()
                .dependsOn(scoped<HomeActivity>())
                .build(scoped<ChoosePictureFragment>()) {
                    bind<Dispatcher<HomeActivityEvents, HomeActivityEvents>>() using provider {
                        Dispatchers.create(get(), HomeActivityEventsReducer())
                    }
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.choose_picture, container!!, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        choosePicture.setOnClickListener {
            chosenUri?.let {
                dispatcher.dispatch(HomeActivityEvents.PictureChosen(activity!!.contentResolver, it))

                activity!!.supportFragmentManager.beginTransaction()
                        .remove(this@ChoosePictureFragment)
                        .commit()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity!!.supportLoaderManager.initLoader(0, null, this@ChoosePictureFragment)
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

            adapter = ImageLoadingAdapter(uris, LayoutInflater.from(activity))
            adapter.setHasStableIds(true)

            recycler.addItemDecoration(RecyclerSpacingDecoration(resources.getDimensionPixelSize(R.dimen.spacing_micro)))
            recycler.layoutManager = GridLayoutManager(activity, 4)
            recycler.adapter = adapter
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns._ID)
        return CursorLoader(activity!!, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }

    private fun setChosenPicture(uri: Uri, imageView: ImageView) {
        chosenUri = uri

        GlideApp.with(this@ChoosePictureFragment)
                .load(uri)
                .centerCrop()
                .into(imageView)
    }

    private inner class ImageLoadingAdapter(private var uris: List<Uri>,
                                            private val inflater: LayoutInflater)
        : RecyclerView.Adapter<ImageLoadingAdapter.ImageViewHolder>() {

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
                        .into(itemView.findViewById<ImageView>(R.id.image))
            }
        }
    }
}