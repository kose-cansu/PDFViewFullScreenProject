package com.example.pdfviewfullscreenproject

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
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

            if(isTwoPageMode) {
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

            if(isTwoPageMode) {
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
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayTwoPages(file: File) {
        try {
            val renderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))

            val pageCount = renderer.pageCount
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.HORIZONTAL

            for (i in 0 until pageCount step 2) {
                val view1 = createPdfView(renderer, i)
                val view2 = if (i + 1 < pageCount) createPdfView(renderer, i + 1) else null

                val pageLayout = LinearLayout(this)
                pageLayout.orientation = LinearLayout.HORIZONTAL
                pageLayout.addView(view1)

                if (view2 != null) {
                    pageLayout.addView(view2)
                }
                layout.addView(pageLayout)
            }

            val scrollView = HorizontalScrollView(this)
            scrollView.addView(layout)
            setContentView(scrollView)

        } catch (e: Exception) {
            e.printStackTrace()
        }
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