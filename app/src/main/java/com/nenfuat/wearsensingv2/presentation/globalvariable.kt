package com.nenfuat.wearsensingv2.presentation
import android.app.Application
import androidx.compose.runtime.MutableState

class GlobalVariable :Application(){
    var mode: String? = null
    var isAccSensorEnabled: Boolean = false
    var isAccGSensorEnabled: Boolean = false
    var isGyroSensorEnabled: Boolean = false
    var isHeartRateSensorEnabled: Boolean = false
    var isLightSensorEnabled: Boolean = false
    var bucket:String?=null

    var accFileName: String? = null
    var accGFileName: String? = null
    var gyroFileName: String? = null
    var lightFileName: String? = null
    var heartrateFileName: String? = null

    var isRightHand:Boolean =false
    var isLeftHand:Boolean =false

    companion object {
        private var instance : GlobalVariable? = null
        fun  getInstance(): GlobalVariable {
            if (instance == null)
                instance = GlobalVariable()
            return instance!!
        }
    }
}