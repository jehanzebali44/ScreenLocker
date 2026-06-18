package com.example.screen.locker.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.screen.locker.domain.repository.LockRepository
import com.example.screen.locker.domain.repository.LockType
import com.example.screen.locker.presentation.overlay.OverlayController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ScreenReceiver : BroadcastReceiver(), KoinComponent {

    private val overlayController: OverlayController by inject()
    private val lockRepository: LockRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val serviceIntent = Intent(context, com.example.screen.locker.data.service.LockService::class.java)
                context.startForegroundService(serviceIntent)
            }
            Intent.ACTION_SCREEN_OFF -> {
                // Show overlay immediately on screen off so it's ready for wake
                prepareOverlay(context)
            }
            Intent.ACTION_SCREEN_ON -> {
                prepareOverlay(context)
            }
        }
    }

    private fun prepareOverlay(context: Context) {
        if (Settings.canDrawOverlays(context)) {
            scope.launch {
                val lockType = lockRepository.getLockType().first()
                if (lockType != LockType.NONE) {
                    overlayController.showOverlay()
                }
            }
        }
    }
}
