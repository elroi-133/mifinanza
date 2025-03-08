package com.example.mifinanza

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class PrestamoNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prestamoId = intent.getIntExtra("prestamo_id", -1)
        val prestamista = intent.getStringExtra("prestamista")
        val monto = intent.getDoubleExtra("monto", 0.0)

        if (prestamoId != -1) {
            mostrarNotificacion(context, prestamoId, prestamista, monto)
        }
    }

    private fun mostrarNotificacion(context: Context, prestamoId: Int, prestamista: String?, monto: Double) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear un canal de notificación (requerido para Android 8.0+)
        val channelId = "prestamos_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Préstamos",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la aplicación al hacer clic en la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, prestamoId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Recordatorio de pago")
            .setContentText("Tienes un pago pendiente de $monto para $prestamista")
            .setSmallIcon(R.drawable.finanzas_2) // Icono de notificación
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Mostrar la notificación
        with(NotificationManagerCompat.from(context)) {
            notify(prestamoId, notification)
        }
    }
}
