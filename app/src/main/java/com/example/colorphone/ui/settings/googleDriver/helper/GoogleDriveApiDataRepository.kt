package com.example.colorphone.ui.settings.googleDriver.helper

import android.util.Base64
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class GoogleDriveApiDataRepository(private val mDriveService: Drive?) {
    private val FILE_MIME_TYPE = "application/zip"
    private val APP_DATA_FOLDER_SPACE = "appDataFolder"
    private val mExecutor: Executor = Executors.newSingleThreadExecutor()


    fun uploadFile(fileId: String, name: String, content: String) {
        val metadata = com.google.api.services.drive.model.File().setName(name)

        val contentStream = ByteArrayContent.fromString("text/plain", content)

        try {
            mDriveService?.files()?.update(fileId, metadata, contentStream)?.execute()
        } catch (e: Exception) {
            Log.i("crash", "uploadFile: $e")
        }


    }

    fun createFile(
        mimeType: String,
        name: String,
        block: (Drive.Files.Create.() -> Drive.Files.Create)? = null
    ): com.google.api.services.drive.model.File {
        val fileMeta = com.google.api.services.drive.model.File()
        fileMeta.name = "iNote"
        var create = mDriveService?.Files()
            ?.create(fileMeta)
        create = if (block != null) {
            create?.block()
        } else {
            create
        }

        return create!!.execute()
    }

    fun downloadFile(file: File, fileName: String): Task<Void?> {
        return queryFiles()
            .continueWithTask(
                mExecutor
            ) { task: Task<FileList?> ->
                val fileList = task.result
                    ?: throw IOException("Null file list when requesting file download.")
                var currentFile: com.google.api.services.drive.model.File? = null
                for (f in fileList.files) {
                    if (f.name == fileName) {
                        currentFile = f
                        break
                    }
                }
                if (currentFile == null) {
                    throw IOException("File not found when requesting file download.")
                }
                val fileId = currentFile.id
                readFile(file, fileId)
            }
    }

    fun searchFile(file: File, fileName: String): Task<Void?> {
        return queryFiles()
            .continueWithTask(
                mExecutor
            ) { task: Task<FileList?> ->
                val fileList = task.result
                    ?: throw IOException("Null file list when requesting file download.")
                var currentFile: com.google.api.services.drive.model.File? = null
                for (f in fileList.files) {
                    if (f.name == fileName) {
                        currentFile = f
                        break
                    }
                }
                if (currentFile == null) {
                    throw IOException("File not found when requesting file download.")
                }
                val fileId = currentFile.id
                readFile(file, fileId)
            }
    }

    private fun readFile(
        file: java.io.File,
        fileId: String
    ): Task<Void?> {
        return Tasks.call(mExecutor) {
            var encoded: String
            mDriveService?.files()?.get(fileId)?.executeMediaAsInputStream().use { input ->
                BufferedReader(InputStreamReader(input)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    encoded = stringBuilder.toString()
                }
            }
            val decoded =
                Base64.decode(encoded, Base64.DEFAULT)
            FileOutputStream(file).use { stream -> stream.write(decoded) }
            null
        }
    }

    fun readFile(id: String): String {
        val metadata: com.google.api.services.drive.model.File =
            mDriveService?.files()?.get(id)!!.execute()

        mDriveService.files().get(id).executeMediaAsInputStream().use { `is` ->
            BufferedReader(InputStreamReader(`is`)).use { reader ->
                val stringBuilder = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    Log.d("TANKLLL", line.toString())
                    stringBuilder.append(line)
                }
                return stringBuilder.toString()
            }
        }
    }

    private fun queryFiles(): Task<FileList?> {
        return Tasks.call(
            mExecutor
        ) {
            mDriveService?.files()?.list()?.setSpaces(APP_DATA_FOLDER_SPACE)?.execute()
        }
    }

    fun query(): String? {
        return try {
            val queryResult = mDriveService?.files()
                ?.list()
                ?.execute()
            queryResult?.files?.find { it.name == "iNote" }?.id
        } catch (e: Exception) {
            Log.i("error", "query: $e")
            ""
        }
    }

    private fun createFile(fileName: String): Task<String?> {
        return Tasks.call(mExecutor) {
            val metadata = getMetaData(fileName)
            metadata.parents = listOf(APP_DATA_FOLDER_SPACE)
            val googleFile =
                mDriveService?.files()?.create(metadata)?.execute()
                    ?: throw IOException("Null result when requesting file creation.")
            googleFile.id
        }
    }

    private fun getMetaData(fileName: String): com.google.api.services.drive.model.File {
        return com.google.api.services.drive.model.File()
            .setMimeType(FILE_MIME_TYPE)
            .setName(fileName)
    }
}