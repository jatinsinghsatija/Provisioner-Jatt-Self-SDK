package com.beastblocks.selfprovisioner.sample

import android.app.admin.DevicePolicyManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beastblocks.selfprovisionerjatt.SelfProvisionerJatt
import com.beastblocks.selfprovisionerjatt.SelfProvisionerOptions

class SampleActivity : ComponentActivity() {
    private val status = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (status.value.isEmpty()) status.value = getString(R.string.status_idle)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                SampleScreen(
                    onStart = { automate ->
                        SelfProvisionerJatt.start(
                            this,
                            { step ->
                                runOnUiThread {
                                    status.value = "${step.position}/${step.total} ${step.phase}: ${step.message}"
                                }
                            },
                            SelfProvisionerOptions(automate = automate),
                        )
                    },
                    onAccessibilityGuide = { SelfProvisionerJatt.openAccessibilityGuide(this) },
                    onDeveloperGuide = { SelfProvisionerJatt.openDeveloperModeGuide(this) },
                    onSetAccountFrp = { setAccountFrp() },
                    onSetOrganizationName = { setOrganizationName() },
                    onRemoveDeviceOwner = { removeDeviceOwner() },
                    status = status.value,
                )
            }
        }
    }

    private fun setAccountFrp() {
        SelfProvisionerJatt.addFRPAccount(this, { result ->
            val message = if (result.success) {
                "name: ${result.name.orEmpty()}\nemail: ${result.email.orEmpty()}"
            } else {
                result.reason.orEmpty()
            }
            runOnUiThread {
                if (!licenseNotice(message)) Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }, getString(R.string.default_web_client_id))
    }

    private fun setOrganizationName() {
        val result = SelfProvisionerJatt.setOrganizationName(getString(R.string.app_name), this)
        val message = if (result.success) "Organization name set" else result.reason.orEmpty()
        if (!licenseNotice(message)) Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun licenseNotice(message: String): Boolean =
        message.isEmpty() ||
            message == "This app is not allowed to use the SDK." ||
            message == "Debug trials has been finished for today and will continue tomorrow"

    private fun removeDeviceOwner() {
        val manager = getSystemService(DevicePolicyManager::class.java)
        val message = if (!manager.isDeviceOwnerApp(packageName)) {
            "This app is not device owner"
        } else {
            try {
                @Suppress("DEPRECATION")
                manager.clearDeviceOwnerApp(packageName)
                "Device owner removed"
            } catch (error: Exception) {
                error.message ?: "Could not remove device owner"
            }
        }
        status.value = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun SampleScreen(
    onStart: (Boolean) -> Unit,
    onAccessibilityGuide: () -> Unit,
    onDeveloperGuide: () -> Unit,
    onSetAccountFrp: () -> Unit,
    onSetOrganizationName: () -> Unit,
    onRemoveDeviceOwner: () -> Unit,
    status: String,
) {
    var automate by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 32.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = null,
            modifier = Modifier.size(88.dp).clip(CircleShape),
        )
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        RowSwitch(stringResource(R.string.automate), automate) { automate = it }
        ActionButton(stringResource(R.string.start), accent = true) { onStart(automate) }
        ActionButton(stringResource(R.string.accessibility_guide), accent = false, onAccessibilityGuide)
        ActionButton(stringResource(R.string.developer_guide), accent = false, onDeveloperGuide)
        ActionButton(stringResource(R.string.set_account_frp), accent = true, onSetAccountFrp)
        ActionButton(stringResource(R.string.set_organization_name), accent = true, onSetOrganizationName)
        ActionButton(stringResource(R.string.remove_device_owner), accent = false, onRemoveDeviceOwner)
        Text(
            status,
            color = Color(0x9E000000),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RowSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color.Black, fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ActionButton(label: String, accent: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (accent) Color(0xFFFF8A00) else Color(0xFFFFF6E8),
        shadowElevation = 6.dp,
    ) {
        Text(
            label,
            modifier = Modifier.padding(vertical = 16.dp),
            color = if (accent) Color.White else Color.Black,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}
