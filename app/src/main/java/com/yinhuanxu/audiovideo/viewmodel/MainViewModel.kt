package com.yinhuanxu.audiovideo.viewmodel

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.os.SystemClock
import com.yinhuanxu.audiovideo.ext.logI
import java.io.File
import java.nio.ByteBuffer

class MainViewModel : ViewModel() {

    private lateinit var mediaExtractor: MediaExtractor
    private lateinit var mediaMuxer: MediaMuxer

    fun deleteAudioTrack(context: Context) {
        logI("deleteAudioTrack start")
        val start = SystemClock.elapsedRealtime()

        val inputPath = Environment.getExternalStorageDirectory().path + File.separator + "input.mp4"

        val outputPath = context.externalCacheDir.absolutePath + File.separator + "output.mp4"
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

        logI("deleteAudioTrack end , total time : $totalTime")

    }

}