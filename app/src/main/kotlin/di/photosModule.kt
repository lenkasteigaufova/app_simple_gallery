package app.di

import app.data.AppDatabase
import app.data.photos.PhotosRepository
import app.data.photos.PhotosRepositoryIml
import app.data.photos.PhotosViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val photosModule = module {
    single<PhotosRepository> { PhotosRepositoryIml(get(), AppDatabase.getInstance(androidContext()).photoDao()) }
    viewModel { PhotosViewModel(get()) }
}