# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

add_library( native-lib
             SHARED
             src/main/cpp/native-lib.cpp
             src/main/cpp/common.cpp
             src/main/cpp/CMT.cpp
             src/main/cpp/Consensus.cpp
             src/main/cpp/Fusion.cpp
             src/main/cpp/Matcher.cpp
             src/main/cpp/Tracker.cpp
             src/main/cpp/fastcluster/fastcluster.cpp)

find_library( log-lib log )

include_directories(src/main/cpp
                    src/main/cpp/include
                    src/main/cpp/include/opencv
                    src/main/cpp/include/opencv2)

add_library(opencv SHARED IMPORTED)

set_target_properties(opencv
                      PROPERTIES IMPORTED_LOCATION
                      ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

target_link_libraries(native-lib opencv ${log-lib} )