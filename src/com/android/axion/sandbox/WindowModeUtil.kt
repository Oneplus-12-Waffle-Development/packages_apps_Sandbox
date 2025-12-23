/*
 * Copyright (C) 2025 The AxionAOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.axion.sandbox

import android.app.ActivityManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.text.TextUtils
import android.util.Log
import android.view.Display
import java.lang.reflect.Method

object WindowModeUtil {
    private const val TAG = "WindowModeUtil"

    @JvmStatic
    fun isAppInMultiWindowMode(): Boolean {
        return isAppInWindowMode("inMultiWindowMode")
    }
    
    @JvmStatic
    fun isAppInFreeformDisplay(context: Context): Boolean {
        return try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            if (displayManager != null) {
                val display = context.display ?: displayManager.getDisplay(Display.DEFAULT_DISPLAY)
                val displayId = display?.displayId ?: Display.DEFAULT_DISPLAY
                displayManager.isFreeformDisplayId(displayId)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.d(TAG, "isAppInFreeformDisplay Exception: ${e}")
            false
        }
    }

    @JvmStatic
    fun isAppInWindowMode(methodName: String): Boolean {
        val tasks = getTask(3)
        if (!tasks.isNullOrEmpty()) {
            for ((index, task) in tasks.withIndex()) {
                try {
                    val windowingMode = getWindowingMode(task)
                    val packageName = task.topActivity?.packageName
                    Log.d(TAG, "isAppInWindowMode $methodName$index - $packageName windowingMode $windowingMode")
                    if (TextUtils.equals(packageName, "com.android.axion.sandbox") && checkMode(methodName, windowingMode)) {
                        return true
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "isAppInWindowMode Exception: ${e}")
                }
            }
        }
        return false
    }

    @JvmStatic
    fun getTask(maxNum: Int): List<ActivityManager.RunningTaskInfo>? {
        return try {
            val cls = Class.forName("android.app.ActivityTaskManager")
            val getInstanceMethod: Method = cls.getDeclaredMethod("getInstance").apply { isAccessible = true }
            val manager = getInstanceMethod.invoke(null)
            val getTasksMethod: Method = cls.getDeclaredMethod("getTasks", Int::class.javaPrimitiveType).apply { isAccessible = true }
            @Suppress("UNCHECKED_CAST")
            getTasksMethod.invoke(manager, maxNum) as? List<ActivityManager.RunningTaskInfo>
        } catch (e: Exception) {
            Log.d(TAG, "getTask Exception: ${e}")
            null
        }
    }

    @JvmStatic
    fun getWindowingMode(taskInfo: ActivityManager.RunningTaskInfo): Int {
        return try {
            val method = Class.forName("android.app.TaskInfo")
                .getDeclaredMethod("getWindowingMode")
                .apply { isAccessible = true }
            (method.invoke(taskInfo) as? Int) ?: 0
        } catch (e: Exception) {
            Log.d(TAG, "getWindowingMode Exception: ${e}")
            0
        }
    }

    @JvmStatic
    fun checkMode(methodName: String, mode: Int): Boolean {
        return try {
            val method = Class.forName("android.app.WindowConfiguration")
                .getDeclaredMethod(methodName, Int::class.javaPrimitiveType)
            val result = method.invoke(null, mode) as? Boolean ?: false
            Log.d(TAG, "checkMode() = [$result]")
            result
        } catch (e: Exception) {
            Log.d(TAG, "checkMode method $methodName Exception: ${e}")
            false
        }
    }
}
