import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.mmk.kmpnotifier.notification.NotificationImage
import com.mmk.kmpnotifier.notification.Notifier
import com.mmk.kmpnotifier.notification.NotifierManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

@Composable
fun SseScreen() {
    val log = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        println("Connecting to SSE...")
        connectToSSE { notification ->
            log.add("Title: ${notification.title}, Body: ${notification.body}")
            val notifier = NotifierManager.getLocalNotifier()
            notifier.notify {
                id = Random.nextInt(0, Int.MAX_VALUE)
                title = notification.title
                body = notification.body
                payloadData = mapOf(
                    Notifier.KEY_URL to "https://github.com/mirzemehdi/KMPNotifier/",
                    "extraKey" to "randomValue"
                )
                image =
                    NotificationImage.Url("https://github.com/user-attachments/assets/a0f38159-b31d-4a47-97a7-cc230e15d30b")
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(log) { item ->
            Text(text = item)
        }
    }
}

// Define a data class to represent the notification
data class Notification(val title: String, val body: String)

// Example usage
suspend fun connectToSSE(
    onNotification: (Notification) -> Unit
) {
    withContext(Dispatchers.IO) {
        val url = URL("http://localhost:8080/sse")
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("Accept", "text/event-stream")
        connection.doInput = true

        parseSSEStream(connection, onNotification)
    }
}

fun parseSSEStream(connection: HttpURLConnection, onNotification: (Notification) -> Unit) {
    try {
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val gson = Gson()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            println("Received line: $line")
            if (line!!.startsWith("data:")) {
                // Parse the JSON string into a Notification object
                val jsonData = line!!.substringAfter("data:").trim()
                val notification = gson.fromJson(jsonData, Notification::class.java)
                onNotification(notification)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        connection.disconnect()
    }
}