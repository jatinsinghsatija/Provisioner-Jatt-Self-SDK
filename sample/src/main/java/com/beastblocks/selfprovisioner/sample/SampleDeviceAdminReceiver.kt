package com.beastblocks.selfprovisioner.sample

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class SampleDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) = Unit
}
