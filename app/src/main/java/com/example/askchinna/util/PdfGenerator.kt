/**
 * File: app/src/main/java/com/example/askchinna/util/PdfGenerator.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import com.example.askchinna.R
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap

/**
 * Generator for PDF reports of identification results
 * Optimized for low-end devices with minimal memory usage
 */
@Singleton
class PdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dateTimeUtils: DateTimeUtils
) {
    companion object {
        private const val TAG = "PdfGenerator"
        private const val PDF_WIDTH_PX = 595 // A4 width in pixels at 72 dpi
        private const val PDF_HEIGHT_PX = 842 // A4 height in pixels at 72 dpi
        private const val MARGIN = 36 // Margin in pixels (0.5 inch)
        private const val HEADER_TEXT_SIZE = 18f
        private const val TITLE_TEXT_SIZE = 16f
        private const val SUBTITLE_TEXT_SIZE = 14f
        private const val BODY_TEXT_SIZE = 12f
        private const val SMALL_TEXT_SIZE = 10f
        private const val LINE_HEIGHT = 16f
        private const val PDF_PREFIX = "AskChinna_Report_"
    }

    /**
     * Generate a PDF report for an identification result
     * @param result The identification result
     * @param crop The crop that was identified
     * @param user The user who performed the identification
     * @param imageBitmap Optional bitmap of the identified crop image
     * @return URI of the generated PDF file or null if generation failed
     */
    suspend fun generateReport(
        result: IdentificationResult,
        crop: Crop,
        user: User,
        imageBitmap: Bitmap? = null
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()

            // Create a page
            val pageInfo = PdfDocument.PageInfo.Builder(PDF_WIDTH_PX, PDF_HEIGHT_PX, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Draw content on the page
            drawReportContent(canvas, result, crop, user, imageBitmap)

            // Finish the page
            pdfDocument.finishPage(page)

            // Create PDF file
            val file = createPdfFile()
            try {
                FileOutputStream(file).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }

                // Convert to content URI
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: IOException) {
                Log.e(TAG, "Error writing PDF: ${e.message}")
                null
            } finally {
                pdfDocument.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating PDF: ${e.message}")
            null
        }
    }

    /**
     * Create a PDF file with unique name
     * @return File object for the created file
     */
    private fun createPdfFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${PDF_PREFIX}${timeStamp}.pdf"

        val storageDir = File(context.filesDir, "reports")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File(storageDir, fileName)
    }

    /**
     * Draw the content of the PDF report
     * @param canvas Canvas to draw on
     * @param result Identification result data
     * @param crop Crop data
     * @param user User data
     * @param imageBitmap Optional bitmap of the identified crop image
     */
    private fun drawReportContent(
        canvas: Canvas,
        result: IdentificationResult,
        crop: Crop,
        user: User,
        imageBitmap: Bitmap?
    ) {
        val paint = Paint()

        // Current Y position tracker
        var yPos = MARGIN.toFloat()

        // Draw header
        paint.color = Color.rgb(0, 100, 0) // Dark green
        paint.textSize = HEADER_TEXT_SIZE
        paint.isFakeBoldText = true
        canvas.drawText(context.getString(R.string.app_name), MARGIN.toFloat(), yPos + HEADER_TEXT_SIZE, paint)

        // Draw divider
        yPos += HEADER_TEXT_SIZE + 10
        paint.color = Color.LTGRAY
        canvas.drawLine(MARGIN.toFloat(), yPos, PDF_WIDTH_PX - MARGIN.toFloat(), yPos, paint)
        yPos += 20

        // Report title
        paint.color = Color.BLACK
        paint.textSize = TITLE_TEXT_SIZE
        paint.isFakeBoldText = true
        val title = context.getString(R.string.report_title, crop.name)
        canvas.drawText(title, MARGIN.toFloat(), yPos, paint)
        yPos += LINE_HEIGHT + 10

        // Date and time
        paint.textSize = SMALL_TEXT_SIZE
        paint.isFakeBoldText = false
        val dateText = context.getString(
            R.string.report_date_time,
            dateTimeUtils.formatDateTime(result.timestamp)
        )
        canvas.drawText(dateText, MARGIN.toFloat(), yPos, paint)
        yPos += LINE_HEIGHT + 20

        // Draw image if available
        if (imageBitmap != null) {
            try {
                val imageWidth = PDF_WIDTH_PX - (2 * MARGIN)
                val aspectRatio = imageBitmap.width.toFloat() / imageBitmap.height.toFloat()
                val imageHeight = (imageWidth / aspectRatio).toInt()

                // Scale the bitmap to fit the page width
                val scaledBitmap = imageBitmap.scale(imageWidth, imageHeight)

                canvas.drawBitmap(
                    scaledBitmap,
                    MARGIN.toFloat(),
                    yPos,
                    null
                )

                yPos += imageHeight + 20

                // Don't recycle original bitmap as it might be used elsewhere
                if (scaledBitmap != imageBitmap) {
                    scaledBitmap.recycle()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error drawing image: ${e.message}")
                yPos += 20 // Add some space anyway
            }
        }

        // Identification result
        paint.isFakeBoldText = true
        paint.textSize = SUBTITLE_TEXT_SIZE
        canvas.drawText(context.getString(R.string.identification_result), MARGIN.toFloat(), yPos, paint)
        yPos += LINE_HEIGHT + 5

        // Problem name
        paint.isFakeBoldText = false
        paint.textSize = BODY_TEXT_SIZE
        canvas.drawText(
            context.getString(R.string.problem_name_label, result.problemName),
            MARGIN.toFloat(),
            yPos,
            paint
        )
        yPos += LINE_HEIGHT + 5

        // Severity
        val severityText = context.getString(
            R.string.severity_label,
            when (result.severity) {
                1 -> context.getString(R.string.severity_low)
                2 -> context.getString(R.string.severity_medium)
                3 -> context.getString(R.string.severity_high)
                else -> context.getString(R.string.unknown)
            }
        )
        canvas.drawText(severityText, MARGIN.toFloat(), yPos, paint)
        yPos += LINE_HEIGHT + 5

        // Confidence
        val confidenceText = context.getString(
            R.string.confidence_label,
            "${result.confidence.toInt()}%"
        )
        canvas.drawText(confidenceText, MARGIN.toFloat(), yPos, paint)
        yPos += LINE_HEIGHT + 20

        // Description section
        paint.isFakeBoldText = true
        paint.textSize = SUBTITLE_TEXT_SIZE
        canvas.drawText(context.getString(R.string.description_heading), MARGIN.toFloat(), yPos, paint)
        yPos += LINE_HEIGHT + 5

        // Description body
        paint.isFakeBoldText = false
        paint.textSize = BODY_TEXT_SIZE

        // Split description into lines to fit the page width
        val descriptionTextWidth = PDF_WIDTH_PX - (2 * MARGIN)
        val descriptionLines = splitTextIntoLines(result.description, descriptionTextWidth, paint)

        for (line in descriptionLines) {
            canvas.drawText(line, MARGIN.toFloat(), yPos, paint)
            yPos += LINE_HEIGHT
        }
        yPos += 15

        // Action plan section
        paint.isFakeBoldText = true
        paint.textSize = SUBTITLE_TEXT_SIZE
        canvas.drawText(context.getString(R.string.recommended_actions), MARGIN.toFloat(), yPos, paint)
        yPos += LINE_HEIGHT + 5

        // Action plan items
        paint.isFakeBoldText = false
        paint.textSize = BODY_TEXT_SIZE

        result.actions.forEachIndexed { index, action ->
            val bulletPoint = "\u2022 " // Bullet point symbol
            val actionText = "$bulletPoint ${action.description}"

            // Split action text into lines
            val actionLines = splitTextIntoLines(actionText, descriptionTextWidth, paint)

            // Draw first line with bullet point
            if (actionLines.isNotEmpty()) {
                canvas.drawText(actionLines[0], MARGIN.toFloat(), yPos, paint)
                yPos += LINE_HEIGHT

                // Draw remaining lines with proper indentation
                for (i in 1 until actionLines.size) {
                    canvas.drawText(
                        actionLines[i],
                        MARGIN.toFloat() + 15, // Indent wrapped lines
                        yPos,
                        paint
                    )
                    yPos += LINE_HEIGHT
                }
            }

            if (index < result.actions.size - 1) {
                yPos += 5 // Add space between action items
            }
        }

        yPos += 20

        // Footer
        paint.textSize = SMALL_TEXT_SIZE
        paint.color = Color.GRAY

        // Farmer info
        val farmerInfo = context.getString(R.string.farmer_info, user.displayName, user.mobileNumber)

        canvas.drawText(farmerInfo, MARGIN.toFloat(), PDF_HEIGHT_PX - MARGIN - 30f, paint)

        // Report ID and timestamp
        val reportInfo = context.getString(
            R.string.report_id_info,
            result.id,
            dateTimeUtils.formatDateTime(Date())
        )
        canvas.drawText(reportInfo, MARGIN.toFloat(), PDF_HEIGHT_PX - MARGIN - 15f, paint)
    }

    /**
     * Split a text string into lines that fit within a specific width
     * @param text Text to split
     * @param maxWidth Maximum width in pixels
     * @param paint Paint object used for measuring text
     * @return List of text lines
     */
    private fun splitTextIntoLines(text: String, maxWidth: Int, paint: Paint): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")

        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = paint.measureText(testLine)

            if (testWidth <= maxWidth) {
                currentLine = testLine
            } else {
                lines.add(currentLine)
                currentLine = word
            }
        }

        // Add the last line if not empty
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }

    /**
     * Generate a PDF report from a view
     * Useful for complex layouts that are difficult to draw programmatically
     * @param view View to convert to PDF
     * @param fileName Name of the PDF file
     * @return URI of the generated PDF file or null if generation failed
     */
    suspend fun generateReportFromView(view: View, fileName: String): Uri? = withContext(Dispatchers.IO) {
        try {
            // Measure and layout the view
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(PDF_WIDTH_PX, View.MeasureSpec.EXACTLY)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(widthMeasureSpec, heightMeasureSpec)

            val measuredWidth = view.measuredWidth
            val measuredHeight = view.measuredHeight

            view.layout(0, 0, measuredWidth, measuredHeight)

            // Create bitmap from view
            val bitmap = createBitmap(measuredWidth, measuredHeight)
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            // Create PDF
            val pdfDocument = PdfDocument()

            // Number of pages needed
            val totalPages = (measuredHeight + PDF_HEIGHT_PX - 1) / PDF_HEIGHT_PX

            for (pageNum in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PDF_WIDTH_PX, PDF_HEIGHT_PX, pageNum + 1).create()
                val page = pdfDocument.startPage(pageInfo)

                // Calculate what part of the bitmap to draw on this page
                val startY = pageNum * PDF_HEIGHT_PX
                val height = minOf(PDF_HEIGHT_PX, measuredHeight - startY)

                if (height > 0) {
                    val srcBitmap = Bitmap.createBitmap(bitmap, 0, startY, measuredWidth, height)
                    page.canvas.drawBitmap(srcBitmap, 0f, 0f, null)
                    srcBitmap.recycle()
                }

                pdfDocument.finishPage(page)
            }

            // Create file and write PDF
            val storageDir = File(context.filesDir, "reports")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val file = File(storageDir, "${fileName}.pdf")

            try {
                FileOutputStream(file).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }

                // Convert to content URI
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: IOException) {
                Log.e(TAG, "Error writing PDF: ${e.message}")
                null
            } finally {
                pdfDocument.close()
                bitmap.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating PDF from view: ${e.message}")
            null
        }
    }

}