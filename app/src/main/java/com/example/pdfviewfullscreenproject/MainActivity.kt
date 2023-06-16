package com.example.pdfviewfullscreenproject

import android.content.res.Configuration
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfviewfullscreenproject.databinding.ActivityMainBinding
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var previousOrientation = Configuration.ORIENTATION_UNDEFINED
    private var isTwoPageMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentOrientation = resources.configuration.orientation
        if (previousOrientation != currentOrientation) {
            previousOrientation = currentOrientation

            val tempFile = File.createTempFile("temp", ".pdf", cacheDir)
            tempFile.writeBytes(assets.open("sample.pdf").readBytes())

            if (isTwoPageMode) {
                displayTwoPages(tempFile)
            } else {
                displayPDF(tempFile)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation != previousOrientation) {
            previousOrientation = newConfig.orientation

            val tempFile = File.createTempFile("temp", ".pdf", cacheDir)
            tempFile.writeBytes(assets.open("sample.pdf").readBytes())

            isTwoPageMode = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE

            if (isTwoPageMode) {
                displayTwoPages(tempFile)
            } else {
                displayPDF(tempFile)
            }
        }
    }

    private fun displayPDF(file: File) {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayTwoPages(file: File) {
        try {
            val renderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
            val pageCount = renderer.pageCount

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.HORIZONTAL

            val recyclerView = RecyclerView(this)
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val adapter = PdfAdapter(this, file)
            recyclerView.adapter = adapter

            layout.addView(recyclerView)

            val scrollView = HorizontalScrollView(this)
            scrollView.addView(layout)
            setContentView(scrollView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
