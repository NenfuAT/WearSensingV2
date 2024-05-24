/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.nenfuat.wearsensingv2.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Chip
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
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.WearNavigator
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.nenfuat.wearsensingv2.R
import com.nenfuat.wearsensingv2.presentation.theme.WearSensingV2Theme

enum class Nav {
    TopScreen,
    SensorSelectScreen,
    SensingScreen
}

class MainActivity : ComponentActivity() , SensorEventListener {
    //センサマネージャ
    private lateinit var sensorManager: SensorManager
    private var AccSensor: Sensor? = null
    private var GyroSensor: Sensor? = null
    private var HeartRateSensor: Sensor? = null
    private var LightSensor: Sensor? = null
    val globalvariable = GlobalVariable.getInstance()
    //センサデータ表示用
    lateinit var accDataArray: Array<MutableState<String>>
    lateinit var gyroDataArray: Array<MutableState<String>>
    lateinit var heartrateDataArray: Array<MutableState<String>>
    lateinit var lightDataArray: Array<MutableState<String>>
    //csv形式の配列
    lateinit var accCsv:List<String>
    lateinit var gyroCsv:List<String>
    lateinit var heartrateCsv:List<String>
    lateinit var lightCsv:List<String>


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

        setContent {
            accDataArray = Array(3) { remember { mutableStateOf("データが取れませんでした") } }
            gyroDataArray = Array(3) { remember { mutableStateOf("データが取れませんでした") } }
            heartrateDataArray = Array(3) { remember { mutableStateOf("データが取れませんでした") } }
            heartrateDataArray[0].value =""
            heartrateDataArray[2].value =""
            lightDataArray = Array(3) { remember { mutableStateOf("データが取れませんでした") } }
            lightDataArray[0].value =""
            lightDataArray[2].value =""

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
                        SensingScreen(navController = navController, globalVariable = globalvariable)
                    }
                }
            }

        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        AccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        GyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        HeartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        LightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        // パーミッションの確認とリクエスト
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            // パーミッションが既に許可されている場合

        } else {
            // パーミッションをリクエスト
            requestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        TODO("Not yet implemented")
        //別の場所でオーバーライドしてる
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    //こっからUI系
    @Composable
    fun TitleScreen(navController: NavController) {
        Log.d("Screen","Title")
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
                            //navController.navigate(Nav.SensorSelectScreen.name)
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
            }
        }
    }


    @Composable
    fun WearApp(navController: NavController,globalVariable: GlobalVariable) {
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
                item { reusableComponents.GyroToggle(contentModifier) }
                item { reusableComponents.HeartRateToggle(contentModifier) }
                item { reusableComponents.LightToggle(contentModifier) }
                item { Chip(
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
                )}
            }
        }
    }

    @Composable
    fun SensingScreen(navController: NavController, globalVariable: GlobalVariable) {
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
                println(globalVariable.isAccSensorEnabled)
                if (globalVariable.isAccSensorEnabled) {
                    item {
                        // 加速度センサーデータの取得
                        reusableComponents.MultiView(sensor = "加速度センサ", sensorDataArray = accDataArray, modifier = Modifier)
                        LaunchedEffect(Unit) {
                            sensorManager.registerListener(object : SensorEventListener {
                                override fun onSensorChanged(event: SensorEvent?) {
                                    if (event != null) {
                                        // 加速度センサーのデータを更新
                                        accDataArray[0].value = "X: ${event.values[0]}"
                                        accDataArray[1].value = "Y: ${event.values[1]}"
                                        accDataArray[2].value = "Z: ${event.values[2]}"
                                    }
                                }

                                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                                }
                            }, AccSensor, SensorManager.SENSOR_DELAY_NORMAL)
                        }

                    }

                }
                if (globalVariable.isGyroSensorEnabled){
                    item { reusableComponents.MultiView(sensor = "ジャイロセンサ", sensorDataArray = gyroDataArray,modifier = Modifier)
                        LaunchedEffect(Unit) {
                            sensorManager.registerListener(object : SensorEventListener {
                                override fun onSensorChanged(event: SensorEvent?) {
                                    if (event != null) {
                                        // 加速度センサーのデータを更新
                                        gyroDataArray[0].value = "X: ${event.values[0]}"
                                        gyroDataArray[1].value = "Y: ${event.values[1]}"
                                        gyroDataArray[2].value = "Z: ${event.values[2]}"
                                    }
                                }

                                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                                }
                            }, GyroSensor, SensorManager.SENSOR_DELAY_NORMAL)
                        }
                    }
                }
                if (globalVariable.isHeartRateSensorEnabled){
                    item { reusableComponents.MultiView(sensor = "心拍センサ", sensorDataArray = heartrateDataArray, modifier = Modifier)
                        LaunchedEffect(Unit) {
                            sensorManager.registerListener(object : SensorEventListener {
                                override fun onSensorChanged(event: SensorEvent?) {
                                    if (event != null) {
                                        // 加速度センサーのデータを更新
                                        heartrateDataArray[0].value = ""
                                        heartrateDataArray[1].value = "心拍: ${event.values[0]}"
                                        heartrateDataArray[2].value = ""
                                    }
                                }

                                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                                }
                            }, HeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
                        }
                    }
                }
                if (globalVariable.isLightSensorEnabled){
                    item { reusableComponents.MultiView(sensor = "照度センサ", sensorDataArray = lightDataArray,modifier = Modifier)
                        LaunchedEffect(Unit) {
                            sensorManager.registerListener(object : SensorEventListener {
                                override fun onSensorChanged(event: SensorEvent?) {
                                    if (event != null) {
                                        // 加速度センサーのデータを更新
                                        lightDataArray[0].value = ""
                                        lightDataArray[1].value = "照度: ${event.values[0]}"
                                        lightDataArray[2].value = ""
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

    fun ResetFlag(globalVariable: GlobalVariable){
        globalVariable.isAccSensorEnabled=false
        globalVariable.isGyroSensorEnabled=false
        globalVariable.isLightSensorEnabled=false
        globalVariable.isHeartRateSensorEnabled=false
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