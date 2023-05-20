package com.example.pdfviewfullscreenproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.pdfviewfullscreenproject.databinding.ActivityMainBinding
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val pdfUrl = "https://www.africau.edu/images/default/sample.pdf"
        val tempFile = File.createTempFile("temp", ".pdf", cacheDir)
        Thread {
            val pdfByteArray = downloadPDF(pdfUrl)
            tempFile.writeBytes(pdfByteArray)

            runOnUiThread {
                displayPDF(tempFile)
            }
        }.start()
    }

    private fun displayPDF(file: File) {
        binding.pdfView.maxZoom = 6.0f
        binding.pdfView.midZoom = 3.0f
        binding.pdfView.useBestQuality(true)
        binding.pdfView.fromFile(file)
            .enableSwipe(true)
            .enableDoubletap(true)
            .defaultPage(0)
            .scrollHandle(DefaultScrollHandle(this))
            .spacing(10)
            .swipeHorizontal(true)
            .enableAntialiasing(true)
            .autoSpacing(true)
            .pageFitPolicy(FitPolicy.BOTH)
            .pageSnap(true)
            .pageFling(true)
            .fitEachPage(true)
            .load()
    }

    private fun downloadPDF(url: String): ByteArray {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 10000
        connection.requestMethod = "GET"

        val inputStream = BufferedInputStream(connection.inputStream)
        val outputStream = ByteArrayOutputStream()

        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
        connection.disconnect()

        return outputStream.toByteArray()
    }
}