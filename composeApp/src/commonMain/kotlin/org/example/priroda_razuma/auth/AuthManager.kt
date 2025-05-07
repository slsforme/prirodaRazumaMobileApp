package org.example.priroda_razuma.auth

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.example.priroda_razuma.preferences.Configuration
import org.example.priroda_razuma.auth.responses.BaseResponse
import org.example.priroda_razuma.auth.responses.TokenResponse
import org.example.priroda_razuma.models.CreateUserRequest
import org.example.priroda_razuma.models.Document
import org.example.priroda_razuma.models.Patient
import org.example.priroda_razuma.models.User
import org.example.priroda_razuma.models.Role
import org.example.priroda_razuma.models.UpdateUserRequest

class AuthManager(private val client: HttpClient) {
    private var _accessToken: String? = null
    private var _refreshToken: String? = null
    private var _userId: Int? = null
    private var _login: String? = null
    private var _userFio: String? = null
    private var _roleName: String? = null
    private var _roleId: Int? = null
    private var _email: String? = null
    private var _photoUrl: String? = null

    val accessToken: String? get() = _accessToken
    val userId: Int? get() = _userId
    val roleId: Int? get() = _roleId
    val userFio: String? get() = _userFio
    val roleName: String? get() = _roleName
    val email: String? get() = _email
    val photoUrl: String? get() = _photoUrl

    suspend fun login(username: String, password: String): Result<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val response = client.post("${Configuration.BASE_API_URL}/auth/login") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("username=$username&password=$password")
            }

            val tokenResponse = handleResponse(response)

            _accessToken = tokenResponse.access_token
            _refreshToken = tokenResponse.refresh_token
            _userId = tokenResponse.user_id
            _login = username

            try {
                val userResponse = getUserById(tokenResponse.user_id)
                _userFio = userResponse.fio
                _email = userResponse.login
                _roleId = userResponse.role_id
                _photoUrl = null

                val roleResponse = getRoleById(userResponse.role_id)
                _roleName = roleResponse.name
            } catch (e: Exception) {
                println("Ошибка при получении дополнительных данных: ${e.message}")
            }

            Result.success(tokenResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val response = client.post("${Configuration.BASE_API_URL}/auth/refresh") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("refresh_token=$refreshToken")
            }
            val tokenResponse = handleResponse(response)
            _accessToken = tokenResponse.access_token
            _refreshToken = tokenResponse.refresh_token
            Result.success(tokenResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: Int): User = withContext(Dispatchers.IO) {
        val response = client.get("${Configuration.BASE_API_URL}/users/$userId") {
            _accessToken?.let { headers.append("Authorization", "Bearer $it") }
        }
        response.body<User>()
    }

    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        val response = client.get("${Configuration.BASE_API_URL}/users") {
            _accessToken?.let { headers.append("Authorization", "Bearer $it") }
        }
        response.body<List<User>>()
    }

    suspend fun createUser(user: CreateUserRequest): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.post("${Configuration.BASE_API_URL}/users") {
                contentType(ContentType.Application.Json)
                setBody(user)
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            Logger.e("Error creating user: ${e.message}")
            false
        }
    }

    suspend fun updateUser(
        userId: Int,
        data: UpdateUserRequest
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.put("${Configuration.BASE_API_URL}/users/$userId") {
                contentType(ContentType.Application.Json)
                setBody(data)
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            Logger.e("Error updating user: ${e.message}")
            false
        }
    }

    suspend fun updateUserPassword(userId: Int, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.put("${Configuration.BASE_API_URL}/users/$userId") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("password" to password))
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            Logger.e("Error updating user: ${e.message}")
            false
        }
    }

    suspend fun deleteUser(userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.delete("${Configuration.BASE_API_URL}/users/$userId") {
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            Logger.e("Error deleting user: ${e.message}")
            false
        }
    }

    suspend fun getRoleById(roleId: Int): Role = withContext(Dispatchers.IO) {
        val response = client.get("${Configuration.BASE_API_URL}/roles/$roleId") {
            _accessToken?.let { headers.append("Authorization", "Bearer $it") }
        }
        response.body<Role>()
    }

    suspend fun getAllRoles(): List<Role> = withContext(Dispatchers.IO) {
        val response = client.get("${Configuration.BASE_API_URL}/roles") {
            _accessToken?.let { headers.append("Authorization", "Bearer $it") }
        }
        response.body<List<Role>>()
    }

    suspend fun deleteRole(roleId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.delete("${Configuration.BASE_API_URL}/roles/$roleId") {
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createRole(role: Role): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.post("${Configuration.BASE_API_URL}/roles") {
                contentType(ContentType.Application.Json)
                setBody(role)
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateRole(roleId: Int, role: Role): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.put("${Configuration.BASE_API_URL}/roles/$roleId") {
                contentType(ContentType.Application.Json)
                setBody(role)
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    suspend fun uploadUserPhoto(userId: Int, photoBytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.post("${Configuration.BASE_API_URL}/users/$userId/photo") {
                contentType(ContentType.MultiPart.FormData)
                setBody(MultiPartFormDataContent(formData {
                    append(
                        "photo",
                        photoBytes,
                        Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"user_photo.jpg\"")
                        }
                    )
                }))
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            println("Error uploading photo: ${e.message}")
            false
        }
    }

    suspend fun getUserPhoto(userId: Int): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val response = client.get("${Configuration.BASE_API_URL}/users/$userId/photo") {
                _accessToken?.let {
                    headers.append(HttpHeaders.Authorization, "Bearer $it")
                }
            }

            return@withContext when (response.status) {
                HttpStatusCode.OK -> response.body<ByteArray>()
                HttpStatusCode.NotFound -> null
                else -> {
                    println("Unexpected response code: ${response.status}")
                    null
                }
            }
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                return@withContext null
            }
            println("Request failed: ${e.message}")
            null
        } catch (e: Exception) {
            println("Error fetching photo: ${e.message}")
            null
        }
    }

    suspend fun updateUserPassword(userId: Int, oldPassword: String, newPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.put("${Configuration.BASE_API_URL}/users/$userId/password") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "old_password" to oldPassword,
                    "new_password" to newPassword
                ))
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun handleResponse(response: HttpResponse): TokenResponse {
        return when (response.status.value) {
            in 200..299 -> response.body<TokenResponse>()
            else -> {
                val errorResponse = response.body<BaseResponse>()
                throw ClientRequestException(
                    response,
                    "Ошибка ${response.status}: ${errorResponse.detail}"
                )
            }
        }
    }

    suspend fun getAllPatients(): List<Patient> = withContext(Dispatchers.IO) {
        val response = client.get("${Configuration.BASE_API_URL}/patients") {
            _accessToken?.let { headers.append("Authorization", "Bearer $it") }
        }
        response.body<List<Patient>>()
    }

    suspend fun createPatient(patient: Patient): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.post("${Configuration.BASE_API_URL}/patients") {
                contentType(ContentType.Application.Json)
                setBody(patient)
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updatePatient(patientId: Int, patient: Patient): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.put("${Configuration.BASE_API_URL}/patients/$patientId") {
                contentType(ContentType.Application.Json)
                setBody(patient)
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getPatientById(patientId: Int): Patient = withContext(Dispatchers.IO) {
        val response = client.get("${Configuration.BASE_API_URL}/patients/$patientId") {
            _accessToken?.let { headers.append("Authorization", "Bearer $it") }
        }
        response.body<Patient>()
    }

    suspend fun deletePatient(patientId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.delete("${Configuration.BASE_API_URL}/patients/$patientId") {
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllDocuments(): List<Document> = withContext(Dispatchers.IO) {
        val response = client.get("${Configuration.BASE_API_URL}/documents") {
            _accessToken?.let { headers.append("Authorization", "Bearer $it") }
        }
        response.body<List<Document>>()
    }

    suspend fun getDocumentById(documentId: Int): Document = withContext(Dispatchers.IO) {
        val response = client.get("${Configuration.BASE_API_URL}/documents/$documentId") {
            _accessToken?.let { headers.append("Authorization", "Bearer $it") }
        }
        response.body<Document>()
    }

    suspend fun deleteDocument(documentId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.delete("${Configuration.BASE_API_URL}/documents/$documentId") {
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            println("Error deleting document: ${e.message}")
            false
        }
    }


    suspend fun deleteUserPhoto(userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.delete("${Configuration.BASE_API_URL}/users/$userId/photo") {
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            println("Error deleting user's photo: ${e.message}")
            false
        }
    }

    suspend fun downloadDocument(documentId: Int): ByteArray = withContext(Dispatchers.IO) {
        val response = client.get("${Configuration.BASE_API_URL}/documents/$documentId/download") {
            _accessToken?.let { headers.append("Authorization", "Bearer $it") }
        }
        response.body()
    }

    suspend fun downloadDocumentWithFilename(documentId: Int): Pair<ByteArray, String> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("${Configuration.BASE_API_URL}/documents/$documentId/download") {
                _accessToken?.let { headers.append("Authorization", "Bearer $it") }
            }

            val contentDisposition = response.headers["Content-Disposition"]
            var filename = "download"

            if (contentDisposition != null) {
                val filenameMatch = Regex("filename=(.*)").find(contentDisposition)
                if (filenameMatch != null && filenameMatch.groupValues.size > 1) {
                    filename = filenameMatch.groupValues[1]
                    if (filename.startsWith("\"") && filename.endsWith("\"")) {
                        filename = filename.substring(1, filename.length - 1)
                    }
                    filename = filename.replace("+", " ")
                        .replace("%([0-9A-Fa-f]{2})".toRegex()) { matchResult ->
                            val hexValue = matchResult.groupValues[1]
                            hexValue.toInt(16).toChar().toString()
                        }
                }
            }

            Pair(response.body<ByteArray>(), filename)
        } catch (e: Exception) {
            println("Error downloading document: ${e.message}")
            throw e
        }
    }

    fun logout() {
        _accessToken = null
        _refreshToken = null
        _userId = null
        _login = null
        _userFio = null
        _roleName = null
        _roleId = null
        _email = null
        _photoUrl = null
    }

    fun isAuthenticated(): Boolean {
        return _accessToken != null
    }
}