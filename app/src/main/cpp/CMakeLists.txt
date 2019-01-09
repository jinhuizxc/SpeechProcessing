# cmake 最低版本要求
cmake_minimum_required(VERSION 3.4.1)

# 声明我们的模块名 audio_processor, 打包目标， so库SHARED
# 以及源文件列表
add_library(audio-processor SHARED
           smbPitchShift.c)
# so库需要包含的模块
#target_link_libraries(audio-processor
#           android
#           log)

target_link_libraries( # Specifies the target library.
                       # 制定目标库.
                       audio-processor

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )