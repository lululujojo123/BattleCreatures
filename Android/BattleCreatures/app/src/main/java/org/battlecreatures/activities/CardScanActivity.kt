/*
 * Copyright (c) 2021 lululujojo123
 *
 * CardScanActivity.kt
 *
 * created by: Andreas G.
 * last edit \ by: 2021/02/03 \ Andreas G.
 */

package org.battlecreatures.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Size
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MediaType
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
import kotlin.math.abs

/**
 * CardScanActivity providing the camera and scan functionality for adding new cards to
 * the card deck
 */
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
     * The unique camera id of the camera to be used
     */
    private var cameraId: String = ""

    /**
     * The background handler for the camera related background tasks
     */
    private var backgroundHandler: Handler? = null

    /**
     * The state callback with the camera devices current state
     */
    private lateinit var stateCallback: CameraDevice.StateCallback

    /**
     * The camera device object containing the currently used camera
     */
    private var cameraDevice: CameraDevice? = null

    /**
     * The textureView to be used for the preview rendering
     */
    private lateinit var textureView: TextureView

    /**
     * The surface texture listener listening for the texture view surface texture state
     */
    private lateinit var surfaceTextureListener: TextureView.SurfaceTextureListener

    /**
     * The background thread used to run the background handler
     */
    private var backgroundThread: HandlerThread? = null

    /**
     * The builder object required for building a picture capture request
     */
    private lateinit var captureRequestBuilder: CaptureRequest.Builder

    /**
     * The actually created capture request
     */
    private lateinit var captureRequest: CaptureRequest

    /**
     * The current capture session used to capture a picture from camera
     */
    private var cameraCaptureSession: CameraCaptureSession? = null

    /**
     * The size object storing the optimal preview size. Especially for Huawei devices
     * it is recommended to calculate the optimal preview size.
     */
    private lateinit var previewSize: Size

    /**
     * The android related onCreate method
     *
     * @param savedInstanceState The saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Super classes onCreate method
        super.onCreate(savedInstanceState)

        // Setting the layout resource to be used for the current activity
        setContentView(R.layout.activity_card_scan)

        // Store the texture view object
        this.textureView = findViewById(R.id.textureView)

        // Gather the camera system service
        this.cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // Create the surface texture listener
        this.surfaceTextureListener = object: TextureView.SurfaceTextureListener {
            /**
             * The SurfaceTextureListener related onSurfaceTextureAvailable method
             *
             * @param surfaceTexture The surface texture object of this listener
             * @param width The width of this surface texture
             * @param height The height of this surface texture
             */
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                // Setting up the camera
                setUpCamera()

                // Opening the currently created camera
                openCamera()
            }

            /**
             * The SurfaceTextureListener related onSurfaceTextureSizeChanged method
             *
             * @param surfaceTexture The surface texture object of this listener
             * @param width The width of this surface texture
             * @param height The height of this surface texture
             */
            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) { }

            /**
             * The SurfaceTextureListener related onSurfaceTextureDestroyed method
             *
             * @param surfaceTexture The surface texture object of this listener
             * @return The result as an boolean
             */
            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                return false
            }

            /**
             * The SurfaceTextureListener related onSurfaceTextureUpdated method
             *
             * @param surfaceTexture The surface texture object of this listener
             */
            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) { }
        }

        // Create a camera device's state callback instance
        this.stateCallback = object: CameraDevice.StateCallback() {
            /**
             * StateCallback related onOpened method
             *
             * @param cameraDevice The camera device related to the current callback
             */
            override fun onOpened(cameraDevice: CameraDevice) {
                // Storing the cameraDevice in the current objects property
                this@CardScanActivity.cameraDevice = cameraDevice

                // Creating the camera preview session
                createPreviewSession()
            }

            /**
             * StateCallback related onDisconnected method
             *
             * @param cameraDevice The camera device related to the current callback
             */
            override fun onDisconnected(cameraDevice: CameraDevice) {
                // Closing the current camera device
                cameraDevice.close()

                // Deleting the reference to current camera device in current object
                this@CardScanActivity.cameraDevice = null
            }

            /**
             * StateCallback related onError method
             *
             * @param cameraDevice The camera device related to the current callback
             * @param error The error code describing the current error
             */
            override fun onError(cameraDevice: CameraDevice, error: Int) {
                // Closing the current camera device
                cameraDevice.close()

                // Deleting the current reference to the camera device from the current object
                this@CardScanActivity.cameraDevice = null
            }
        }

        // Set the onClickListener for the back button
        findViewById<ImageView>(R.id.btnBack2).setOnClickListener {
            // Only execute the code if the button is still clickable
            if (it.isClickable) {
                // Set all clickable views to not clickable
                this.setClickableAndOnTouchForAllViews(false)

                // Run the onBackPressed routine to remove the activity from back stack
                this.onBackPressed()
            }
        }

        // Set the scan buttons onClickListener
        findViewById<FloatingActionButton>(R.id.scanCardButton2).setOnClickListener {
            // Only execute the code if button is still clickable
            if (it.isClickable) {
                // Prevent the scan button from being clicked again
                findViewById<FloatingActionButton>(R.id.scanCardButton2).isClickable = false

                // Creating a byte array output stream for streaming the image data to a blob
                val byteArrayOutputStream = ByteArrayOutputStream()

                // Encoding the textureViews current frame to an jpeg image and writing the bytes
                // to the byteArrayOutputStream's processing buffer
                textureView.bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

                // Storing the byte array buffered in the outputStream into the blob
                val imageBlob: ByteArray = byteArrayOutputStream.toByteArray()

                // Closing the outputStream
                byteArrayOutputStream.close()

                // Creating the request body for the api request by processing the image blob
                val image: RequestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageBlob)

                // Creating a gson object for the JSON tasks
                val gson: Gson = GsonBuilder()
                        .setLenient()
                        .create()

                // Creating a retrofit object for the api request execution
                val retrofit: Retrofit = Retrofit.Builder()
                        .baseUrl("https://api.qrserver.com/")
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()

                // Creating a QRCodeAPi object for the QR api request
                val qrCodeAPI: QRCodeAPI = retrofit.create(QRCodeAPI::class.java)

                // Creating a call with a list of QRScanResponse objects
                val call: Call<List<QRScanResponse>> = qrCodeAPI.getQRCodeResult(image)

                // Enqueuing the call for api request execution
                call.enqueue(object: Callback<List<QRScanResponse>> {
                    /**
                     * Callback related onResponse method
                     *
                     * @param call The call used for the api request
                     * @param response The response provided by the api
                     */
                    override fun onResponse(call: Call<List<QRScanResponse>>, response: Response<List<QRScanResponse>>) {
                        // Store the reference to the QRScanResponses for further usage
                        val result: List<QRScanResponse>? = response.body()

                        // Check the important result data. They shouldn't be empty
                        if (result != null && result.first() != null && result.first().symbol.first() != null) {
                            // Check if a error was thrown by the api
                            if (result.first().symbol.first().error == null) {
                                // Create all the database related objects
                                val bcDatabase: BCDatabase = BCDatabase.getMainThreadBCDatabase(this@CardScanActivity)
                                val cardDAO: CardDAO = bcDatabase.cardDao()
                                val cards: List<Card> = cardDAO.getAllCards()

                                // Search for the card with the same id as the QR-Codes content
                                val card: Card? = cards.find {
                                    it.id == result.first().symbol.first().data
                                }

                                // Check whether a card with that id was found or not
                                if (card == null) {
                                    // Close the database session again
                                    bcDatabase.close()

                                    // Inform user that the QR-Code doesn't refer to one of the available cards
                                    Toast.makeText(this@CardScanActivity, getString(R.string.qr_result_invalid), Toast.LENGTH_LONG).show()

                                    // Make the button clickable again
                                    findViewById<FloatingActionButton>(R.id.scanCardButton2).isClickable = true
                                } else {
                                    // Set the playerOwns value to true as the card was successfully scanned
                                    card.playerOwns = true
                                    cardDAO.updateCard(card)

                                    // Close the current database session
                                    bcDatabase.close()

                                    // Create the message array from string resource for further message construction
                                    val messageSplit = getString(R.string.card_successfully_added).split("%")

                                    // Inform the user that the card was successfully scanned
                                    Toast.makeText(this@CardScanActivity, messageSplit[0] + "" + card.getCardName(this@CardScanActivity) + "" + messageSplit[1], Toast.LENGTH_LONG).show()

                                    // Start the card detail activity to show the scanned card
                                    val intent: Intent = Intent(this@CardScanActivity, CardDetailActivity::class.java)
                                    intent.putExtra(getString(R.string.card_id_intent_extra), card.id)
                                    startActivity(intent)

                                    // Close this activity
                                    finish()
                                }
                            } else {
                                // ToDo: Mutilanguage error translation
                                // Inform the user about the api error
                                Toast.makeText(this@CardScanActivity, result.first().symbol.first().error, Toast.LENGTH_LONG).show()

                                // Make the button clickable again
                                findViewById<FloatingActionButton>(R.id.scanCardButton2).isClickable = true
                            }
                        } else {
                            // Inform the user about the api request error
                            Toast.makeText(this@CardScanActivity, getString(R.string.qr_scan_failed), Toast.LENGTH_LONG).show()

                            // Make the button clickable again
                            findViewById<FloatingActionButton>(R.id.scanCardButton2).isClickable = true
                        }
                    }

                    /**
                     * Callback related onResponse method
                     *
                     * @param call The call used for the api request
                     * @param t The throwable object created if an error appeared
                     */
                    override fun onFailure(call: Call<List<QRScanResponse>>, t: Throwable) {
                        // Inform the user that the request failed
                        Toast.makeText(this@CardScanActivity, getString(R.string.qr_scan_failed), Toast.LENGTH_LONG).show()

                        // Make the button clickable again
                        findViewById<FloatingActionButton>(R.id.scanCardButton2).isClickable = true
                    }
                })
            }
        }
    }

    /**
     * Android related onResume method
     */
    override fun onResume() {
        // The super classes onResume method
        super.onResume()

        // Opening the background thread for the handler
        openBackgroundThread()

        // Check whether the textureView is already available
        if (textureView.isAvailable) {
            // Set up the camera
            setUpCamera()

            // Open the created camera
            openCamera()
        } else {
            // Wait for the textureView to be ready by adding the listener
            textureView.surfaceTextureListener = this.surfaceTextureListener
        }
    }

    /**
     * Android related onStop method
     */
    override fun onStop() {
        // Super classes onStop method
        super.onStop()

        // Close the current activity's camera
        this.closeCamera()

        // Closing the handler's background thread
        this.closeBackgroundThread()
    }

    /**
     * Android related onDestroy method
     */
    override fun onDestroy() {
        // Super classes onDestroy method
        super.onDestroy()

        // ToDo: Do some cleanup for minimizing the memory usage
    }

    /**
     * Private method updating the clickable value for all the views
     *
     * @param newValue The new value for the clickable property
     */
    private fun setClickableAndOnTouchForAllViews(newValue: Boolean) {
        // Get all the objects and set isClickable to newValue
        findViewById<ImageView>(R.id.btnBack2).isClickable = newValue
        findViewById<FloatingActionButton>(R.id.scanCardButton2).isClickable = newValue
    }

    /**
     * Private method creating the camera preview session
     */
    private fun createPreviewSession() {
        // Handling the camera access error. Should not appear because of splash logic
        try {
            // Prepare all the required object
            val surfaceTexture: SurfaceTexture? = textureView.surfaceTexture
            surfaceTexture!!.setDefaultBufferSize(previewSize.width, previewSize.height)
            val previewSurface: Surface = Surface(surfaceTexture)
            this.captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            this.captureRequestBuilder.addTarget(previewSurface)

            // Creating a capture session for the current camera device. Deprecated method needs to be migrated.
            cameraDevice!!.createCaptureSession(Collections.singletonList(previewSurface), object: CameraCaptureSession.StateCallback() {
                /**
                 * CaptureSession related onConfigured method
                 *
                 * @param cameraCaptureSession The created camera capture session
                 */
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    // Check whether the camera device is not available
                    if (cameraDevice == null) {
                            return
                    }

                    // Be aware of camera access exception
                    try {
                        // Prepare the capture session and the capture request
                        this@CardScanActivity.captureRequest = captureRequestBuilder.build()
                        this@CardScanActivity.cameraCaptureSession = cameraCaptureSession
                        this@CardScanActivity.cameraCaptureSession!!.setRepeatingRequest(captureRequest, null, backgroundHandler)
                    } catch (e: CameraAccessException) {
                        // ToDo: Do proper error handling
                    }
                }

                /**
                 * CaptureSession related onConfigureFailed method
                 *
                 * @param cameraCaptureSession The created camera capture session
                 */
                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) { }
            }, backgroundHandler)
        } catch (e: CameraAccessException) {
            // ToDo: Do proper error handling
        }
    }

    /**
     * Private method closing the current camera session
     */
    private fun closeCamera() {
        // Only close and delete if capture session is available
        if (this.cameraCaptureSession != null) {
            this.cameraCaptureSession!!.close()
            this.cameraCaptureSession = null
        }

        // Only close and delete if camera device is available
        if (this.cameraDevice != null) {
            this.cameraDevice!!.close()
            this.cameraDevice = null
        }
    }

    /**
     * Private method closing the handler's background thread
     */
    private fun closeBackgroundThread() {
        // Only quit and delete if handler is available
        if (backgroundHandler != null) {
            backgroundThread!!.quitSafely()
            backgroundThread = null
            backgroundHandler = null
        }
    }

    /**
     * Private method preparing the camera
     */
    private fun setUpCamera() {
        // Being aware of the camera access exception
        try {
            // Iterate over all the camera devices attached to the smartphone
            for (cameraId: String in cameraManager.cameraIdList) {
                // Get the current cameras characteristics
                val cameraCharacteristics: CameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)

                // Check whether it is the camera we want to use
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraFacing) {
                    // Calculate the optimal size and store it
                    cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let {
                        it.getOutputSizes(ImageFormat.JPEG)?.let {
                            val displayMetrics = DisplayMetrics()

                            windowManager.defaultDisplay.getMetrics(displayMetrics)

                            this.previewSize = this.chooseOptimalSize(it, displayMetrics.widthPixels, displayMetrics.heightPixels)
                        }
                    }

                    // Store the camera id
                    this.cameraId = cameraId
                }
            }
        } catch (e: CameraAccessException) {
            // ToDo: Do some proper error handling
        }
    }

    /**
     * Private method opening the camera for usage
     */
    private fun openCamera() {
        // Be aware of the camera access exception
        try {
            // Open the camera if user permitted this app to use the camera
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, this.stateCallback, this.backgroundHandler)
            }
        } catch (e: CameraAccessException) {
            // ToDo: Do some proper error handling
        }
    }

    /**
     * Private method preparing and opening the thread with the background handler
     */
    private fun openBackgroundThread() {
        // Prepare the objects and start the handler thread
        this.backgroundThread = HandlerThread("camera_background_thread")
        this.backgroundThread!!.start()
        this.backgroundHandler = Handler(backgroundThread!!.looper)
    }

    /**
     * Private method calculating the optimal resolution and size for the camera preview
     *
     * @param outputSizes The array with all the output sizes to choose from
     * @param width The width of the destination view
     * @param height The height of the destination view
     * @return The optimal size to use
     */
    private fun chooseOptimalSize(outputSizes: Array<Size>, width: Int, height: Int): Size {
        // Preparing all the required values
        val preferredRatio: Double = height / width.toDouble()
        var currentOptimalSize: Size = outputSizes[0]
        var currentOptimalRatio: Double = currentOptimalSize.width / currentOptimalSize.height.toDouble()

        // Check all the sizes and store the best ones
        for (currentSize: Size in outputSizes) {
            val currentRatio: Double = currentSize.width / currentSize.height.toDouble()
            if (abs(preferredRatio - currentRatio) <
                    abs(preferredRatio - currentOptimalRatio)) {
                currentOptimalSize = currentSize
                currentOptimalRatio = currentRatio
            }
        }

        // Returning the optimal size
        return currentOptimalSize
    }
}