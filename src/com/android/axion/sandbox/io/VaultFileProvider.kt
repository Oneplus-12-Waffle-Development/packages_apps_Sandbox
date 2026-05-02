/*
 * Copyright (C) 2025-2026 AxionOS Project
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
package com.android.axion.sandbox.io

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.UserHandle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import java.io.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

class VaultFileProvider : ContentProvider() {
    private val TAG = "VaultFileProvider"
    private val bridgeLock = Any()

    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val context = context ?: return null
        val fileId = uri.lastPathSegment ?: return null
        val vaultManager = FileVaultManager(context)
        val vaultFile = vaultManager.getFileById(fileId) ?: return null
        
        val currentUserId = UserHandle.myUserId()
        val bridgeDir = File(context.externalCacheDir ?: context.cacheDir, "vault_bridge")
        val tempFile = File(bridgeDir, "u${currentUserId}_${vaultFile.id}_${vaultFile.name}")
        
        return if (tempFile.exists() && tempFile.length() == vaultFile.size) {
            ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        } else {
            openSeekableFile(context, vaultManager, vaultFile)
        }
    }

    private fun openSeekableFile(context: android.content.Context, vaultManager: FileVaultManager, vaultFile: VaultFile): ParcelFileDescriptor? {
        synchronized(bridgeLock) {
            try {
                val currentUserId = UserHandle.myUserId()
                val bridgeDir = File(context.externalCacheDir ?: context.cacheDir, "vault_bridge").apply { if (!exists()) mkdirs() }
                val tempFile = File(bridgeDir, "u${currentUserId}_${vaultFile.id}_${vaultFile.name}")
                
                if (tempFile.exists() && tempFile.length() == vaultFile.size) {
                    return ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                }

                val key = vaultManager.getMasterKey() ?: return null
                FileInputStream(vaultFile.file).use { fis ->
                    val iv = ByteArray(12)
                    if (fis.read(iv) == 12) {
                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
                        
                        FileOutputStream(tempFile).use { fos ->
                            val buffer = ByteArray(65536)
                            while (true) {
                                val read = fis.read(buffer)
                                if (read == -1) break
                                val decrypted = cipher.update(buffer, 0, read)
                                if (decrypted != null) fos.write(decrypted)
                            }
                            val finalBlock = cipher.doFinal()
                            if (finalBlock != null) fos.write(finalBlock)
                            fos.flush()
                        }
                    }
                }
                tempFile.setReadable(true, false)
                return ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            } catch (e: Exception) {
                return null
            }
        }
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val context = context ?: return null
        val fileId = uri.lastPathSegment ?: return null
        val vaultFile = FileVaultManager(context).getFileById(fileId) ?: return null

        val columnNames = projection ?: arrayOf(
            BaseColumns._ID, OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        
        val cursor = MatrixCursor(columnNames, 1)
        val row = cursor.newRow()
        
        val currentUserId = UserHandle.myUserId()
        val bridgeDir = File(context.externalCacheDir ?: context.cacheDir, "vault_bridge")
        val bridgePath = File(bridgeDir, "u${currentUserId}_${vaultFile.id}_${vaultFile.name}").absolutePath

        for (column in columnNames) {
            when (column) {
                BaseColumns._ID -> row.add(1)
                OpenableColumns.DISPLAY_NAME, MediaStore.MediaColumns.DISPLAY_NAME -> row.add(vaultFile.name)
                OpenableColumns.SIZE, MediaStore.MediaColumns.SIZE -> row.add(vaultFile.size)
                MediaStore.MediaColumns.MIME_TYPE -> row.add(vaultFile.mimeType)
                MediaStore.MediaColumns.DATA -> row.add(bridgePath)
                MediaStore.MediaColumns.DATE_MODIFIED -> row.add(System.currentTimeMillis() / 1000)
                else -> row.add(null)
            }
        }
        return cursor
    }

    override fun getType(uri: Uri): String? {
        val context = context ?: return null
        val fileId = uri.lastPathSegment ?: return null
        return FileVaultManager(context).getFileById(fileId)?.mimeType
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
}
