package com.cyberpunk.debttracker.util

import com.cyberpunk.debttracker.data.model.Debt
import com.cyberpunk.debttracker.data.model.DebtStatus
import com.cyberpunk.debttracker.data.model.DebtType
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object JsonBackupUtil {

    private const val KEY_VERSION = "version"
    private const val KEY_EXPORTED_AT = "exportedAt"
    private const val KEY_DEBTS = "debts"

    private const val KEY_ID = "id"
    private const val KEY_PERSON_NAME = "personName"
    private const val KEY_AMOUNT = "amount"
    private const val KEY_PAID_AMOUNT = "paidAmount"
    private const val KEY_DESCRIPTION = "description"
    private const val KEY_DUE_DATE = "dueDate"
    private const val KEY_DEBT_TYPE = "debtType"
    private const val KEY_STATUS = "status"
    private const val KEY_CREATED_AT = "createdAt"
    private const val KEY_UPDATED_AT = "updatedAt"
    private const val KEY_IS_ARCHIVED = "isArchived"

    fun getDefaultFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "DebtTracker_Backup_$timeStamp.json"
    }

    fun exportDebtsToJson(outputStream: OutputStream, debts: List<Debt>): Boolean {
        return try {
            val root = JSONObject()
                .put(KEY_VERSION, 1)
                .put(KEY_EXPORTED_AT, System.currentTimeMillis())

            val array = JSONArray()
            debts.forEach { debt ->
                array.put(toJson(debt))
            }
            root.put(KEY_DEBTS, array)

            outputStream.write(root.toString().toByteArray(Charsets.UTF_8))
            outputStream.flush()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun parseDebts(inputStream: InputStream): List<Debt>? {
        return try {
            val text = inputStream.readBytes().toString(Charsets.UTF_8)
            parseDebts(text)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun parseDebts(text: String): List<Debt>? {
        return try {
            val root = JSONObject(text)
            val array = root.getJSONArray(KEY_DEBTS)
            (0 until array.length()).map { index -> fromJson(array.getJSONObject(index)) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun toJson(debt: Debt): JSONObject {
        val json = JSONObject()
            .put(KEY_ID, debt.id)
            .put(KEY_PERSON_NAME, debt.personName)
            .put(KEY_AMOUNT, debt.amount)
            .put(KEY_PAID_AMOUNT, debt.paidAmount)
            .put(KEY_DESCRIPTION, debt.description)
            .put(KEY_DEBT_TYPE, debt.debtType.name)
            .put(KEY_STATUS, debt.status.name)
            .put(KEY_CREATED_AT, debt.createdAt)
            .put(KEY_UPDATED_AT, debt.updatedAt)
            .put(KEY_IS_ARCHIVED, debt.isArchived)

        if (debt.dueDate != null) {
            json.put(KEY_DUE_DATE, debt.dueDate)
        } else {
            json.put(KEY_DUE_DATE, JSONObject.NULL)
        }
        return json
    }

    private fun fromJson(json: JSONObject): Debt {
        val dueDate = if (json.isNull(KEY_DUE_DATE)) null else json.optLong(KEY_DUE_DATE)
        return Debt(
            id = json.optLong(KEY_ID, 0L),
            personName = json.optString(KEY_PERSON_NAME),
            amount = json.optDouble(KEY_AMOUNT, 0.0),
            paidAmount = json.optDouble(KEY_PAID_AMOUNT, 0.0),
            description = json.optString(KEY_DESCRIPTION),
            dueDate = dueDate,
            debtType = runCatching { DebtType.valueOf(json.optString(KEY_DEBT_TYPE)) }
                .getOrDefault(DebtType.OWES_ME),
            status = runCatching { DebtStatus.valueOf(json.optString(KEY_STATUS)) }
                .getOrDefault(DebtStatus.ACTIVE),
            createdAt = json.optLong(KEY_CREATED_AT, System.currentTimeMillis()),
            updatedAt = json.optLong(KEY_UPDATED_AT, System.currentTimeMillis()),
            isArchived = json.optBoolean(KEY_IS_ARCHIVED),
        )
    }
}
