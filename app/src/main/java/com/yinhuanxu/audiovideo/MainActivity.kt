package com.yinhuanxu.audiovideo

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.widget.Button
import com.yinhuanxu.audiovideo.ext.logI
import com.yinhuanxu.audiovideo.ext.verifyStoragePermissions
import com.yinhuanxu.audiovideo.viewmodel.MainViewModel
import java.io.File
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        const val TAG = "AVStudy"

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    lateinit var button: Button

    lateinit var surfaceView: SurfaceView
    lateinit var textureView: TextureView
    lateinit var camera: Camera

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return modelClass.newInstance()
            }
        })[MainViewModel::class.java]

        verifyStoragePermissions()

        initView()
    }

    private fun initView() {
        button = findViewById(R.id.button)
        button.setOnClickListener {
            onButtonClick()
        }

        initSurfaceView()

        initTextureView()

//        camera = Camera.open()
//        camera.setDisplayOrientation(90)

    }

    private fun initTextureView() {
        textureView = findViewById(R.id.texture)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                //camera.release()
                return false
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                //startTexturePreview(surface)
            }

        }

    }

    private fun startTexturePreview(surface: SurfaceTexture?) {
        camera.setPreviewTexture(surface)
        camera.startPreview()
    }

    private fun initSurfaceView() {
        surfaceView = findViewById(R.id.surface)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                //camera.release()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                drawBitmap(holder)

                //startSurfacePreview(holder)
            }

        })
    }

    private fun startSurfacePreview(holder: SurfaceHolder?) {
        camera.setPreviewDisplay(holder)
        camera.startPreview()
    }

    private fun drawBitmap(holder: SurfaceHolder?) {
        holder?.apply {
            logI("surfaceCreated")
            val picPath = Environment.getExternalStorageDirectory().path + File.separator + "daxue.jpg"
            val bitmap = BitmapFactory.decodeFile(picPath)
            val paint = Paint()
            paint.isAntiAlias = true
            paint.style = Paint.Style.STROKE

            val canvas = lockCanvas()
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            unlockCanvasAndPost(canvas)
            logI("unlockCanvasAndPost")

        }
    }


    private fun onButtonClick() {
        logI("onButtonClick ${stringFromJNI()}")
//        Thread(Runnable {
//            try {
//                viewModel.deleteAudioTrack(this)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }).start()
    }

}
