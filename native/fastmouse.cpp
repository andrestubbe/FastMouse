#define UNICODE
#define _UNICODE

#include "fastmouse.h"
#include <windows.h>
#include <stdio.h>
#include <functional>

/**
 * @file fastmouse.cpp
 * @brief Native JNI implementation for FastMouse using Win32 Raw Input API
 * @version 2.0 - Production Ready
 * 
 * Fixes:
 * - Single global context (simplified, no multi-context bugs)
 * - JNI Attach once per thread (not per event)
 * - Stable device ID hashing
 * - Proper cleanup
 */

// ============================================================================
// Data Structures
// ============================================================================

struct MouseContext {
    jlong handle;
    jobject javaObject;
    jmethodID onMouseMove;
    jmethodID onMouseButton;
    jmethodID onMouseWheel;
    HWND messageWindow;
    std::thread messageThread;
    std::atomic<bool> running{false};
    JavaVM* vm;
    JNIEnv* env;
    std::atomic<bool> threadAttached{false};
};

// Single global context (simplified architecture)
static MouseContext* g_ctx = nullptr;
static std::mutex g_mutex;

// Stable device ID cache
static std::map<HANDLE, jlong> g_stableDeviceIds;
static jlong g_nextDeviceId = 1;
static std::mutex g_deviceMutex;

// Window class name
static const wchar_t* WINDOW_CLASS_NAME = L"FastMouseMessageWindow_v2";

// ============================================================================
// Window Procedure for WM_INPUT
// ============================================================================

// Helper: Get or create stable device ID
static jlong GetStableDeviceId(HANDLE hDevice) {
    std::lock_guard<std::mutex> lock(g_deviceMutex);
    auto it = g_stableDeviceIds.find(hDevice);
    if (it != g_stableDeviceIds.end()) {
        return it->second;
    }
    jlong newId = g_nextDeviceId++;
    g_stableDeviceIds[hDevice] = newId;
    return newId;
}

LRESULT CALLBACK MouseWindowProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    if (msg == WM_INPUT) {
        HRAWINPUT hRawInput = (HRAWINPUT)lParam;
        
        UINT size;
        GetRawInputData(hRawInput, RID_INPUT, NULL, &size, sizeof(RAWINPUTHEADER));
        
        std::vector<BYTE> buffer(size);
        GetRawInputData(hRawInput, RID_INPUT, buffer.data(), &size, sizeof(RAWINPUTHEADER));
        
        RAWINPUT* raw = (RAWINPUT*)buffer.data();
        
        if (raw->header.dwType == RIM_TYPEMOUSE) {
            MouseContext* ctx = g_ctx;
            if (ctx && ctx->running && ctx->threadAttached) {
                JNIEnv* env = ctx->env;
                jlong stableDeviceId = GetStableDeviceId(raw->header.hDevice);
                
                if (raw->data.mouse.lLastX != 0 || raw->data.mouse.lLastY != 0) {
                    POINT pt = {0, 0};
                    GetCursorPos(&pt);
                    env->CallVoidMethod(ctx->javaObject, ctx->onMouseMove,
                        stableDeviceId,
                        (jint)raw->data.mouse.lLastX,
                        (jint)raw->data.mouse.lLastY,
                        (jint)pt.x,
                        (jint)pt.y);
                }
                
                USHORT buttonFlags = raw->data.mouse.usButtonFlags;
                if (buttonFlags & RI_MOUSE_LEFT_BUTTON_DOWN) {
                    env->CallVoidMethod(ctx->javaObject, ctx->onMouseButton,
                        stableDeviceId, (jint)0, (jboolean)JNI_TRUE);
                }
                if (buttonFlags & RI_MOUSE_LEFT_BUTTON_UP) {
                    env->CallVoidMethod(ctx->javaObject, ctx->onMouseButton,
                        stableDeviceId, (jint)0, (jboolean)JNI_FALSE);
                }
                if (buttonFlags & RI_MOUSE_RIGHT_BUTTON_DOWN) {
                    env->CallVoidMethod(ctx->javaObject, ctx->onMouseButton,
                        stableDeviceId, (jint)1, (jboolean)JNI_TRUE);
                }
                if (buttonFlags & RI_MOUSE_RIGHT_BUTTON_UP) {
                    env->CallVoidMethod(ctx->javaObject, ctx->onMouseButton,
                        stableDeviceId, (jint)1, (jboolean)JNI_FALSE);
                }
                if (buttonFlags & RI_MOUSE_MIDDLE_BUTTON_DOWN) {
                    env->CallVoidMethod(ctx->javaObject, ctx->onMouseButton,
                        stableDeviceId, (jint)2, (jboolean)JNI_TRUE);
                }
                if (buttonFlags & RI_MOUSE_MIDDLE_BUTTON_UP) {
                    env->CallVoidMethod(ctx->javaObject, ctx->onMouseButton,
                        stableDeviceId, (jint)2, (jboolean)JNI_FALSE);
                }
                if (buttonFlags & RI_MOUSE_BUTTON_4_DOWN) {
                    env->CallVoidMethod(ctx->javaObject, ctx->onMouseButton,
                        stableDeviceId, (jint)3, (jboolean)JNI_TRUE);
                }
                if (buttonFlags & RI_MOUSE_BUTTON_4_UP) {
                    env->CallVoidMethod(ctx->javaObject, ctx->onMouseButton,
                        stableDeviceId, (jint)3, (jboolean)JNI_FALSE);
                }
                if (buttonFlags & RI_MOUSE_BUTTON_5_DOWN) {
                    env->CallVoidMethod(ctx->javaObject, ctx->onMouseButton,
                        stableDeviceId, (jint)4, (jboolean)JNI_TRUE);
                }
                if (buttonFlags & RI_MOUSE_BUTTON_5_UP) {
                    env->CallVoidMethod(ctx->javaObject, ctx->onMouseButton,
                        stableDeviceId, (jint)4, (jboolean)JNI_FALSE);
                }
                
                if (buttonFlags & RI_MOUSE_WHEEL) {
                    short delta = (short)raw->data.mouse.usButtonData;
                    env->CallVoidMethod(ctx->javaObject, ctx->onMouseWheel,
                        stableDeviceId, (jint)delta);
                }
            }
        }
        return 0;
    }
    
    if (msg == WM_DESTROY) {
        PostQuitMessage(0);
        return 0;
    }
    
    return DefWindowProc(hwnd, msg, wParam, lParam);
}

// ============================================================================
// Message Loop
// ============================================================================

void messageLoopThread(MouseContext* ctx) {
    // Attach JNI thread once at the start
    ctx->vm->AttachCurrentThread((void**)&ctx->env, NULL);
    ctx->threadAttached = true;
    
    // Register window class (with unique name per process)
    WNDCLASS wc = {0};
    wc.lpfnWndProc = MouseWindowProc;
    wc.hInstance = GetModuleHandle(NULL);
    wc.lpszClassName = WINDOW_CLASS_NAME;
    
    ATOM classAtom = RegisterClass(&wc);
    if (!classAtom) {
        fprintf(stderr, "[FastMouse] Failed to register window class (error: %lu)\n", GetLastError());
        ctx->threadAttached = false;
        ctx->vm->DetachCurrentThread();
        return;
    }
    
    // Create message-only window
    ctx->messageWindow = CreateWindowEx(
        0, WINDOW_CLASS_NAME, L"FastMouse",
        0, 0, 0, 0, 0, HWND_MESSAGE, NULL, GetModuleHandle(NULL), NULL
    );
    
    if (!ctx->messageWindow) {
        fprintf(stderr, "[FastMouse] Failed to create message window (error: %lu)\n", GetLastError());
        UnregisterClassW(WINDOW_CLASS_NAME, GetModuleHandle(NULL));
        ctx->threadAttached = false;
        ctx->vm->DetachCurrentThread();
        return;
    }
    
    // Register for raw input
    RAWINPUTDEVICE rid[1];
    rid[0].usUsagePage = 0x01;
    rid[0].usUsage = 0x02;
    rid[0].dwFlags = RIDEV_INPUTSINK;
    rid[0].hwndTarget = ctx->messageWindow;
    
    if (!RegisterRawInputDevices(rid, 1, sizeof(RAWINPUTDEVICE))) {
        fprintf(stderr, "[FastMouse] Failed to register raw input devices\n");
        DestroyWindow(ctx->messageWindow);
        UnregisterClassW(WINDOW_CLASS_NAME, GetModuleHandle(NULL));
        ctx->threadAttached = false;
        ctx->vm->DetachCurrentThread();
        return;
    }
    
    // Message loop
    MSG msg;
    while (ctx->running && GetMessage(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }
    
    // Unregister raw input by setting RIDEV_REMOVE flag
    rid[0].dwFlags = RIDEV_REMOVE;
    rid[0].hwndTarget = NULL;
    RegisterRawInputDevices(rid, 1, sizeof(RAWINPUTDEVICE));
    
    DestroyWindow(ctx->messageWindow);
    ctx->messageWindow = NULL;
    UnregisterClassW(WINDOW_CLASS_NAME, GetModuleHandle(NULL));
    
    ctx->threadAttached = false;
    ctx->vm->DetachCurrentThread();
}

// ============================================================================
// JNI Implementations
// ============================================================================

JNIEXPORT jlong JNICALL Java_fastmouse_FastMouseImpl_nativeInitialize(JNIEnv* env, jobject obj) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    if (g_ctx) {
        return g_ctx->handle;
    }
    
    g_ctx = new MouseContext();
    g_ctx->handle = 1;
    g_ctx->javaObject = env->NewGlobalRef(obj);
    env->GetJavaVM(&g_ctx->vm);
    
    jclass clazz = env->GetObjectClass(obj);
    g_ctx->onMouseMove = env->GetMethodID(clazz, "onNativeMouseMove", "(JIIII)V");
    g_ctx->onMouseButton = env->GetMethodID(clazz, "onNativeMouseButton", "(JIZ)V");
    g_ctx->onMouseWheel = env->GetMethodID(clazz, "onNativeMouseWheel", "(JI)V");
    
    if (!g_ctx->onMouseMove || !g_ctx->onMouseButton || !g_ctx->onMouseWheel) {
        fprintf(stderr, "[FastMouse] Failed to get callback method IDs\n");
        env->DeleteGlobalRef(g_ctx->javaObject);
        delete g_ctx;
        g_ctx = nullptr;
        return 0;
    }
    
    return g_ctx->handle;
}

JNIEXPORT void JNICALL Java_fastmouse_FastMouseImpl_nativeStartListening(JNIEnv* env, jobject obj, jlong handle) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    if (!g_ctx || g_ctx->handle != handle) {
        fprintf(stderr, "[FastMouse] Invalid handle %lld\n", handle);
        return;
    }
    
    if (g_ctx->running) {
        return;
    }
    
    g_ctx->running = true;
    g_ctx->messageThread = std::thread(messageLoopThread, g_ctx);
}

JNIEXPORT void JNICALL Java_fastmouse_FastMouseImpl_nativeStopListening(JNIEnv* env, jobject obj, jlong handle) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    if (!g_ctx || g_ctx->handle != handle) {
        return;
    }
    
    if (!g_ctx->running) {
        return;
    }
    
    g_ctx->running = false;
    
    // Post message to window to wake up message loop
    if (g_ctx->messageWindow) {
        PostMessage(g_ctx->messageWindow, WM_CLOSE, 0, 0);
    }
    
    if (g_ctx->messageThread.joinable()) {
        g_ctx->messageThread.join();
    }
}

// Helper: Find raw handle by stable ID
static HANDLE FindRawHandleByStableId(jlong stableId) {
    std::lock_guard<std::mutex> lock(g_deviceMutex);
    for (auto& pair : g_stableDeviceIds) {
        if (pair.second == stableId) {
            return pair.first;
        }
    }
    return NULL;
}

JNIEXPORT jlongArray JNICALL Java_fastmouse_FastMouseImpl_nativeGetConnectedDevices(JNIEnv* env, jobject obj) {
    UINT numDevices;
    GetRawInputDeviceList(NULL, &numDevices, sizeof(RAWINPUTDEVICELIST));
    
    if (numDevices == 0) {
        return env->NewLongArray(0);
    }
    
    std::vector<RAWINPUTDEVICELIST> devices(numDevices);
    GetRawInputDeviceList(devices.data(), &numDevices, sizeof(RAWINPUTDEVICELIST));
    
    std::vector<jlong> stableIds;
    for (UINT i = 0; i < numDevices; i++) {
        if (devices[i].dwType == RIM_TYPEMOUSE) {
            stableIds.push_back(GetStableDeviceId(devices[i].hDevice));
        }
    }
    
    jlongArray result = env->NewLongArray(stableIds.size());
    env->SetLongArrayRegion(result, 0, stableIds.size(), stableIds.data());
    
    return result;
}

JNIEXPORT jstring JNICALL Java_fastmouse_FastMouseImpl_nativeGetDeviceName(JNIEnv* env, jobject obj, jlong stableId) {
    HANDLE hDevice = FindRawHandleByStableId(stableId);
    if (!hDevice) {
        return env->NewStringUTF("Unknown Device");
    }
    
    UINT size;
    GetRawInputDeviceInfo(hDevice, RIDI_DEVICENAME, NULL, &size);
    
    if (size == 0) {
        return env->NewStringUTF("Unknown Device");
    }
    
    std::vector<wchar_t> nameBuffer(size);
    GetRawInputDeviceInfo(hDevice, RIDI_DEVICENAME, nameBuffer.data(), &size);
    
    int len = WideCharToMultiByte(CP_UTF8, 0, nameBuffer.data(), -1, NULL, 0, NULL, NULL);
    std::vector<char> utf8Name(len);
    WideCharToMultiByte(CP_UTF8, 0, nameBuffer.data(), -1, utf8Name.data(), len, NULL, NULL);
    
    // Extract friendly name from path
    char* friendlyName = utf8Name.data();
    char* lastSlash = strrchr(friendlyName, '\\');
    if (lastSlash) {
        friendlyName = lastSlash + 1;
    }
    char* lastHash = strchr(friendlyName, '#');
    if (lastHash) {
        *lastHash = '\0';
    }
    
    return env->NewStringUTF(friendlyName);
}

JNIEXPORT jint JNICALL Java_fastmouse_FastMouseImpl_nativeGetDeviceButtonCount(JNIEnv* env, jobject obj, jlong stableId) {
    HANDLE hDevice = FindRawHandleByStableId(stableId);
    if (!hDevice) {
        return 3;
    }
    
    RID_DEVICE_INFO info;
    info.cbSize = sizeof(RID_DEVICE_INFO);
    UINT size = sizeof(RID_DEVICE_INFO);
    
    if (GetRawInputDeviceInfo(hDevice, RIDI_DEVICEINFO, &info, &size) == (UINT)-1) {
        return 3;
    }
    
    if (info.dwType == RIM_TYPEMOUSE) {
        return info.mouse.dwNumberOfButtons;
    }
    
    return 3;
}

JNIEXPORT jintArray JNICALL Java_fastmouse_FastMouseImpl_nativeGetCursorPosition(JNIEnv* env, jobject obj) {
    POINT pt = {0, 0};
    GetCursorPos(&pt);
    
    jintArray result = env->NewIntArray(2);
    jint coords[2] = { (jint)pt.x, (jint)pt.y };
    env->SetIntArrayRegion(result, 0, 2, coords);
    return result;
}

// ============================================================================
// DLL Entry Point
// ============================================================================

BOOL APIENTRY DllMain(HMODULE hModule, DWORD ul_reason_for_call, LPVOID lpReserved) {
    switch (ul_reason_for_call) {
        case DLL_PROCESS_ATTACH:
            DisableThreadLibraryCalls(hModule);
            break;
        case DLL_PROCESS_DETACH:
            {
                std::lock_guard<std::mutex> lock(g_mutex);
                if (g_ctx) {
                    if (g_ctx->running) {
                        g_ctx->running = false;
                        if (g_ctx->messageWindow) {
                            PostMessage(g_ctx->messageWindow, WM_CLOSE, 0, 0);
                        }
                        if (g_ctx->messageThread.joinable()) {
                            g_ctx->messageThread.join();
                        }
                    }
                    if (g_ctx->javaObject && g_ctx->vm) {
                        JNIEnv* env;
                        if (g_ctx->vm->AttachCurrentThread((void**)&env, NULL) == JNI_OK) {
                            env->DeleteGlobalRef(g_ctx->javaObject);
                            g_ctx->vm->DetachCurrentThread();
                        }
                    }
                    delete g_ctx;
                    g_ctx = nullptr;
                }
                g_stableDeviceIds.clear();
            }
            break;
    }
    return TRUE;
}
