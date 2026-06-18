package com.example.screen.locker.di

import com.example.screen.locker.data.network.WallpaperApi
import com.example.screen.locker.data.repository.DataStoreLockRepository
import com.example.screen.locker.data.repository.WallpaperRepositoryImpl
import com.example.screen.locker.data.receiver.ScreenReceiver
import com.example.screen.locker.domain.repository.LockRepository
import com.example.screen.locker.domain.repository.WallpaperRepository
import com.example.screen.locker.presentation.overlay.OverlayController
import com.example.screen.locker.presentation.screens.home.HomeViewModel
import com.example.screen.locker.presentation.screens.lock.LockViewModel
import com.example.screen.locker.presentation.screens.lock.PatternViewModel
import com.example.screen.locker.presentation.screens.wallpaper.WallpaperViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single { 
        Retrofit.Builder()
            .baseUrl("https://api.pexels.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WallpaperApi::class.java)
    }

    single { OverlayController(androidContext()) }
    single { ScreenReceiver() }
    single<LockRepository> { DataStoreLockRepository(androidContext()) }
    single<WallpaperRepository> { WallpaperRepositoryImpl(get()) }

    viewModel { LockViewModel(get()) }
    viewModel { PatternViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { WallpaperViewModel(get(), get()) }
}
