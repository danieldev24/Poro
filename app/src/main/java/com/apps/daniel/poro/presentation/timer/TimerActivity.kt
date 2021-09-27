package com.apps.daniel.poro.presentation.timer

import com.apps.daniel.poro.presentation.settings.reminders.ReminderHelper.Companion.removeNotification
import com.apps.daniel.poro.util.BatteryUtils.Companion.isIgnoringBatteryOptimizations
import dagger.hilt.android.AndroidEntryPoint
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.apps.daniel.poro.presentation.statistics.main.SelectLabelDialog.OnLabelSelectedListener
import javax.inject.Inject
import com.apps.daniel.poro.presentation.settings.PreferenceHelper
import com.apps.daniel.poro.bl.CurrentSessionManager
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.apps.daniel.poro.presentation.statistics.SessionViewModel
import com.apps.daniel.poro.bl.SessionType
import com.apps.daniel.poro.bl.TimerState
import android.content.Intent
import com.apps.daniel.poro.bl.TimerService
import android.os.Build
import android.os.Bundle
import org.greenrobot.eventbus.EventBus
import androidx.databinding.DataBindingUtil
import com.apps.daniel.poro.R
import com.kobakei.ratethisapp.RateThisApp
import android.view.animation.Animation
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.BaseTransientBottomBar
import android.annotation.SuppressLint
import com.apps.daniel.poro.presentation.settings.SettingsActivity
import com.apps.daniel.poro.bl.NotificationHelper
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.content.DialogInterface
import com.apps.daniel.poro.bl.CurrentSession
import android.widget.Toast
import android.widget.FrameLayout
import org.greenrobot.eventbus.Subscribe
import com.apps.daniel.poro.util.Constants.FinishWorkEvent
import com.apps.daniel.poro.util.Constants.FinishBreakEvent
import com.apps.daniel.poro.util.Constants.FinishLongBreakEvent
import com.apps.daniel.poro.util.Constants.StartSessionEvent
import com.apps.daniel.poro.util.Constants.OneMinuteLeft
import com.apps.daniel.poro.presentation.statistics.main.SelectLabelDialog
import android.content.res.ColorStateList
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.apps.daniel.poro.BuildConfig
import com.apps.daniel.poro.domain.models.Label
import com.apps.daniel.poro.domain.models.Session
import com.apps.daniel.poro.databinding.ActivityMainBinding
import com.apps.daniel.poro.helpers.FullscreenHelper
import com.apps.daniel.poro.helpers.ThemeHelper
import com.apps.daniel.poro.presentation.bottom_nav.BottomNavigationDrawerFragment
import com.apps.daniel.poro.presentation.dialog.FinishedSessionDialog
import com.apps.daniel.poro.presentation.intro.MainIntroActivity
import com.apps.daniel.poro.ui.ActivityWithBilling
import com.apps.daniel.poro.util.*
import com.apps.daniel.poro.util.Constants.ClearNotificationEvent
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class TimerActivity : ActivityWithBilling(), OnSharedPreferenceChangeListener,
    OnLabelSelectedListener,
    FinishedSessionDialog.Listener {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var currentSessionManager: CurrentSessionManager

    private var sessionFinishedDialog: FinishedSessionDialog? = null

    private var fullscreenHelper: FullscreenHelper? = null
    private var backPressedAt: Long = 0
    private lateinit var labelButton: MenuItem
    private val sessionViewModel: SessionViewModel by viewModels()
    private val mainViewModel: TimerActivityViewModel by viewModels()

    private var sessionsCounterText: TextView? = null
    private var currentSessionType = SessionType.INVALID

    fun onStartButtonClick() {
        start(SessionType.WORK)
    }

    private fun onStopButtonClick() {
        stop()
    }

    private fun onSkipButtonClick() {
        skip()
    }

    fun onAdd60SecondsButtonClick() {
        add60Seconds()
    }

    private fun skip() {
        if (currentSession.timerState.value !== TimerState.INACTIVE) {
            val skipIntent: Intent = IntentWithAction(
                this@TimerActivity, TimerService::class.java,
                Constants.ACTION.SKIP
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(skipIntent)
            } else {
                startService(skipIntent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        if (preferenceHelper.isFirstRun()) {
            // show app intro
            val i = Intent(this@TimerActivity, MainIntroActivity::class.java)
            startActivity(i)
            preferenceHelper.consumeFirstRun()
        }
        ThemeHelper.setTheme(this, preferenceHelper.isAmoledTheme())
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.apply {
            labelView.setOnClickListener { showEditLabelDialog() }
            setupTimeLabelEvents()
            setSupportActionBar(binding.bar)
            supportActionBar?.title = null
        }

        // dismiss it at orientation change
        val selectLabelDialog =
            supportFragmentManager.findFragmentByTag(DIALOG_SELECT_LABEL_TAG) as DialogFragment?
        selectLabelDialog?.dismiss()
        if (!BuildConfig.F_DROID) {
            // Monitor launch times and interval from installation
            RateThisApp.onCreate(this)
            // If the condition is satisfied, "Rate this app" dialog will be shown
            RateThisApp.showRateDialogIfNeeded(this)
        }
        Glide.with(this).asGif().load(R.drawable.star).into( binding.planet)
    }

    override fun showSnackBar(@StringRes resourceId: Int) {
        if (this::binding.isInitialized) {
            Snackbar.make(binding.root, getString(resourceId), Snackbar.LENGTH_LONG)
                .setAnchorView(binding.bar).show()
        }
    }

    private fun showTutorialSnackBars() {
        val messageSize = 4
        val i = preferenceHelper.lastIntroStep
        if (i < messageSize) {
            val messages = listOf(
                getString(R.string.tutorial_tap),
                getString(R.string.tutorial_swipe_left),
                getString(R.string.tutorial_swipe_up),
                getString(R.string.tutorial_swipe_down)
            )
            val animations = listOf(
                AnimationUtils.loadAnimation(applicationContext, R.anim.tutorial_tap),
                AnimationUtils.loadAnimation(applicationContext, R.anim.tutorial_swipe_right),
                AnimationUtils.loadAnimation(applicationContext, R.anim.tutorial_swipe_up),
                AnimationUtils.loadAnimation(applicationContext, R.anim.tutorial_swipe_down)
            )
            binding.tutorialDot.apply {
                visibility = View.VISIBLE
                animate().translationX(0f).translationY(0f)
                clearAnimation()
                animation = animations[preferenceHelper.lastIntroStep]
            }

            val s = Snackbar.make(
                binding.bar,
                messages[preferenceHelper.lastIntroStep],
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("OK") {
                    val nextStep = i + 1
                    preferenceHelper.lastIntroStep = nextStep
                    showTutorialSnackBars()
                }
                .setAnchorView(binding.bar)
            s.behavior = object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View): Boolean {
                    return false
                }
            }
            s.show()
        } else {
            binding.tutorialDot.apply {
                animate().translationX(0f).translationY(0f)
                clearAnimation()
                visibility = View.GONE
            }

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTimeLabelEvents() {
        binding.apply {
            timeLabel.setOnTouchListener(object : OnSwipeTouchListener(this@TimerActivity) {
                public override fun onSwipeRight(view: View) {
                    onSkipSession()
                }

                public override fun onSwipeLeft(view: View) {
                    onSkipSession()
                }

                public override fun onSwipeBottom(view: View) {
                    onStopSession()
                    if (preferenceHelper.isScreensaverEnabled()) {
                        recreate()
                    }
                }

                public override fun onSwipeTop(view: View) {
                    if (currentSession.timerState.value !== TimerState.INACTIVE) {
                        onAdd60SecondsButtonClick()
                    }
                }

                public override fun onClick(view: View) {
                    onStartButtonClick()
                }

                public override fun onLongClick(view: View) {
                    val settingsIntent = Intent(this@TimerActivity, SettingsActivity::class.java)
                    startActivity(settingsIntent)
                }

                public override fun onPress(view: View) {
                    timeLabel.startAnimation(
                        AnimationUtils.loadAnimation(
                            applicationContext, R.anim.scale_reversed
                        )
                    )
                }

                public override fun onRelease(view: View) {
                    timeLabel.startAnimation(
                        AnimationUtils.loadAnimation(
                            applicationContext, R.anim.scale
                        )
                    )
                    if (currentSession.timerState.value === TimerState.PAUSED) {
                        lifecycleScope.launch {
                            delay(300)
                            timeLabel.startAnimation(
                                AnimationUtils.loadAnimation(applicationContext, R.anim.blink))
                        }
                    }
                }
            })
        }
    }

    private fun onSkipSession() {
        if (currentSession.timerState.value !== TimerState.INACTIVE) {
            onSkipButtonClick()
        }
    }

    private fun onStopSession() {
        if (currentSession.timerState.value !== TimerState.INACTIVE) {
            onStopButtonClick()
        }
    }

    override fun onAttachedToWindow() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    override fun onPause() {
        super.onPause()
        mainViewModel.isActive = false
    }

    override fun onResume() {
        super.onResume()
        removeNotification(applicationContext)
        mainViewModel.isActive = true
        if (mainViewModel.showFinishDialog) {
            showFinishedSessionUI()
        }

        // initialize notification channels on the first run
        if (preferenceHelper.isFirstRun()) {
            NotificationHelper(this)
        }

        // this is to refresh the current status icon color
        invalidateOptionsMenu()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.registerOnSharedPreferenceChangeListener(this)
        toggleKeepScreenOn(preferenceHelper.isScreenOnEnabled())
        toggleFullscreenMode()
        showTutorialSnackBars()
        setTimeLabelColor()
        binding.blackCover.animate().alpha(0f).duration = 500

        // the only reason we're doing this here is because a FinishSessionEvent
        // comes together with a "bring activity on top"
        if (preferenceHelper.isFlashingNotificationEnabled() && mainViewModel.enableFlashingNotification) {
            binding.whiteCover.visibility = View.VISIBLE
            if (preferenceHelper.isAutoStartBreak() && (currentSessionType === SessionType.BREAK || currentSessionType === SessionType.LONG_BREAK)
                || preferenceHelper.isAutoStartWork() && currentSessionType === SessionType.WORK
            ) {
                startFlashingNotificationShort()
            } else {
                startFlashingNotification()
            }
        }
    }

    override fun onDestroy() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.unregisterOnSharedPreferenceChangeListener(this)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_main, menu)
        val batteryButton = menu.findItem(R.id.action_battery_optimization)
        batteryButton.isVisible = !isIgnoringBatteryOptimizations(this)
        labelButton = menu.findItem(R.id.action_current_label).also {
            it.icon.setColorFilter(
                ThemeHelper.getColor(this, ThemeHelper.COLOR_INDEX_ALL_LABELS),
                PorterDuff.Mode.SRC_ATOP
            )
        }
        setupEvents()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val bottomNavigationDrawerFragment = BottomNavigationDrawerFragment()
                bottomNavigationDrawerFragment.show(
                    supportFragmentManager,
                    bottomNavigationDrawerFragment.tag
                )
            }
            R.id.action_battery_optimization -> showBatteryOptimizationDialog()
            R.id.action_current_label -> showEditLabelDialog()
            R.id.action_sessions_counter -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.action_reset_counter_title)
                    .setMessage(R.string.action_reset_counter)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        sessionViewModel.deleteSessionsFinishedToday()
                        preferenceHelper.resetCurrentStreak()
                    }
                    .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> }
                    .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("BatteryLife")
    private fun showBatteryOptimizationDialog() {
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:" + this.packageName)
        startActivity(intent)
    }

    private fun setupEvents() {
        currentSession.duration.observe(
            this,
            { millis: Long -> updateTimeLabel(millis) })
        currentSession.sessionType.observe(this, { sessionType: SessionType ->
            currentSessionType = sessionType
            setupLabelView()
            setTimeLabelColor()
        })
        currentSession.timerState.observe(this, { timerState: TimerState ->
            when {
                timerState === TimerState.INACTIVE -> {
                    setupLabelView()
                    setTimeLabelColor()
                    lifecycleScope.launch {
                        delay(300)
                        binding.timeLabel.clearAnimation()
                    }
                }
                timerState === TimerState.PAUSED -> {
                    lifecycleScope.launch {
                        delay(300)
                        binding.timeLabel.startAnimation(
                            AnimationUtils.loadAnimation(applicationContext, R.anim.blink))
                    }
                }
                else -> {
                    lifecycleScope.launch {
                        delay(300)
                        binding.timeLabel.clearAnimation()
                    }
                }
            }
        })
    }

    private val currentSession: CurrentSession
        get() = currentSessionManager.currentSession

    @SuppressLint("WrongConstant")
    override fun onBackPressed() {
        if (currentSession.timerState.value !== TimerState.INACTIVE) {
            moveTaskToBack(true)
        } else {
            if (backPressedAt + 2000 > System.currentTimeMillis()) {
                super.onBackPressed()
            } else {
                try {
                    Toast.makeText(
                        baseContext,
                        R.string.action_press_back_button,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } catch (th: Throwable) {
                    // ignoring this exception
                }
            }
            backPressedAt = System.currentTimeMillis()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val alertMenuItem = menu.findItem(R.id.action_sessions_counter)
        alertMenuItem.isVisible = false
        val sessionsCounterEnabled = preferenceHelper.isSessionsCounterEnabled
        if (sessionsCounterEnabled) {
            val mSessionsCounter = alertMenuItem.actionView as FrameLayout
            sessionsCounterText = mSessionsCounter.findViewById(R.id.view_alert_count_textview)
            mSessionsCounter.setOnClickListener { onOptionsItemSelected(alertMenuItem) }
            sessionViewModel.allSessionsUnarchivedToday.observe(this, { sessions: List<Session> ->
                if (sessionsCounterText != null) {
                    sessionsCounterText!!.text = sessions.count().toString()
                }
                alertMenuItem.isVisible = true
            })
        }
        return super.onPrepareOptionsMenu(menu)
    }

    @Subscribe
    fun onEventMainThread(o: Any?) {
        if (o is FinishWorkEvent) {
            if (preferenceHelper.isAutoStartBreak()) {
                if (preferenceHelper.isFlashingNotificationEnabled()) {
                    mainViewModel.enableFlashingNotification = true
                }
            } else {
                mainViewModel.dialogPendingType = SessionType.WORK
                showFinishedSessionUI()
            }
        } else if (o is FinishBreakEvent || o is FinishLongBreakEvent) {
            if (preferenceHelper.isAutoStartWork()) {
                if (preferenceHelper.isFlashingNotificationEnabled()) {
                    mainViewModel.enableFlashingNotification = true
                }
            } else {
                mainViewModel.dialogPendingType = SessionType.BREAK
                showFinishedSessionUI()
            }
        } else if (o is StartSessionEvent) {
            if (sessionFinishedDialog != null) {
                sessionFinishedDialog!!.dismissAllowingStateLoss()
            }
            mainViewModel.showFinishDialog = false
            if (!preferenceHelper.isAutoStartBreak() && !preferenceHelper.isAutoStartWork()) {
                stopFlashingNotification()
            }
        } else if (o is OneMinuteLeft) {
            if (preferenceHelper.isFlashingNotificationEnabled()) {
                startFlashingNotificationShort()
            }
        }
    }

    private fun updateTimeLabel(millis: Long) {
        var seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        seconds -= minutes * 60
        val currentFormattedTick: String
        val isMinutesStyle =
            preferenceHelper.timerStyle == resources.getString(R.string.pref_timer_style_minutes_value)
        currentFormattedTick = if (isMinutesStyle) {
            TimeUnit.SECONDS.toMinutes(minutes * 60 + seconds + 59).toString()
        } else {
            ((if (minutes > 9) minutes else "0$minutes")
                .toString() + ":"
                    + if (seconds > 9) seconds else "0$seconds")
        }
        binding.timeLabel.text = currentFormattedTick
        Log.v(TAG, "drawing the time label.")
        if (preferenceHelper.isScreensaverEnabled() && seconds == 1L && currentSession.timerState.value !== TimerState.PAUSED) {
            teleportTimeView()
        }
    }

    private fun start(sessionType: SessionType) {
        var startIntent = Intent()
        when (currentSession.timerState.value) {
            TimerState.INACTIVE -> startIntent = IntentWithAction(
                this@TimerActivity, TimerService::class.java,
                Constants.ACTION.START, sessionType
            )
            TimerState.ACTIVE, TimerState.PAUSED -> startIntent = IntentWithAction(
                this@TimerActivity,
                TimerService::class.java,
                Constants.ACTION.TOGGLE
            )
            else -> Log.wtf(TAG, "Invalid timer state.")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent)
        } else {
            startService(startIntent)
        }
    }

    private fun stop() {
        val stopIntent: Intent =
            IntentWithAction(this@TimerActivity, TimerService::class.java, Constants.ACTION.STOP)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent)
        } else {
            startService(stopIntent)
        }
        binding.whiteCover.visibility = View.GONE
        binding.whiteCover.clearAnimation()
    }

    private fun add60Seconds() {
        val stopIntent: Intent = IntentWithAction(
            this@TimerActivity,
            TimerService::class.java,
            Constants.ACTION.ADD_SECONDS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent)
        } else {
            startService(stopIntent)
        }
    }

    private fun showEditLabelDialog() {
        val fragmentManager = supportFragmentManager
        SelectLabelDialog.newInstance(
            this,
            preferenceHelper.currentSessionLabel.title,
            isExtendedVersion = false,
            showProfileSelection = true
        )
            .show(fragmentManager, DIALOG_SELECT_LABEL_TAG)
    }

    private fun showFinishedSessionUI() {
        if (mainViewModel.isActive) {
            mainViewModel.showFinishDialog = false
            mainViewModel.enableFlashingNotification = true
            Log.i(TAG, "Showing the finish dialog.")
            sessionFinishedDialog = FinishedSessionDialog.newInstance(this)
                .also { it.show(supportFragmentManager, TAG) }
        } else {
            mainViewModel.showFinishDialog = true
            mainViewModel.enableFlashingNotification = false
        }
    }

    private fun toggleFullscreenMode() {
        if (preferenceHelper.isFullscreenEnabled()) {
            if (fullscreenHelper == null) {
                fullscreenHelper = FullscreenHelper(findViewById(R.id.main), supportActionBar)
            } else {
                fullscreenHelper!!.hide()
            }
        } else {
            if (fullscreenHelper != null) {
                fullscreenHelper!!.disable()
                fullscreenHelper = null
            }
        }
    }

    private fun delayToggleFullscreenMode() {
        lifecycleScope.launch {
            delay(300)
            toggleFullscreenMode()
        }
    }

    private fun toggleKeepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceHelper.WORK_DURATION -> if (currentSession.timerState.value
                === TimerState.INACTIVE
            ) {
                currentSession.setDuration(
                    TimeUnit.MINUTES.toMillis(
                        preferenceHelper.getSessionDuration(SessionType.WORK)
                    )
                )
            }
            PreferenceHelper.ENABLE_SCREEN_ON -> toggleKeepScreenOn(preferenceHelper.isScreenOnEnabled())
            PreferenceHelper.AMOLED -> {
                recreate()
                if (!preferenceHelper.isScreensaverEnabled()) {
                    recreate()
                }
            }
            PreferenceHelper.ENABLE_SCREENSAVER_MODE -> if (!preferenceHelper.isScreensaverEnabled()) {
                recreate()
            }
            else -> {
            }
        }
    }

    private fun setupLabelView() {
        val label = preferenceHelper.currentSessionLabel
        if (isInvalidLabel(label)) {
            binding.labelView.visibility = View.GONE
            labelButton.isVisible = true
            val color = ThemeHelper.getColor(this, ThemeHelper.COLOR_INDEX_ALL_LABELS)
            labelButton.icon.setColorFilter(
                color, PorterDuff.Mode.SRC_ATOP
            )
        } else {
            val color = ThemeHelper.getColor(this, label.colorId)
            if (preferenceHelper.showCurrentLabel()) {
                labelButton.isVisible = false
                binding.labelView.visibility = View.VISIBLE
                binding.labelView.text = label.title
                binding.labelView.chipBackgroundColor = ColorStateList.valueOf(color)
            } else {
                binding.labelView.visibility = View.GONE
                labelButton.isVisible = true
                labelButton.icon.setColorFilter(
                    color, PorterDuff.Mode.SRC_ATOP
                )
            }
        }
    }

    private fun isInvalidLabel(label: Label): Boolean {
        return label.title == "" || label.title == getString(R.string.label_unlabeled)
    }

    private fun setTimeLabelColor() {
        val label = preferenceHelper.currentSessionLabel
        if (currentSessionType === SessionType.BREAK || currentSessionType === SessionType.LONG_BREAK) {
            binding.timeLabel.setTextColor(ThemeHelper.getColor(this, ThemeHelper.COLOR_INDEX_BREAK))
            return
        }
        if (!isInvalidLabel(label)) {
            binding.timeLabel.setTextColor(ThemeHelper.getColor(this, label.colorId))
        } else {
            binding.timeLabel.setTextColor(
                ThemeHelper.getColor(
                    this,
                    ThemeHelper.COLOR_INDEX_UNLABELED
                )
            )
        }
    }

    override fun onLabelSelected(label: Label) {
        currentSession.setLabel(label.title)
        preferenceHelper.currentSessionLabel = label
        setupLabelView()
        setTimeLabelColor()
    }

    private fun teleportTimeView() {
        val margin = ThemeHelper.dpToPx(this, 48f)
        val maxX = binding.main.width - binding.timeLabel.width - margin
        val maxY = binding.main.height - binding.timeLabel.height - margin
        val boundX = maxX - margin
        val boundY = maxY - margin
        if (boundX > 0 && boundY > 0) {
            val r = Random()
            val newX = r.nextInt(boundX) + margin
            val newY = r.nextInt(boundY) + margin
            binding.timeLabel.animate().x(newX.toFloat()).y(newY.toFloat()).duration = 100
        }
    }

    override fun onFinishedSessionDialogPositiveButtonClick(sessionType: SessionType) {
        if (sessionType === SessionType.WORK) {
            start(SessionType.BREAK)
        } else {
            start(SessionType.WORK)
        }
        delayToggleFullscreenMode()
        stopFlashingNotification()
    }

    override fun onFinishedSessionDialogNeutralButtonClick(sessionType: SessionType) {
        EventBus.getDefault().post(ClearNotificationEvent())
        delayToggleFullscreenMode()
        stopFlashingNotification()
    }

    private fun startFlashingNotification() {
        binding.whiteCover.visibility = View.VISIBLE
        binding.whiteCover.startAnimation(
            AnimationUtils.loadAnimation(
                applicationContext, R.anim.blink_screen
            )
        )
    }

    private fun startFlashingNotificationShort() {
        binding.whiteCover.visibility = View.VISIBLE
        val anim = AnimationUtils.loadAnimation(applicationContext, R.anim.blink_screen_3_times)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                stopFlashingNotification()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        binding.whiteCover.startAnimation(anim)
    }

    private fun stopFlashingNotification() {
        binding.whiteCover.visibility = View.GONE
        binding.whiteCover.clearAnimation()
        mainViewModel.enableFlashingNotification = false
    }

    companion object {
        private val TAG = TimerActivity::class.java.simpleName
        private const val DIALOG_SELECT_LABEL_TAG = "dialogSelectLabel"
    }
}