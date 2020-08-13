package ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.viewmodel.AlbumsViewModel
import com.afollestad.recyclical.ViewHolder
import com.afollestad.recyclical.datasource.dataSourceTypedOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.example.photoalbum.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.albums__fragment__album_item.*
import kotlinx.android.synthetic.main.albums__fragment.*
import org.koin.android.viewmodel.ext.android.viewModel
import ui.common.State
import ui.common.extensions.beVisibleIf
import ui.common.extensions.snack
import android.content.Intent
import app.data.albums.AlbumItemEntity
import kotlinx.android.synthetic.main.content_states.view.*
import ui.common.getErrorMessage

const val ALBUM_ID = "ALBUM_ID"
const val ALBUM_NAME = "ALBUM_NAME"

class AlbumsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.albums__activity)

        if (supportFragmentManager.findFragmentByTag(AlbumsFragment::class.java.simpleName) == null) {
            val transaction = supportFragmentManager.beginTransaction()

            transaction.replace(R.id.albums__activity__container, AlbumsFragment(), AlbumsFragment::class.java.simpleName)
            transaction.commit()
        }
    }
}

class AlbumsFragment : Fragment() {

    private val albumsViewModel by viewModel<AlbumsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.albums__fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dividerItemDecoration = DividerItemDecoration(context!!, LinearLayoutManager.VERTICAL)
        ContextCompat.getDrawable(context!!, R.drawable.recycler_divider)?.also {
            dividerItemDecoration.setDrawable(it)
        }

        albums_fragment__recycler.addItemDecoration(dividerItemDecoration)

        albumsViewModel.albumsLiveData.observe(viewLifecycleOwner, Observer { albumsState ->

            albums__fragment__content_states.content_states__progress_bar.beVisibleIf(albumsState is State.Loading)
            albums__fragment__content_states.content_states__nothing_to_show.beVisibleIf(albumsState is State.Success && albumsState.value.isEmpty())
            albums__fragment__content_states.content_states__error.beVisibleIf(albumsState is State.Failure)

            albums__fragment__content_states.content_states__button_try_again.setOnClickListener { albumsViewModel.fetchAlbums() }

            when (albumsState) {
                is State.Success -> {
                    albums_fragment__recycler.setup {
                        withDataSource(dataSourceTypedOf(albumsState.value))

                        withItem<AlbumItemEntity, AlbumViewHolder>(R.layout.albums__fragment__album_item) {
                            onBind(::AlbumViewHolder) { _, album ->
                                album_item__name.text = album.title
                            }
                            onClick {
                                val intent = Intent(activity, AlbumDetailActivity::class.java)
                                intent.putExtra(ALBUM_ID, this.item.id)
                                intent.putExtra(ALBUM_NAME, this.item.title)
                                startActivity(intent)
                            }
                        }
                    }
                }
                is State.Failure -> albums__fragment__content.snack(albumsState.error.getErrorMessage(resources))
                null -> albumsViewModel.fetchAlbums()
            }
        })
    }
}

class AlbumViewHolder(override val containerView: View) : ViewHolder(containerView), LayoutContainer