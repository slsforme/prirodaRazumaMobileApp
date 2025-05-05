package org.example.priroda_razuma

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.priroda_razuma.auth.AuthManager
import org.example.priroda_razuma.auth.responses.TokenResponse
import org.example.priroda_razuma.screens.DocumentListScreen
import org.example.priroda_razuma.screens.PatientFormScreen
import org.example.priroda_razuma.screens.PatientListScreen
import org.example.priroda_razuma.screens.RoleFormScreen
import org.example.priroda_razuma.screens.RoleListScreen
import org.example.priroda_razuma.screens.UserListScreen
import org.example.priroda_razuma.ui.components.SideBar
import org.example.priroda_razuma.ui.screens.*
import org.jetbrains.compose.ui.tooling.preview.Preview

private val PaleGreen = Color(0xFFE8F5E9)

@Composable
@Preview
fun App() {
    val httpClient = remember {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    val authManager = remember { AuthManager(httpClient) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var tokenResponse by remember { mutableStateOf<TokenResponse?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }

    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }
    var isSidebarVisible by remember { mutableStateOf(true) }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isAuthenticated && tokenResponse != null) {
                val roleId = authManager.roleId ?: 0

                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = { isSidebarVisible = !isSidebarVisible }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Переключить сайдбар"
                            )
                        }
                    }

                    when (currentScreen) {
                        Screen.Dashboard -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = PaleGreen.copy(alpha = 0.5f),
                                elevation = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Text(
                                        "Добро пожаловать в систему «Природа Разума»",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "ID пользователя: ${tokenResponse?.user_id}",
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "ФИО: ${authManager.userFio ?: "Загрузка..."}",
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Роль: ${authManager.roleName ?: if (roleId == 1) "Администратор" else "Специалист"}",
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                        Screen.Users -> {
                            var isCreatingUser by remember { mutableStateOf(false) }
                            var editingUserId by remember { mutableStateOf<Int?>(null) }

                            if (isCreatingUser) {
                                PatientFormScreen(
                                    authManager = authManager,
                                    isEdit = false,
                                    onNavigateBack = { isCreatingUser = false }
                                )
                            } else if (editingUserId != null) {
                                PatientFormScreen(
                                    authManager = authManager,
                                    isEdit = true,
                                    patientId = editingUserId,
                                    onNavigateBack = { editingUserId = null }
                                )
                            } else {
                                UserListScreen(
                                    authManager = authManager,
                                    onNavigateToCreateUser = { isCreatingUser = true },
                                    onNavigateToEditUser = {}
                                    // onNavigateToEditPatient = { roleId -> editingPatientId = roleId }
                                )
                            }
                        }
                        Screen.Patients -> {
                                var isCreatingPatient by remember { mutableStateOf(false) }
                                var editingPatientId by remember { mutableStateOf<Int?>(null) }

                                if (isCreatingPatient) {
                                    PatientFormScreen(
                                        authManager = authManager,
                                        isEdit = false,
                                        onNavigateBack = { isCreatingPatient = false }
                                    )
                                } else if (editingPatientId != null) {
                                    PatientFormScreen(
                                        authManager = authManager,
                                        isEdit = true,
                                        patientId = editingPatientId,
                                        onNavigateBack = { editingPatientId = null }
                                    )
                                } else {
                                    PatientListScreen(
                                        authManager = authManager,
                                        onNavigateToCreatePatient = { isCreatingPatient = true },
                                        onNavigateToEditPatient = { roleId -> editingPatientId = roleId }
                                    )
                                }
                        }
                        Screen.Documents -> {
                            var isCreatingDocument by remember { mutableStateOf(false) }
                            var editingDocumentId by remember { mutableStateOf<Int?>(null) }

                            if (isCreatingDocument) {
                                PatientFormScreen(
                                    authManager = authManager,
                                    isEdit = false,
                                    onNavigateBack = { isCreatingDocument = false }
                                )
                            } else if (editingDocumentId != null) {
                                PatientFormScreen(
                                    authManager = authManager,
                                    isEdit = true,
                                    patientId = editingDocumentId,
                                    onNavigateBack = { editingDocumentId = null }
                                )
                            } else {
                                DocumentListScreen(
                                    authManager = authManager,
                                    onNavigateToCreateDocument = { isCreatingDocument = true },
                                    onNavigateToEditDocument = {}
                                    // onNavigateToEditPatient = { roleId -> editingDocumentId = roleId }
                                )
                            }
                        }
                        Screen.Roles -> {
                            if (roleId == 1) {
                                var isCreatingRole by remember { mutableStateOf(false) }
                                var editingRoleId by remember { mutableStateOf<Int?>(null) }

                                if (isCreatingRole) {
                                    RoleFormScreen(
                                        authManager = authManager,
                                        isEdit = false,
                                        onNavigateBack = { isCreatingRole = false }
                                    )
                                } else if (editingRoleId != null) {
                                    RoleFormScreen(
                                        authManager = authManager,
                                        isEdit = true,
                                        roleId = editingRoleId,
                                        onNavigateBack = { editingRoleId = null }
                                    )
                                } else {
                                    RoleListScreen(
                                        authManager = authManager,
                                        onNavigateToCreateRole = { isCreatingRole = true },
                                        onNavigateToEditRole = { roleId -> editingRoleId = roleId }
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        "У вас нет доступа к управлению ролями",
                                        fontSize = 18.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                        Screen.Profile -> {
                            ProfileScreen(
                                authManager = authManager,
                                onLogout = {
                                    authManager.logout()
                                    isAuthenticated = false
                                    tokenResponse = null
                                },
                                openDrawer = { isSidebarVisible = !isSidebarVisible }
                            )
                        }
                    }
                }

                SideBar(
                    roleId = roleId,
                    onNavigateToUsers = { currentScreen = Screen.Users },
                    onNavigateToPatients = { currentScreen = Screen.Patients },
                    onNavigateToDocuments = { currentScreen = Screen.Documents },
                    onNavigateToRoles = { currentScreen = Screen.Roles },
                    onNavigateToProfile = { currentScreen = Screen.Profile },
                    onLogout = {
                        authManager.logout()
                        isAuthenticated = false
                        tokenResponse = null
                    },
                    isVisible = isSidebarVisible,
                    onToggleVisibility = { isSidebarVisible = !isSidebarVisible }
                )
            } else {
                AuthScreen(
                    authManager = authManager,
                    onLoginSuccess = { response ->
                        tokenResponse = response
                        isAuthenticated = true
                        authError = null
                    },
                    onAuthError = { error ->
                        authError = error
                    }
                )
            }

            authError?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    backgroundColor = Color(0xFFD32F2F),
                    contentColor = Color.White,
                    action = {
                        IconButton(
                            onClick = { authError = null },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Закрыть",
                                tint = Color.White
                            )
                        }
                    }
                ) {
                    Text(text = error)
                }
            }
        }
    }
}

enum class Screen {
    Dashboard,
    Users,
    Patients,
    Documents,
    Roles,
    Profile
}