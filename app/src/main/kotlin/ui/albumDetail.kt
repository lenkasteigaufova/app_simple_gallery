package ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import app.data.photos.PhotoEntity
import app.data.photos.PhotosViewModel
import app.ui.PhotosGalleryActivity
import com.afollestad.recyclical.ViewHolder
import com.afollestad.recyclical.datasource.dataSourceTypedOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.example.photoalbum.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.album_detail__activity.*
import kotlinx.android.synthetic.main.album_detail__fragment.*
import kotlinx.android.synthetic.main.album_detail__fragment__photo_item.*
import org.koin.android.viewmodel.ext.android.viewModel
import ui.common.State
import ui.common.extensions.beVisibleIf
import ui.common.extensions.load
import ui.common.extensions.snack
import ui.common.getErrorMessage
import app.ui.AddPhotoActivity
import kotlinx.android.synthetic.main.content_states.view.*

const val ALBUM_ID_GALLERY = "ALBUM_ID_GALLERY"
const val PHOTO_POSITION = "PHOTO_POSITION"

class AlbumDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.album_detail__activity)

        if (supportFragmentManager.findFragmentByTag(AlbumDetailFragment::class.java.simpleName) == null) {
            val transaction = supportFragmentManager.beginTransaction()

            transaction.replace(R.id.album_detail__activity__container, AlbumDetailFragment(), AlbumDetailFragment::class.java.simpleName)
            transaction.commit()
        }

        album_detail__activity__toolbar.setNavigationOnClickListener { finish() }
        album_detail__activity__toolbar.title = intent?.getStringExtra(ALBUM_NAME) ?: ""
    }
}

class AlbumDetailFragment : Fragment() {

    private val photosViewModel by viewModel<PhotosViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.album_detail__fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val albumId = activity?.intent?.getIntExtra(ALBUM_ID, -1) ?: -1

        photosViewModel.photosLiveData.observe(viewLifecycleOwner, Observer { albumDetailState ->

            album_detail__fragment__content_states.content_states__progress_bar.beVisibleIf(albumDetailState is State.Loading)
            album_detail__fragment__content_states.content_states__nothing_to_show.beVisibleIf(
                (albumDetailState is State.Success && albumDetailState.value.isEmpty()) || albumDetailState == null)
            album_detail__fragment__content_states.content_states__error.beVisibleIf(albumDetailState is State.Failure)

            album_detail__fragment__content_states.content_states__button_try_again.setOnClickListener {
                photosViewModel.fetchPhotos(albumId)
            }

            when (albumDetailState) {
                is State.Success -> {

                    album_detail__fragment__recycler.layoutManager = GridLayoutManager(context, 2)

                    album_detail__fragment__recycler.setup {
                        withDataSource(dataSourceTypedOf(albumDetailState.value))
                        sharedElementEnterTransition

                        withItem<PhotoEntity, PhotosViewHolder>(R.layout.album_detail__fragment__photo_item) {
                            onBind(::PhotosViewHolder) { _, photo ->
                                album_detail__photo_item__photo.load(photo.thumbnailUrl)
                                album_detail__photo_item__name.text = photo.title
                            }
                            onClick { index ->
                                val intent = Intent(activity, PhotosGalleryActivity::class.java)
                                intent.putExtra(ALBUM_ID_GALLERY, this.item.albumId)
                                intent.putExtra(PHOTO_POSITION, index)
                                startActivity(intent)
                            }
                        }
                    }

                    album_detail__fragment__add_photo.setOnClickListener {
                        if (albumId != -1) {
                            val intent = Intent(activity, AddPhotoActivity::class.java)
                            intent.putExtra(ALBUM_ID, albumId)
                            startActivity(intent)
                        } else {
                            album_detail__fragment__content.snack(getString(R.string.add_photo_error))
                        }
                    }
                }
                is State.Failure -> {
                    album_detail__fragment__content.snack(albumDetailState.error.getErrorMessage(resources))

                    album_detail__fragment__add_photo.setOnClickListener {
                        album_detail__fragment__content.snack(getString(R.string.add_photo_error))
                    }
                }
                null -> {
                    if (albumId != -1) photosViewModel.fetchPhotos(albumId)
                    else album_detail__fragment__content.snack(getString(R.string.wrong_album_id))
                }
            }
        })
    }
}

class PhotosViewHolder(override val containerView: View) : ViewHolder(containerView), LayoutContainer