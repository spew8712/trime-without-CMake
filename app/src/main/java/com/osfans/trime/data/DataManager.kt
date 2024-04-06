package com.osfans.trime.data

import com.osfans.trime.BuildConfig
import com.osfans.trime.core.Rime
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception

object DataManager {
    private val prefs get() = AppPrefs.defaultInstance()

    @JvmStatic
    fun getDataDir(child: String = ""): String {
        return if (File(prefs.conf.sharedDataDir, child).exists()) {
            File(prefs.conf.sharedDataDir, child).absolutePath
        } else {
            File(prefs.conf.userDataDir, child).absolutePath
        }
    }

    fun copyFile(file: File, outputPath: String): Boolean {
        if (file.exists()) {
            try {
                val input: InputStream = FileInputStream(file)
                val outputStream: OutputStream = FileOutputStream(File(outputPath))
                val buffer = ByteArray(1024)
                var len: Int
                while (input.read(buffer).also { len = it } > 0) {
                    outputStream.write(buffer, 0, len)
                }
                Timber.i("copyFile = ${file.absolutePath}, $outputPath")
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    fun backupPref(): Boolean {
        val file = File(
            "/data/data/APPLICATION_ID/shared_prefs/APPLICATION_ID_preferences.xml"
                .replace("APPLICATION_ID", BuildConfig.APPLICATION_ID)
        )

        return copyFile(
            file,
            Rime.get_sync_dir() + File.separator + Rime.get_user_id() + File.separator + file.name
        )
    }

    fun recoverPref(): Boolean {
        val file = File(Rime.get_sync_dir() + File.separator + Rime.get_user_id() + File.separator + "recover.xml")

        if (copyFile(
                file,
                "/data/data/APPLICATION_ID/shared_prefs/APPLICATION_ID_preferences.xml"
                    .replace("APPLICATION_ID", BuildConfig.APPLICATION_ID)
            )
        ) {
            return file.renameTo(File(file.parent, "recovered.xml"))
        }
        return false
    }
}
