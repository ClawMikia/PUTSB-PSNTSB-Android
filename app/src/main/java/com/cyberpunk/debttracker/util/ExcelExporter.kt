package com.cyberpunk.debttracker.util

import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelExporter {

    fun getDefaultFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "DebtTracker_Export_$timeStamp.xlsx"
    }

    fun exportDebtsToExcel(outputStream: OutputStream, debts: List<Debt>): Boolean {
        val workbook = XSSFWorkbook()

        val iOweDebts = debts.filter { it.debtType == DebtType.I_OWE }
        val owesMeDebts = debts.filter { it.debtType == DebtType.OWES_ME }

        createSheet(workbook, "I OWE", iOweDebts)
        createSheet(workbook, "OWES ME", owesMeDebts)

        return try {
            workbook.write(outputStream)
            outputStream.close()
            workbook.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
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


    }
}
