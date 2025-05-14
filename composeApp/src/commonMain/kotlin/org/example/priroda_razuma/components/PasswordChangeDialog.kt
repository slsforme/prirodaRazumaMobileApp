package org.example.priroda_razuma.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.priroda_razuma.auth.AuthManager
import org.example.priroda_razuma.preferences.Theme
import org.jetbrains.compose.resources.painterResource
import prirodarazumamobile.composeapp.generated.resources.Res
import prirodarazumamobile.composeapp.generated.resources.eye
import prirodarazumamobile.composeapp.generated.resources.hidden

private val PrimaryColor = Color(0xFF2E7D32)
private val SecondaryColor = Color(0xFFD3E29F)
private val TextPrimaryColor = Color(0xFF2c3e50)
private val TextSecondaryColor = Color(0xFF666666)
private val DividerColor = Color(0xFFBDBDBD)
private val ErrorColor = Color(0xFFE53935)
private val ErrorBackgroundColor = Color(0xFFFFDDDD)

@Composable
fun PasswordChangeDialog(
    authManager: AuthManager,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    var errors by remember {
        mutableStateOf(
            mapOf(
                "oldPassword" to "",
                "newPassword" to "",
                "confirmPassword" to ""
            )
        )
    }

    val oldPasswordFocusRequester = remember { FocusRequester() }

    val passwordRegex = remember { Regex("^[a-zA-Z0-9!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+\$") }
    val minLength = 5
    val maxLength = 50

    val validateField = { name: String, value: String ->
        when (name) {
            "oldPassword" -> if (value.isEmpty()) "Обязательное поле" else ""
            "newPassword" -> when {
                value.isEmpty() -> "Обязательное поле"
                value.length < minLength || value.length > maxLength ->
                    "Длина пароля должна быть от $minLength до $maxLength символов"
                !passwordRegex.matches(value) -> "Недопустимые символы в пароле"
                else -> ""
            }
            "confirmPassword" -> if (value != newPassword) "Пароли не совпадают" else ""
            else -> ""
        }
    }

    val updateErrors = { name: String, value: String ->
        val error = validateField(name, value)
        errors = errors + (name to error)
    }

    val handleSubmit = {
        val newErrors = mapOf(
            "oldPassword" to validateField("oldPassword", oldPassword),
            "newPassword" to validateField("newPassword", newPassword),
            "confirmPassword" to validateField("confirmPassword", confirmPassword)
        )

        errors = newErrors

        if (newErrors.values.all { it.isEmpty() }) {
            coroutineScope.launch {
                isLoading = true
                showToast = false

                val userId = authManager.userId ?: 0
                val updateResult = authManager.updateUserPassword(
                    userId,
                    newPassword
                )

                isLoading = false

                if (updateResult) {
                    toastMessage = "Пароль успешно изменен"
                    isSuccess = true
                    showToast = true

                    oldPassword = ""
                    newPassword = ""
                    confirmPassword = ""

                    delay(3000)
                    onDismiss()
                } else {
                    toastMessage = "Произошла ошибка при смене пароля"
                    isSuccess = false
                    showToast = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            oldPasswordFocusRequester.requestFocus()
        } catch (e: Exception) { }
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            backgroundColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Изменить пароль",
                        tint = PrimaryColor,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Изменение пароля",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Theme.fonts.nunito,
                        color = TextPrimaryColor,
                        modifier = Modifier.weight(1f)
                    )

                    if (!isLoading) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть",
                                tint = TextSecondaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(
                    color = DividerColor.copy(alpha = 0.5f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = {
                        oldPassword = it
                        updateErrors("oldPassword", it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(oldPasswordFocusRequester),
                    label = { Text("Текущий пароль") },
                    placeholder = { Text("Введите текущий пароль") },
                    visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors["oldPassword"]?.isNotEmpty() == true,
                    enabled = !isLoading,
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (oldPasswordVisible) Res.drawable.hidden else Res.drawable.eye
                                ),
                                contentDescription = if (oldPasswordVisible) "Скрыть пароль" else "Показать пароль",
                                tint = if (oldPasswordVisible) PrimaryColor else TextSecondaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = DividerColor,
                        errorBorderColor = ErrorColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )

                if (errors["oldPassword"]?.isNotEmpty() == true) {
                    Text(
                        text = errors["oldPassword"] ?: "",
                        color = ErrorColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        updateErrors("newPassword", it)
                        if (confirmPassword.isNotEmpty()) {
                            updateErrors("confirmPassword", confirmPassword)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Новый пароль") },
                    placeholder = { Text("Введите новый пароль") },
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors["newPassword"]?.isNotEmpty() == true,
                    enabled = !isLoading,
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (newPasswordVisible) Res.drawable.hidden else Res.drawable.eye
                                ),
                                contentDescription = if (newPasswordVisible) "Скрыть пароль" else "Показать пароль",
                                tint = if (newPasswordVisible) PrimaryColor else TextSecondaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = DividerColor,
                        errorBorderColor = ErrorColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )

                if (errors["newPassword"]?.isNotEmpty() == true) {
                    Text(
                        text = errors["newPassword"] ?: "",
                        color = ErrorColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        updateErrors("confirmPassword", it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Подтверждение пароля") },
                    placeholder = { Text("Повторите новый пароль") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    isError = errors["confirmPassword"]?.isNotEmpty() == true,
                    enabled = !isLoading,
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (confirmPasswordVisible) Res.drawable.hidden else Res.drawable.eye
                                ),
                                contentDescription = if (confirmPasswordVisible) "Скрыть пароль" else "Показать пароль",
                                tint = if (confirmPasswordVisible) PrimaryColor else TextSecondaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = DividerColor,
                        errorBorderColor = ErrorColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )

                if (errors["confirmPassword"]?.isNotEmpty() == true) {
                    Text(
                        text = errors["confirmPassword"] ?: "",
                        color = ErrorColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isLoading) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = TextSecondaryColor
                            )
                        ) {
                            Text(
                                text = "Отмена",
                                fontSize = 13.sp,
                                fontFamily = Theme.fonts.nunito,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Button(
                        onClick = { handleSubmit() },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryColor,
                            contentColor = Color.White,
                            disabledBackgroundColor = PrimaryColor.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = if (isLoading) "Сохранение..." else "Сохранить",
                            fontSize = 13.sp,
                            fontFamily = Theme.fonts.nunito,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showToast,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    elevation = 6.dp,
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = if (isSuccess) Color(0xFFE8F5E9) else ErrorBackgroundColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.Lock else Icons.Default.Warning,
                            contentDescription = if (isSuccess) "Успех" else "Ошибка",
                            tint = if (isSuccess) PrimaryColor else ErrorColor,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = toastMessage,
                            color = if (isSuccess) PrimaryColor else ErrorColor,
                            fontFamily = Theme.fonts.nunito,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { showToast = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть",
                                tint = if (isSuccess) PrimaryColor else ErrorColor
                            )
                        }
                    }
                }
            }
        }
    }
}