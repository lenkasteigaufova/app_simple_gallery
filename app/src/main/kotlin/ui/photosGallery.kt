package app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import app.data.photos.PhotosViewModel
import com.example.photoalbum.R
import kotlinx.android.synthetic.main.photos_gallery__activity.*
import kotlinx.android.synthetic.main.photos_gallery__fragment.*
import kotlinx.android.synthetic.main.photos_gallery__item.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import ui.ALBUM_ID_GALLERY
import ui.common.State
import ui.common.extensions.inflate
import ui.common.extensions.load
import ui.common.extensions.snack

const val CURRENT_ITEM_INDEX = "CURRENT_ITEM_INDEX"

class PhotosGalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.photos_gallery__activity)

        if (supportFragmentManager.findFragmentByTag(PhotosGalleryFragment::class.java.simpleName) == null) {
            val transaction = supportFragmentManager.beginTransaction()

            transaction.replace(R.id.photos_gallery__activity__container, PhotosGalleryFragment(), PhotosGalleryFragment::class.java.simpleName)
            transaction.commit()
        }

        photos_gallery__activity__toolbar.setNavigationOnClickListener { finish() }
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
    }
}

class PhotosGalleryFragment : Fragment() {

    private val photosViewModel by viewModel<PhotosViewModel>()
    private var currentItemIndex = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentItemIndex = savedInstanceState?.getInt(CURRENT_ITEM_INDEX) ?: -1
        return inflater.inflate(R.layout.photos_gallery__fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photosViewModel.photosLiveData.observe(viewLifecycleOwner, Observer {

            when (it) {
                is State.Success -> {

                    val urls = it.value.map { photo -> photo.url }
                    photos_gallery__fragment__view_pager.adapter = PhotosGalleryAdapter(urls)
                    photos_gallery__fragment__view_pager.clearOnPageChangeListeners()

                    if (currentItemIndex == -1) currentItemIndex = activity?.intent?.getIntExtra("PHOTO_POSITION", 0) ?: 0
                    photos_gallery__fragment__view_pager.currentItem = currentItemIndex
                    photos_gallery__fragment__count.text  = "${photos_gallery__fragment__view_pager.currentItem + 1}/${urls.size}"

                    photos_gallery__fragment__view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                        override fun onPageSelected(position: Int) {
                            photos_gallery__fragment__count.text = "${position + 1}/${urls.size}"
                            currentItemIndex = position
                        }
                        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                        override fun onPageScrollStateChanged(state: Int) {}
                    })

                }
                is State.Failure -> photos_gallery__fragment__content.snack(it.error.toString())
                null -> activity?.let { activity -> photosViewModel.fetchPhotos(activity.intent.getIntExtra(ALBUM_ID_GALLERY, -1)) }
            }

        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_ITEM_INDEX, currentItemIndex)
    }
}

private class PhotosGalleryAdapter(val photos: List<String>) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): View = container.inflate(R.layout.photos_gallery__item).apply {
        photos_gallery__item__photo.load(photos[position])
        container.addView(this)
    }

    override fun getCount() = photos.size
    override fun isViewFromObject(view: View, obj: Any) = view === obj
    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) = container.removeView(obj as View)
}