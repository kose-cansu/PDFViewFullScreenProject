package com.example.pdfviewfullscreenproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.IOException

class PdfAdapter(private val context: Context, pdfFile: File?) :
    RecyclerView.Adapter<PdfAdapter.PdfViewHolder>() {
    private var pdfRenderer: PdfRenderer? = null
    private var pageCount = 0

    init {
        try {
            pdfRenderer = PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
            pageCount = pdfRenderer!!.pageCount
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_pdf_page, parent, false)
        return PdfViewHolder(view)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val page1 = position * 2
        val page2 = page1 + 1
        holder.renderPage(page1)
        holder.renderPage(page2)
    }

    override fun getItemCount(): Int {
        return Math.ceil(pageCount.toDouble() / 2).toInt()
    }

    inner class PdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView1: ImageView = itemView.findViewById(R.id.image_view_1)
        private val imageView2: ImageView = itemView.findViewById(R.id.image_view_2)

        fun renderPage(page: Int) {
            if (page < pageCount) {
                val pdfPage = pdfRenderer?.openPage(page)
                val bitmap = Bitmap.createBitmap(pdfPage!!.width * 2, pdfPage.height, Bitmap.Config.ARGB_8888)
                pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                pdfPage.close()
                if (page % 2 == 0) {
                    imageView1.setImageBitmap(bitmap)
                } else {
                    imageView2.setImageBitmap(bitmap)
                }
            } else {
                imageView1.setImageBitmap(null)
                imageView2.setImageBitmap(null)
            }
        }
    }
}

