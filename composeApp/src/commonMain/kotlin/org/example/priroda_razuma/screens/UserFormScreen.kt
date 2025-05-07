package org.example.priroda_razuma.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.example.priroda_razuma.auth.AuthManager
import org.example.priroda_razuma.models.CreateUserRequest
import org.example.priroda_razuma.models.Role
import org.example.priroda_razuma.models.UpdateUserRequest
import org.example.priroda_razuma.models.User
import org.example.priroda_razuma.preferences.Theme
import org.example.priroda_razuma.utils.toImageBitmap
import org.jetbrains.compose.resources.painterResource
import prirodarazumamobile.composeapp.generated.resources.Res
import prirodarazumamobile.composeapp.generated.resources.eye
import prirodarazumamobile.composeapp.generated.resources.hidden


@Composable
fun UserFormScreen(
    authManager: AuthManager,
    isEdit: Boolean = false,
    userId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var patronymic by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    var selectedRoleId by remember { mutableStateOf(0) }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showRoleDropdown by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    // Photo related state
    var userProfileImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isPhotoSelected by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var showPhotoPickerDialog by remember { mutableStateOf(false) }
    var isPhotoDeleted by remember { mutableStateOf(false) }
    var photoBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Error states
    var error by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var patronymicError by remember { mutableStateOf<String?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var showErrorToast by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var roles by remember { mutableStateOf<List<Role>>(emptyList()) }

    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = coroutineScope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let { imageBytes ->
                photoBytes = imageBytes
                userProfileImage = imageBytes.toImageBitmap()
                isPhotoSelected = true
                isPhotoDeleted = false
            }
        }
    )


    LaunchedEffect(isEdit, userId) {
        isLoading = true
        try {
            // Fetch roles
            roles = authManager.getAllRoles()

            if (isEdit && userId != null) {
                val user = authManager.getUserById(userId)
                val nameParts = user.fio.split(" ")

                lastName = nameParts.getOrNull(0) ?: ""
                firstName = nameParts.getOrNull(1) ?: ""
                patronymic = nameParts.getOrElse(2) { "" }
                login = user.login
                userEmail = user.email ?: ""
                isActive = user.active
                selectedRoleId = user.role_id
                photoUrl = user.photo_url

                if (photoUrl != null) {
                    val photo = authManager.getUserPhoto(userId)
                    photo?.let {
                        userProfileImage = it.toImageBitmap()
                    }
                }
            }
        } catch (e: Exception) {
            error = "Ошибка загрузки данных: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Validation functions
    fun validateLastName(value: String): String? {
        val cyrillicRegex = Regex("^[а-яА-ЯёЁ\\- ]+$")
        return when {
            value.length < 2 -> "Минимальная длина - 2 символа"
            value.length > 100 -> "Максимальная длина - 100 символов"
            !cyrillicRegex.matches(value) -> "Допустимы только кириллические символы"
            else -> null
        }
    }

    fun validateFirstName(value: String): String? {
        val cyrillicRegex = Regex("^[а-яА-ЯёЁ\\- ]+$")
        return when {
            value.length < 2 -> "Минимальная длина - 2 символа"
            value.length > 100 -> "Максимальная длина - 100 символов"
            !cyrillicRegex.matches(value) -> "Допустимы только кириллические символы"
            else -> null
        }
    }

    fun validatePatronymic(value: String): String? {
        if (value.isEmpty()) return null

        val cyrillicRegex = Regex("^[а-яА-ЯёЁ\\- ]+$")
        return when {
            value.length > 100 -> "Максимальная длина - 100 символов"
            !cyrillicRegex.matches(value) -> "Допустимы только кириллические символы"
            else -> null
        }
    }

    fun validateLogin(value: String): String? {
        val loginRegex = Regex("^(?=.*[a-zA-Z])[a-zA-Z0-9]+$")
        return when {
            value.isEmpty() -> "Обязательное поле"
            value.length < 5 || value.length > 50 -> "Длина должна быть от 5 до 50 символов"
            !loginRegex.matches(value) -> "Допустимы только латинские буквы и цифры, минимум одна буква"
            else -> null
        }
    }

    fun validatePassword(value: String): String? {
        val passwordRegex = Regex("^[a-zA-Z0-9!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+$")
        return when {
            !isEdit && value.isEmpty() -> "Обязательное поле"
            value.isNotEmpty() && (value.length < 5 || value.length > 50) -> "Длина должна быть от 5 до 50 символов"
            value.isNotEmpty() && !passwordRegex.matches(value) -> "Недопустимые символы"
            else -> null
        }
    }

    fun validateEmail(value: String): String? {
        if (value.isEmpty()) return null

        val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        return when {
            !emailRegex.matches(value) -> "Некорректный формат электронной почты"
            value.length > 255 -> "Максимальная длина email - 255 символов"
            else -> null
        }
    }

    fun validateAll(): Boolean {
        lastNameError = validateLastName(lastName)
        firstNameError = validateFirstName(firstName)
        patronymicError = validatePatronymic(patronymic)
        loginError = validateLogin(login)
        passwordError = validatePassword(password)
        emailError = validateEmail(userEmail)

        val hasFieldErrors = lastNameError != null || firstNameError != null ||
                patronymicError != null || loginError != null ||
                passwordError != null || emailError != null

        if (selectedRoleId == 0) {
            error = "Выберите роль пользователя"
            return false
        }

        return !hasFieldErrors
    }

    fun handleSubmit() {
        if (!validateAll()) return

        coroutineScope.launch {
            isLoading = true
            try {
                val fio = listOf(lastName, firstName, patronymic)
                    .filter { it.isNotEmpty() }
                    .joinToString(" ")

                val updateData = UpdateUserRequest(
                    fio = fio,
                    login = login,
                    email = userEmail.ifEmpty { null },
                    active = isActive,
                    role_id = selectedRoleId,
                    photo_url = photoUrl,
                    password = if (password.isEmpty()) null else password
                )

                val user = CreateUserRequest(
                    id = userId ?: 0,
                    fio = fio,
                    login = login,
                    email = userEmail.ifEmpty { null },
                    active = isActive,
                    role_id = selectedRoleId,
                    photo_url = null,
                    password = password
                )

                if (isEdit && userId != null) {
                    val success = authManager.updateUser(userId, updateData)

                    if (success) {
                        if (isPhotoDeleted) {
                            val deleteSuccess = authManager.deleteUserPhoto(userId)
                            if (!deleteSuccess) {
                                errorMessage = "Не удалось удалить фото"
                                showErrorToast = true
                            }
                        }
                        else if (isPhotoSelected && photoBytes != null) {
                            val photoSuccess = authManager.uploadUserPhoto(userId, photoBytes!!)
                            if (!photoSuccess) {
                                errorMessage = "Не удалось загрузить фото"
                                showErrorToast = true
                            }
                        }
                        onNavigateBack()
                    } else {
                        error = "Ошибка при обновлении пользователя"
                    }
                } else {
                    val newUserId = authManager.createUser(user)

                    if (newUserId != null) {
                        if (isPhotoSelected && photoBytes != null) {
                            val photoSuccess = authManager.uploadUserPhoto(user.id, photoBytes!!)
                            if (!photoSuccess) {
                                errorMessage = "Не удалось загрузить фото"
                                showErrorToast = true
                            }
                        }
                        onNavigateBack()
                    } else {
                        error = "Ошибка при создании пользователя"
                    }
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
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(15.dp))
                .background(Color.White),
            backgroundColor = Color.White,
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEdit) "Редактирование пользователя" else "Создание нового пользователя",
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

                // Error dialog
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

                // Toast error message
                if (showErrorToast) {
                    LaunchedEffect(showErrorToast) {
                        // Automatically hide toast after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        showErrorToast = false
                    }

                    AlertDialog(
                        onDismissRequest = { showErrorToast = false },
                        title = { Text("Внимание") },
                        text = { Text(errorMessage) },
                        confirmButton = {
                            Button(onClick = { showErrorToast = false }) {
                                Text("ОК")
                            }
                        },
                        backgroundColor = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = userEmail,
                    onValueChange = {
                        userEmail = it
                        emailError = validateEmail(it)
                    },
                    label = { Text("Email", color = Color(0xFF34495E)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFA3F49F),
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        errorBorderColor = Color(0xFFDC3545)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    isError = emailError != null,
                    singleLine = true
                )
                if (emailError != null) {
                    Text(
                        text = emailError!!,
                        color = Color(0xFFDC3545),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFCED4DA),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(16.dp)
                ) {
                    if (userProfileImage != null && !isPhotoDeleted) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = userProfileImage!!,
                                contentDescription = "Фото пользователя",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xAAFF4D4F))
                                    .clickable {
                                        isPhotoDeleted = true
                                        userProfileImage = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Удалить фото",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(
                        onClick = { singleImagePicker.launch() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFD3E29F),
                            contentColor = Color(0xFF2C3E50)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading && !isUploadingPhoto
                    ) {
                        if (isUploadingPhoto) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                if (userProfileImage != null && !isPhotoDeleted) "Изменить фото" else "Загрузить фото"
                            )
                        }
                    }

                    if (isPhotoDeleted && userProfileImage == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Фото будет удалено при сохранении",
                            color = Color(0xFFDC3545),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Поддерживаются файлы JPG, JPEG и PNG",
                        fontSize = 12.sp,
                        color = Color(0xFF6C757D)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = {
                        lastName = if (it.isNotEmpty()) {
                            it.first().uppercaseChar() + it.drop(1).lowercase()
                        } else it
                        lastNameError = validateLastName(it)
                    },
                    label = { Text("Фамилия*", color = Color(0xFF34495E)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFA3F49F),
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        errorBorderColor = Color(0xFFDC3545)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    isError = lastNameError != null,
                    singleLine = true
                )
                if (lastNameError != null) {
                    Text(
                        text = lastNameError!!,
                        color = Color(0xFFDC3545),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = {
                        firstName = if (it.isNotEmpty()) {
                            it.first().uppercaseChar() + it.drop(1).lowercase()
                        } else it
                        firstNameError = validateFirstName(it)
                    },
                    label = { Text("Имя*", color = Color(0xFF34495E)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFA3F49F),
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        errorBorderColor = Color(0xFFDC3545)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    isError = firstNameError != null,
                    singleLine = true
                )
                if (firstNameError != null) {
                    Text(
                        text = firstNameError!!,
                        color = Color(0xFFDC3545),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = patronymic,
                    onValueChange = {
                        patronymic = if (it.isNotEmpty()) {
                            it.first().uppercaseChar() + it.drop(1).lowercase()
                        } else it
                        patronymicError = validatePatronymic(it)
                    },
                    label = { Text("Отчество", color = Color(0xFF34495E)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFA3F49F),
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        errorBorderColor = Color(0xFFDC3545)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    isError = patronymicError != null,
                    singleLine = true
                )
                if (patronymicError != null) {
                    Text(
                        text = patronymicError!!,
                        color = Color(0xFFDC3545),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Login field
                OutlinedTextField(
                    value = login,
                    onValueChange = {
                        val filteredInput = it.replace(Regex("[^a-zA-Z0-9]"), "")
                        login = filteredInput
                        loginError = validateLogin(filteredInput)
                    },
                    label = { Text("Логин*", color = Color(0xFF34495E)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFA3F49F),
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        errorBorderColor = Color(0xFFDC3545)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    isError = loginError != null,
                    singleLine = true
                )
                if (loginError != null) {
                    Text(
                        text = loginError!!,
                        color = Color(0xFFDC3545),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = validatePassword(it)
                    },
                    label = { Text(if (isEdit) "Пароль (оставьте пустым, чтобы не менять)" else "Пароль*", color = Color(0xFF34495E)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFA3F49F),
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        errorBorderColor = Color(0xFFDC3545)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    isError = passwordError != null,
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Image(
                                painter = painterResource(
                                    if (showPassword) Res.drawable.hidden else Res.drawable.eye
                                ),
                                contentDescription = if (showPassword) "Показать пароль" else "Скрыть пароль",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = Color(0xFFDC3545),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Role dropdown
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Роль*",
                        color = Color(0xFF34495E),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showRoleDropdown = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = Color.White,
                                contentColor = Color(0xFF2C3E50)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (selectedRoleId == 0 && error != null) Color(0xFFDC3545) else Color(0xFFCED4DA)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = roles.find { it.id == selectedRoleId }?.name ?: "Выберите роль",
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Выбрать"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRoleDropdown,
                            onDismissRequest = { showRoleDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedRoleId = role.id
                                        showRoleDropdown = false
                                    }
                                ) {
                                    Text(text = role.name)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Status dropdown
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Статус*",
                        color = Color(0xFF34495E),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showStatusDropdown = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = Color.White,
                                contentColor = Color(0xFF2C3E50)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFFCED4DA)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isActive) "Активен" else "Неактивен",
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Выбрать"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    isActive = true
                                    showStatusDropdown = false
                                }
                            ) {
                                Text(text = "Активен")
                            }
                            DropdownMenuItem(
                                onClick = {
                                    isActive = false
                                    showStatusDropdown = false
                                }
                            ) {
                                Text(text = "Неактивен")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { handleSubmit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFD3E29F),
                        contentColor = Color(0xFF2C3E50)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(text = if (isEdit) "Сохранить изменения" else "Создать пользователя")
                    }
                }
            }
        }
    }
}

