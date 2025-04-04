package org.example.sseclient

import SseScreen
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mmk.kmpnotifier.extensions.composeDesktopResourcesPath
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import java.io.File

fun main() = application {
    NotifierManager.initialize(
        NotificationPlatformConfiguration.Desktop(
            showPushNotification = true,
            notificationIconPath = composeDesktopResourcesPath() + File.separator + "ic_notification.png"
        )
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "SSEClient",
    ) {
        SseScreen()
    }
}