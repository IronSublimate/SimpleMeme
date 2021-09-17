project(paddleocrncnn)

cmake_minimum_required(VERSION 3.4.1)

set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv-mobile-3.4.15-android/sdk/native/jni)
# set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv-mobile-4.5.3-android/sdk/native/jni)
find_package(OpenCV REQUIRED core imgproc)

set(ncnn_DIR ${CMAKE_SOURCE_DIR}/ncnn-android-vulkan-lib/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

add_library(paddleocrncnn SHARED paddleocr_ncnn.cpp common.cpp clipper.cpp)

target_link_libraries(paddleocrncnn ncnn ${OpenCV_LIBS} jnigraphics)
