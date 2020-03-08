#include <jni.h>
#include <string>
#include <stdio.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavfilter/avfilter.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
}

#ifdef ANDROID

#include <android/log.h>
#include <bitset>
#include <unistd.h>

#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "NativeLib", format, ##__VA_ARGS__)
#else
#define LOGE(format, ...)  printf("(>_<) " format "\n", ##__VA_ARGS__)
#endif

struct URLProtocol;

extern "C" JNIEXPORT jstring JNICALL
Java_com_yinhuanxu_audiovideo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_yinhuanxu_audiovideo_ffmpeg_FFmpegInfoActivity_getProtocolInfo(JNIEnv *env, jobject thiz) {
    char info[40000] = {0};
    av_register_all();

    struct URLProtocol *pup = NULL;

    struct URLProtocol **p_temp = &pup;

    avio_enum_protocols((void **) p_temp, 0);
    while ((*p_temp) != NULL) {
        sprintf(info, "%s[In ][%10s]\n", info, avio_enum_protocols((void **) p_temp, 0));
    }
    pup = NULL;
    //Output
    avio_enum_protocols((void **) p_temp, 1);
    while ((*p_temp) != NULL) {
        sprintf(info, "%s[Out][%10s]\n", info, avio_enum_protocols((void **) p_temp, 1));
    }
    return env->NewStringUTF(info);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_yinhuanxu_audiovideo_ffmpeg_FFmpegInfoActivity_getAVFormatInfo(JNIEnv *env, jobject thiz) {
    char info[40000] = {0};

    av_register_all();

    AVInputFormat *if_temp = av_iformat_next(NULL);
    AVOutputFormat *of_temp = av_oformat_next(NULL);
    //Input
    while (if_temp != NULL) {
        sprintf(info, "%s[In ][%10s]\n", info, if_temp->name);
        if_temp = if_temp->next;
    }
    //Output
    while (of_temp != NULL) {
        sprintf(info, "%s[Out][%10s]\n", info, of_temp->name);
        of_temp = of_temp->next;
    }

    return env->NewStringUTF(info);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_yinhuanxu_audiovideo_ffmpeg_FFmpegInfoActivity_getAVCodecInfo(JNIEnv *env, jobject thiz) {
    char info[40000] = {0};

    av_register_all();

    AVCodec *c_temp = av_codec_next(NULL);

    while (c_temp != NULL) {
        if (c_temp->decode != NULL) {
            sprintf(info, "%s[Dec]", info);
        } else {
            sprintf(info, "%s[Enc]", info);
        }
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                sprintf(info, "%s[Video]", info);
                break;
            case AVMEDIA_TYPE_AUDIO:
                sprintf(info, "%s[Audio]", info);
                break;
            default:
                sprintf(info, "%s[Other]", info);
                break;
        }
        sprintf(info, "%s[%10s]\n", info, c_temp->name);


        c_temp = c_temp->next;
    }

    return env->NewStringUTF(info);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_yinhuanxu_audiovideo_ffmpeg_FFmpegInfoActivity_getAVFilterInfo(JNIEnv *env, jobject thiz) {
    char info[40000] = {0};
    avfilter_register_all();
    AVFilter *f_temp = (AVFilter *) avfilter_next(NULL);
    while (f_temp != NULL) {
        sprintf(info, "%s[%10s]\n", info, f_temp->name);
    }
    return env->NewStringUTF(info);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_yinhuanxu_audiovideo_ffmpeg_FFmpegInfoActivity_getConfigurationInfo(JNIEnv *env,
                                                                             jobject thiz) {
    char info[10000] = {0};
    av_register_all();

    sprintf(info, "%s\n", avcodec_configuration());
    return env->NewStringUTF(info);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_yinhuanxu_audiovideo_ffmpeg_FFmpegPlayActivity_playLocalVideo(JNIEnv *env, jobject thiz,
                                                                       jstring videoPath,
                                                                       jobject surface) {
    const char *input = env->GetStringUTFChars(videoPath, NULL);
    if (input == NULL) {
        LOGE("字符串转换失败......");
        return;
    }
    //注册FFmpeg所有编解码器，以及相关协议。
    av_register_all();
    //分配结构体
    AVFormatContext *formatContext = avformat_alloc_context();
    //打开视频数据源。由于Android 对SDK存储权限的原因，如果没有为当前项目赋予SDK存储权限，打开本地视频文件时会失败
    int open_state = avformat_open_input(&formatContext, input, NULL, NULL);
    if (open_state < 0) {
        char errbuf[128];
        if (av_strerror(open_state, errbuf, sizeof(errbuf)) == 0) {
            LOGE("打开视频输入流信息失败，失败原因： %s", errbuf);
        }
        return;
    }
    //为分配的AVFormatContext 结构体中填充数据
    if (avformat_find_stream_info(formatContext, NULL) < 0) {
        LOGE("读取输入的视频流信息失败。");
        return;
    }
    int video_stream_index = -1;//记录视频流所在数组下标
    LOGE("当前视频数据，包含的数据流数量：%d", formatContext->nb_streams);
    //找到"视频流".AVFormatContext 结构体中的nb_streams字段存储的就是当前视频文件中所包含的总数据流数量——
    //视频流，音频流，字幕流
    for (int i = 0; i < formatContext->nb_streams; i++) {

        //如果是数据流的编码格式为AVMEDIA_TYPE_VIDEO——视频流。
        if (formatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_stream_index = i;//记录视频流下标
            break;
        }
    }
    if (video_stream_index == -1) {
        LOGE("没有找到 视频流。");
        return;
    }
    //通过编解码器的id——codec_id 获取对应（视频）流解码器
    AVCodecParameters *codecParameters = formatContext->streams[video_stream_index]->codecpar;
    AVCodec *videoDecoder = avcodec_find_decoder(codecParameters->codec_id);

    if (videoDecoder == NULL) {
        LOGE("未找到对应的流解码器。");
        return;
    }
    //通过解码器分配(并用  默认值   初始化)一个解码器context
    AVCodecContext *codecContext = avcodec_alloc_context3(videoDecoder);

    if (codecContext == NULL) {
        LOGE("分配 解码器上下文失败。");
        return;
    }
    //更具指定的编码器值填充编码器上下文
    if (avcodec_parameters_to_context(codecContext, codecParameters) < 0) {
        LOGE("填充编解码器上下文失败。");
        return;
    }
    //通过所给的编解码器初始化编解码器上下文
    if (avcodec_open2(codecContext, videoDecoder, NULL) < 0) {
        LOGE("初始化 解码器上下文失败。");
        return;
    }
    AVPixelFormat dstFormat = AV_PIX_FMT_RGBA;
    //分配存储压缩数据的结构体对象AVPacket
    //如果是视频流，AVPacket会包含一帧的压缩数据。
    //但如果是音频则可能会包含多帧的压缩数据
    AVPacket *packet = av_packet_alloc();
    //分配解码后的每一数据信息的结构体（指针）
    AVFrame *frame = av_frame_alloc();
    //分配最终显示出来的目标帧信息的结构体（指针）
    AVFrame *outFrame = av_frame_alloc();
    uint8_t *out_buffer = (uint8_t *) av_malloc(
            (size_t) av_image_get_buffer_size(dstFormat, codecContext->width, codecContext->height,
                                              1));
    //更具指定的数据初始化/填充缓冲区
    av_image_fill_arrays(outFrame->data, outFrame->linesize, out_buffer, dstFormat,
                         codecContext->width, codecContext->height, 1);
    //初始化SwsContext
    SwsContext *swsContext = sws_getContext(
            codecContext->width   //原图片的宽
            , codecContext->height  //源图高
            , codecContext->pix_fmt //源图片format
            , codecContext->width  //目标图的宽
            , codecContext->height  //目标图的高
            , dstFormat, SWS_BICUBIC, NULL, NULL, NULL
    );
    if (swsContext == NULL) {
        LOGE("swsContext==NULL");
        return;
    }
    //Android 原生绘制工具
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
    //定义绘图缓冲区
    ANativeWindow_Buffer outBuffer;
    //通过设置宽高限制缓冲区中的像素数量，而非屏幕的物流显示尺寸。
    //如果缓冲区与物理屏幕的显示尺寸不相符，则实际显示可能会是拉伸，或者被压缩的图像
    ANativeWindow_setBuffersGeometry(nativeWindow, codecContext->width, codecContext->height,
                                     WINDOW_FORMAT_RGBA_8888);
    //循环读取数据流的下一帧
    while (av_read_frame(formatContext, packet) == 0) {

        if (packet->stream_index == video_stream_index) {
            //讲原始数据发送到解码器
            int sendPacketState = avcodec_send_packet(codecContext, packet);
            if (sendPacketState == 0) {
                int receiveFrameState = avcodec_receive_frame(codecContext, frame);
                if (receiveFrameState == 0) {
                    //锁定窗口绘图界面
                    ANativeWindow_lock(nativeWindow, &outBuffer, NULL);
                    //对输出图像进行色彩，分辨率缩放，滤波处理
                    sws_scale(swsContext, (const uint8_t *const *) frame->data, frame->linesize, 0,
                              frame->height, outFrame->data, outFrame->linesize);
                    uint8_t *dst = (uint8_t *) outBuffer.bits;
                    //解码后的像素数据首地址
                    //这里由于使用的是RGBA格式，所以解码图像数据只保存在data[0]中。但如果是YUV就会有data[0]
                    //data[1],data[2]
                    uint8_t *src = outFrame->data[0];
                    //获取一行字节数
                    int oneLineByte = outBuffer.stride * 4;
                    //复制一行内存的实际数量
                    int srcStride = outFrame->linesize[0];
                    for (int i = 0; i < codecContext->height; i++) {
                        memcpy(dst + i * oneLineByte, src + i * srcStride, srcStride);
                    }
                    //解锁
                    ANativeWindow_unlockAndPost(nativeWindow);
                    //进行短暂休眠。如果休眠时间太长会导致播放的每帧画面有延迟感，如果短会有加速播放的感觉。
                    //一般一每秒60帧——16毫秒一帧的时间进行休眠
                    usleep(1000 * 20);//20毫秒

                } else if (receiveFrameState == AVERROR(EAGAIN)) {
                    LOGE("从解码器-接收-数据失败：AVERROR(EAGAIN)");
                } else if (receiveFrameState == AVERROR_EOF) {
                    LOGE("从解码器-接收-数据失败：AVERROR_EOF");
                } else if (receiveFrameState == AVERROR(EINVAL)) {
                    LOGE("从解码器-接收-数据失败：AVERROR(EINVAL)");
                } else {
                    LOGE("从解码器-接收-数据失败：未知");
                }
            } else if (sendPacketState == AVERROR(EAGAIN)) {//发送数据被拒绝，必须尝试先读取数据
                LOGE("向解码器-发送-数据包失败：AVERROR(EAGAIN)");//解码器已经刷新数据但是没有新的数据包能发送给解码器
            } else if (sendPacketState == AVERROR_EOF) {
                LOGE("向解码器-发送-数据失败：AVERROR_EOF");
            } else if (sendPacketState == AVERROR(EINVAL)) {//遍解码器没有打开，或者当前是编码器，也或者需要刷新数据
                LOGE("向解码器-发送-数据失败：AVERROR(EINVAL)");
            } else if (sendPacketState == AVERROR(ENOMEM)) {//数据包无法压如解码器队列，也可能是解码器解码错误
                LOGE("向解码器-发送-数据失败：AVERROR(ENOMEM)");
            } else {
                LOGE("向解码器-发送-数据失败：未知");
            }
        }
        av_packet_unref(packet);
    }
    //内存释放
    ANativeWindow_release(nativeWindow);
    av_frame_free(&outFrame);
    av_frame_free(&frame);
    av_packet_free(&packet);
    avcodec_free_context(&codecContext);
    avformat_close_input(&formatContext);
    avformat_free_context(formatContext);
    env->ReleaseStringUTFChars(videoPath, input);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_yinhuanxu_audiovideo_ffmpeg_FFmpegPlayActivity_playRemoteVideo(JNIEnv *env,
                                                                        jobject instance,
                                                                        jstring url_,
                                                                        jobject surface) {
    const char *url = env->GetStringUTFChars(url_, 0);
    /**
     * 注册
     * 在4.0之后已经过期，可以直接忽略调用这个函数
     */
//    av_register_all();

    // 打开地址并获取里面的内容
    // avFormatContext是内容的一个上下文
    AVFormatContext *avFormatContext = avformat_alloc_context();
    avformat_open_input(&avFormatContext, url, NULL, NULL);
    avformat_find_stream_info(avFormatContext, NULL);

    // 找出视频流
    int video_index = -1;
    for (int i = 0; i < avFormatContext->nb_streams; ++i) {
        if (avFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_index = i;
        }
    }

    /**
     * 下面将进行解码、转换、绘制
     */
    // 获取解码器上下文
    AVCodecContext *avCodecContext = avFormatContext->streams[video_index]->codec;
    // 获取解码器
    AVCodec *avCodec = avcodec_find_decoder(avCodecContext->codec_id);
    // 打开解码器
    if (avcodec_open2(avCodecContext, avCodec, NULL) < 0) { //打开失败直接返回
        return;
    }
    /**
     * 申请AVPacket和AVFrame
     * AVPacket的作用是保存解码之前的数据和一些附加信息，例如显示时间戳(pts)、解码时间戳(dts)、数据时长和所在媒体流的索引等
     * AVFrame的作用是存放解码过后的数据
     */
    AVPacket *avPacket = static_cast<AVPacket *>(av_malloc(sizeof(AVPacket)));
    av_init_packet(avPacket);
    /**
     * 分配一个AVFrame结构体
     * AVFrame结构体一般用于存储原始数,指向解码后的原始帧
     */
    AVFrame *avFrame = av_frame_alloc();
    /**
     * 分配一个AVFrame结构体，指向存放转换成rgb后的帧
     */
    AVFrame *rgb_frame = av_frame_alloc();
    /**
     * rgb_frame是一个缓存区域，需要设置
     * 缓存区
     */
    uint8_t *out_buffer = static_cast<uint8_t *>(av_malloc(
            avpicture_get_size(AV_PIX_FMT_RGBA, avCodecContext->width, avCodecContext->height)));
    /**
     * 与缓存区关联
     * 设置rgb_frame缓存区
     */
    avpicture_fill((AVPicture *) rgb_frame, out_buffer, AV_PIX_FMT_RGBA, avCodecContext->width,
                   avCodecContext->height);
    /**
     * 需要一个ANativeWindow来进行原生绘制
     */
    ANativeWindow *pANativeWindow = ANativeWindow_fromSurface(env, surface);
    if (pANativeWindow == 0) { // 获取native window失败直接返回
        return;
    }

    SwsContext *swsContext = sws_getContext(avCodecContext->width, avCodecContext->height,
                                            avCodecContext->pix_fmt, avCodecContext->width,
                                            avCodecContext->height, AV_PIX_FMT_RGBA, SWS_BICUBIC,
                                            NULL, NULL, NULL);
    // 视频缓冲区
    ANativeWindow_Buffer nativeWindow_outBuffer;

    /**
     * 开始解码
     */
    int frameCount;
    while (av_read_frame(avFormatContext, avPacket) >= 0) {
        if (avPacket->stream_index == video_index) {
            avcodec_decode_video2(avCodecContext, avFrame, &frameCount, avPacket);
            if (frameCount) {
                ANativeWindow_setBuffersGeometry(pANativeWindow, avCodecContext->width,
                                                 avCodecContext->height, WINDOW_FORMAT_RGBA_8888);
                /**
                 * 上锁
                 */
                ANativeWindow_lock(pANativeWindow, &nativeWindow_outBuffer, NULL);
                /**
                 * 转换成RGB格式
                 */
                sws_scale(swsContext, (const uint8_t *const *) avFrame->data, avFrame->linesize, 0,
                          avFrame->height, rgb_frame->data, rgb_frame->linesize);
                uint8_t *dst = static_cast<uint8_t *>(nativeWindow_outBuffer.bits);
                int destStride = nativeWindow_outBuffer.stride * 4;
                uint8_t *src = rgb_frame->data[0];
                int srcStride = rgb_frame->linesize[0];
                for (int i = 0; i < avCodecContext->height; i++) {
                    memcpy(dst + i * destStride, src + i * srcStride, srcStride);
                }
                ANativeWindow_unlockAndPost(pANativeWindow);
            }
        }
        av_free_packet(avPacket);
    }

    ANativeWindow_release(pANativeWindow);
    av_frame_free(&avFrame);
    av_frame_free(&rgb_frame);
    avcodec_close(avCodecContext);
    avformat_free_context(avFormatContext);

    env->ReleaseStringUTFChars(url_, url);
}