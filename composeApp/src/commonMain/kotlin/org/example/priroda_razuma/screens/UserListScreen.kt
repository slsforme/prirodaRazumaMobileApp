package org.example.priroda_razuma.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
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
import org.example.priroda_razuma.models.User
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
fun UserListScreen(
    authManager: AuthManager,
    onNavigateToCreateUser: () -> Unit,
    onNavigateToEditUser: (Int) -> Unit
) {
    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var allRoles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("all") }
    var selectedRole by remember { mutableStateOf("all") }
    var currentPage by remember { mutableStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showRoleDropdown by remember { mutableStateOf(false) }

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
            val users = authManager.getAllUsers()
            val roles = authManager.getAllRoles()
            allUsers = users
            allRoles = roles
            isLoading = false
        } catch (e: Exception) {
            error = "Ошибка загрузки: ${e.message}"
            isLoading = false
        }
    }

    val filteredUsers = allUsers.filter { user ->
        val matchesSearch = user.fio.lowercase().contains(searchTerm.lowercase())
        val matchesStatus = when (selectedStatus) {
            "active" -> user.active
            "inactive" -> !user.active
            else -> true
        }
        val matchesRole = selectedRole == "all" || user.role_id.toString() == selectedRole
        matchesSearch && matchesStatus && matchesRole
    }

    val totalPages = ceil(filteredUsers.size.toFloat() / ITEMS_PER_PAGE).toInt().coerceAtLeast(1)
    val paginatedUsers = filteredUsers
        .drop((currentPage - 1) * ITEMS_PER_PAGE)
        .take(ITEMS_PER_PAGE)

    fun handleDelete(user: User) {
        coroutineScope.launch {
            if (!authManager.isAuthenticated()) {
                scaffoldState.snackbarHostState.showSnackbar("Не авторизован")
                return@launch
            }
            try {
                val success = authManager.deleteUser(user.id)
                if (success) {
                    allUsers = allUsers.filter { it.id != user.id }
                    scaffoldState.snackbarHostState.showSnackbar("Пользователь \"${user.fio}\" успешно удален")
                } else {
                    scaffoldState.snackbarHostState.showSnackbar("Ошибка при удалении")
                }
            } catch (e: Exception) {
                scaffoldState.snackbarHostState.showSnackbar("Ошибка при удалении: ${e.message}")
            }
        }
    }

    fun getRoleName(roleId: Int): String {
        return allRoles.find { it.id == roleId }?.name ?: "Неизвестно"
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Управление пользователями",
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
                        onClick = onNavigateToCreateUser,
                        modifier = Modifier
                            .padding(8.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(LightAccentColor)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Создать пользователя",
                            tint = Color.Black
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 4.dp
            )
        },
        snackbarHost = { hostState ->
            SnackbarHost(hostState) { data ->
                androidx.compose.material.Snackbar(
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = searchTerm,
                        onValueChange = {
                            searchTerm = it
                            currentPage = 1
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        placeholder = {
                            Text(
                                "Поиск по ФИО...",
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

                    Box {
                        Button(
                            onClick = { showStatusDropdown = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White,
                                contentColor = TextPrimaryColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (selectedStatus) {
                                        "all" -> "Все статусы"
                                        "active" -> "Активные"
                                        "inactive" -> "Неактивные"
                                        else -> "Выберите статус"
                                    },
                                    fontFamily = Theme.fonts.nunito,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Выбрать статус",
                                    tint = TextPrimaryColor
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false },
                            modifier = Modifier
                                .background(Color.White)
                                .width(250.dp)
                                .heightIn(max = 300.dp)
                        ) {
                            listOf(
                                "all" to "Все статусы",
                                "active" to "Активные",
                                "inactive" to "Неактивные"
                            ).forEach { (key, label) ->
                                DropdownMenuItem(onClick = {
                                    selectedStatus = key
                                    currentPage = 1
                                    showStatusDropdown = false
                                }) {
                                    Text(
                                        label,
                                        fontFamily = Theme.fonts.nunito,
                                        color = TextPrimaryColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Box {
                        Button(
                            onClick = { showRoleDropdown = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White,
                                contentColor = TextPrimaryColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (selectedRole == "all") "Все роли"
                                    else allRoles.find { it.id.toString() == selectedRole }?.name ?: "Выберите роль",
                                    fontFamily = Theme.fonts.nunito,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Выбрать роль",
                                    tint = TextPrimaryColor
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showRoleDropdown,
                            onDismissRequest = { showRoleDropdown = false },
                            modifier = Modifier
                                .background(Color.White)
                                .width(250.dp)
                                .heightIn(max = 300.dp)
                        ) {
                            DropdownMenuItem(onClick = {
                                selectedRole = "all"
                                currentPage = 1
                                showRoleDropdown = false
                            }) {
                                Text(
                                    "Все роли",
                                    fontFamily = Theme.fonts.nunito,
                                    color = TextPrimaryColor
                                )
                            }
                            allRoles.forEach { role ->
                                DropdownMenuItem(onClick = {
                                    selectedRole = role.id.toString()
                                    currentPage = 1
                                    showRoleDropdown = false
                                }) {
                                    Text(
                                        role.name,
                                        fontFamily = Theme.fonts.nunito,
                                        color = TextPrimaryColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                                                    val users = authManager.getAllUsers()
                                                    val roles = authManager.getAllRoles()
                                                    allUsers = users
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
                    allUsers.isEmpty() -> {
                        EmptyStateView(
                            message = "Пока что данные отсутствуют",
                            actionLabel = "Создать пользователя",
                            onAction = onNavigateToCreateUser
                        )
                    }
                    filteredUsers.isEmpty() -> {
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
                                    text = "Попробуйте изменить параметры поиска или фильтры",
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
                                itemsIndexed(paginatedUsers) { index, user ->
                                    UserItem(
                                        user = user,
                                        roleName = getRoleName(user.role_id),
                                        onEdit = { onNavigateToEditUser(user.id) },
                                        onDelete = {
                                            selectedUser = user
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

        if (showDeleteDialog && selectedUser != null) {
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
                        text = "Вы уверены, что хотите удалить пользователя \"${selectedUser?.fio}\"?",
                        fontFamily = Theme.fonts.nunito,
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedUser?.let { handleDelete(it) }
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
fun UserItem(
    user: User,
    roleName: String,
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
                        text = user.fio,
                        fontFamily = Theme.fonts.nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "ID: ${user.id}",
                        fontFamily = Theme.fonts.nunito,
                        fontSize = 14.sp,
                        color = TextSecondaryColor.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Логин: ${user.login}",
                    fontFamily = Theme.fonts.nunito,
                    fontSize = 14.sp,
                    color = TextSecondaryColor
                )
                Text(
                    text = "Статус: ${if (user.active) "Активен" else "Неактивен"}",
                    fontFamily = Theme.fonts.nunito,
                    fontSize = 14.sp,
                    color = if (user.active) Color(0xFF64B664) else Color(0xFF6C757D)
                )
                Text(
                    text = "Роль: $roleName",
                    fontFamily = Theme.fonts.nunito,
                    fontSize = 14.sp,
                    color = TextSecondaryColor
                )
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