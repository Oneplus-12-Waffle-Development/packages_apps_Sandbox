package com.android.axion.sandbox.security

import android.content.Context
import android.content.SharedPreferences
import android.hardware.biometrics.BiometricManager
import android.provider.Settings
import java.security.MessageDigest

enum class SecurityType {
    NONE,
    PIN,
    PASSWORD,
    PATTERN
}

enum class LockedAppBehavior {
    ON_LEAVE,
    TIMEOUT,
    ON_SCREEN_OFF,
    ON_KILL
}

enum class PrivateSectionBehavior {
    ON_LEAVE,
    TIMEOUT
}

class SandboxSecurityManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    fun getSecurityType(): SecurityType {
        val type = prefs.getString(KEY_SECURITY_TYPE, null) ?: return SecurityType.NONE
        return try {
            SecurityType.valueOf(type)
        } catch (e: IllegalArgumentException) {
            SecurityType.NONE
        }
    }
    
    fun isSetup(): Boolean {
        return getSecurityType() != SecurityType.NONE && prefs.contains(KEY_CREDENTIAL_HASH)
    }
    
    fun setPin(pin: String): Boolean {
        if (pin.length < MIN_PIN_LENGTH || pin.length > MAX_PIN_LENGTH) {
            return false
        }
        if (!pin.all { it.isDigit() }) {
            return false
        }
        
        val hash = hashCredential(pin)
        prefs.edit()
            .putString(KEY_SECURITY_TYPE, SecurityType.PIN.name)
            .putString(KEY_CREDENTIAL_HASH, hash)
            .apply()
        return true
    }
    
    fun setPassword(password: String): Boolean {
        if (password.length < MIN_PASSWORD_LENGTH) {
            return false
        }
        
        val hash = hashCredential(password)
        prefs.edit()
            .putString(KEY_SECURITY_TYPE, SecurityType.PASSWORD.name)
            .putString(KEY_CREDENTIAL_HASH, hash)
            .apply()
        return true
    }
    
    fun setPattern(pattern: List<Int>): Boolean {
        if (pattern.size < MIN_PATTERN_LENGTH) {
            return false
        }
        
        val patternString = pattern.joinToString(",")
        val hash = hashCredential(patternString)
        prefs.edit()
            .putString(KEY_SECURITY_TYPE, SecurityType.PATTERN.name)
            .putString(KEY_CREDENTIAL_HASH, hash)
            .apply()
        return true
    }
    
    fun verifyCredential(credential: String): Boolean {
        val storedHash = prefs.getString(KEY_CREDENTIAL_HASH, null) ?: return false
        val inputHash = hashCredential(credential)
        return storedHash == inputHash
    }
    
    fun verifyPattern(pattern: List<Int>): Boolean {
        val patternString = pattern.joinToString(",")
        return verifyCredential(patternString)
    }
    
    enum class BiometricType {
        NONE,
        FINGERPRINT,
        FACE
    }

    fun isBiometricAvailable(): Boolean {
        val biometricManager = context.getSystemService(BiometricManager::class.java)
        return biometricManager?.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }
    
    fun getBiometricType(): BiometricType {
        val pm = context.packageManager
        if (pm.hasSystemFeature(android.content.pm.PackageManager.FEATURE_FINGERPRINT)) {
            return BiometricType.FINGERPRINT
        }
        if (pm.hasSystemFeature(android.content.pm.PackageManager.FEATURE_FACE)) {
            return BiometricType.FACE
        }
        return BiometricType.NONE
    }
    
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()
    }
    
    
    fun getPrivateSectionBehavior(): PrivateSectionBehavior {
        val behavior = prefs.getString(KEY_PRIVATE_SECTION_BEHAVIOR, null) ?: return PrivateSectionBehavior.ON_LEAVE
        return try {
            PrivateSectionBehavior.valueOf(behavior)
        } catch (e: IllegalArgumentException) {
            PrivateSectionBehavior.ON_LEAVE
        }
    }

    fun setPrivateSectionBehavior(behavior: PrivateSectionBehavior) {
        prefs.edit()
            .putString(KEY_PRIVATE_SECTION_BEHAVIOR, behavior.name)
            .apply()
    }
    
    fun getLockTimeout(): Int {
        return prefs.getInt(KEY_LOCK_TIMEOUT, DEFAULT_TIMEOUT_SECONDS)
    }
    
    fun setLockTimeout(seconds: Int) {
        prefs.edit()
            .putInt(KEY_LOCK_TIMEOUT, seconds)
            .apply()
    }
    
    fun getLastUnlockTime(): Long {
        return prefs.getLong(KEY_LAST_UNLOCK_TIME, 0L)
    }
    
    fun setLastUnlockTime(time: Long = System.currentTimeMillis()) {
        prefs.edit()
            .putLong(KEY_LAST_UNLOCK_TIME, time)
            .apply()
    }
    
    fun isTimeoutExpired(): Boolean {
        if (getPrivateSectionBehavior() != PrivateSectionBehavior.TIMEOUT) return false
        val elapsed = System.currentTimeMillis() - getLastUnlockTime()
        return elapsed > getLockTimeout() * 1000L
    }
    
    private fun hashCredential(credential: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(credential.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    fun getLockedAppBehavior(): LockedAppBehavior {
        return try {
            val ordinal = Settings.Secure.getInt(
                context.contentResolver,
                SETTING_LOCKED_APP_BEHAVIOR,
                LockedAppBehavior.ON_LEAVE.ordinal
            )
            LockedAppBehavior.entries.getOrElse(ordinal) { LockedAppBehavior.ON_LEAVE }
        } catch (e: Exception) {
            LockedAppBehavior.ON_LEAVE
        }
    }
    
    fun setLockedAppBehavior(behavior: LockedAppBehavior) {
        Settings.Secure.putInt(
            context.contentResolver,
            SETTING_LOCKED_APP_BEHAVIOR,
            behavior.ordinal
        )
    }
    
    fun getLockedAppTimeout(): Int {
        return Settings.Secure.getInt(
            context.contentResolver,
            SETTING_LOCKED_APP_TIMEOUT,
            DEFAULT_TIMEOUT_SECONDS)
    }
    
    fun setLockedAppTimeout(seconds: Int) {
        Settings.Secure.putInt(
            context.contentResolver,
            SETTING_LOCKED_APP_TIMEOUT,
            seconds
        )
    }
    
    companion object {
        private const val PREFS_NAME = "sandbox_security"
        private const val KEY_SECURITY_TYPE = "security_type"
        private const val KEY_CREDENTIAL_HASH = "credential_hash"
        private const val KEY_PRIVATE_SECTION_BEHAVIOR = "private_section_behavior"
        private const val KEY_LOCK_TIMEOUT = "lock_timeout"
        private const val KEY_LAST_UNLOCK_TIME = "last_unlock_time"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        
        const val SETTING_LOCKED_APP_BEHAVIOR = "sandbox_locked_app_behavior"
        const val SETTING_LOCKED_APP_TIMEOUT = "sandbox_locked_app_timeout"
        
        const val MIN_PIN_LENGTH = 4
        const val MAX_PIN_LENGTH = 4
        const val MIN_PASSWORD_LENGTH = 4
        const val MIN_PATTERN_LENGTH = 4
        const val DEFAULT_TIMEOUT_SECONDS = 30
    }
}
