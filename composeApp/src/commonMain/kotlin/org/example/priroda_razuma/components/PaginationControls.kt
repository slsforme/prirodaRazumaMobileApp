package org.example.priroda_razuma.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.example.priroda_razuma.preferences.Theme

private val PrimaryColor = Color(0xFF2E7D32)
private val SurfaceColor = Color(0xFFECF5EC)
private val AccentColor = Color(0xFF81C784)
private val LightAccentColor = Color(0xFFD3E29F)
private val TextPrimaryColor = Color(0xFF1B5E20)
private val TextSecondaryColor = Color(0xFF424242)
private val DividerColor = Color(0xFFBDBDBD)

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState()
    val pageInput = remember { mutableStateOf("") }
    val isEditingPage = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val cardWidth by animateDpAsState(
        targetValue = if (isEditingPage.value) 240.dp else 180.dp,
        animationSpec = tween(300),
        label = "cardWidth"
    )

    Card(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .width(cardWidth)
            .height(56.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        backgroundColor = Color.White,
        elevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NavigationButton(
                    enabled = currentPage > 1,
                    onClick = { onPageChange(currentPage - 1) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Предыдущая страница",
                        tint = if (currentPage > 1) PrimaryColor else Color.Gray.copy(alpha = 0.5f)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isEditingPage.value) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextField(
                                value = pageInput.value,
                                onValueChange = { value ->
                                    if (value.isEmpty() || value.all { it.isDigit() }) {
                                        pageInput.value = value
                                        errorMessage.value = null
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                                    .focusRequester(focusRequester),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = Theme.fonts.nunito,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    color = TextPrimaryColor
                                ),
                                placeholder = {
                                    Text(
                                        text = "№ стр.",
                                        fontFamily = Theme.fonts.nunito,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = TextSecondaryColor.copy(alpha = 0.6f)
                                    )
                                },
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = AccentColor
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        submitPageNumber(
                                            pageInput.value,
                                            totalPages,
                                            onPageChange,
                                            errorMessage,
                                            isEditingPage,
                                            focusManager
                                        )
                                    }
                                ),
                                interactionSource = interactionSource,
                                singleLine = true
                            )

                            IconButton(
                                onClick = {
                                    submitPageNumber(
                                        pageInput.value,
                                        totalPages,
                                        onPageChange,
                                        errorMessage,
                                        isEditingPage,
                                        focusManager
                                    )
                                },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AccentColor)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Перейти",
                                    tint = Color.White
                                )
                            }
                        }

                        LaunchedEffect(isEditingPage.value) {
                            if (isEditingPage.value) {
                                delay(100)
                                focusRequester.requestFocus()
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pageInput.value = ""
                                    isEditingPage.value = true
                                },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$currentPage",
                                fontFamily = Theme.fonts.nunito,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryColor
                            )
                            Text(
                                text = " из $totalPages",
                                fontFamily = Theme.fonts.nunito,
                                fontSize = 16.sp,
                                color = TextSecondaryColor
                            )
                        }
                    }
                }

                NavigationButton(
                    enabled = currentPage < totalPages,
                    onClick = { onPageChange(currentPage + 1) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Следующая страница",
                        tint = if (currentPage < totalPages) PrimaryColor else Color.Gray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = errorMessage.value != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        errorMessage.value?.let {
            Text(
                text = it,
                fontFamily = Theme.fonts.nunito,
                fontSize = 12.sp,
                color = Color.Red,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    if (totalPages > 3) {
        PageDotsIndicator(
            currentPage = currentPage,
            totalPages = totalPages,
            onPageChange = onPageChange
        )
    }
}

private fun submitPageNumber(
    input: String,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    errorMessage: MutableState<String?>,
    isEditingPage: MutableState<Boolean>,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    val pageNumber = input.toIntOrNull()

    when {
        input.isEmpty() -> {
            errorMessage.value = "Введите номер"
        }
        pageNumber == null -> {
            errorMessage.value = "Некорректный номер"
        }
        pageNumber < 1 || pageNumber > totalPages -> {
            errorMessage.value = "Доступно: 1-$totalPages"
        }
        else -> {
            onPageChange(pageNumber)
            isEditingPage.value = false
            errorMessage.value = null
        }
    }

    focusManager.clearFocus()
}

@Composable
private fun NavigationButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                color = if (enabled) SurfaceColor else Color.Transparent
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun PageDotsIndicator(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    val visibleDots = 5
    val startDot = maxOf(1, minOf(currentPage - (visibleDots / 2), totalPages - visibleDots + 1))
    val endDot = minOf(startDot + visibleDots - 1, totalPages)

    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .height(24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (startDot > 1) {
            PageDot(
                page = 1,
                isSelected = currentPage == 1,
                onSelected = { onPageChange(1) }
            )

            if (startDot > 2) {
                Text(
                    text = "...",
                    modifier = Modifier.padding(horizontal = 2.dp),
                    fontFamily = Theme.fonts.nunito,
                    fontSize = 12.sp,
                    color = TextSecondaryColor
                )
            }
        }

        for (i in startDot..endDot) {
            PageDot(
                page = i,
                isSelected = currentPage == i,
                onSelected = { onPageChange(i) }
            )
        }

        if (endDot < totalPages) {
            if (endDot < totalPages - 1) {
                Text(
                    text = "...",
                    modifier = Modifier.padding(horizontal = 2.dp),
                    fontFamily = Theme.fonts.nunito,
                    fontSize = 12.sp,
                    color = TextSecondaryColor
                )
            }

            PageDot(
                page = totalPages,
                isSelected = currentPage == totalPages,
                onSelected = { onPageChange(totalPages) }
            )
        }
    }
}

@Composable
fun PageDot(
    page: Int,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val dotSize = if (isSelected) 28.dp else 24.dp
    val backgroundColor = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                LightAccentColor,
                AccentColor.copy(alpha = 0.7f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White,
                Color.White
            )
        )
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(dotSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .shadow(
                elevation = if (isSelected) 1.dp else 0.dp,
                shape = CircleShape
            )
            .run {
                if (isSelected) {
                    this
                } else {
                    this.background(Color.White)
                        .padding(1.dp)
                        .background(
                            color = DividerColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                }
            }
            .clickable { onSelected() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = page.toString(),
            fontFamily = Theme.fonts.nunito,
            fontSize = if (isSelected) 12.sp else 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) TextPrimaryColor else TextSecondaryColor
        )
    }
}