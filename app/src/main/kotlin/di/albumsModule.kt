package app.di

import app.data.AppDatabase
import app.data.albums.AlbumsRepository
import app.data.albums.AlbumsRepositoryIml
import app.viewmodel.AlbumsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val albumsModule = module {
    single<AlbumsRepository> { AlbumsRepositoryIml(get(), AppDatabase.getInstance(androidContext()).albumDao()) }
    viewModel { AlbumsViewModel(get()) }
}