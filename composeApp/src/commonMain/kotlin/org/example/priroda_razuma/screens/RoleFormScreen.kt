package org.example.priroda_razuma.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.priroda_razuma.auth.AuthManager
import org.example.priroda_razuma.models.Role
import org.example.priroda_razuma.preferences.Theme

@Composable
fun RoleFormScreen(
    authManager: AuthManager,
    isEdit: Boolean = false,
    roleId: Int? = null,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isEdit, roleId) {
        if (isEdit && roleId != null) {
            isLoading = true
            try {
                val role = authManager.getRoleById(roleId)
                name = role.name
                description = role.description ?: ""
            } catch (e: Exception) {
                error = "Ошибка загрузки данных роли"
            } finally {
                isLoading = false
            }
        }
    }

    fun validateName(name: String): Boolean {
        val regex = Regex("^[a-zA-Zа-яА-ЯёЁ0-9\\s-]+$")
        return name.isNotBlank() && name.length in 3..255 && regex.matches(name)
    }

    fun handleSubmit() {
        if (!validateName(name)) {
            error = "Название роли должно быть от 3 до 255 символов и содержать только буквы, цифры, пробелы и дефисы"
            return
        }
        coroutineScope.launch {
            isLoading = true
            try {
                if (isEdit && roleId != null) {
                    val updatedRole = Role(roleId, name, description)
                    val success = authManager.updateRole(roleId, updatedRole)
                    if (success) onNavigateBack()
                    else error = "Ошибка при обновлении роли"
                } else {
                    val newRole = Role(0, name, description)
                    val success = authManager.createRole(newRole)
                    if (success) onNavigateBack()
                    else error = "Ошибка при создании роли"
                }
            } catch (e: Exception) {
                error = e.message ?: "Неизвестная ошибка"
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(20.dp)
                .shadow(8.dp, RoundedCornerShape(15.dp))
                .background(Color.White),
            backgroundColor = Color.White,
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEdit) "Редактирование роли" else "Создание новой роли",
                    fontFamily = Theme.fonts.nunito,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = Color(0xFF2C3E50),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(3.dp)
                        .background(Color(0xFFD3E29F))
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (error != null) {
                    AlertDialog(
                        onDismissRequest = { error = null },
                        title = { Text("Ошибка") },
                        text = { Text(error ?: "") },
                        confirmButton = {
                            Button(onClick = { error = null }) {
                                Text("OK")
                            }
                        },
                        backgroundColor = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название роли", color = Color(0xFF34495E)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFA3F49F),
                        unfocusedBorderColor = Color(0xFFCED4DA)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание", color = Color(0xFF34495E)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    maxLines = 4,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFA3F49F),
                        unfocusedBorderColor = Color(0xFFCED4DA)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onNavigateBack,
                        enabled = !isLoading,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6C757D))
                    ) {
                        Text("Отмена", fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { handleSubmit() },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFD3E29F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Сохранить", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}