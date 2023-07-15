package com.dicoding.habitapp.ui.countdown

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.HABIT
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import com.dicoding.habitapp.utils.NOTIF_UNIQUE_WORK
import java.util.concurrent.TimeUnit

class CountDownActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"

        val habit = intent.getParcelableExtra<Habit>(HABIT) as Habit

        findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

        val viewModel = ViewModelProvider(this).get(CountDownViewModel::class.java)

        //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished
        viewModel.setInitialTime(habit.minutesFocus)
        viewModel.currentTimeString.observe(this) {
            findViewById<TextView>(R.id.tv_count_down).text = it
        }

        viewModel.eventCountDownFinish.observe(this) {isFinished ->
            if (isFinished) {
                updateButtonState(false, false)
            }
        }

        //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.
        val workManager = WorkManager.getInstance(this)

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            val data = Data.Builder().apply {
                putInt(HABIT_ID, habit.id)
                putString(HABIT_TITLE, habit.title)
            }.build()

            val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(habit.minutesFocus, TimeUnit.MINUTES)
                .setInputData(data)
                .build()

            workManager.enqueueUniqueWork(
                NOTIF_UNIQUE_WORK,
                ExistingWorkPolicy.REPLACE,
                notificationRequest
            )

            viewModel.startTimer()
            updateButtonState(true, true)
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            viewModel.resetTimer()
            updateButtonState(false, false)
            workManager.cancelUniqueWork(NOTIF_UNIQUE_WORK)
        }
    }

    private fun updateButtonState(isRunning: Boolean, isFinished: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning && !isFinished
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }
}