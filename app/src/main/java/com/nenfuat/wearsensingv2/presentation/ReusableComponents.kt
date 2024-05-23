package com.nenfuat.wearsensingv2.presentation

import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Chip
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip

class ReusableComponents {


    @Composable
    fun AccToggle(modifier: Modifier = Modifier,) {
        val globalvariable = GlobalVariable.getInstance()
        var checked by remember { mutableStateOf(globalvariable.isAccSensorEnabled) }

        ToggleChip(
            modifier = modifier,
            checked = checked,
            toggleControl = {
                Switch(
                    checked = checked,
                    modifier = Modifier.semantics {
                        this.contentDescription = if (checked) "On" else "Off"
                    }
                )
            },
            onCheckedChange = {
                checked = it
                globalvariable.isAccSensorEnabled = checked
                println(globalvariable.isAccSensorEnabled)
            },
            label = {
                Text(
                    text = "加速度",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }
    @Composable
    fun GyroToggle(modifier: Modifier = Modifier) {
        val globalvariable = GlobalVariable.getInstance()
        var checked by remember { mutableStateOf(globalvariable.isGyroSensorEnabled) }
        ToggleChip(
            modifier = modifier,
            checked = checked,
            toggleControl = {
                Switch(
                    checked = checked,
                    modifier = Modifier.semantics {
                        this.contentDescription = if (checked) "On" else "Off"
                    }
                )
            },
            onCheckedChange = {
                checked = it
                globalvariable.isGyroSensorEnabled = checked
            },
            label = {
                Text(
                    text = "ジャイロ",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }
    @Composable
    fun HeartRateToggle(modifier: Modifier = Modifier) {
        val globalvariable = GlobalVariable.getInstance()
        var checked by remember { mutableStateOf(globalvariable.isHeartRateSensorEnabled) }
        ToggleChip(
            modifier = modifier,
            checked = checked,
            toggleControl = {
                Switch(
                    checked = checked,
                    modifier = Modifier.semantics {
                        this.contentDescription = if (checked) "On" else "Off"
                    }
                )
            },
            onCheckedChange = {
                checked = it
                globalvariable.isHeartRateSensorEnabled = checked
            },
            label = {
                Text(
                    text = "心拍",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }
    @Composable
    fun LightToggle(modifier: Modifier = Modifier) {
        val globalvariable = GlobalVariable.getInstance()
        var checked by remember { mutableStateOf(globalvariable.isLightSensorEnabled) }
        ToggleChip(
            modifier = modifier,
            checked = checked,
            toggleControl = {
                Switch(
                    checked = checked,
                    modifier = Modifier.semantics {
                        this.contentDescription = if (checked) "On" else "Off"
                    }
                )
            },
            onCheckedChange = {
                checked = it
                globalvariable.isLightSensorEnabled = checked
            },
            label = {
                Text(
                    text = "照度",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }
    @Composable
    fun SetMultiChip(
        modifier: Modifier = Modifier,
        iconModifier: Modifier = Modifier
    ) {
        val context = LocalContext.current

    }

    @Composable
    fun MultiView(sensor: String, modifier: Modifier = Modifier, sensorDataArray: Array<MutableState<String>>) {
        Column(
            modifier = modifier
        ) {
            Text(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = sensor,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                textAlign = TextAlign.Left,
                color = MaterialTheme.colors.primary,
                text = sensorDataArray[0].value,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                textAlign = TextAlign.Left,
                color = MaterialTheme.colors.primary,
                text = sensorDataArray[1].value,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                textAlign = TextAlign.Left,
                color = MaterialTheme.colors.primary,
                text = sensorDataArray[2].value,
            )
        }
    }

    @Composable
    fun SendMenu(sensorDataArray: Array<MutableState<String>>){

    }

}