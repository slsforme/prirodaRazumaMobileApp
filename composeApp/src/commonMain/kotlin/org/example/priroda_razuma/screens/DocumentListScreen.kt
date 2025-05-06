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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.priroda_razuma.auth.AuthManager
import org.example.priroda_razuma.components.EmptyStateView
import org.example.priroda_razuma.components.PaginationControls
import org.example.priroda_razuma.models.Document
import org.example.priroda_razuma.models.Patient
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
fun DocumentListScreen(
    authManager: AuthManager,
    onNavigateToCreateDocument: () -> Unit,
    onNavigateToEditDocument: (Int) -> Unit
) {
    var allDocuments by remember { mutableStateOf<List<Document>>(emptyList()) }
    var allPatients by remember { mutableStateOf<List<Patient>>(emptyList()) }
    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    var selectedPatient by remember { mutableStateOf("all") }
    var selectedDirectory by remember { mutableStateOf("all") }
    var currentPage by remember { mutableStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedDoc by remember { mutableStateOf<Document?>(null) }
    var showPatientDropdown by remember { mutableStateOf(false) }
    var showDirectoryDropdown by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val launcher = rememberFileSaverLauncher { file ->
        if (file != null && selectedDoc != null) {
            coroutineScope.launch {
                try {
                    val (bytes, _) = authManager.downloadDocumentWithFilename(selectedDoc!!.id)
                    file.write(bytes)
                } catch (e: Exception) {
                    scaffoldState.snackbarHostState.showSnackbar("Ошибка сохранения: ${e.message}")
                }
            }
        }
    }

    val ITEMS_PER_PAGE = 10

    LaunchedEffect(Unit) {
        if (!authManager.isAuthenticated()) {
            error = "Не авторизован"
            isLoading = false
            return@LaunchedEffect
        }
        try {
            isLoading = true
            val documents = authManager.getAllDocuments()
            val patients = authManager.getAllPatients()
            val users = authManager.getAllUsers()
            allDocuments = documents
            allPatients = patients
            allUsers = users
            isLoading = false
        } catch (e: Exception) {
            error = "Ошибка загрузки: ${e.message}"
            isLoading = false
        }
    }

    val filteredDocs = allDocuments.filter { doc ->
        val matchesSearch = doc.name.lowercase().contains(searchTerm.lowercase())
        val matchesPatient = selectedPatient == "all" || doc.patient_id.toString() == selectedPatient
        val matchesDir = selectedDirectory == "all" || doc.subdirectory_type == selectedDirectory
        matchesSearch && matchesPatient && matchesDir
    }

    val totalPages = ceil(filteredDocs.size.toFloat() / ITEMS_PER_PAGE).toInt().coerceAtLeast(1)
    val paginatedDocs = filteredDocs
        .drop((currentPage - 1) * ITEMS_PER_PAGE)
        .take(ITEMS_PER_PAGE)

    fun handleDelete(doc: Document) {
        coroutineScope.launch {
            if (!authManager.isAuthenticated()) {
                scaffoldState.snackbarHostState.showSnackbar("Не авторизован")
                return@launch
            }
            try {
                val success = authManager.deleteDocument(doc.id)
                if (success) {
                    allDocuments = allDocuments.filter { it.id != doc.id }
                    scaffoldState.snackbarHostState.showSnackbar("Документ \"${doc.name}\" успешно удален")
                } else {
                    scaffoldState.snackbarHostState.showSnackbar("Ошибка при удалении")
                }
            } catch (e: Exception) {
                scaffoldState.snackbarHostState.showSnackbar("Ошибка при удалении: ${e.message}")
            }
        }
    }

    fun getPatientName(patientId: Int): String {
        return allPatients.find { it.id == patientId }?.fio ?: "Неизвестно"
    }

    fun getAuthorName(authorId: Int?): String {
        return authorId?.let { allUsers.find { u -> u.id == it }?.fio } ?: "-"
    }

    fun handleDownload(docId: Int, filename: String) {
        coroutineScope.launch {
            try {
                selectedDoc = allDocuments.find { it.id == docId }
                launcher.launch(filename, "pdf")
            } catch (e: Exception) {
                scaffoldState.snackbarHostState.showSnackbar("Ошибка при скачивании: ${e.message}")
            }
        }
    }

    val directoryTypes = allDocuments.map { it.subdirectory_type }.distinct()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Управление документами",
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
                            .height(54.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp)),
                        placeholder = {
                            Text(
                                "Поиск по названию...",
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
                        )
                    )

                    Box {
                        Button(
                            onClick = { showPatientDropdown = true },
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
                                    text = if (selectedPatient == "all") "Все пациенты"
                                    else getPatientName(selectedPatient.toInt()),
                                    fontFamily = Theme.fonts.nunito,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Выбрать пациента",
                                    tint = TextPrimaryColor
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showPatientDropdown,
                            onDismissRequest = { showPatientDropdown = false },
                            modifier = Modifier
                                .background(Color.White)
                                .width(250.dp)
                                .heightIn(max = 300.dp)
                        ) {
                            DropdownMenuItem(onClick = {
                                selectedPatient = "all"
                                currentPage = 1
                                showPatientDropdown = false
                            }) {
                                Text(
                                    "Все пациенты",
                                    fontFamily = Theme.fonts.nunito,
                                    color = TextPrimaryColor
                                )
                            }
                            allPatients.forEach { patient ->
                                DropdownMenuItem(onClick = {
                                    selectedPatient = patient.id.toString()
                                    currentPage = 1
                                    showPatientDropdown = false
                                }) {
                                    Text(
                                        patient.fio,
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
                            onClick = { showDirectoryDropdown = true },
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
                                    text = if (selectedDirectory == "all") "Все директории" else selectedDirectory,
                                    fontFamily = Theme.fonts.nunito,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Выбрать директорию",
                                    tint = TextPrimaryColor
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showDirectoryDropdown,
                            onDismissRequest = { showDirectoryDropdown = false },
                            modifier = Modifier
                                .background(Color.White)
                                .width(250.dp)
                                .heightIn(max = 300.dp)
                        ) {
                            DropdownMenuItem(onClick = {
                                selectedDirectory = "all"
                                currentPage = 1
                                showDirectoryDropdown = false
                            }) {
                                Text(
                                    "Все директории",
                                    fontFamily = Theme.fonts.nunito,
                                    color = TextPrimaryColor
                                )
                            }
                            directoryTypes.forEach { dir ->
                                DropdownMenuItem(onClick = {
                                    selectedDirectory = dir
                                    currentPage = 1
                                    showDirectoryDropdown = false
                                }) {
                                    Text(
                                        dir,
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
                                                    val documents = authManager.getAllDocuments()
                                                    val patients = authManager.getAllPatients()
                                                    val users = authManager.getAllUsers()
                                                    allDocuments = documents
                                                    allPatients = patients
                                                    allUsers = users
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
                    allDocuments.isEmpty() -> {
                        EmptyStateView(
                            message = "Пока что данные отсутствуют",
                            actionLabel = "Загрузить документ",
                            onAction = onNavigateToCreateDocument
                        )
                    }
                    filteredDocs.isEmpty() -> {
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
                                itemsIndexed(paginatedDocs) { index, doc ->
                                    DocumentItem(
                                        document = doc,
                                        patientName = getPatientName(doc.patient_id),
                                        authorName = getAuthorName(doc.author_id),
                                        onDelete = {
                                            selectedDoc = doc
                                            showDeleteDialog = true
                                        },
                                        onDownload = { handleDownload(doc.id, doc.name) },
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

        if (showDeleteDialog && selectedDoc != null) {
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
                        text = "Вы уверены, что хотите удалить документ \"${selectedDoc?.name}\"?",
                        fontFamily = Theme.fonts.nunito,
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedDoc?.let { handleDelete(it) }
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
fun DocumentItem(
    document: Document,
    patientName: String,
    authorName: String,
    onDelete: () -> Unit,
    onDownload: () -> Unit,
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
                        text = document.name,
                        fontFamily = Theme.fonts.nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "ID: ${document.id}",
                        fontFamily = Theme.fonts.nunito,
                        fontSize = 14.sp,
                        color = TextSecondaryColor.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Пациент: $patientName",
                    fontFamily = Theme.fonts.nunito,
                    fontSize = 14.sp,
                    color = TextSecondaryColor
                )

                Text(
                    text = "Директория: ${document.subdirectory_type}",
                    fontFamily = Theme.fonts.nunito,
                    fontSize = 14.sp,
                    color = TextSecondaryColor
                )

                Text(
                    text = "Автор: $authorName",
                    fontFamily = Theme.fonts.nunito,
                    fontSize = 14.sp,
                    color = TextSecondaryColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFA3F49F),
                            contentColor = TextPrimaryColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Скачать",
                            fontFamily = Theme.fonts.nunito,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
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