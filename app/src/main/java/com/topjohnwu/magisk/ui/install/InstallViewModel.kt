package com.topjohnwu.magisk.ui.install

import android.net.Uri
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.extensions.addOnPropertyChangedCallback
import com.topjohnwu.magisk.model.download.DownloadService
import com.topjohnwu.magisk.model.entity.internal.Configuration
import com.topjohnwu.magisk.model.entity.internal.DownloadSubject
import com.topjohnwu.magisk.model.events.RequestFileEvent
import com.topjohnwu.magisk.redesign.compat.CompatViewModel
import com.topjohnwu.magisk.utils.KObservableField
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import org.koin.core.get

class InstallViewModel : CompatViewModel() {

    val isRooted = Shell.rootAccess()
    val isAB = isABDevice()

    val step = KObservableField(0)
    val method = KObservableField(-1)

    var data = KObservableField<Uri?>(null)

    init {
        method.addOnPropertyChangedCallback {
            if (method.value == R.id.method_patch) {
                RequestFileEvent().publish()
            }
        }
    }

    fun step(nextStep: Int) {
        step.value = nextStep
    }

    fun install() = DownloadService(get()) {
        subject = DownloadSubject.Magisk(resolveConfiguration())
    }

    // ---

    private fun resolveConfiguration() = when (method.value) {
        R.id.method_download -> Configuration.Download
        R.id.method_patch -> Configuration.Patch(data.value!!)
        R.id.method_direct -> Configuration.Flash.Primary
        R.id.method_inactive_slot -> Configuration.Flash.Secondary
        else -> throw IllegalArgumentException("Unknown value")
    }

    private fun isABDevice() = ShellUtils
        .fastCmd("grep_prop ro.build.ab_update")
        .let { it.isNotEmpty() && it.toBoolean() }

}