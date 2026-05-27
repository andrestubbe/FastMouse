/**
 * @file fastmouse.h
 * @brief FastMouse JNI Header - Raw Input Mouse API for Java
 *
 * @details Provides ultra-low latency mouse input using Win32 Raw Input API (WM_INPUT).
 * Bypasses Windows mouse ballistics to get exact sensor deltas.
 * Supports multi-device tracking by device handle.
 *
 * @par Architecture
 * - Singleton MouseManager for global state
 * - Message-only window for WM_INPUT messages
 * - Background thread for message loop
 * - JNI thread attachment for callbacks
 *
 * @par Platform Requirements
 * - Windows XP or later (Raw Input API)
 * - No admin privileges required
 *
 * @author FastJava Team
 * @version 1.0.0
 * @copyright MIT License
 */

#ifndef FASTMOUSE_H
#define FASTMOUSE_H

#include <jni.h>
#include <windows.h>
#include <map>
#include <string>
#include <thread>
#include <atomic>
#include <vector>
#include <mutex>

/** @defgroup JNI JNI Functions
 *  @brief Java Native Interface exports
 *  @{ */

/**
 * @brief Initialize the Raw Input listener
 * @param env JNI environment
 * @param obj FastMouseImpl instance
 * @return Native handle for the listener
 */
JNIEXPORT jlong JNICALL Java_fastmouse_FastMouseImpl_nativeInitialize(JNIEnv* env, jobject obj);

/**
 * @brief Start listening for mouse events
 * @param env JNI environment
 * @param obj FastMouseImpl instance
 * @param handle Native handle from initialize
 */
JNIEXPORT void JNICALL Java_fastmouse_FastMouseImpl_nativeStartListening(JNIEnv* env, jobject obj, jlong handle);

/**
 * @brief Stop listening and cleanup
 * @param env JNI environment
 * @param obj FastMouseImpl instance
 * @param handle Native handle from initialize
 */
JNIEXPORT void JNICALL Java_fastmouse_FastMouseImpl_nativeStopListening(JNIEnv* env, jobject obj, jlong handle);

/**
 * @brief Get connected mouse devices
 * @param env JNI environment
 * @param obj FastMouseImpl instance
 * @return Array of device handles
 */
JNIEXPORT jlongArray JNICALL Java_fastmouse_FastMouseImpl_nativeGetConnectedDevices(JNIEnv* env, jobject obj);

/**
 * @brief Get device name for a handle
 * @param env JNI environment
 * @param obj FastMouseImpl instance
 * @param handle The device handle
 * @return Device name
 */
JNIEXPORT jstring JNICALL Java_fastmouse_FastMouseImpl_nativeGetDeviceName(JNIEnv* env, jobject obj, jlong handle);

/**
 * @brief Get button count for a device
 * @param env JNI environment
 * @param obj FastMouseImpl instance
 * @param handle The device handle
 * @return Number of buttons
 */
JNIEXPORT jint JNICALL Java_fastmouse_FastMouseImpl_nativeGetDeviceButtonCount(JNIEnv* env, jobject obj, jlong handle);

/**
 * @brief Get absolute mouse coordinates
 * @param env JNI environment
 * @param obj FastMouseImpl instance
 * @return Array of [x, y] screen pixel coordinates
 */
JNIEXPORT jintArray JNICALL Java_fastmouse_FastMouseImpl_nativeGetCursorPosition(JNIEnv* env, jobject obj);

/** @} */

#endif // FASTMOUSE_H
