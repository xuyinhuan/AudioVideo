package com.yinhuanxu.audiovideo

import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import android.view.TextureView
import com.yinhuanxu.audiovideo.kotlinext.log
import java.io.File

import kotlinx.android.synthetic.main.activity_androidmedia.*
import java.nio.ByteBuffer

class AndroidMediaActivity : AppCompatActivity() {

    lateinit var camera: Camera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_androidmedia)
        initView()
    }

    private fun initView() {

        initSurfaceView()

        initTextureView()

//        camera = Camera.open()
//        camera.setDisplayOrientation(90)

    }

    private fun initTextureView() {
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
            log("surfaceCreated")
            val picPath = Environment.getExternalStorageDirectory().path + File.separator + "daxue.jpg"
            val bitmap = BitmapFactory.decodeFile(picPath)
            val paint = Paint()
            paint.isAntiAlias = true
            paint.style = Paint.Style.STROKE

            val canvas = lockCanvas()
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            unlockCanvasAndPost(canvas)
            log("unlockCanvasAndPost")

        }
    }

    private lateinit var mediaExtractor: MediaExtractor
    private lateinit var mediaMuxer: MediaMuxer

    /**
     * 删除音轨
     */
    fun deleteAudioTrack() {
        log("deleteAudioTrack start")
        val start = SystemClock.elapsedRealtime()

        val inputPath = Environment.getExternalStorageDirectory().path + File.separator + "input.mp4"

        val outputPath = externalCacheDir.absolutePath + File.separator + "output.mp4"
        mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(inputPath)

        var videoTrackIndex = -1
        var framerate = 0

        for (i in 0 until mediaExtractor.trackCount) {
            val mediaFormat = mediaExtractor.getTrackFormat(i)

            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (!mime.startsWith("video/")) {
                continue
            }

            framerate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE)

            mediaExtractor.selectTrack(i)

            mediaMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            videoTrackIndex = mediaMuxer.addTrack(mediaFormat)
            mediaMuxer.start()
        }

        val bufferInfo = MediaCodec.BufferInfo()
        bufferInfo.presentationTimeUs = 0
        val byteBuffer = ByteBuffer.allocate(500 * 1024)
        while (true) {
            val sampleSize = mediaExtractor.readSampleData(byteBuffer, 0)
            if (sampleSize < 0) {
                break
            }

            mediaExtractor.advance()
            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME
            bufferInfo.presentationTimeUs += 1000 * 1000 / framerate
            mediaMuxer.writeSampleData(videoTrackIndex, byteBuffer, bufferInfo)

        }

        mediaExtractor.release()
        mediaMuxer.stop()
        mediaMuxer.release()

        val totalTime = SystemClock.elapsedRealtime() - start

        log("deleteAudioTrack end , total time : $totalTime")

    }
}