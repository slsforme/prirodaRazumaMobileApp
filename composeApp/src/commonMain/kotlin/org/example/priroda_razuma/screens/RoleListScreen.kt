package org.example.priroda_razuma.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.priroda_razuma.auth.AuthManager
import org.example.priroda_razuma.components.EmptyStateView
import org.example.priroda_razuma.components.PaginationControls
import org.example.priroda_razuma.models.Role
import org.example.priroda_razuma.preferences.Theme
import kotlin.math.ceil

private val PrimaryColor = Color(0xFF2E7D32)
private val SurfaceColor = Color(0xFFECF5EC)
private val AccentColor = Color(0xFF81C784)
private val LightAccentColor = Color(0xFFD3E29F)
private val TextPrimaryColor = Color(0xFF1B5E20)
private val TextSecondaryColor = Color(0xFF424242)
private val DividerColor = Color(0xFFBDBDBD)

@Composable
fun RoleListScreen(
    authManager: AuthManager,
    onNavigateToCreateRole: () -> Unit,
    onNavigateToEditRole: (Int) -> Unit
) {
    var allRoles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf<Role?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    val ITEMS_PER_PAGE = 10

    LaunchedEffect(Unit) {
        if (!authManager.isAuthenticated()) {
            error = "Не авторизован"
            isLoading = false
            return@LaunchedEffect
        }
        try {
            isLoading = true
            val roles = authManager.getAllRoles()
            allRoles = roles
            isLoading = false
        } catch (e: Exception) {
            error = "Ошибка загрузки: ${e.message}"
            isLoading = false
        }
    }

    val filteredRoles = allRoles.filter {
        it.name.lowercase().contains(searchTerm.lowercase())
    }

    val totalPages = ceil(filteredRoles.size.toFloat() / ITEMS_PER_PAGE).toInt().coerceAtLeast(1)
    val paginatedRoles = filteredRoles
        .drop((currentPage - 1) * ITEMS_PER_PAGE)
        .take(ITEMS_PER_PAGE)

    fun handleDelete(role: Role) {
        coroutineScope.launch {
            if (!authManager.isAuthenticated()) {
                scaffoldState.snackbarHostState.showSnackbar("Не авторизован")
                return@launch
            }
            try {
                val success = authManager.deleteRole(role.id)
                if (success) {
                    allRoles = allRoles.filter { it.id != role.id }
                    scaffoldState.snackbarHostState.showSnackbar("Роль \"${role.name}\" успешно удалена")
                } else {
                    scaffoldState.snackbarHostState.showSnackbar("Ошибка при удалении")
                }
            } catch (e: Exception) {
                scaffoldState.snackbarHostState.showSnackbar("Ошибка при удалении: ${e.message}")
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Управление ролями",
                            fontFamily = Theme.fonts.nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimaryColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(3.dp)
                                .background(
                                    LightAccentColor,
                                    shape = RoundedCornerShape(1.5.dp)
                                )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToCreateRole,
                        modifier = Modifier
                            .padding(8.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(LightAccentColor)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Создать роль",
                            tint = Color.Black
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 4.dp
            )
        },
        floatingActionButton = {},
        snackbarHost = { hostState ->
            SnackbarHost(hostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    backgroundColor = SurfaceColor,
                    contentColor = TextPrimaryColor,
                    action = {
                        TextButton(onClick = { data.performAction() }) {
                            Text(
                                text = data.actionLabel ?: "ОК",
                                color = PrimaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                ) {
                    Text(
                        text = data.message,
                        fontFamily = Theme.fonts.nunito
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = {
                        searchTerm = it
                        currentPage = 1
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .height(54.dp),
                    placeholder = {
                        Text(
                            "Поиск по названию роли...",
                            fontFamily = Theme.fonts.nunito,
                            color = TextSecondaryColor.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Поиск",
                            tint = TextSecondaryColor.copy(alpha = 0.6f)
                        )
                    },
                    trailingIcon = {
                        if (searchTerm.isNotEmpty()) {
                            IconButton(onClick = { searchTerm = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Очистить",
                                    tint = TextSecondaryColor
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White,
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = DividerColor,
                        cursorColor = PrimaryColor
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = Theme.fonts.nunito,
                        fontSize = 16.sp
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = LightAccentColor,
                                    modifier = Modifier.size(64.dp),
                                    strokeWidth = 6.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Загрузка данных...",
                                    fontFamily = Theme.fonts.nunito,
                                    fontSize = 18.sp,
                                    color = TextSecondaryColor
                                )
                            }
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "😕",
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = error ?: "Неизвестная ошибка",
                                    fontFamily = Theme.fonts.nunito,
                                    fontSize = 18.sp,
                                    color = TextSecondaryColor,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (authManager.isAuthenticated()) {
                                            isLoading = true
                                            error = null
                                            coroutineScope.launch {
                                                try {
                                                    val roles = authManager.getAllRoles()
                                                    allRoles = roles
                                                    isLoading = false
                                                } catch (e: Exception) {
                                                    error = "Ошибка загрузки: ${e.message}"
                                                    isLoading = false
                                                }
                                            }
                                        } else {
                                            error = "Не авторизован"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = LightAccentColor,
                                        contentColor = TextPrimaryColor
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = "Повторить",
                                        fontFamily = Theme.fonts.nunito,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    allRoles.isEmpty() -> {
                        EmptyStateView(
                            message = "Пока что данные отсутствуют",
                            actionLabel = "Создать роль",
                            onAction = onNavigateToCreateRole
                        )
                    }
                    filteredRoles.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "🔍",
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Ничего не найдено",
                                    fontFamily = Theme.fonts.nunito,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = TextPrimaryColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Попробуйте изменить параметры поиска",
                                    fontFamily = Theme.fonts.nunito,
                                    fontSize = 16.sp,
                                    color = TextSecondaryColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(paginatedRoles) { index, role ->
                                    RoleItem(
                                        role = role,
                                        onEdit = { onNavigateToEditRole(role.id) },
                                        onDelete = {
                                            selectedRole = role
                                            showDeleteDialog = true
                                        },
                                        animationDelay = index * 50L
                                    )
                                }
                            }

                            if (totalPages > 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                                PaginationControls(
                                    currentPage = currentPage,
                                    totalPages = totalPages,
                                    onPageChange = { newPage ->
                                        currentPage = newPage
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog && selectedRole != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = "Подтверждение удаления",
                        fontFamily = Theme.fonts.nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Вы уверены, что хотите удалить роль \"${selectedRole?.name}\"?",
                        fontFamily = Theme.fonts.nunito,
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedRole?.let { handleDelete(it) }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = LightAccentColor,
                            contentColor = TextPrimaryColor
                        )
                    ) {
                        Text(
                            text = "Удалить",
                            fontFamily = Theme.fonts.nunito,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text(
                            text = "Отмена",
                            fontFamily = Theme.fonts.nunito,
                            color = TextSecondaryColor
                        )
                    }
                },
                backgroundColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun RoleItem(
    role: Role,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    animationDelay: Long = 0
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(300)
                )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
            backgroundColor = Color.White,
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = role.name,
                        fontFamily = Theme.fonts.nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "ID: ${role.id}",
                        fontFamily = Theme.fonts.nunito,
                        fontSize = 14.sp,
                        color = TextSecondaryColor.copy(alpha = 0.6f)
                    )
                }

                if (!role.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = role.description,
                        fontFamily = Theme.fonts.nunito,
                        fontSize = 14.sp,
                        color = TextSecondaryColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFA3F49F),
                            contentColor = TextPrimaryColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Редактировать",
                            fontFamily = Theme.fonts.nunito,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = LightAccentColor,
                            contentColor = TextPrimaryColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Удалить",
                            fontFamily = Theme.fonts.nunito,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}