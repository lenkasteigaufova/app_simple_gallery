package app

import android.app.Application
import app.di.albumsModule
import app.di.apiModule
import app.di.photosModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()

            androidContext(this@App)

            modules(apiModule)
            modules(albumsModule)
            modules(photosModule)
        }
    }
}