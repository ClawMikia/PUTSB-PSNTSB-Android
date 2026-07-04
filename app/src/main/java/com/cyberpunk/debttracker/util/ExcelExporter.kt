package com.cyberpunk.debttracker.util

import android.content.Context
import android.os.Environment
import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelExporter {

    fun exportDebtsToExcel(context: Context, debts: List<Debt>): File? {
        val workbook = XSSFWorkbook()
        
        val iOweDebts = debts.filter { it.debtType == DebtType.I_OWE }
        val owesMeDebts = debts.filter { it.debtType == DebtType.OWES_ME }

        createSheet(workbook, "I OWE", iOweDebts)
        createSheet(workbook, "OWES ME", owesMeDebts)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "DebtTracker_Export_$timeStamp.xlsx"
        
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, fileName)

        return try {
            val outputStream = FileOutputStream(file)
            workbook.write(outputStream)
            outputStream.close()
            workbook.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createSheet(workbook: XSSFWorkbook, sheetName: String, debts: List<Debt>) {
        val sheet = workbook.createSheet(sheetName)
        val headerRow = sheet.createRow(0)
        
        val headers = arrayOf("ID", "Person Name", "Amount", "Paid Amount", "Remaining", "Description", "Due Date", "Status", "Created At", "Archived")
        for ((index, header) in headers.withIndex()) {
            headerRow.createCell(index).setCellValue(header)
        }

        var rowNum = 1
        for (debt in debts) {
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(debt.id.toDouble())
            row.createCell(1).setCellValue(debt.personName)
            row.createCell(2).setCellValue(debt.amount)
            row.createCell(3).setCellValue(debt.paidAmount)
            row.createCell(4).setCellValue(debt.remaining)
            row.createCell(5).setCellValue(debt.description)
            row.createCell(6).setCellValue(debt.dueDate?.let { DateFormatter.formatDisplay(it) } ?: "N/A")
            row.createCell(7).setCellValue(debt.status.name)
            row.createCell(8).setCellValue(DateFormatter.formatDisplay(debt.createdAt))
            row.createCell(9).setCellValue(if (debt.isArchived) "Yes" else "No")
        }

        for (i in headers.indices) {
            sheet.autoSizeColumn(i)
        }
    }
}
