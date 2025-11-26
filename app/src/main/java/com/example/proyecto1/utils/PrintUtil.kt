package com.example.proyecto1.utils

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.proyecto1.data.repository.KpiResult
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object PrintUtil {

    fun printPdf(context: Context, kpiResult: KpiResult) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "SLA_Report_Document"

        printManager.print(jobName, object : PrintDocumentAdapter() {
            override fun onLayout(oldAttributes: PrintAttributes?, newAttributes: PrintAttributes, cancellationSignal: CancellationSignal?, callback: LayoutResultCallback, extras: Bundle?) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("sla_report.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()
                callback.onLayoutFinished(info, newAttributes != oldAttributes)
            }

            override fun onWrite(pages: Array<out PageRange>?, destination: ParcelFileDescriptor, cancellationSignal: CancellationSignal?, callback: WriteResultCallback) {
                var output: OutputStream? = null
                try {
                    output = FileOutputStream(destination.fileDescriptor)
                    PdfReportGenerator.writeToStream(output, kpiResult)
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: IOException) {
                    callback.onWriteFailed(e.toString())
                } finally {
                    try {
                        output?.close()
                    } catch (e: IOException) {
                        // Ignorar
                    }
                }
            }
        }, null)
    }
}