
package com.apps.daniel.poro.presentation.about

import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.apps.daniel.poro.R
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import androidx.core.content.ContextCompat
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import android.content.Intent
import com.apps.daniel.poro.presentation.intro.MainIntroActivity
import com.apps.daniel.poro.presentation.settings.PreferenceHelper
import com.apps.daniel.poro.presentation.timer.TimerActivity
import com.apps.daniel.poro.util.DeviceInfo
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.apps.daniel.poro.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AboutActivity : MaterialAboutActivity() {

    @Inject lateinit var preferenceHelper: PreferenceHelper

    override fun getMaterialAboutList(c: Context): MaterialAboutList {
        val builder1 = MaterialAboutCard.Builder()
        val colorIcon = R.color.grey50
        builder1.addItem(
            MaterialAboutTitleItem.Builder()
                .text(getString(R.string.app_name_long))
                .icon(R.mipmap.ic_launcher)
                .build()
        )
        builder1.addItem(
            ConvenienceBuilder.createVersionActionItem(
                c,
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon2.cmd_information_outline)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18),
                getString(R.string.about_version),
                false
            )
        )

        builder1.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.about_app_intro))
            .icon(
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon2.cmd_presentation)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18)
            )
            .setOnClickAction {
                val i = Intent(this@AboutActivity, MainIntroActivity::class.java)
                startActivity(i)
            }
            .build())
        builder1.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.tutorial_title))
            .icon(
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon2.cmd_rocket)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18)
            )
            .setOnClickAction {
                preferenceHelper.lastIntroStep = 0
                preferenceHelper.archivedLabelHintWasShown = false
                val i = Intent(this@AboutActivity, TimerActivity::class.java)
                startActivity(i)
            }
            .build())
        val builder2 = MaterialAboutCard.Builder()
        builder2.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.feedback))
            .icon(
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon.cmd_email)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18)
            )
            .setOnClickAction { openFeedback() }.build()
        )

        builder2.addItem(
            ConvenienceBuilder.createRateActionItem(
                c,
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon2.cmd_star)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18),
                getString(R.string.about_rate_this_app),
                null
            )
        )
        val builder3 = MaterialAboutCard.Builder()
        builder3.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.other_apps))
            .icon(
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon.cmd_application)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18)
            )
            .setOnClickAction {
                //TODO need to add url
                val url = ""
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }.build()
        )
        return MaterialAboutList.Builder()
            .addCard(builder1.build())
            .addCard(builder2.build())
            .addCard(builder3.build())
            .build()
    }

    override fun getActivityTitle(): CharSequence {
        return getString(R.string.mal_title_about)
    }

    private fun openFeedback() {
        val email = Intent(Intent.ACTION_SENDTO)
        email.data = Uri.Builder().scheme("mailto").build()
        email.putExtra(Intent.EXTRA_EMAIL, arrayOf(""))
        email.putExtra(Intent.EXTRA_SUBJECT, "[Poro] Feedback")
        email.putExtra(
            Intent.EXTRA_TEXT, """
     
     My device info: 
     ${DeviceInfo.deviceInfo}
     App version: ${BuildConfig.VERSION_NAME}
     """.trimIndent()
        )
        try {
            startActivity(Intent.createChooser(email, this.getString(R.string.feedback_title)))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, R.string.about_no_email, Toast.LENGTH_SHORT).show()
        }
    }
}