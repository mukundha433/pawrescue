package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.AccountStatus
import com.example.model.ImageHotlinks
import com.example.model.UserAccount
import com.example.model.UserRole
import com.example.model.UserSession
import org.json.JSONArray
import org.json.JSONObject

object SessionManager {
    private const val TAG = "SessionManager"
    private const val PREFS_NAME = "pawrescue_auth_prefs"

    // Active session keys
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_PHONE = "user_phone"
    private const val KEY_ROLE = "user_role"
    private const val KEY_STATUS = "user_status"
    private const val KEY_TEAM_ID = "team_id"
    private const val KEY_TEAM_NAME = "team_name"
    private const val KEY_ORG_ID = "org_id"
    private const val KEY_ORG_NAME = "org_name"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_AVATAR_URL = "avatar_url"

    // Persistent registered accounts key
    private const val KEY_REGISTERED_ACCOUNTS_JSON = "persistent_registered_accounts_json"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Save the currently authenticated session to persistent SharedPreferences
    fun saveActiveSession(context: Context, session: UserSession) {
        try {
            getPrefs(context).edit().apply {
                putBoolean(KEY_IS_LOGGED_IN, true)
                putString(KEY_USER_ID, session.userId)
                putString(KEY_NAME, session.name)
                putString(KEY_EMAIL, session.email)
                putString(KEY_PHONE, session.phone)
                putString(KEY_ROLE, session.role.name)
                putString(KEY_STATUS, session.status.name)
                putString(KEY_TEAM_ID, session.teamId)
                putString(KEY_TEAM_NAME, session.teamName)
                putString(KEY_ORG_ID, session.organizationId)
                putString(KEY_ORG_NAME, session.organizationName)
                putString(KEY_AUTH_TOKEN, session.authToken)
                putString(KEY_AVATAR_URL, session.avatarUrl)
                apply()
            }
            Log.d(TAG, "Active session persisted for ${session.email} [${session.role.name}]")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving active session", e)
        }
    }

    // Restore the active session on application startup
    fun getActiveSession(context: Context): UserSession? {
        try {
            val prefs = getPrefs(context)
            if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
                return null
            }

            val uid = prefs.getString(KEY_USER_ID, null) ?: return null
            val roleStr = prefs.getString(KEY_ROLE, UserRole.CITIZEN.name) ?: UserRole.CITIZEN.name
            val role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.CITIZEN }
            val statusStr = prefs.getString(KEY_STATUS, AccountStatus.ACTIVE.name) ?: AccountStatus.ACTIVE.name
            val status = AccountStatus.fromString(statusStr)
            if (!status.isApproved) {
                Log.w(TAG, "Rejecting cached session with non-approved status: $status")
                return null
            }

            val email = prefs.getString(KEY_EMAIL, "") ?: ""
            val name = prefs.getString(KEY_NAME, if (email.isNotEmpty()) email.substringBefore("@") else "PawRescue User") ?: "PawRescue User"
            val phone = prefs.getString(KEY_PHONE, "") ?: ""
            val teamId = prefs.getString(KEY_TEAM_ID, null)
            val teamName = prefs.getString(KEY_TEAM_NAME, null)
            val orgId = prefs.getString(KEY_ORG_ID, null)
            val orgName = prefs.getString(KEY_ORG_NAME, null)
            val authToken = prefs.getString(KEY_AUTH_TOKEN, "AUTH-$uid") ?: "AUTH-$uid"
            val avatarUrl = prefs.getString(KEY_AVATAR_URL, if (role == UserRole.RESCUE_TEAM) ImageHotlinks.RESCUE_VAN_CREW else ImageHotlinks.VOLUNTEER_PRIYA)
                ?: ImageHotlinks.VOLUNTEER_PRIYA

            return UserSession(
                userId = uid,
                name = name,
                email = email,
                phone = phone,
                role = role,
                status = status,
                teamId = teamId,
                teamName = teamName,
                organizationId = orgId,
                organizationName = orgName,
                authToken = authToken,
                avatarUrl = avatarUrl
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring active session", e)
            return null
        }
    }

    // Clear active session when the user explicitly clicks Logout
    fun clearActiveSession(context: Context) {
        try {
            getPrefs(context).edit().apply {
                remove(KEY_IS_LOGGED_IN)
                remove(KEY_USER_ID)
                remove(KEY_NAME)
                remove(KEY_EMAIL)
                remove(KEY_PHONE)
                remove(KEY_ROLE)
                remove(KEY_STATUS)
                remove(KEY_TEAM_ID)
                remove(KEY_TEAM_NAME)
                remove(KEY_ORG_ID)
                remove(KEY_ORG_NAME)
                remove(KEY_AUTH_TOKEN)
                remove(KEY_AVATAR_URL)
                apply()
            }
            Log.d(TAG, "Active session cleared from SharedPreferences")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing active session", e)
        }
    }

    // Persist a newly registered or updated user account so it survives app restarts
    fun saveAccount(context: Context, account: UserAccount) {
        try {
            val currentAccounts = loadSavedAccounts(context).toMutableList()
            val existingIdx = currentAccounts.indexOfFirst {
                it.id == account.id || it.identifier.equals(account.identifier, ignoreCase = true)
            }
            if (existingIdx >= 0) {
                currentAccounts[existingIdx] = account
            } else {
                currentAccounts.add(account)
            }

            val jsonArray = JSONArray()
            for (acc in currentAccounts) {
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
                jsonArray.put(obj)
            }

            getPrefs(context).edit().putString(KEY_REGISTERED_ACCOUNTS_JSON, jsonArray.toString()).apply()
            Log.d(TAG, "Account ${account.identifier} permanently saved to device storage")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving account to persistent storage", e)
        }
    }

    // Load all accounts that have been registered on this device
    fun loadSavedAccounts(context: Context): List<UserAccount> {
        try {
            val jsonStr = getPrefs(context).getString(KEY_REGISTERED_ACCOUNTS_JSON, null) ?: return emptyList()
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<UserAccount>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val roleStr = obj.optString("role", UserRole.CITIZEN.name)
                val role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.CITIZEN }
                val statusStr = obj.optString("status", AccountStatus.ACTIVE.name)
                val status = try { AccountStatus.valueOf(statusStr) } catch (_: Exception) { AccountStatus.ACTIVE }

                list.add(
                    UserAccount(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        identifier = obj.getString("identifier"),
                        phone = obj.optString("phone", ""),
                        passwordHash = obj.optString("passwordHash", ""),
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
            return list
        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved accounts", e)
            return emptyList()
        }
    }
}
