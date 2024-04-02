package com.example.pdfviewfullscreenproject

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.pdfviewfullscreenproject.databinding.ActivityMainBinding
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.RectangleReadOnly
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min


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
                displayTwoPages()
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
                displayTwoPages()
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

    private fun mergeTwoPagesIntoOne(originalPdfFile: String, outputPdfFile: String?) {
        val tempFile = File.createTempFile("temp", ".pdf", cacheDir)
        tempFile.writeBytes(assets.open(originalPdfFile).readBytes())
        val reader = PdfReader(tempFile.path)
        val doc = Document(RectangleReadOnly(842f, 595f), 0f, 0f, 0f, 0f)
        val writer = PdfWriter.getInstance(doc, FileOutputStream(outputPdfFile))
        doc.open()
        val totalPages = reader.numberOfPages
        var i = 1
        while (i <= totalPages) {
            doc.newPage()
            val cb = writer.directContent
            val page = writer.getImportedPage(reader, i) // page #1
            val documentWidth = doc.pageSize.width / 2
            var documentHeight = doc.pageSize.height
            if (i > 1) documentHeight -= 50f
            var pageWidth = page.width
            var pageHeight = page.height
            var widthScale = documentWidth / pageWidth
            var heightScale = documentHeight / pageHeight
            var scale = min(widthScale, heightScale)
            var offsetX = (documentWidth - pageWidth * scale) / 2
            val offsetY = 0f
            cb.addTemplate(page, scale, 0f, 0f, scale, offsetX, offsetY)
            if (i + 1 <= totalPages) {
                val page2 = writer.getImportedPage(reader, i + 1) // page #2
                pageWidth = page.width
                pageHeight = page.height
                widthScale = documentWidth / pageWidth
                heightScale = documentHeight / pageHeight
                scale = min(widthScale, heightScale)
                offsetX = (documentWidth - pageWidth * scale) / 2 + documentWidth
                cb.addTemplate(page2, scale, 0f, 0f, scale, offsetX, offsetY)
            }
            i += 2
        }
        doc.close()
    }

    private fun displayTwoPages() {
        try {
            val mergedPdfPath = cacheDir.toString() + File.separator + "merged.pdf"
            mergeTwoPagesIntoOne("sample.pdf", mergedPdfPath)
            val mergedPdfFile = File(mergedPdfPath)
            displayPDF(mergedPdfFile)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}
