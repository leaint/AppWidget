package com.example.appwidget

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.BatteryManager
import android.os.Bundle
import android.provider.AlarmClock
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.appwidget.databinding.ActivityMainBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class Interv(var begin: Int, var end: Int, var minu: Int, var mul: Int) {}

class MainActivity : androidx.activity.ComponentActivity() {

    private lateinit var B: ActivityMainBinding

    private lateinit var timeText: EditText
    private lateinit var spinner: Spinner
    private lateinit var messageText: EditText
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private lateinit var savedTime: LocalTime
    private var _savedTimeText: String = ""

    private var skipUI = true
    private var vibrate = false
    private var isLocalAlarm = false
    private val ringtoneMap = HashMap<String, String>()

    private val levelList = ArrayList<Interv>()

    private val notifyChannel = NotificationChannel(
        NOTIFY_CHANNEL_NAME, "Set Clock", NotificationManager.IMPORTANCE_DEFAULT
    )

    private var cancelCallback: (() -> Unit)? = null

    companion object {
        private const val USER_SELECT_KEY = "*user select*"
        private const val LAST_KEY = "LAST_KEY"
        const val NOTIFY_CHANNEL_NAME = "SetClock"
        const val SET_ALARM_INTERV = "setting"
        private const val CANCEL_ALARM_ACTION = "com.example.clock.MainActivity.CANCEL_ALARM"
        private const val RINGTONE_KEY = "RINGTONE_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        B = ActivityMainBinding.inflate(layoutInflater)
        setContentView(B.root)

//        registerReceiver(myReceiver, IntentFilter(Intent.ACTION_POWER_CONNECTED))


        B.localCheckbox.apply {
            isChecked = isLocalAlarm
            setOnCheckedChangeListener { _, checked -> isLocalAlarm = checked }
        }

        Intent(this, PowerService::class.java).also { intent ->
            startService(intent)
        }
        Intent(this, BatteryService::class.java).also { intent ->
            startService(intent)
        }

        Intent(this, MyAccessibilityService::class.java).also { intent ->
            startService(intent)
        }

        val notifyMana = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyMana.createNotificationChannel(notifyChannel)

        messageText = B.messageEditText
        timeText = B.editTextTime

        setTimeValue(LocalTime.now())

        timeText.setOnFocusChangeListener { _, hasFocus ->
            run {
                if (!hasFocus) {
                    updateText()
                }
            }
        }
        timeText.setOnEditorActionListener { _, _, _ ->
            run {
                updateText()
                false
            }
        }

        B.plus5Btn.setOnClickListener {
            setTimeValue(
                savedTime.plusMinutes(
                    5L
                )
            )
        }

        B.plus10Btn.setOnClickListener {
            setTimeValue(
                savedTime.plusMinutes(
                    10L
                )
            )
        }
        B.setBtn.setOnClickListener { onSetClick() }
        B.nowBtn.setOnClickListener { setTimeValue(LocalTime.now()) }

        with(B.skipUiSwitch) {
            isChecked = skipUI
            setOnCheckedChangeListener { _, checked -> skipUI = checked }
        }

        spinner = B.ringtoneSpinner

        val arrayAdapter = run {

            getPreferences(MODE_PRIVATE).getStringSet(RINGTONE_KEY, null)?.let {
                it.forEach {
                    val (k, v) = it.split('|')
                    ringtoneMap[k] = v
                }
            }

            if (ringtoneMap.size == 0) {

                val ringtoneManager = RingtoneManager(applicationContext)
                ringtoneManager.setType(RingtoneManager.TYPE_ALARM)

                val cursor = ringtoneManager.cursor

                cursor.use { c ->

                    for (i in 0 until c.count) {
                        val u = ringtoneManager.getRingtoneUri(i)
                        val t = u.getQueryParameter("title") ?: ""
                        ringtoneMap[t] = u.toString()
                    }
                }

                with(getPreferences(MODE_PRIVATE).edit()) {
                    ringtoneMap.entries.map {
                        it.key + "|" + it.value
                    }.let {
                        putStringSet(RINGTONE_KEY, it.toMutableSet())
                    }
                    apply()
                }

            }

            ArrayAdapter(
                applicationContext, R.layout.simple_spinner_item, ringtoneMap.keys.toList()
            )
        }

        spinner.adapter = arrayAdapter

        getPreferences(Context.MODE_PRIVATE).let {
            val lastKey = it.getString(LAST_KEY, "")
            if (lastKey != "") {
                spinner.setSelection(arrayAdapter.getPosition(lastKey))
            }
        }


        val launcherResult = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) {
            it?.let {
                ringtoneMap[USER_SELECT_KEY] = it.toString()
                arrayAdapter.remove(USER_SELECT_KEY)
                arrayAdapter.insert(USER_SELECT_KEY, 0)
                spinner.setSelection(0)

                showToast(it.toString())
            }
        }

        B.selectBtn.setOnClickListener { launcherResult.launch(arrayOf("audio/*")) }

        val chargeSettingResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {

                result.data?.getStringExtra(SET_ALARM_INTERV)?.let {
                    val la = genArr(it)
                    if (la.size > 0) {
                        levelList.clear()
                        levelList.addAll(la)
                        with(getPreferences(MODE_PRIVATE).edit()) {
                            this.putString(SET_ALARM_INTERV, it)
                            apply()
                        }
                        showToast("saved rule")
                    }
                }
            }
        }

        B.settingImgbtn.setOnClickListener {
            chargeSettingResult.launch(
                Intent(this, ChargeSettingActivity::class.java).putExtra(
                    SET_ALARM_INTERV, getPreferences(MODE_PRIVATE).getString(SET_ALARM_INTERV, null)
                )
            )
        }

        with(B.vibrateSwitch) {
            setOnCheckedChangeListener { _, checked ->
                vibrate = checked
            }
            isChecked = vibrate
        }

        getPreferences(MODE_PRIVATE).getString(SET_ALARM_INTERV, null)?.let {
            val la = genArr(it)
            if (la.size > 0) {
                levelList.clear()
                levelList.addAll(la)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val level = getCurrentBatteryLevel()?.toInt()

        level?.let {
            levelList.find { i -> it in i.begin until i.end }?.let {

                val minutes = (100 - level) / it.mul + it.minu
                setTimeValue(LocalTime.now().plusMinutes(minutes.toLong()))
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            if (it.action == CANCEL_ALARM_ACTION) {
                cancelCallback?.invoke()
                showToast("cancel this alarm")
            }
        }
    }

    private fun updateText() {

        if (_savedTimeText == timeText.text.toString()) {
            return
        }

        try {
            val l = timeText.text.split(':')
            if (l.size == 2) {
                val (hour, minute) = l.map { it.toInt(10) }

                if (hour in 0..23 && minute in 0..59) {
                    setTimeValue(LocalTime.of(hour, minute))
                    return
                } else throw NumberFormatException("[00:00 , 23:59]")
            } else throw NumberFormatException("must be this format: 12:00")
        } catch (e: NumberFormatException) {
            showToast(e.message ?: "NumberFormatException")
            timeText.setText(_savedTimeText)
        }

    }

    private fun genArr(str: String): ArrayList<Interv> {
        val arr = ArrayList<Interv>()
        str.split('\n').forEach { s ->
            run {

                try {

                    val data = s.split(',').map { it.toInt() }
                    if (data.size == 4) {
                        var (b, e, minu, mul) = data
                        if (mul <= 0) mul = 1
                        arr.add(Interv(b, e, minu, mul))
                    }
                } catch (e: NumberFormatException) {
                    //
                }
            }
        }

        return arr
    }

    private fun getCurrentBatteryLevel(): Float? {
        val batteryIntent = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let {
            registerReceiver(null, it)
        }
        val l = batteryIntent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            level * 100 / scale.toFloat()
        }
        return l
    }

    private fun showToast(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show()
    }

    private fun setTimeValue(time: LocalTime) {
        savedTime = time
        val t = timeFormatter.format(time)
        _savedTimeText = t
        timeText.setText(t)
    }

    private fun setAlarm() {
        val k = spinner.selectedItem

        if (k != USER_SELECT_KEY) {
            with(this.getPreferences(Context.MODE_PRIVATE).edit()) {
                putString(LAST_KEY, k.toString())
                apply()
            }
        }
        val alarmIntent = Intent().apply {
            action = AlarmClock.ACTION_SET_ALARM
            putExtra(AlarmClock.EXTRA_HOUR, savedTime.hour)
            putExtra(AlarmClock.EXTRA_MINUTES, savedTime.minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, messageText.text.toString())
            putExtra(AlarmClock.EXTRA_VIBRATE, vibrate)
            putExtra(AlarmClock.EXTRA_RINGTONE, ringtoneMap[k])
            putExtra(AlarmClock.EXTRA_SKIP_UI, skipUI)
        }

        try {
            startActivity(alarmIntent)
        } catch (e: ActivityNotFoundException) {
            showToast(e.message ?: "ActivityNotFoundException")
        }
    }

    private fun setLocalAlarm() {
        val k = spinner.selectedItem

        val alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, savedTime.hour)
            set(Calendar.MINUTE, savedTime.minute)
        }

        val alarmIntent = Intent(applicationContext, AlarmActivity::class.java).let {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            it.putExtra(AlarmClock.EXTRA_RINGTONE, ringtoneMap[k])
            it.putExtra(AlarmClock.EXTRA_MESSAGE, messageText.text.toString())

            PendingIntent.getActivity(
                applicationContext, 0, it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }


        if (alarmMgr.canScheduleExactAlarms()) {
            alarmMgr.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmIntent
            )
            showToast("Setting a alarm")
            sendNotification()
            cancelCallback = { alarmMgr.cancel(alarmIntent) }
        }
    }

    private fun sendNotification() {

        val intent = Intent(this, MainActivity::class.java).apply {
            action = CANCEL_ALARM_ACTION
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val builder = Notification.Builder(this, NOTIFY_CHANNEL_NAME)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentTitle(savedTime.toString())
            .setContentText("Click this to cancel alarm clock")
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notifyMana =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyMana.notify(intent.hashCode(), builder.build())
    }

    private fun onSetClick() {
        if (isLocalAlarm) {
            setLocalAlarm()
        } else {
            setAlarm()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menu?.add("TTS")?.setOnMenuItemClickListener {

            startActivity(Intent(this, TTS::class.java))

            true
        }

        menu?.add("Downloader")?.setOnMenuItemClickListener {

            startActivity(Intent(this, DownloaderActivity::class.java))
            true
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return super.onOptionsItemSelected(item)
    }

}