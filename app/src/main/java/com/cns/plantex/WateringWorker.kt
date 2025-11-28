// app/src/main/java/com/cns/plantex/WateringWorker.kt
package com.cns.plantex

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cns.plantex.util.NotificationHelper

class WateringWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.createNotification()
        return Result.success()
    }
}