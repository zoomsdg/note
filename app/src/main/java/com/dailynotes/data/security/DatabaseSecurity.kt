package com.dailynotes.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom

object DatabaseSecurity {
    
    private const val DB_PASSPHRASE_KEY = "db_passphrase"
    private const val SALT_KEY = "db_salt"
    private const val ENCRYPTED_PREFS_NAME = "secure_database_prefs"
    
    fun getDatabasePassphrase(context: Context): String {
        val encryptedPrefs = getEncryptedSharedPreferences(context)
        
        return encryptedPrefs.getString(DB_PASSPHRASE_KEY, null) ?: run {
            // 生成新的数据库密码
            val passphrase = generateSecurePassphrase()
            encryptedPrefs.edit()
                .putString(DB_PASSPHRASE_KEY, passphrase)
                .apply()
            passphrase
        }
    }
    
    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    private fun generateSecurePassphrase(): String {
        val secureRandom = SecureRandom()
        val passphraseBytes = ByteArray(32) // 256位密钥
        secureRandom.nextBytes(passphraseBytes)
        
        // 使用SHA-256哈希生成更安全的密码
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(passphraseBytes)
        
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
    
    fun clearDatabaseSecurity(context: Context) {
        try {
            val encryptedPrefs = getEncryptedSharedPreferences(context)
            encryptedPrefs.edit()
                .remove(DB_PASSPHRASE_KEY)
                .remove(SALT_KEY)
                .apply()
        } catch (e: Exception) {
            // 如果无法清理，至少删除SharedPreferences文件
            context.deleteSharedPreferences(ENCRYPTED_PREFS_NAME)
        }
    }
}