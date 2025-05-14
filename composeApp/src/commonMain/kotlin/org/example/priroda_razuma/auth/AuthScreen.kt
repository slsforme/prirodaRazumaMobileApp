package org.example.priroda_razuma.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.launch
import org.example.priroda_razuma.auth.AuthManager
import org.example.priroda_razuma.auth.responses.TokenResponse
import org.example.priroda_razuma.preferences.Theme
import org.jetbrains.compose.resources.painterResource
import prirodarazumamobile.composeapp.generated.resources.Res
import prirodarazumamobile.composeapp.generated.resources.eye
import prirodarazumamobile.composeapp.generated.resources.hidden
import prirodarazumamobile.composeapp.generated.resources.nature

val LightGreen = Color(0xFFA3F49F)
val PaleGreen = Color(0xFFD3E29F)
val DarkGreen = Color(0xFF2E7D32)
val White = Color.White
val ErrorRed = Color(0xFFD32F2F)
val ErrorLightBg = Color(0xFFFFEBEE)
val CardShadow = Color(0x14000000)


@Composable
fun AuthScreen(
    authManager: AuthManager,
    onLoginSuccess: (TokenResponse) -> Unit,
    onAuthError: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }

    fun validateField(name: String, value: String): String {
        val loginRegex = Regex("^[a-zA-Z0-9]+$")
        val passwordRegex = Regex("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+$")

        return when (name) {
            "username" -> {
                when {
                    value.isEmpty() -> "Обязательное поле"
                    value.length < 5 || value.length > 50 -> "Длина должна быть от 5 до 50 символов"
                    !loginRegex.matches(value) -> "Допустимы только латинские буквы и цифры"
                    else -> ""
                }
            }
            "password" -> {
                when {
                    value.isEmpty() -> "Обязательное поле"
                    value.length < 5 || value.length > 50 -> "Длина должна быть от 5 до 50 символов"
                    !passwordRegex.matches(value) -> "Недопустимые символы"
                    else -> ""
                }
            }
            else -> ""
        }
    }

    fun handleChange(name: String, value: String) {
        var processedValue = value

        if (name == "username") {
            processedValue = value.replace(Regex("[^a-zA-Z0-9]"), "")
            username = processedValue
            usernameError = validateField("username", processedValue)
        } else if (name == "password") {
            password = value
            passwordError = validateField("password", value)
        }
    }

    fun handleLoginError(exception: Throwable) {
        Logger.e("Login Error", exception)

        loginError = when (exception) {
            is ClientRequestException -> {
                val statusCode = exception.response.status.value
                when {
                    statusCode == HttpStatusCode.Unauthorized.value -> "Вы неправильно ввели данные"
                    statusCode == HttpStatusCode.NotFound.value -> "Сервер не найден"
                    statusCode in 500..599 -> "Ошибка сервера ($statusCode)"
                    else -> "Ошибка запроса: $statusCode"
                }
            }
            is SocketTimeoutException -> "Таймаут соединения"
            is IOException -> "Проблемы с интернет-соединением"
            else -> "Ошибка системы: ${exception.message?.take(50) ?: "код ${exception.hashCode()}"}"
        }
    }

    fun validateForm(): Boolean {
        usernameError = validateField("username", username)
        passwordError = validateField("password", password)

        return usernameError.isEmpty() && passwordError.isEmpty()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Theme.colors.primary,
                        Theme.colors.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = CardShadow,
                    spotColor = CardShadow
                ),
            shape = RoundedCornerShape(24.dp),
            elevation = 0.dp,
            backgroundColor = White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    LightGreen.copy(alpha = 0.7f),
                                    PaleGreen.copy(alpha = 0.5f)
                                )
                            )
                        )
                ) {
                    Image(
                        painter = painterResource(Res.drawable.nature),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Text(
                    text = "Природа Разума",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                    fontFamily = Theme.fonts.nunito,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = username,
                    onValueChange = { handleChange("username", it) },
                    label = "Логин",
                    icon = Icons.Default.Person,
                    isError = usernameError.isNotEmpty(),
                    errorMessage = usernameError,
                    showErrorMessage = usernameError.isNotEmpty()
                )

                CustomTextField(
                    value = password,
                    onValueChange = { handleChange("password", it) },
                    label = "Пароль",
                    icon = Icons.Default.Lock,
                    isError = passwordError.isNotEmpty(),
                    errorMessage = passwordError,
                    showErrorMessage = passwordError.isNotEmpty(),
                    isPassword = true,
                    isPasswordVisible = isPasswordVisible,
                    onPasswordVisibilityToggle = { isPasswordVisible = !isPasswordVisible }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                ) {
                    this@Column.AnimatedVisibility(
                        visible = loginError.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        ErrorMessage(
                            message = loginError,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Button(
                    onClick = {
                        if (!validateForm()) {
                            loginError = "Пожалуйста, заполните все поля корректно"
                            return@Button
                        }

                        coroutineScope.launch {
                            isLoading = true
                            loginError = ""
                            try {
                                val result = authManager.login(username, password)
                                result.onSuccess { tokenResponse ->
                                    if (authManager.userFio == null) {
                                        loginError = "Ошибка при загрузке данных пользователя"
                                    } else {
                                        onLoginSuccess(tokenResponse)
                                        loginError = ""
                                    }
                                }.onFailure { exception ->
                                    handleLoginError(exception)
                                }
                            } catch (e: Exception) {
                                handleLoginError(e)
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = LightGreen,
                        contentColor = DarkGreen,
                        disabledBackgroundColor = LightGreen.copy(alpha = 0.6f),
                        disabledContentColor = DarkGreen.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = DarkGreen,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Войти",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = Theme.fonts.nunito
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Any,
    isError: Boolean,
    errorMessage: String,
    showErrorMessage: Boolean,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityToggle: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label,
                    fontFamily = Theme.fonts.robotoFlex,
                    fontSize = 16.sp
                )
            },
            leadingIcon = {
                when (icon) {
                    is androidx.compose.ui.graphics.vector.ImageVector -> {
                        Icon(
                            imageVector = icon,
                            contentDescription = "$label Icon",
                            tint = if (isError) ErrorRed else Color.Gray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Image(
                            painter = painterResource(
                                if (isPasswordVisible) Res.drawable.hidden else Res.drawable.eye
                            ),
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else null,
            isError = isError,
            singleLine = true,
            visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = if (isError) ErrorRed else LightGreen,
                unfocusedBorderColor = if (isError) ErrorRed.copy(alpha = 0.7f) else PaleGreen,
                focusedLabelColor = if (isError) ErrorRed else DarkGreen,
                unfocusedLabelColor = if (isError) ErrorRed.copy(alpha = 0.7f) else Color.Gray,
                cursorColor = if (isError) ErrorRed else LightGreen,
                backgroundColor = if (isError) ErrorLightBg.copy(alpha = 0.1f) else White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)) {
            this@Column.AnimatedVisibility(
                visible = showErrorMessage,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Text(
                    text = errorMessage,
                    color = ErrorRed,
                    fontSize = 12.sp,
                    fontFamily = Theme.fonts.robotoFlex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        backgroundColor = ErrorLightBg,
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = "Error Icon",
                tint = ErrorRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = ErrorRed,
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                fontFamily = Theme.fonts.nunito,
                modifier = Modifier.weight(1f)
            )
        }
    }
}