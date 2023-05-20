package com.example.pdfviewfullscreenproject

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
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
    private var previousOrientation = Configuration.ORIENTATION_UNDEFINED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentOrientation = resources.configuration.orientation
        if (previousOrientation != currentOrientation) {
            previousOrientation = currentOrientation

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
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation != previousOrientation) {
            previousOrientation = newConfig.orientation

            val pdfUrl = "https://www.africau.edu/images/default/sample.pdf"
            val tempFile = File.createTempFile("temp", ".pdf", cacheDir)
            Thread {
                val pdfByteArray = downloadPDF(pdfUrl)
                tempFile.writeBytes(pdfByteArray)

                runOnUiThread {
                    displayTwoPages(tempFile)
                }
            }.start()
        }
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

    private fun displayTwoPages(file: File) {
        val renderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
        val view1 = createPdfView(renderer, 0)
        val view2 = createPdfView(renderer, 1)

        view1.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        view2.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.addView(view1)
        layout.addView(view2)

        setContentView(layout)
    }

    private fun createPdfView(renderer: PdfRenderer, pageNumber: Int): ImageView {
        val page = renderer.openPage(pageNumber)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        val imageView = ImageView(this)
        imageView.setImageBitmap(bitmap)

        page.close()

        return imageView
    }

}