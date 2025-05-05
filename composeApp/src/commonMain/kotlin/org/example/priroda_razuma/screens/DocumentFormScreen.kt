//package org.example.priroda_razuma.screens
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.launch
//import org.example.priroda_razuma.auth.AuthManager
//import org.example.priroda_razuma.models.Document
//import org.example.priroda_razuma.models.Patient
//import org.example.priroda_razuma.models.SubDirectories
//import org.example.priroda_razuma.preferences.Theme
//import androidx.compose.ui.text.style.TextOverflow
//
//@Composable
//fun DocumentFormScreen(
//    authManager: AuthManager,
//    isEdit: Boolean = false,
//    documentId: Int? = null,
//    onNavigateBack: () -> Unit
//) {
//    var name by remember { mutableStateOf("") }
//    var file by remember { mutableStateOf<File?>(null) }
//    var subdirectoryType by remember { mutableStateOf(SubDirectories.ANAMNESIS) }
//    var patientId by remember { mutableStateOf(0) }
//    var patients by remember { mutableStateOf<List<Patient>>(emptyList()) }
//    var errors by remember { mutableStateOf(mapOf<String, String>()) }
//    var error by remember { mutableStateOf<String?>(null) }
//    var isLoading by remember { mutableStateOf(false) }
//    val coroutineScope = rememberCoroutineScope()
//
//    val filePicker = FilePicker()
//    val cameraLauncher = CameraLauncher()
//
//    LaunchedEffect(isEdit, documentId) {
//        try {
//            val fetchedPatients = authManager.getAllPatients()
//            patients = fetchedPatients
//            if (isEdit && documentId != null) {
//                isLoading = true
//                val document = authManager.getDocumentById(documentId)
//                name = document.name
//                subdirectoryType = SubDirectories.valueOf(document.subdirectory_type)
//                patientId = document.patient_id
//            }
//        } catch (e: Exception) {
//            error = "Ошибка загрузки данных"
//        } finally {
//            isLoading = false
//        }
//    }
//
//    fun validateField(name: String, value: Any): String? {
//        return when (name) {
//            "name" -> if (value.toString().isBlank()) "Название обязательно" else null
//            "file" -> if (!isEdit && value == null) "Файл обязателен" else null
//            "patient_id" -> if (value == 0) "Выберите пациента" else null
//            "subdirectory_type" -> null // Assuming always valid
//            else -> null
//        }
//    }
//
//    fun handleSubmit() {
//        val newErrors = mapOf(
//            "name" to validateField("name", name),
//            "file" to validateField("file", file),
//            "patient_id" to validateField("patient_id", patientId),
//            "subdirectory_type" to validateField("subdirectory_type", subdirectoryType)
//        ).filterValues { it != null }
//
//        if (newErrors.isNotEmpty()) {
//            errors = newErrors as Map<String, String>
//            return
//        }
//
//        coroutineScope.launch {
//            isLoading = true
//            try {
//                val document = Document(
//                    id = documentId ?: 0,
//                    name = name,
//                    patient_id = patientId,
//                    subdirectory_type = subdirectoryType.name,
//                    author_id = authManager.userId
//                )
//                val success = if (isEdit && documentId != null) {
//                    authManager.updateDocument(documentId, document, file)
//                } else {
//                    authManager.createDocument(document, file)
//                }
//                if (success) onNavigateBack()
//                else error = "Ошибка при сохранении документа"
//            } catch (e: Exception) {
//                error = e.message ?: "Неизвестная ошибка"
//            } finally {
//                isLoading = false
//            }
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White),
//        contentAlignment = Alignment.Center
//    ) {
//        Card(
//            modifier = Modifier
//                .padding(20.dp)
//                .shadow(8.dp, RoundedCornerShape(15.dp))
//                .background(Color.White),
//            backgroundColor = Color.White,
//            elevation = 0.dp
//        ) {
//            Column(
//                modifier = Modifier.padding(32.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = if (isEdit) "Редактирование документа" else "Создание нового документа",
//                    fontFamily = Theme.fonts.nunito,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 24.sp,
//                    color = Color(0xFF2C3E50),
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
//                Box(
//                    modifier = Modifier
//                        .width(100.dp)
//                        .height(3.dp)
//                        .background(Color(0xFFD3E29F))
//                )
//                Spacer(modifier = Modifier.height(24.dp))
//
//                if (error != null) {
//                    AlertDialog(
//                        onDismissRequest = { error = null },
//                        title = { Text("Ошибка") },
//                        text = { Text(error ?: "") },
//                        confirmButton = {
//                            Button(onClick = { error = null }) {
//                                Text("OK")
//                            }
//                        },
//                        backgroundColor = Color.White,
//                        shape = RoundedCornerShape(10.dp)
//                    )
//                }
//
//                OutlinedTextField(
//                    value = name,
//                    onValueChange = { name = it },
//                    label = { Text("Название документа*", color = Color(0xFF34495E)) },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = !isLoading,
//                    readOnly = true, // Name is set from file
//                    isError = errors.containsKey("name"),
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedBorderColor = Color(0xFFA3F49F),
//                        unfocusedBorderColor = Color(0xFFCED4DA)
//                    ),
//                    shape = RoundedCornerShape(10.dp)
//                )
//                if (errors.containsKey("name")) {
//                    Text(
//                        text = errors["name"] ?: "",
//                        color = Color.Red,
//                        fontSize = 12.sp,
//                        modifier = Modifier.padding(start = 16.dp)
//                    )
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // File selection buttons
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Button(
//                        onClick = {
//                            coroutineScope.launch {
//                                val pickedFile = filePicker.launch()
//                                if (pickedFile != null) {
//                                    file = pickedFile
//                                    name = pickedFile.name
//                                    errors = errors - "file" - "name"
//                                }
//                            }
//                        },
//                        enabled = !isLoading,
//                        colors = ButtonDefaults.buttonColors(
//                            backgroundColor = Color(0xFFF5F9E7),
//                            contentColor = Color(0xFF6B8E23)
//                        ),
//                        shape = RoundedCornerShape(10.dp)
//                    ) {
//                        Text("Выбрать файл")
//                    }
//                    Button(
//                        onClick = {
//                            coroutineScope.launch {
//                                val photoFile = cameraLauncher.launch()
//                                if (photoFile != null) {
//                                    file = photoFile
//                                    name = photoFile.name
//                                    errors = errors - "file" - "name"
//                                }
//                            }
//                        },
//                        enabled = !isLoading,
//                        colors = ButtonDefaults.buttonColors(
//                            backgroundColor = Color(0xFFF5F9E7),
//                            contentColor = Color(0xFF6B8E23)
//                        ),
//                        shape = RoundedCornerShape(10.dp)
//                    ) {
//                        Text("Сделать фото")
//                    }
//                }
//                if (file != null) {
//                    Text(
//                        text = "Выбран файл: ${file.name}",
//                        color = Color(0xFF2C3E50),
//                        fontSize = 14.sp,
//                        modifier = Modifier.padding(top = 8.dp)
//                    )
//                } else if (isEdit) {
//                    Text(
//                        text = "Оставьте пустым для текущего файла",
//                        color = Color(0xFF6C757D),
//                        fontSize = 12.sp,
//                        modifier = Modifier.padding(top = 8.dp)
//                    )
//                }
//                if (errors.containsKey("file")) {
//                    Text(
//                        text = errors["file"] ?: "",
//                        color = Color.Red,
//                        fontSize = 12.sp,
//                        modifier = Modifier.padding(top = 8.dp)
//                    )
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Subdirectory type dropdown
//                var expanded by remember { mutableStateOf(false) }
//                Box {
//                    Button(
//                        onClick = { expanded = true },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(54.dp)
//                            .shadow(4.dp, RoundedCornerShape(16.dp)),
//                        colors = ButtonDefaults.buttonColors(
//                            backgroundColor = Color.White,
//                            contentColor = Color(0xFF2C3E50)
//                        ),
//                        shape = RoundedCornerShape(16.dp)
//                    ) {
//                        Text(
//                            text = subdirectoryType.name,
//                            fontFamily = Theme.fonts.nunito,
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis
//                        )
//                    }
//                    DropdownMenu(
//                        expanded = expanded,
//                        onDismissRequest = { expanded = false },
//                        modifier = Modifier.background(Color.White)
//                    ) {
//                        SubDirectories.values().forEach { dir ->
//                            DropdownMenuItem(onClick = {
//                                subdirectoryType = dir
//                                expanded = false
//                            }) {
//                                Text(dir.name, fontFamily = Theme.fonts.nunito)
//                            }
//                        }
//                    }
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Patient dropdown
//                var patientExpanded by remember { mutableStateOf(false) }
//                Box {
//                    Button(
//                        onClick = { patientExpanded = true },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(54.dp)
//                            .shadow(4.dp, RoundedCornerShape(16.dp)),
//                        colors = ButtonDefaults.buttonColors(
//                            backgroundColor = Color.White,
//                            contentColor = Color(0xFF2C3E50)
//                        ),
//                        shape = RoundedCornerShape(16.dp)
//                    ) {
//                        Text(
//                            text = if (patientId == 0) "Выберите пациента" else patients.find { it.id == patientId }?.fio ?: "",
//                            fontFamily = Theme.fonts.nunito,
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis
//                        )
//                    }
//                    DropdownMenu(
//                        expanded = patientExpanded,
//                        onDismissRequest = { patientExpanded = false },
//                        modifier = Modifier.background(Color.White)
//                    ) {
//                        patients.forEach { patient ->
//                            DropdownMenuItem(onClick = {
//                                patientId = patient.id
//                                patientExpanded = false
//                                errors = errors - "patient_id"
//                            }) {
//                                Text(patient.fio, fontFamily = Theme.fonts.nunito)
//                            }
//                        }
//                    }
//                }
//                if (errors.containsKey("patient_id")) {
//                    Text(
//                        text = errors["patient_id"] ?: "",
//                        color = Color.Red,
//                        fontSize = 12.sp,
//                        modifier = Modifier.padding(start = 16.dp)
//                    )
//                }
//                Spacer(modifier = Modifier.height(24.dp))
//
//                Row(
//                    horizontalArrangement = Arrangement.End,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    TextButton(
//                        onClick = onNavigateBack,
//                        enabled = !isLoading,
//                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6C757D))
//                    ) {
//                        Text("Отмена", fontWeight = FontWeight.Medium)
//                    }
//                    Spacer(modifier = Modifier.width(16.dp))
//                    Button(
//                        onClick = { handleSubmit() },
//                        enabled = !isLoading,
//                        colors = ButtonDefaults.buttonColors(
//                            backgroundColor = Color(0xFFD3E29F),
//                            contentColor = Color.White
//                        ),
//                        shape = RoundedCornerShape(8.dp),
//                        modifier = Modifier.padding(vertical = 4.dp)
//                    ) {
//                        if (isLoading) {
//                            CircularProgressIndicator(
//                                modifier = Modifier.size(24.dp),
//                                color = Color.White
//                            )
//                        } else {
//                            Text("Сохранить", fontWeight = FontWeight.Medium)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//expect class FilePicker() {
//    suspend fun launch(): File?
//}
//
//expect class CameraLauncher() {
//    suspend fun launch(): File?
//}