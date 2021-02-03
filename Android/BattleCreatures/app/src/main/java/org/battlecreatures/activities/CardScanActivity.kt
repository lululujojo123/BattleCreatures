/*
 * Copyright (c) 2021 lululujojo123
 *
 * CardScanActivity.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/2/1 \ Andreas G.
 */

package org.battlecreatures.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Size
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.battlecreatures.R
import org.battlecreatures.logics.QRCodeAPI
import org.battlecreatures.logics.database.BCDatabase
import org.battlecreatures.logics.database.CardDAO
import org.battlecreatures.logics.entities.Card
import org.battlecreatures.logics.entities.QRScanResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class CardScanActivity : AppCompatActivity() {
    /**
     * The camera manager object for management purposes of the android cameras
     */
    private lateinit var cameraManager: CameraManager

    /**
     * Constant defining which camera should be used
     */
    private val cameraFacing = CameraCharacteristics.LENS_FACING_BACK

    /**
     * The unique
     */
    private var cameraId: String = ""

    private var backgroundHandler: Handler? = null

    private lateinit var stateCallback: CameraDevice.StateCallback

    private var cameraDevice: CameraDevice? = null

    private lateinit var textureView: TextureView

    private lateinit var surfaceTextureListener: TextureView.SurfaceTextureListener

    private var backgroundThread: HandlerThread? = null

    private lateinit var captureRequestBuilder: CaptureRequest.Builder

    private lateinit var captureRequest: CaptureRequest

    private var cameraCaptureSession: CameraCaptureSession? = null

    private lateinit var previewSize: Size

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_scan)

        this.textureView = findViewById(R.id.textureView)

        this.cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        this.surfaceTextureListener = object: TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                setUpCamera()
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) { }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) { }
        }

        this.stateCallback = object: CameraDevice.StateCallback() {
            override fun onOpened(cameraDevice: CameraDevice) {
                this@CardScanActivity.cameraDevice = cameraDevice
                createPreviewSession()
            }

            override fun onDisconnected(cameraDevice: CameraDevice) {
                cameraDevice.close()
                this@CardScanActivity.cameraDevice = null
            }

            override fun onError(cameraDevice: CameraDevice, error: Int) {
                cameraDevice.close()
                this@CardScanActivity.cameraDevice = null
            }
        }

        findViewById<ImageView>(R.id.btnBack2).setOnClickListener {
            if (it.isClickable) {
                this.setClickableAndOnTouchForAllViews(false)

                this.onBackPressed()
            }
        }

        findViewById<FloatingActionButton>(R.id.scanCardButton2).setOnClickListener {
            if (it.isClickable) {
                findViewById<FloatingActionButton>(R.id.scanCardButton2).isClickable = false

                val byteArrayOutputStream = ByteArrayOutputStream()

                textureView.bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

                val imageBlob: ByteArray = byteArrayOutputStream.toByteArray()

                byteArrayOutputStream.close()

                val image: RequestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageBlob)

                val gson: Gson = GsonBuilder()
                        .setLenient()
                        .create()

                val retrofit: Retrofit = Retrofit.Builder()
                        .baseUrl("https://api.qrserver.com/")
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()

                val qrCodeAPI: QRCodeAPI = retrofit.create(QRCodeAPI::class.java)

                val call: Call<List<QRScanResponse>> = qrCodeAPI.getQRCodeResult(image)

                call.enqueue(object: Callback<List<QRScanResponse>> {
                    override fun onResponse(call: Call<List<QRScanResponse>>, response: Response<List<QRScanResponse>>) {
                        val result: List<QRScanResponse>? = response.body()

                        if (result != null && result.first() != null && result.first().symbol.first() != null) {
                            if (result.first().symbol.first().error == null) {
                                val bcDatabase: BCDatabase = BCDatabase.getMainThreadBCDatabase(this@CardScanActivity)
                                val cardDAO: CardDAO = bcDatabase.cardDao()
                                val cards: List<Card> = cardDAO.getAllCards()

                                val card: Card? = cards.find {
                                    it.id == result.first().symbol.first().data
                                }

                                if (card == null) {
                                    bcDatabase.close()

                                    Toast.makeText(this@CardScanActivity, getString(R.string.qr_result_invalid), Toast.LENGTH_LONG).show()
                                    findViewById<FloatingActionButton>(R.id.scanCardButton2).isClickable = true
                                } else {
                                    card.playerOwns = true
                                    cardDAO.updateCard(card)

                                    bcDatabase.close()

                                    val messageSplit = getString(R.string.card_successfully_added).split("%")

                                    Toast.makeText(this@CardScanActivity, messageSplit[0] + "" + card.getCardName(this@CardScanActivity) + "" + messageSplit[1], Toast.LENGTH_LONG).show()

                                    // ToDo: Switch activity
                                }
                            } else {
                                Toast.makeText(this@CardScanActivity, result.first().symbol.first().error, Toast.LENGTH_LONG).show()
                                findViewById<FloatingActionButton>(R.id.scanCardButton2).isClickable = true
                            }
                        } else {
                            Toast.makeText(this@CardScanActivity, getString(R.string.qr_scan_failed), Toast.LENGTH_LONG).show()
                            findViewById<FloatingActionButton>(R.id.scanCardButton2).isClickable = true
                        }
                    }

                    override fun onFailure(call: Call<List<QRScanResponse>>, t: Throwable) {
                        // ToDo: Proper error handling
                        Toast.makeText(this@CardScanActivity, getString(R.string.qr_scan_failed), Toast.LENGTH_LONG).show()
                        findViewById<FloatingActionButton>(R.id.scanCardButton2).isClickable = true
                    }
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        openBackgroundThread()
        if (textureView.isAvailable) {
            setUpCamera()
            openCamera()
        } else {
            textureView.surfaceTextureListener = this.surfaceTextureListener
        }
    }

    override fun onStop() {
        super.onStop()
        this.closeCamera()
        this.closeBackgroundThread()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setClickableAndOnTouchForAllViews(newValue: Boolean) {
        // Get all the objects and set isClickable to newValue
        findViewById<ImageView>(R.id.btnBack2).isClickable = newValue
        findViewById<FloatingActionButton>(R.id.scanCardButton2).isClickable = newValue
    }

    private fun createPreviewSession() {
        try {
            val surfaceTexture: SurfaceTexture? = textureView.surfaceTexture
            surfaceTexture!!.setDefaultBufferSize(previewSize.width, previewSize.height)
            val previewSurface: Surface = Surface(surfaceTexture)
            this.captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            this.captureRequestBuilder.addTarget(previewSurface)

            cameraDevice!!.createCaptureSession(Collections.singletonList(previewSurface), object: CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (cameraDevice == null) {
                            return
                        }

                        try {
                            this@CardScanActivity.captureRequest = captureRequestBuilder.build()
                            this@CardScanActivity.cameraCaptureSession = cameraCaptureSession
                            this@CardScanActivity.cameraCaptureSession!!.setRepeatingRequest(captureRequest, null, backgroundHandler)
                        } catch (e: CameraAccessException) {
                            // Do error handling
                        }
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) { }
                }, backgroundHandler)
        } catch (e: CameraAccessException) {
            // Do error handling
        }
    }

    private fun closeCamera() {
        if (this.cameraCaptureSession != null) {
            this.cameraCaptureSession!!.close()
            this.cameraCaptureSession = null
        }

        if (this.cameraDevice != null) {
            this.cameraDevice!!.close()
            this.cameraDevice = null
        }
    }

    private fun closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread!!.quitSafely()
            backgroundThread = null
            backgroundHandler = null
        }
    }

    private fun setUpCamera() {
        try {
            for (cameraId: String in cameraManager.cameraIdList) {
                val cameraCharacteristics: CameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraFacing) {
                    val streamConfigurationMap: StreamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!

                    cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let {
                        it.getOutputSizes(ImageFormat.JPEG)?.let {
                            val displayMetrics = DisplayMetrics()

                            windowManager.defaultDisplay.getMetrics(displayMetrics)

                            this.previewSize = this.chooseOptimalSize(it, displayMetrics.widthPixels, displayMetrics.heightPixels)
                        }
                    }

                    this.cameraId = cameraId
                }
            }
        } catch (e: CameraAccessException) {
            // Do some error handling
        }
    }

    private fun openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, this.stateCallback, this.backgroundHandler)
            }
        } catch (e: CameraAccessException) {
            // Do some error handling
        }
    }

    private fun openBackgroundThread() {
        this.backgroundThread = HandlerThread("camera_background_thread")
        this.backgroundThread!!.start()
        this.backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun chooseOptimalSize(outputSizes: Array<Size>, width: Int, height: Int): Size {
        val preferredRatio: Double = height / width.toDouble()
        var currentOptimalSize: Size = outputSizes[0]
        var currentOptimalRatio: Double = currentOptimalSize.width / currentOptimalSize.height.toDouble()
        for (currentSize: Size in outputSizes) {
            val currentRatio: Double = currentSize.width / currentSize.height.toDouble()
            if (abs(preferredRatio - currentRatio) <
                    abs(preferredRatio - currentOptimalRatio)) {
                currentOptimalSize = currentSize
                currentOptimalRatio = currentRatio
            }
        }
        return currentOptimalSize
    }
}