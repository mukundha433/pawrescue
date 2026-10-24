package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AccountStatus
import com.example.model.ImageHotlinks
import com.example.model.UserAccount
import com.example.model.UserRole
import com.example.model.UserSession
import org.json.JSONArray
import org.json.JSONObject

object SessionManager {
    private const val PREFS_NAME = "pawrescue_secure_session_prefs"
    private const val KEY_ACTIVE_SESSION = "active_user_session"
    private const val KEY_ACCOUNTS_CACHE = "cached_accounts_list"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveActiveSession(context: Context, session: UserSession?) {
        val prefs = getPrefs(context)
        if (session == null) {
            prefs.edit().remove(KEY_ACTIVE_SESSION).apply()
            return
        }

        try {
            val json = JSONObject().apply {
                put("userId", session.userId)
                put("name", session.name)
                put("email", session.email)
                put("phone", session.phone)
                put("role", session.role.name)
                put("status", session.status.name)
                put("teamId", session.teamId ?: "")
                put("teamName", session.teamName ?: "")
                put("organizationId", session.organizationId ?: "")
                put("organizationName", session.organizationName ?: "")
                put("authToken", session.authToken)
                put("avatarUrl", session.avatarUrl)
            }
            prefs.edit().putString(KEY_ACTIVE_SESSION, json.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getActiveSession(context: Context): UserSession? {
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString(KEY_ACTIVE_SESSION, null) ?: return null

        return try {
            val json = JSONObject(jsonStr)
            val roleStr = json.optString("role", UserRole.CITIZEN.name)
            val role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.CITIZEN }

            val statusStr = json.optString("status", AccountStatus.ACTIVE.name)
            val status = AccountStatus.fromString(statusStr)

            // Strict authorization check: Only active/approved sessions can be restored
            if (!status.isApproved) {
                clearActiveSession(context)
                return null
            }

            UserSession(
                userId = json.optString("userId"),
                name = json.optString("name", "PawRescue User"),
                email = json.optString("email"),
                phone = json.optString("phone", ""),
                role = role,
                status = status,
                teamId = json.optString("teamId").takeIf { it.isNotEmpty() },
                teamName = json.optString("teamName").takeIf { it.isNotEmpty() },
                organizationId = json.optString("organizationId").takeIf { it.isNotEmpty() },
                organizationName = json.optString("organizationName").takeIf { it.isNotEmpty() },
                authToken = json.optString("authToken", ""),
                avatarUrl = json.optString("avatarUrl", ImageHotlinks.VOLUNTEER_PRIYA)
            )
        } catch (_: Exception) {
            null
        }
    }

    fun clearActiveSession(context: Context) {
        getPrefs(context).edit().remove(KEY_ACTIVE_SESSION).apply()
    }

    fun saveAccount(context: Context, account: UserAccount) {
        val accounts = getSavedAccounts(context).toMutableList()
        val index = accounts.indexOfFirst { it.id == account.id || it.identifier.equals(account.identifier, ignoreCase = true) }
        if (index >= 0) {
            accounts[index] = account
        } else {
            accounts.add(account)
        }

        try {
            val array = JSONArray()
            accounts.forEach { acc ->
                val obj = JSONObject().apply {
                    put("id", acc.id)
                    put("name", acc.name)
                    put("identifier", acc.identifier)
                    put("phone", acc.phone)
                    put("passwordHash", acc.passwordHash)
                    put("role", acc.role.name)
                    put("status", acc.status.name)
                    put("teamId", acc.teamId ?: "")
                    put("teamName", acc.teamName ?: "")
                    put("organizationId", acc.organizationId ?: "")
                    put("organizationName", acc.organizationName ?: "")
                    put("avatarUrl", acc.avatarUrl)
                }
                array.put(obj)
            }
            getPrefs(context).edit().putString(KEY_ACCOUNTS_CACHE, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getSavedAccounts(context: Context): List<UserAccount> {
        val jsonStr = getPrefs(context).getString(KEY_ACCOUNTS_CACHE, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<UserAccount>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val role = try { UserRole.valueOf(obj.optString("role")) } catch (_: Exception) { UserRole.CITIZEN }
                val status = AccountStatus.fromString(obj.optString("status"))
                list.add(
                    UserAccount(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        identifier = obj.optString("identifier"),
                        phone = obj.optString("phone"),
                        passwordHash = obj.optString("passwordHash"),
                        role = role,
                        status = status,
                        teamId = obj.optString("teamId").takeIf { it.isNotEmpty() },
                        teamName = obj.optString("teamName").takeIf { it.isNotEmpty() },
                        organizationId = obj.optString("organizationId").takeIf { it.isNotEmpty() },
                        organizationName = obj.optString("organizationName").takeIf { it.isNotEmpty() },
                        avatarUrl = obj.optString("avatarUrl", ImageHotlinks.VOLUNTEER_PRIYA)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}
