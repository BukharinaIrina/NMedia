package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.AppActivity
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.service.notifications.*
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        val name = getString(R.string.channel_remote_name)
        val descriptionText = getString(R.string.channel_remote_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val userId = AppAuth.getInstance().authFlow.value?.id
        val push = gson.fromJson(message.data[content], PushMessage::class.java)

        when (push.recipientId) {
            userId, null -> {
                handleNotification(push)
            }

            else -> AppAuth.getInstance().sendPushToken()
        }

        try {
            message.data[action]?.let {
                when (Action.valueOf(it)) {
                    Action.LIKE -> handleLike(
                        gson.fromJson(
                            message.data[content],
                            Like::class.java
                        )
                    )

                    Action.POST -> handleNewPost(
                        gson.fromJson(
                            message.data[content],
                            NewPost::class.java
                        )
                    )
                }
            }
        } catch (error: IllegalArgumentException) {
            errorAction(gson.fromJson(message.data[content], ErrorAction::class.java))
        }
    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
        println(token)
    }

    private fun handleNotification(push: PushMessage) {
        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, AppActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(
                getString(
                    R.string.notification
                )
            )
            .setContentText(
                push.content
            )
            .setAutoCancel(true)
            .setContentIntent(notifyPendingIntent)
            .build()

        notify(notification)
    }

    private fun handleLike(content: Like) {
        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, AppActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.userName,
                    content.postAuthor,
                )
            )
            .setAutoCancel(true)
            .setContentIntent(notifyPendingIntent)
            .build()

        notify(notification)
    }

    private fun handleNewPost(content: NewPost) {
        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, AppActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(
                getString(
                    R.string.notification_user_new_posted,
                    content.postAuthor
                )
            )
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.postContent))
            .setAutoCancel(true)
            .setContentIntent(notifyPendingIntent)
            .build()

        notify(notification)
    }

    private fun errorAction(content: ErrorAction) {
        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, AppActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(
                getString(
                    R.string.error_action_title,
                )
            )
            .setContentText(
                content.textErrorAction
            )
            .setAutoCancel(true)
            .setContentIntent(notifyPendingIntent)
            .build()

        notify(notification)
    }

    private fun notify(notification: Notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        }
    }
}






