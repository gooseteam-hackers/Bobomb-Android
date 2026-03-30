package org.gooseprjkt.bobomb.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.*
import org.gooseprjkt.bobomb.BuildConfig
import org.gooseprjkt.bobomb.BuildVars
import org.gooseprjkt.bobomb.R
import org.gooseprjkt.bobomb.services.Services
import org.gooseprjkt.bobomb.services.collectAll
import org.gooseprjkt.bobomb.services.filter
import org.gooseprjkt.bobomb.worker.AttackWorker
import org.gooseprjkt.bobomb.worker.DownloadWorker
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

class MainViewModel(
    private val context: android.content.Context,
    private val repository: MainRepository,
    private val workManager: WorkManager
) : ViewModel() {
    private var services: Services = listOf()

    private var currentWorkId: UUID? = null

    val proxyEnabled: MutableLiveData<Boolean> = MutableLiveData(repository.isProxyEnabled)
    val snowfallEnabled: MutableLiveData<Boolean> = MutableLiveData(repository.isSnowfallEnabled)

    val progress = MutableLiveData(Progress(R.drawable.logo, R.string.attack))
    val workStatus = MutableLiveData(false)

    val advertising = MutableLiveData<Map<String, Any>?>()

    val servicesCount = MutableLiveData(0)

    val repositoriesProgress = MutableLiveData(RepositoriesLoadingProgress(0, 0))

    private val advertisingCounter = MutableLiveData(5)
    private val advertisingAvailable = MutableLiveData(false)
    val advertisingTrigger = MutableLiveData(false)

    private var counter: Job? = null

    private var lastCountryCode = BuildVars.COUNTRY_CODES[0]

    val scheduledAttacks: LiveData<List<WorkInfo>>

    private val client = OkHttpClient()

    data class RepositoriesLoadingProgress(
        val currentProgress: Int,
        val maxProgress: Int
    )

    data class Progress(
        val currentProgress: Int,
        val maxProgress: Int,
        val iconResource: Int,
        val titleResource: Int
    ) {
        constructor(iconResource: Int, titleResource: Int) : this(0, 0, iconResource, titleResource)
    }

    init {
        workManager.getWorkInfosLiveData(
            WorkQuery.Builder.fromStates(
                listOf(
                    WorkInfo.State.RUNNING,
                    WorkInfo.State.CANCELLED,
                    WorkInfo.State.SUCCEEDED,
                    WorkInfo.State.FAILED
                )
            )
                .build()
        ).observeForever { workInfoList: List<WorkInfo> ->
            for (workInfo in workInfoList)
                if (workInfo.id == currentWorkId) {
                    if (workInfo.state.isFinished)
                        workStatus.value = false

                    val data = workInfo.progress

                    for (tag in workInfo.tags) {
                        when (tag) {
                            ATTACK -> progress.setValue(
                                Progress(
                                    data.getInt(KEY_PROGRESS, 0),
                                    data.getInt(KEY_MAX_PROGRESS, 0),
                                    R.drawable.logo,
                                    R.string.attack
                                )
                            )

                        }
                    }
                }
        }

        scheduledAttacks = workManager.getWorkInfosLiveData(
            WorkQuery.Builder.fromStates(
                listOf(
                    WorkInfo.State.RUNNING,
                    WorkInfo.State.ENQUEUED
                )
            )
                .addTags(listOf(ATTACK))
                .build()
        )




        collectAll()

        loadAdvertising()
        loadCloudStatic()
    }

    fun getAdvertisingCounter(): LiveData<Int> {
        return advertisingCounter
    }

    fun getAdvertisingAvailable(): LiveData<Boolean?> {
        return advertisingAvailable
    }

    fun startCounting() {
        val snapshot = advertising.getValue()
        if (counter?.isActive == true) return
        counter = CoroutineScope(Dispatchers.IO).launch {
            val value = (snapshot?.get("seconds") as? Number)?.toInt() ?: 10
            for (current in value downTo 0) {
                advertisingCounter.postValue(current)
                delay(1000)
            }
        }
    }

    fun collectAll() = CoroutineScope(Dispatchers.IO).launch {
        android.util.Log.d("MainViewModel", "Starting collectAll...")
        collectAll(repository.getAllRepositories(client)) { progress, maxProgress, loadedServices ->
            android.util.Log.d("MainViewModel", "collectAll callback: progress=$progress, maxProgress=$maxProgress, loadedServices=${loadedServices.size}")
            services = loadedServices

            repositoriesProgress.postValue(
                RepositoriesLoadingProgress(progress, maxProgress)
            )

            android.util.Log.d("MainViewModel", "Calling selectCountryCode($lastCountryCode) with ${services.size} services")
            selectCountryCode(lastCountryCode)
        }
    }

    fun selectCountryCode(countryCode: String) {
        lastCountryCode = countryCode


        val count = services.count { service ->
            service.countryCodes.isEmpty() || service.countryCodes.contains(countryCode.toIntOrNull() ?: 7)
        }

        android.util.Log.d("MainViewModel", "selectCountryCode($countryCode): total services=${services.size}, filtered count=$count")
        android.util.Log.d("MainViewModel", "  Services with empty countryCodes: ${services.count { it.countryCodes.isEmpty() }}")
        android.util.Log.d("MainViewModel", "  Services with country code 7: ${services.count { it.countryCodes.contains(7) }}")

        servicesCount.postValue(count)
    }

    fun setProxyEnabled(enabled: Boolean) {
        repository.isProxyEnabled = enabled
        proxyEnabled.value = enabled
    }

    fun setSnowfallEnabled(enabled: Boolean) {
        repository.isSnowfallEnabled = enabled
        snowfallEnabled.value = enabled
    }

    private fun loadCloudStatic() {
        val request: Request = Request.Builder()
            .url(BuildVars.DATA_SOURCE)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            private val gson = Gson()

            override fun onResponse(call: Call, response: Response) {
                runCatching {
                    val cloudStatic = gson.fromJson(response.body?.string(), CloudStatic::class.java)
                }.onFailure {
                    it.printStackTrace()
                }
            }
        })
    }

    private fun loadAdvertising() {

        val defaultAdvertising = mapOf(
            "active" to false,
            "seconds" to 5,
            "description" to "Default advertising",
            "button" to "Default Button",
            "url" to "https://example.com",
            "image" to ""
        )
        advertisingAvailable.value = false
        advertising.setValue(defaultAdvertising)
    }


    fun scheduleAttack(countryCode: String, phoneNumber: String, repeats: Int, date: Long, current: Long) {

        val notWhitelist = true

        // Получаем настройки drip mode из repository
        val dripModeEnabled = repository.isDripModeEnabled
        val dripDelayMs = repository.dripDelayMs
        val dripRandomDelayEnabled = repository.isDripRandomDelayEnabled
        val dripRandomDelayMinMs = repository.dripRandomDelayMinMs
        val dripRandomDelayMaxMs = repository.dripRandomDelayMaxMs
        val dripAutoDisable = repository.dripModeType == BuildVars.DripModeType.AUTO_DISABLE

        val inputData = Data.Builder()
            .putString(AttackWorker.KEY_COUNTRY_CODE, countryCode)
            .putString(AttackWorker.KEY_PHONE, phoneNumber)
            .putInt(AttackWorker.KEY_REPEATS, min(repeats, BuildVars.MAX_REPEATS_COUNT))
            .putBoolean(AttackWorker.KEY_PROXY_ENABLED, repository.isProxyEnabled)
            .putInt(AttackWorker.KEY_CHUNK_SIZE, repository.attackSpeed.chunkSize)
            .putBoolean(AttackWorker.KEY_FAKE_SERVICES, !notWhitelist)
            .putBoolean(AttackWorker.KEY_DRIP_MODE, dripModeEnabled)
            .putLong(AttackWorker.KEY_DRIP_DELAY_MS, dripDelayMs)
            .putBoolean(AttackWorker.KEY_DRIP_RANDOM_DELAY_ENABLED, dripRandomDelayEnabled)
            .putLong(AttackWorker.KEY_DRIP_RANDOM_DELAY_MIN_MS, dripRandomDelayMinMs)
            .putLong(AttackWorker.KEY_DRIP_RANDOM_DELAY_MAX_MS, dripRandomDelayMaxMs)
            .putBoolean(AttackWorker.KEY_DRIP_AUTO_DISABLE, dripAutoDisable)
            .build()

        val workRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(AttackWorker::class.java)
            .addTag(ATTACK)
            .addTag("+$countryCode$phoneNumber;$date")
            .apply {
                if (dripModeEnabled) {
                    addTag("drip_mode")
                }
            }
            .setInitialDelay(date - current, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        if (current == 0L) {
            progress.value = Progress(R.drawable.logo, R.string.attack)
            pushCurrentWork(workRequest)
        }

        workManager.enqueue(workRequest)
    }

    fun scheduleDripAttack(countryCode: String, phoneNumber: String, repeats: Int, date: Long, current: Long) {
        scheduleAttack(countryCode, phoneNumber, repeats, date, current)
    }

    private fun pushCurrentWork(request: WorkRequest) {
        currentWorkId = request.id
        workStatus.value = true
    }

    fun startAttack(countryCode: String, phoneNumber: String, numberOfCyclesNum: Int) =
        scheduleAttack(countryCode, phoneNumber, numberOfCyclesNum, 0, 0)

    fun startDripAttack(countryCode: String, phoneNumber: String, numberOfCyclesNum: Int) =
        scheduleDripAttack(countryCode, phoneNumber, numberOfCyclesNum, 0, 0)

    fun cancelCurrentWork() =
        currentWorkId?.let { workManager.cancelWorkById(it) }


    companion object {
        const val KEY_PROGRESS = "progress"
        const val KEY_MAX_PROGRESS = "max_progress"
        const val ATTACK = "attack"
    }
}
