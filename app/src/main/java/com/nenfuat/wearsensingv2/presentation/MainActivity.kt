package com.nenfuat.wearsensingv2.presentation

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ChipColors
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway
import com.nenfuat.wearsensingv2.R
import com.nenfuat.wearsensingv2.presentation.theme.WearSensingV2Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


enum class Nav {
    TopScreen,
    SensorSelectScreen,
    PairingScreen,
    SensingScreen,
    SettingScreen
}

class MainActivity : ComponentActivity(), SensorEventListener {
    //センサマネージャ
    private lateinit var sensorManager: SensorManager
    private var AccSensor: Sensor? = null
    private var AccGSensor: Sensor?=null
    private var GyroSensor: Sensor? = null
    private var HeartRateSensor: Sensor? = null
    private var LightSensor: Sensor? = null
    val globalvariable = GlobalVariable.getInstance()
    val connectAPI = ConnectAPI(globalvariable)

    //センサデータ表示用
    lateinit var accDataArray: Array<MutableState<String>>
    lateinit var accGDataArray: Array<MutableState<String>>
    lateinit var gyroDataArray: Array<MutableState<String>>
    lateinit var heartrateDataArray: Array<MutableState<String>>
    lateinit var lightDataArray: Array<MutableState<String>>

    // 設定保持
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // パーミッションが許可された場合

            } else {
                // パーミッションが拒否された場合
                Log.e("MainActivity", "心拍センサーのパーミッションが拒否されました。")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        sharedPreferences = getSharedPreferences("SensingAppPrefs", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        setContent {
            accDataArray = Array(3) { remember { mutableStateOf("データが取れませんでした") } }
            accGDataArray = Array(3) { remember { mutableStateOf("データが取れませんでした") } }
            gyroDataArray = Array(3) { remember { mutableStateOf("データが取れませんでした") } }
            heartrateDataArray =
                Array(3) { remember { mutableStateOf("データが取れませんでした") } }
            heartrateDataArray[0].value = ""
            heartrateDataArray[2].value = ""
            lightDataArray = Array(3) { remember { mutableStateOf("データが取れませんでした") } }
            lightDataArray[0].value = ""
            lightDataArray[2].value = ""

            WearSensingV2Theme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Nav.TopScreen.name) {
                    composable(route = Nav.TopScreen.name) {
                        TitleScreen(navController = navController)
                    }
                    composable(route = Nav.SensorSelectScreen.name) {
                        WearApp(navController = navController, globalVariable = globalvariable)
                    }
                    composable(route = Nav.SensingScreen.name) {
                        SensingScreen(
                            navController = navController,
                            globalVariable = globalvariable
                        )
                    }
                    composable(route = Nav.SettingScreen.name) {
                        SettingScreen(navController = navController)
                    }
                    composable(route = Nav.PairingScreen.name){
                        PairingScreen(navController=navController)
                    }
                }
            }

        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        AccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        AccGSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        GyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        HeartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        LightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        // パーミッションの確認とリクエスト
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BODY_SENSORS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // パーミッションが既に許可されている場合

        } else {
            // パーミッションをリクエスト
            requestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        //別の場所でオーバーライドしてる
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    //こっからUI系
    @Composable
    fun TitleScreen(navController: NavController) {
        //println(sharedPreferences.getString("bucket", null))
        globalvariable.bucket=sharedPreferences.getString("bucket", null)
        // 初期描画時にshouldNavigateの値を設定
        LaunchedEffect(Unit) {
            if (sharedPreferences.getString("bucket", null) == null) {
                navController.navigate(Nav.SettingScreen.name)
            }
        }
        val listState = rememberScalingLazyListState()
        Scaffold(
            timeText = {
                TimeText(modifier = Modifier.scrollAway(listState))
            },
            vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState
                )
            }
        ) {
            val contentModifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)

            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                autoCentering = AutoCenteringParams(itemIndex = 0),
                state = listState
            ) {

                item {
                    Text("センサデータ", fontSize = 20.sp)
                }
                item {
                    Text("取る蔵V2", fontSize = 20.sp)
                }
                item {
                    Chip(
                        modifier = contentModifier,
                        onClick = {
                            ResetFlag(globalvariable)
                            navController.navigate(Nav.SensorSelectScreen.name)
                        },
                        label = {
                            Text(
                                text = "片手",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                    )
                }
                item {
                    Chip(
                        modifier = contentModifier,
                        onClick = {
                            navController.navigate(Nav.PairingScreen.name)
                        },
                        label = {
                            Text(
                                text = "両手(未実装)",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                    )
                }
                item {
                    Chip(
                        modifier = contentModifier,
                        onClick = {
                            navController.navigate(Nav.SettingScreen.name)
                        },
                        label = {
                            Text(
                                text = "設定",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                    )
                }
            }
        }
    }

    @Composable
    fun WearApp(navController: NavController, globalVariable: GlobalVariable) {
        val listState = rememberScalingLazyListState()
        Scaffold(
            timeText = {
                TimeText(modifier = Modifier.scrollAway(listState))
            },
            vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState
                )
            }
        ) {

            val contentModifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)

            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                autoCentering = AutoCenteringParams(itemIndex = 0),
                state = listState
            ) {

                val reusableComponents = ReusableComponents()
                item {
                    Text("使用するセンサ", fontSize = 20.sp)
                }
                item {
                    Chip(
                        modifier = contentModifier,
                        onClick = {
                            navController.popBackStack()
                        },
                        label = {
                            Text(
                                text = "戻る",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                    )
                }
                item { reusableComponents.AccToggle(contentModifier) }
                item { reusableComponents.AccGToggle(contentModifier) }
                item { reusableComponents.GyroToggle(contentModifier) }
                item { reusableComponents.HeartRateToggle(contentModifier) }
                item { reusableComponents.LightToggle(contentModifier) }
                item {
                    Chip(
                        modifier = contentModifier,
                        onClick = {
                            navController.navigate(Nav.SensingScreen.name)
                        },
                        label = {
                            Text(
                                text = "使用センサ確定",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                    )
                }
            }
        }
    }


    @Composable
    fun RateSettingScreen(navController: NavController){
        fun saveRate(rate:String,onSuccess: () -> Unit){
            editor.putString("rate",rate)
            val commitSuccess = editor.commit()
            if (commitSuccess) {
                onSuccess()
            }
        }
    }

    @Composable
    fun SettingScreen(navController: NavController) {
        fun saveBucket(bucket: String, onSuccess: () -> Unit) {
            editor.putString("bucket", bucket)
            val commitSuccess = editor.commit()
            if (commitSuccess) {
                onSuccess()
            }
        }

        val listState = rememberScalingLazyListState()
        // バケットリストの状態を保持
        var buckets by remember { mutableStateOf<List<String>?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        Scaffold(
            timeText = {
                TimeText(modifier = Modifier.scrollAway(listState))
            },
            vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState
                )
            }
        ) {
            // コルーチンを起動してAPI呼び出しを実行
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) {
                scope.launch {
                    try {
                        val result = connectAPI.getBuckets()
                        buckets = result
                    } catch (e: Exception) {
                        errorMessage = "Failed to fetch buckets"
                        e.printStackTrace()
                    }
                }
            }
            // バケットリストを表示
            if (buckets != null) {
                val contentModifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)

                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    autoCentering = AutoCenteringParams(itemIndex = 0),
                    state = listState
                ) {


                    val reusableComponents = ReusableComponents()
                    item {
                        Text("保存先バケット選択", fontSize = 20.sp)
                    }
                    item {
                        Text(
                            "現在の保存先:${sharedPreferences.getString("bucket", "")}",
                            fontSize = 15.sp
                        )
                    }

                    for (bucket in buckets!!) {
                        item {
                            Chip(
                                modifier = contentModifier,
                                onClick = {
                                    saveBucket(bucket) {
                                        navController.popBackStack()
                                    }
                                },
                                label = {
                                    Text(
                                        text = bucket,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                            )
                        }
                    }


                    if (sharedPreferences.getString("bucket", null) != null) {
                        item {
                            Chip(
                                modifier = contentModifier,
                                onClick = {
                                    navController.popBackStack()
                                },
                                label = {
                                    Text(
                                        text = "戻る",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                            )
                        }
                    }


                }
            } else if (errorMessage != null) {
                // エラーメッセージを表示
                Text(text = errorMessage!!)
            } else {
                // データの読み込み中を示すプログレスバーを表示
                CircularProgressIndicator()
            }


        }


    }

    @Composable
    fun PairingScreen(navController: NavController){
        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center){
            Text(text = "つけてる腕")
            Row (modifier = Modifier.fillMaxWidth()){
                Chip(
                    modifier = Modifier.weight(1f),
                    onClick = {

                    },
                    label = {
                        Text(
                            text = "左手",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                )
                Chip(
                    modifier = Modifier.weight(1f),
                    onClick = {

                    },
                    label = {
                        Text(
                            text = "右手",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                )
            }
            Chip(
                modifier = Modifier,
                onClick = {
                    navController.popBackStack()
                },
                label = {
                    Text(
                        text = "戻る",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
            )
        }

    }

    @Composable
    fun SensingScreen(navController: NavController, globalVariable: GlobalVariable) {
        var recordingFlag by remember { mutableStateOf(false) }
        var saveFlag by remember { mutableStateOf(false) }
        val listState = rememberScalingLazyListState()
        val accFileStorage = remember { mutableStateOf<OtherFileStorage?>(null) }
        val accGFileStorage=remember { mutableStateOf<OtherFileStorage?>(null) }
        val gyroFileStorage = remember { mutableStateOf<OtherFileStorage?>(null) }
        val lightFileStorage = remember { mutableStateOf<OtherFileStorage?>(null) }
        val heartrateFileStorage = remember { mutableStateOf<OtherFileStorage?>(null) }
        val context= LocalContext.current
        if (globalVariable.isAccSensorEnabled && accFileStorage.value == null) {
            println("Initializing accFileStorage")
            globalVariable.accFileName="${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}_acc"
            accFileStorage.value = OtherFileStorage(context,globalVariable.accFileName?: "","acc")
        }

        if (globalVariable.isAccGSensorEnabled && accGFileStorage.value == null) {
            println("Initializing accGFileStorage")
            globalVariable.accGFileName="${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}_accg"
            accGFileStorage.value = OtherFileStorage(context,globalVariable.accGFileName?: "","acc")
        }

        if (globalVariable.isGyroSensorEnabled && gyroFileStorage.value == null) {
            println("Initializing gyroFileStorage")
            globalVariable.gyroFileName="${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}_gyro"
            gyroFileStorage.value = OtherFileStorage(context, globalVariable.gyroFileName?: "","gyro")
        }
        if (globalVariable.isLightSensorEnabled && lightFileStorage.value == null) {
            println("Initializing lightFileStorage")
            globalVariable.lightFileName="${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}_light"
            lightFileStorage.value = OtherFileStorage(context, globalVariable.lightFileName?: "","light")
        }
        if (globalVariable.isHeartRateSensorEnabled && heartrateFileStorage.value == null) {
            println("Initializing heartrateFileStorage")
            globalVariable.heartrateFileName="${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}_heartrate"
            heartrateFileStorage.value = OtherFileStorage(context, globalVariable.heartrateFileName?: "","heartrate")
        }


        fun CoroutineScope.sendCsv(context: Context,fileName:String,path:String) {
            launch {
                try {
                    println(fileName)
                    // sendCsv関数の呼び出し
                    connectAPI.sendCsv(context, fileName,path)

                } catch (e: Exception) {
                    // エラーが発生したときの処理
                    Log.e("error", e.toString())
                }
            }
        }
        Scaffold(
            timeText = {
                TimeText(modifier = Modifier.scrollAway(listState))
            },
            vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState
                )
            }
        ) {

            val contentModifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)

            if (saveFlag){
                Column(modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    Text(text = "保存しますか?", modifier = Modifier.padding(bottom = 8.dp))
                    var pathValue by remember { mutableStateOf("") }

                    val keyboardController = LocalSoftwareKeyboardController.current

                    TextField(
                        value = pathValue,
                        onValueChange = { newValue ->
                            pathValue = newValue

                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(bottom = 8.dp),
                        placeholder = { Text("例:root/") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                // キーボードのDoneボタンが押されたときの処理
                                keyboardController?.hide()
                            }
                        )
                    )
                    Row (modifier = Modifier.fillMaxWidth(0.8f)){
                        Chip(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    try {
                                        if (globalVariable.isAccSensorEnabled) {
                                            sendCsv(context, globalVariable.accFileName ?: "", pathValue)
                                        }
                                        if (globalVariable.isAccGSensorEnabled) {
                                            sendCsv(context, globalVariable.accGFileName ?: "", pathValue)
                                        }
                                        if (globalVariable.isGyroSensorEnabled) {
                                            sendCsv(context, globalVariable.gyroFileName ?: "", pathValue)
                                        }
                                        if (globalVariable.isLightSensorEnabled) {
                                            sendCsv(context, globalVariable.lightFileName ?: "", pathValue)
                                        }
                                        if (globalVariable.isHeartRateSensorEnabled) {
                                            sendCsv(context, globalVariable.heartrateFileName ?: "", pathValue)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("SendCsv", "Error occurred: ${e.message}", e)
                                    }
                                    ResetFlag(globalVariable)
                                    navController.popBackStack()
                                }
                            },
                            label = {
                                Text(
                                    text = "YES",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                        )
//                        Box(modifier = Modifier.weight(0.2f))
//                        Chip(
//                            modifier = Modifier.weight(1f),
//                            onClick = {
//                                if (globalVariable.isAccSensorEnabled) {
//                                    File(context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString().plus("/").plus(globalVariable.accFileName).plus(".csv")).delete()
//                                }
//                                if (globalVariable.isAccGSensorEnabled) {
//                                    File(context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString().plus("/").plus(globalVariable.accGFileName).plus(".csv")).delete()
//                                }
//                                if (globalVariable.isGyroSensorEnabled) {
//                                    File(context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString().plus("/").plus(globalVariable.gyroFileName).plus(".csv")).delete()
//                                }
//                                if (globalVariable.isLightSensorEnabled) {
//                                    File(context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString().plus("/").plus(globalVariable.lightFileName).plus(".csv")).delete()
//                                }
//                                if (globalVariable.isHeartRateSensorEnabled) {
//                                    File(context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString().plus("/").plus(globalVariable.heartrateFileName).plus(".csv")).delete()
//                                }
//                                ResetFlag(globalVariable)
//                                navController.popBackStack()
//                            },
//                            label = {
//                                Text(
//                                    text = "NO",
//                                    maxLines = 1,
//                                    overflow = TextOverflow.Ellipsis
//                                )
//                            },
//                        )
                    }
                }
            }
            else{
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    autoCentering = AutoCenteringParams(itemIndex = 0),
                    state = listState
                ) {
                    val reusableComponents = ReusableComponents()

                    item {
                        if (recordingFlag) {
                            Text(text = "記録中", fontSize = 15.sp)
                        }
                        else{
                            Text(text = "", fontSize = 15.sp)
                        }
                    }
                    if (globalVariable.isAccSensorEnabled) {
                        item {
                            // 加速度センサーデータの取得
                            reusableComponents.MultiView(
                                sensor = "加速度センサ",
                                sensorDataArray = accDataArray,
                                modifier = Modifier
                            )
                            LaunchedEffect(Unit) {
                                sensorManager.registerListener(object : SensorEventListener {
                                    override fun onSensorChanged(event: SensorEvent?) {
                                        if (event != null) {
                                            // 加速度センサーのデータを更新
                                            accDataArray[0].value = "X: ${event.values[0]}"
                                            accDataArray[1].value = "Y: ${event.values[1]}"
                                            accDataArray[2].value = "Z: ${event.values[2]}"
                                            if (recordingFlag){
                                                accFileStorage.value?.writeText("${System.currentTimeMillis()},${event.values[0]},${event.values[1]},${event.values[2]}")
                                            }
                                        }
                                    }

                                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                                    }
                                }, AccSensor, SensorManager.SENSOR_DELAY_NORMAL)
                            }

                        }

                    }
                    if (globalVariable.isAccGSensorEnabled) {
                        item {
                            // 加速度センサーデータの取得
                            reusableComponents.MultiView(
                                sensor = "加速度センサ(重力)",
                                sensorDataArray = accGDataArray,
                                modifier = Modifier
                            )
                            LaunchedEffect(Unit) {
                                sensorManager.registerListener(object : SensorEventListener {
                                    override fun onSensorChanged(event: SensorEvent?) {
                                        if (event != null) {
                                            // 加速度センサーのデータを更新
                                            accGDataArray[0].value = "X: ${event.values[0]}"
                                            accGDataArray[1].value = "Y: ${event.values[1]}"
                                            accGDataArray[2].value = "Z: ${event.values[2]}"
                                            if (recordingFlag){
                                                accGFileStorage.value?.writeText("${System.currentTimeMillis()},${event.values[0]},${event.values[1]},${event.values[2]}")
                                            }
                                        }
                                    }

                                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                                    }
                                }, AccGSensor, SensorManager.SENSOR_DELAY_NORMAL)
                            }

                        }

                    }
                    if (globalVariable.isGyroSensorEnabled) {
                        item {
                            reusableComponents.MultiView(
                                sensor = "ジャイロセンサ",
                                sensorDataArray = gyroDataArray,
                                modifier = Modifier
                            )
                            LaunchedEffect(Unit) {
                                sensorManager.registerListener(object : SensorEventListener {
                                    override fun onSensorChanged(event: SensorEvent?) {
                                        if (event != null) {
                                            // センサーのデータを更新
                                            gyroDataArray[0].value = "X: ${event.values[0]}"
                                            gyroDataArray[1].value = "Y: ${event.values[1]}"
                                            gyroDataArray[2].value = "Z: ${event.values[2]}"
                                            if (recordingFlag){
                                                gyroFileStorage.value?.writeText("${System.currentTimeMillis()},${event.values[0]},${event.values[1]},${event.values[2]}")
                                            }
                                        }
                                    }

                                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                                    }
                                }, GyroSensor, SensorManager.SENSOR_DELAY_NORMAL)
                            }
                        }
                    }
                    if (globalVariable.isHeartRateSensorEnabled) {
                        item {
                            reusableComponents.MultiView(
                                sensor = "心拍センサ",
                                sensorDataArray = heartrateDataArray,
                                modifier = Modifier
                            )
                            LaunchedEffect(Unit) {
                                sensorManager.registerListener(object : SensorEventListener {
                                    override fun onSensorChanged(event: SensorEvent?) {
                                        if (event != null) {
                                            // センサーのデータを更新
                                            heartrateDataArray[0].value = ""
                                            heartrateDataArray[1].value = "心拍: ${event.values[0]}"
                                            heartrateDataArray[2].value = ""
                                            if (recordingFlag){
                                                heartrateFileStorage.value?.writeText("${System.currentTimeMillis()},${event.values[0]}")
                                            }
                                        }
                                    }

                                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                                    }
                                }, HeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
                            }
                        }
                    }
                    if (globalVariable.isLightSensorEnabled) {
                        item {
                            reusableComponents.MultiView(
                                sensor = "照度センサ",
                                sensorDataArray = lightDataArray,
                                modifier = Modifier
                            )
                            LaunchedEffect(Unit) {
                                sensorManager.registerListener(object : SensorEventListener {
                                    override fun onSensorChanged(event: SensorEvent?) {
                                        if (event != null) {
                                            // センサーのデータを更新
                                            lightDataArray[0].value = ""
                                            lightDataArray[1].value = "照度: ${event.values[0]}"
                                            lightDataArray[2].value = ""
                                            if (recordingFlag){
                                                lightFileStorage.value?.writeText("${System.currentTimeMillis()},${event.values[0]}")
                                            }
                                        }
                                    }

                                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                                    }
                                }, LightSensor, SensorManager.SENSOR_DELAY_NORMAL)
                            }
                        }
                    }
                    item {
                        Chip(
                            modifier = contentModifier,
                            onClick = {
                                if (recordingFlag){
                                    saveFlag=true
                                }
                                recordingFlag = !recordingFlag
                            },
                            label = {
                                if (recordingFlag) {
                                    Text(
                                        text = "記録停止",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Text(
                                        text = "記録開始",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                            },
                        )
                    }
                    item {
                        Chip(
                            modifier = contentModifier,
                            onClick = {
                                ResetFlag(globalVariable)
                                navController.popBackStack()
                            },
                            label = {
                                Text(
                                    text = "戻る",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    fun ResetFlag(globalVariable: GlobalVariable) {
        globalVariable.isAccSensorEnabled = false
        globalVariable.isAccGSensorEnabled = false
        globalVariable.isGyroSensorEnabled = false
        globalVariable.isLightSensorEnabled = false
        globalVariable.isHeartRateSensorEnabled = false
    }
}


@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    //WearApp("Preview Android")
}