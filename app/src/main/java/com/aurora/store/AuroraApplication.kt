/*
 * Aurora Store
 *  Copyright (C) 2021, Rahul Kumar Patel <whyorean@gmail.com>
 *
 *  Aurora Store is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Aurora Store is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.aurora.store

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.aurora.Constants
import com.aurora.store.data.downloader.DownloadManager
import com.aurora.store.data.providers.NetworkProvider
import com.aurora.store.data.receiver.PackageManagerReceiver
import com.aurora.store.data.service.NotificationService
import com.aurora.store.util.CommonUtil
import com.aurora.store.util.PackageUtil
import com.tonyodev.fetch2.Fetch
import java.util.ArrayList
import nl.komponents.kovenant.android.startKovenant
import nl.komponents.kovenant.android.stopKovenant

class AuroraApplication : MultiDexApplication() {

    private lateinit var fetch: Fetch
    private lateinit var packageManagerReceiver: PackageManagerReceiver

    companion object{
        val enqueuedInstalls: MutableSet<String> = mutableSetOf()
    }

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        //Create Notification Channels : General & Alert
        createNotificationChannel()

        NotificationService.startService(this)

        fetch = DownloadManager.with(this).fetch

        packageManagerReceiver = object : PackageManagerReceiver() {

        }

        //Register broadcast receiver for package install/uninstall
        registerReceiver(packageManagerReceiver, PackageUtil.getFilter())

        NetworkProvider
            .with(this)
            .bind()

        startKovenant()

        CommonUtil.cleanupInstallationSessions(applicationContext)
    }

    override fun onTerminate() {
        NetworkProvider
            .with(this)
            .unbind()
        stopKovenant()
        super.onTerminate()
    }

    override fun onLowMemory() {
        NetworkProvider
            .with(this)
            .unbind()
        super.onLowMemory()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channels = ArrayList<NotificationChannel>()
            channels.add(
                NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ALERT,
                    getString(R.string.notification_channel_alert),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            channels.add(
                NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_GENERAL,
                    getString(R.string.notification_channel_general),
                    NotificationManager.IMPORTANCE_MIN
                )
            )
            channels.add(
                NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_UPDATER_SERVICE,
                    getString(R.string.notification_channel_updater_service),
                    NotificationManager.IMPORTANCE_MIN
                )
            )
            notificationManager.createNotificationChannels(channels)
        }
    }
}
