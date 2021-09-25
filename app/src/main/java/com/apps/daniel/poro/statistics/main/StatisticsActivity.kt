
package com.apps.daniel.poro.statistics.main

import com.apps.daniel.poro.util.UpgradeDialogHelper.Companion.launchUpgradeDialog
import com.apps.daniel.poro.statistics.main.SelectLabelDialog.Companion.newInstance
import dagger.hilt.android.AndroidEntryPoint
import com.apps.daniel.poro.statistics.main.SelectLabelDialog.OnLabelSelectedListener
import com.apps.daniel.poro.main.LabelsViewModel
import javax.inject.Inject
import com.apps.daniel.poro.settings.PreferenceHelper
import android.os.Bundle
import com.apps.daniel.poro.util.ThemeHelper
import androidx.databinding.DataBindingUtil
import com.apps.daniel.poro.R
import androidx.core.view.MenuItemCompat
import android.content.res.ColorStateList
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.apps.daniel.poro.database.Label
import com.apps.daniel.poro.databinding.StatisticsActivityMainBinding
import com.apps.daniel.poro.statistics.all_sessions.AddEditEntryDialog
import com.apps.daniel.poro.statistics.all_sessions.AllSessionsFragment

@AndroidEntryPoint
class StatisticsActivity : AppCompatActivity(), OnLabelSelectedListener {

    private val labelsViewModel: LabelsViewModel by viewModels()
    private var mMenuItemCrtLabel: MenuItem? = null
    private var mIsMainView = false

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.setTheme(this, preferenceHelper.isAmoledTheme())
        val binding: StatisticsActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.statistics_activity_main)
        setSupportActionBar(binding.toolbarWrapper.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        labelsViewModel.crtExtendedLabel.observe(
            this,
            { refreshCurrentLabel() })
        mIsMainView = false
        toggleStatisticsView()

        // dismiss at orientation changes
        dismissDialogs()
    }

    private fun dismissDialogs() {
        val fragmentManager = supportFragmentManager
        val dialogAddEntry =
            fragmentManager.findFragmentByTag(DIALOG_ADD_ENTRY_TAG) as DialogFragment?
        dialogAddEntry?.dismiss()
        val dialogSelectLabel =
            fragmentManager.findFragmentByTag(DIALOG_SELECT_LABEL_TAG) as DialogFragment?
        dialogSelectLabel?.dismiss()
        val dialogDate =
            fragmentManager.findFragmentByTag(DIALOG_DATE_PICKER_TAG) as DialogFragment?
        dialogDate?.dismiss()
        val dialogTime =
            fragmentManager.findFragmentByTag(DIALOG_TIME_PICKER_TAG) as DialogFragment?
        dialogTime?.dismiss()
    }

    private fun refreshCurrentLabel() {
        if (labelsViewModel.crtExtendedLabel.value != null && mMenuItemCrtLabel != null) {
            MenuItemCompat.setIconTintList(
                mMenuItemCrtLabel,
                ColorStateList.valueOf(
                    ThemeHelper.getColor(
                        this,
                        labelsViewModel.crtExtendedLabel.value!!.colorId
                    )
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_statistics_main, menu)
        mMenuItemCrtLabel = menu.findItem(R.id.action_select_label)
        refreshCurrentLabel()
        menu.findItem(R.id.action_view_list).icon = ContextCompat.getDrawable(
            this, if (mIsMainView) R.drawable.ic_list else R.drawable.ic_trending
        )
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragmentManager = supportFragmentManager
        when (item.itemId) {
            R.id.action_add -> {
                val newFragment = AddEditEntryDialog.newInstance(null)
                newFragment.show(fragmentManager, DIALOG_ADD_ENTRY_TAG)
            }
            /*if (preferenceHelper.isPro()) {

            } else {
                launchUpgradeDialog(supportFragmentManager)
            }*/
            R.id.action_select_label -> newInstance(
                this,
                labelsViewModel.crtExtendedLabel.value!!.title,
                true
            )
                .show(fragmentManager, DIALOG_SELECT_LABEL_TAG)
            R.id.action_view_list -> toggleStatisticsView()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleStatisticsView() {
        mIsMainView = !mIsMainView
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment,
                if (mIsMainView) StatisticsFragment() else AllSessionsFragment()
            )
            .commitAllowingStateLoss()
    }

    override fun onLabelSelected(label: Label) {
        labelsViewModel.crtExtendedLabel.value = label
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    companion object {
        const val DIALOG_ADD_ENTRY_TAG = "dialogAddEntry"
        const val DIALOG_SELECT_LABEL_TAG = "dialogSelectLabel"
        const val DIALOG_DATE_PICKER_TAG = "datePickerDialog"
        const val DIALOG_TIME_PICKER_TAG = "timePickerDialog"
    }
}