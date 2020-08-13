package app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import app.data.photos.PhotosViewModel
import app.viewmodel.AlbumsViewModel
import com.example.photoalbum.R
import kotlinx.android.synthetic.main.add_photo__activity.*
import kotlinx.android.synthetic.main.add_photo__fragment.*
import kotlinx.android.synthetic.main.content_states.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import ui.ALBUM_ID
import ui.common.State
import ui.common.extensions.beVisibleIf
import ui.common.extensions.getColorFromAttr
import ui.common.extensions.snack
import ui.common.getErrorMessage

const val ADD_PHOTO_TITLE = "ADD_PHOTO_TITLE"
const val SPINNER_POSITION = "SPINNER_POSITION"

class AddPhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.add_photo__activity)

        if (supportFragmentManager.findFragmentByTag(AddPhotoFragment::class.java.simpleName) == null) {
            val transaction = supportFragmentManager.beginTransaction()

            transaction.replace(R.id.add_photo__activity__container, AddPhotoFragment(), AddPhotoFragment::class.java.simpleName)
            transaction.commit()
        }

        add_photo__activity__toolbar.setNavigationOnClickListener { finish() }
    }
}

class AddPhotoFragment : Fragment() {

    private val photosViewModel by viewModel<PhotosViewModel>()
    private val albumsViewModel by viewModel<AlbumsViewModel>()

    private var albumId = -1
    private var addPhotoTitle = ""
    private var spinnerPosition = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        albumId = activity?.intent?.getIntExtra(ALBUM_ID, -1) ?: -1

        if (savedInstanceState != null) {
            addPhotoTitle = savedInstanceState.getString(ADD_PHOTO_TITLE) ?: ""
            spinnerPosition = savedInstanceState.getInt(SPINNER_POSITION)
        } else {
            addPhotoTitle = ""
            spinnerPosition = -1
        }
        return inflater.inflate(R.layout.add_photo__fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (addPhotoTitle.isNotEmpty()) add_photo__fragment__photo_title.setText(addPhotoTitle)
        if (spinnerPosition != - 1) add_photo__fragment__spinner.selectedIndex = spinnerPosition

        albumsViewModel.albumsLiveData.observe(viewLifecycleOwner, Observer { albumsState ->

            add_photo__fragment__scroll.beVisibleIf(albumsState is State.Success)
            add_photo__fragment__content_states__albums.content_states__progress_bar.beVisibleIf(albumsState is State.Loading)
            add_photo__fragment__content_states__albums.content_states__nothing_to_show.beVisibleIf(albumsState is State.Success && albumsState.value.isEmpty())
            add_photo__fragment__content_states__albums.content_states__error.beVisibleIf(albumsState is State.Failure)

            add_photo__fragment__content_states__albums.content_states__button_try_again.setOnClickListener { albumsViewModel.fetchAlbums() }

            when (albumsState) {
                is State.Success -> {
                    val albumsNames = albumsState.value.map { it.title }

                    add_photo__fragment__spinner.setItems(albumsNames)
                    add_photo__fragment__spinner.setOnItemSelectedListener { _, position, _, _ ->
                        spinnerPosition = position
                        add_photo__fragment__spinner.error = null
                    }

                    if (spinnerPosition != - 1) add_photo__fragment__spinner.selectedIndex = spinnerPosition

                    add_photo__fragment__button.setOnClickListener {

                        when {
                            add_photo__fragment__photo_title.text.toString().trim().isEmpty() ->
                                setErrorsOnInputs(getString(R.string.photo_title_error), null)
                            spinnerPosition < 0 -> {
                                spinnerPosition = 0
                                setErrorsOnInputs("", null)
                                photosViewModel.uploadPhoto(add_photo__fragment__photo_title.text.toString(), albumsState.value[spinnerPosition].id)
                            }
                            else -> {
                                setErrorsOnInputs("", null)
                                photosViewModel.uploadPhoto(add_photo__fragment__photo_title.text.toString(), albumsState.value[spinnerPosition].id)
                            }
                        }
                    }
                }
                is State.Failure -> {
                    add_photo__fragment__content_states__albums.content_states__error_text.text = getString(R.string.unable_load_albums_names)
                    add_photo__fragment__content.snack(getString(R.string.add_photo_error))
                }
                null -> albumsViewModel.fetchAlbums()
            }
        })

        photosViewModel.photoUploadLiveData.observe(viewLifecycleOwner, Observer { photoUploadState ->

            add_photo__fragment__content_states__photo_upload.content_states__progress_bar.beVisibleIf(photoUploadState is State.Loading)
            add_photo__fragment__content_states__photo_upload.content_states__progress_shadow.beVisibleIf(photoUploadState is State.Loading)

            when (photoUploadState) {
                is State.Success -> {
                    Toast.makeText(context, getString(R.string.photo_uploaded_successful), Toast.LENGTH_LONG).show()
                    activity!!.finish()
                }
                is State.Failure -> add_photo__fragment__content.snack(photoUploadState.error.getErrorMessage(resources))
                null -> { }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ADD_PHOTO_TITLE, addPhotoTitle)
        outState.putInt(SPINNER_POSITION, spinnerPosition)
    }

    private fun setErrorsOnInputs(titleError: String?, albumError: String?) {
        add_photo__fragment__input__photo_title.error = titleError
        add_photo__fragment__spinner.error = albumError

        albumError?.let {
            add_photo__fragment__spinner_background.setBackgroundResource(R.drawable.background_with_error_stroke)
            add_photo__fragment__spinner_text.setTextColor(requireContext().getColorFromAttr(R.attr.colorError))
        } ?: run {
            add_photo__fragment__spinner_background.setBackgroundResource(R.drawable.background_with_grey_stroke)
            add_photo__fragment__spinner_text.setTextColor(requireContext().getColorFromAttr(R.attr.colorControlActivated))
        }
    }
}