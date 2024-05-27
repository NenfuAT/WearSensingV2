package com.nenfuat.wearsensingv2.presentation

import android.content.Context
import android.os.Environment
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter

class OtherFileStorage(context: Context,fileName:String,sensor:String) {

    val fileAppend : Boolean = true //true=追記, false=上書き
    val extension : String = ".csv"
    val filePath: String = context.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString().plus("/").plus(fileName).plus(extension) //内部ストレージのDocumentのURL

    init {
        val fil = FileWriter(filePath,fileAppend)
        val pw = PrintWriter(BufferedWriter(fil))
        var text:String?=null
        if (sensor=="acc"||sensor=="gyro"){
            text = "time,x,y,z"
        }
        if (sensor=="light"){
            text = "time,lux"
        }
        if (sensor=="heartrate"){
            text = "time,rate"
        }
        pw.println(text?:"")
        pw.close()
    }

    fun writeText(sensorData:String){
        val fil = FileWriter(filePath,fileAppend)
        val pw = PrintWriter(BufferedWriter(fil))
        pw.println(sensorData)
        pw.close()
    }
}