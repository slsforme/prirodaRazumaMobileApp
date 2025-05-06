package org.example.priroda_razuma.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.priroda_razuma.auth.AuthManager
import org.example.priroda_razuma.components.PasswordChangeDialog
import org.example.priroda_razuma.preferences.Theme
import org.example.priroda_razuma.utils.toImageBitmap
import org.jetbrains.compose.resources.painterResource
import prirodarazumamobile.composeapp.generated.resources.Res
import prirodarazumamobile.composeapp.generated.resources.default_user
import prirodarazumamobile.composeapp.generated.resources.email
import prirodarazumamobile.composeapp.generated.resources.info
import prirodarazumamobile.composeapp.generated.resources.plus
import prirodarazumamobile.composeapp.generated.resources.shield
import prirodarazumamobile.composeapp.generated.resources.user

private val PrimaryColor = Color(0xFF2E7D32)
private val SecondaryColor = Color(0xFFD3E29F)
private val SurfaceColor = Color(0xFFF3F6F3)
private val TextPrimaryColor = Color(0xFF2c3e50)
private val TextSecondaryColor = Color(0xFF666666)
private val DividerColor = Color(0xFFBDBDBD)

@Composable
fun ProfileScreen(
    authManager: AuthManager,
    onLogout: () -> Unit,
    openDrawer: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isUploadingPhoto by remember { mutableStateOf(false) }
    var showPhotoPickerDialog by remember { mutableStateOf(false) }
    var showErrorToast by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var userProfileImage by remember { mutableStateOf<ImageBitmap?>(null) }

    val userId = authManager.userId ?: 0
    val userFio = authManager.userFio ?: "Не указано"
    val roleName = authManager.roleName ?: "Не указана"
    val email = authManager.email ?: "Не указана"
    val roleId = authManager.roleId ?: 0

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val photoBytes = authManager.getUserPhoto(userId)
                photoBytes?.let { bytes ->
                    withContext(Dispatchers.Main) {
                        userProfileImage = bytes.toImageBitmap()
                    }
                }
            } catch (e: Exception) {
                println("Ошибка при загрузке фото: ${e.message}")
            }
        }
    }

    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = coroutineScope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let { imageBytes ->

                isUploadingPhoto = true
                coroutineScope.launch {
                    try {
                        val success = authManager.uploadUserPhoto(userId, imageBytes)
                        if (success) {
                            val newPhotoBytes = authManager.getUserPhoto(userId)
                            newPhotoBytes?.let { bytes ->
                                withContext(Dispatchers.Main) {
                                    userProfileImage = bytes.toImageBitmap()
                                }
                            }
                        } else {
                            errorMessage = "Не удалось загрузить фото"
                            showErrorToast = true
                        }
                    } catch (e: Exception) {
                        errorMessage = "Ошибка: ${e.message}"
                        showErrorToast = true
                    } finally {
                        isUploadingPhoto = false
                        showPhotoPickerDialog = false
                    }
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceColor)
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimaryColor,
                            SecondaryColor
                        )
                    )
                )
        ) {
            Text(
                text = "Личный кабинет",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Theme.fonts.nunito,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-60).dp)
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, SecondaryColor, CircleShape)
                    .clickable { showPhotoPickerDialog = true }
            ) {
                if (userProfileImage != null) {
                    Image(
                        bitmap = userProfileImage!!,
                        contentDescription = "Фото профиля",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(Res.drawable.default_user),
                        contentDescription = "Фото профиля",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                if (isUploadingPhoto) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(Color(0x80000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.plus),
                        contentDescription = "Добавить фото",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Text(
            text = userFio,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryColor,
            fontFamily = Theme.fonts.nunito,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp)
                .padding(horizontal = 24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-20).dp)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Основная информация",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        fontFamily = Theme.fonts.nunito,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    InfoRow(
                        icon = painterResource(Res.drawable.user),
                        title = "Полное имя",
                        value = userFio,
                        iconBackgroundColor = SecondaryColor
                    )

                    Divider(
                        color = DividerColor.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    InfoRow(
                        icon = painterResource(Res.drawable.shield),
                        title = "Роль",
                        value = roleName,
                        iconBackgroundColor = SecondaryColor
                    )

                    Divider(
                        color = DividerColor.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    InfoRow(
                        icon = painterResource(Res.drawable.email),
                        title = "Электронная почта",
                        value = email,
                        iconBackgroundColor = SecondaryColor
                    )

                    Divider(
                        color = DividerColor.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    InfoRow(
                        icon = painterResource(Res.drawable.info),
                        title = "Логин",
                        value = authManager.accessToken?.let { "user_${userId}" } ?: "Не указан",
                        iconBackgroundColor = SecondaryColor
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Действия",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        fontFamily = Theme.fonts.nunito,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ActionButton(
                        text = "Изменить пароль",
                        icon = Icons.Default.Lock,
                        onClick = {
                            showPasswordChangeDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ActionButton(
                        text = "Выйти из аккаунта",
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        backgroundColor = Color(0xFFFFECEC),
                        textColor = Color(0xFFE53935),
                        iconTint = Color(0xFFE53935),
                        onClick = {
                            coroutineScope.launch {
                                authManager.logout()
                                onLogout()
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showPasswordChangeDialog) {
        PasswordChangeDialog(
            authManager = authManager,
            onDismiss = { showPasswordChangeDialog = false }
        )
    }

    if (showPhotoPickerDialog) {
        Dialog(
            onDismissRequest = { showPhotoPickerDialog = false },
            properties = DialogProperties(dismissOnClickOutside = true)
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
                    Text(
                        text = "Изменить фото профиля",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Theme.fonts.nunito,
                        color = TextPrimaryColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { singleImagePicker.launch() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = Color.Transparent,
                                contentColor = PrimaryColor
                            ),
                            border = BorderStroke(1.dp, PrimaryColor)
                        ) {
                            Text(
                                "Галерея",
                                fontFamily = Theme.fonts.robotoFlex
                            )
                        }

                        Button(
                            onClick = { showPhotoPickerDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = PrimaryColor,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "Отмена",
                                fontFamily = Theme.fonts.robotoFlex
                            )
                        }
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = showErrorToast,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                elevation = 6.dp,
                shape = RoundedCornerShape(8.dp),
                backgroundColor = Color(0xFFFFDDDD)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Ошибка",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = errorMessage,
                        color = Color(0xFFE53935),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { showErrorToast = false }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = Color(0xFFE53935)
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(showErrorToast) {
        if (showErrorToast) {
            kotlinx.coroutines.delay(5000)
            showErrorToast = false
        }
    }
}

@Composable
fun InfoRow(
    icon: Any,
    title: String,
    value: String,
    iconBackgroundColor: Color = SecondaryColor
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Иконка
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                when (icon) {
                    is androidx.compose.ui.graphics.painter.Painter -> {
                        Icon(
                            painter = icon,
                            contentDescription = title,
                            tint = iconBackgroundColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    is androidx.compose.ui.graphics.vector.ImageVector -> {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconBackgroundColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondaryColor,
                fontFamily = Theme.fonts.nunito
            )

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryColor,
                fontFamily = Theme.fonts.nunito
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color = Color(0xFFF5F5F5),
    textColor: Color = TextPrimaryColor,
    iconTint: Color = PrimaryColor,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            fontFamily = Theme.fonts.nunito,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Перейти",
            tint = textColor.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
    }
}