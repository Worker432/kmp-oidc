package io.github.zm.auth_core.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

@Suppress("DEPRECATION")
internal class AndroidEncryptedStringStorage(
    context: Context,
    storageName: String
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        storageName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(
        key: String,
        value: String
    ) {
        preferences.edit()
            .putString(key, value)
            .commit()
    }

    fun read(key: String): String? {
        return preferences.getString(key, null)
    }

    fun clear(key: String) {
        preferences.edit()
            .remove(key)
            .apply()
    }
}
