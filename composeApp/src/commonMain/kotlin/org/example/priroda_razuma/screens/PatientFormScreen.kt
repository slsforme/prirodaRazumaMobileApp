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
import kotlinx.datetime.toLocalDateTime
import org.example.priroda_razuma.auth.AuthManager
import org.example.priroda_razuma.models.Patient
import org.example.priroda_razuma.preferences.Theme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

@Composable
fun DatePickerDialog(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialDate = if (selectedDate.isNotEmpty()) {
        try {
            val (year, month, day) = parseDate(selectedDate)
            LocalDate(year, month, day)
        } catch (e: Exception) {
            getCurrentLocalDate()
        }
    } else {
        getCurrentLocalDate()
    }

    var currentMonth = remember { mutableStateOf(initialDate.month) }
    var currentYear = remember { mutableStateOf(initialDate.year) }
    var selectedDay = remember { mutableStateOf(initialDate.dayOfMonth) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            backgroundColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var monthMenuExpanded by remember { mutableStateOf(false) }
                    val russianMonths = listOf(
                        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { monthMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFD3E29F),
                                contentColor = Color(0xFF2C3E50)
                            ),
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = russianMonths[currentMonth.value.ordinal],
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Выбрать месяц"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = monthMenuExpanded,
                            onDismissRequest = { monthMenuExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .width(with(LocalDensity.current) { 180.dp })
                        ) {
                            russianMonths.forEachIndexed { index, monthName ->
                                DropdownMenuItem(
                                    onClick = {
                                        currentMonth.value = Month.entries[index]
                                        monthMenuExpanded = false
                                    }
                                ) {
                                    Text(
                                        text = monthName,
                                        color = if (index == currentMonth.value.ordinal)
                                            Color(0xFF4CAF50) else Color(0xFF2C3E50),
                                        fontWeight = if (index == currentMonth.value.ordinal)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    var yearMenuExpanded by remember { mutableStateOf(false) }
                    val currentSystemYear = getCurrentLocalDate().year
                    val yearRange = (currentSystemYear - 100)..currentSystemYear

                    Box(modifier = Modifier.weight(0.7f)) {
                        Button(
                            onClick = { yearMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFD3E29F),
                                contentColor = Color(0xFF2C3E50)
                            ),
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = currentYear.value.toString(),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Выбрать год"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = yearMenuExpanded,
                            onDismissRequest = { yearMenuExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .height(250.dp)
                        ) {
                            yearRange.reversed().forEach { year ->
                                DropdownMenuItem(
                                    onClick = {
                                        currentYear.value = year
                                        yearMenuExpanded = false
                                    }
                                ) {
                                    Text(
                                        text = year.toString(),
                                        color = if (year == currentYear.value)
                                            Color(0xFF4CAF50) else Color(0xFF2C3E50),
                                        fontWeight = if (year == currentYear.value)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val daysInMonth = getDaysInMonth(currentYear.value, currentMonth.value.ordinal + 1)
                val firstDayOfWeek = getFirstDayOfWeekForMonth(currentYear.value, currentMonth.value.ordinal + 1)
                val numRows = (daysInMonth + firstDayOfWeek + 6) / 7

                Card(
                    shape = RoundedCornerShape(10.dp),
                    elevation = 2.dp,
                    backgroundColor = Color(0xFFF8F9FA),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = Color(0xFF5D6D7E),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Divider(
                            color = Color(0xFFD3E29F),
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        for (i in 0 until numRows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (j in 1..7) {
                                    val day = i * 7 + j - firstDayOfWeek

                                    if (day in 1..daysInMonth) {
                                        val isSelected = day == selectedDay.value &&
                                                currentMonth.value == initialDate.month &&
                                                currentYear.value == initialDate.year
                                        val isFutureDate = currentYear.value > getCurrentLocalDate().year ||
                                                (currentYear.value == getCurrentLocalDate().year &&
                                                        currentMonth.value.ordinal + 1 > getCurrentLocalDate().monthNumber) ||
                                                (currentYear.value == getCurrentLocalDate().year &&
                                                        currentMonth.value.ordinal + 1 == getCurrentLocalDate().monthNumber &&
                                                        day > getCurrentLocalDate().dayOfMonth)
                                        val isToday = day == getCurrentLocalDate().dayOfMonth &&
                                                currentMonth.value.ordinal + 1 == getCurrentLocalDate().monthNumber &&
                                                currentYear.value == getCurrentLocalDate().year

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(2.dp)
                                                .background(
                                                    when {
                                                        isSelected -> Color(0xFFD3E29F)
                                                        isToday -> Color(0xFFE8F5E9)
                                                        else -> Color.Transparent
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .clickable(enabled = !isFutureDate) {
                                                    selectedDay.value = day
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = day.toString(),
                                                color = when {
                                                    isFutureDate -> Color.LightGray
                                                    isSelected -> Color(0xFF2C3E50)
                                                    isToday -> Color(0xFF4CAF50)
                                                    else -> Color(0xFF34495E)
                                                },
                                                fontWeight = when {
                                                    isSelected || isToday -> FontWeight.Bold
                                                    else -> FontWeight.Normal
                                                },
                                                fontSize = 16.sp
                                            )
                                        }
                                    } else {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF8BC34A)
                        )
                    ) {
                        Text(
                            "Отмена",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            val selectedDate = formatDate(
                                currentYear.value,
                                currentMonth.value.ordinal + 1,
                                selectedDay.value
                            )
                            onDateSelected(selectedDate)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFD3E29F),
                            contentColor = Color(0xFF2C3E50)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Text(
                            "Выбрать",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    enabled: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = { /* Read-only */ },
        label = { Text(label, color = Color(0xFF34495E)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        isError = isError,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFFA3F49F),
            unfocusedBorderColor = Color(0xFFCED4DA)
        ),
        shape = RoundedCornerShape(10.dp),
        readOnly = true,
        trailingIcon = {
            Icon(
                Icons.Default.DateRange,
                contentDescription = "Select Date",
                modifier = Modifier.clickable(enabled = enabled) { showDialog = true }
            )
        }
    )

    if (showDialog) {
        DatePickerDialog(
            selectedDate = value,
            onDateSelected = onValueChange,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun PatientFormScreen(
    authManager: AuthManager,
    isEdit: Boolean = false,
    patientId: Int? = null,
    onNavigateBack: () -> Unit
) {
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var patronymic by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isEdit, patientId) {
        if (isEdit && patientId != null) {
            isLoading = true
            try {
                val patient = authManager.getPatientById(patientId)
                val parts = patient.fio.split(" ")
                lastName = parts.getOrNull(0) ?: ""
                firstName = parts.getOrNull(1) ?: ""
                patronymic = parts.getOrNull(2) ?: ""
                birthDate = patient.date_of_birth
            } catch (e: Exception) {
                error = "Ошибка загрузки данных пациента"
            } finally {
                isLoading = false
            }
        }
    }

    fun validateField(name: String, value: String): String? {
        val cyrillicRegex = Regex("^[а-яА-ЯёЁ\\- ]+$")
        return when (name) {
            "lastName", "firstName" -> {
                if (value.isBlank()) "Поле обязательно для заполнения"
                else if (value.length < 2) "Минимальная длина - 2 символа"
                else if (value.length > 100) "Максимальная длина - 100 символов"
                else if (!cyrillicRegex.matches(value)) "Допустимы только кириллические символы"
                else null
            }
            "patronymic" -> {
                if (value.isNotEmpty()) {
                    if (value.length > 100) "Максимальная длина - 100 символов"
                    else if (!cyrillicRegex.matches(value)) "Допустимы только кириллические символы"
                    else null
                } else null
            }
            "birthDate" -> {
                if (value.isBlank()) "Дата рождения обязательна"
                else {
                    try {
                        val (year, month, day) = parseDate(value)
                        if (!isValidDate(year, month, day)) "Неверная дата"
                        else if (isFutureDate(year, month, day)) "Дата рождения не может быть в будущем"
                        else null
                    } catch (e: Exception) {
                        "Неверный формат даты (гггг-мм-дд)"
                    }
                }
            }
            else -> null
        }
    }

    fun handleSubmit() {
        val newErrors = mapOf(
            "lastName" to validateField("lastName", lastName),
            "firstName" to validateField("firstName", firstName),
            "patronymic" to validateField("patronymic", patronymic),
            "birthDate" to validateField("birthDate", birthDate)
        ).filterValues { it != null }

        if (newErrors.isNotEmpty()) {
            errors = newErrors as Map<String, String>
            return
        }

        val fio = listOf(lastName, firstName, patronymic).filter { it.isNotBlank() }.joinToString(" ")

        coroutineScope.launch {
            isLoading = true
            try {
                if (isEdit && patientId != null) {
                    val updatedPatient = Patient(
                        id = patientId,
                        fio = fio,
                        date_of_birth = birthDate,
                        created_at = "",
                        updated_at = ""
                    )
                    val success = authManager.updatePatient(patientId, updatedPatient)
                    if (success) onNavigateBack()
                    else error = "Ошибка при обновлении пациента"
                } else {
                    val newPatient = Patient(
                        id = 0,
                        fio = fio,
                        date_of_birth = birthDate,
                        created_at = "",
                        updated_at = ""
                    )
                    val success = authManager.createPatient(newPatient)
                    if (success) onNavigateBack()
                    else error = "Ошибка при создании пациента"
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
                    text = if (isEdit) "Редактирование экспозе ребёнка" else "Создание нового экспозе ребёнка",
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
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Фамилия*", color = Color(0xFF34495E)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = errors.containsKey("lastName"),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFA3F49F),
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        cursorColor = Color(0xFFA3F49F),
                        focusedLabelColor = Color(0xFFA3F49F),
                        textColor = Color(0xFF34495E),
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                if (errors.containsKey("lastName")) {
                    Text(
                        text = errors["lastName"] ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Имя*", color = Color(0xFF34495E)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = errors.containsKey("firstName"),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFA3F49F),
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        cursorColor = Color(0xFFA3F49F),
                        focusedLabelColor = Color(0xFFA3F49F),
                        textColor = Color(0xFF34495E),
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                if (errors.containsKey("firstName")) {
                    Text(
                        text = errors["firstName"] ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = patronymic,
                    onValueChange = { patronymic = it },
                    label = { Text("Отчество", color = Color(0xFF34495E)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = errors.containsKey("patronymic"),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFA3F49F),
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        cursorColor = Color(0xFFA3F49F),
                        focusedLabelColor = Color(0xFFA3F49F),
                        textColor = Color(0xFF34495E),
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                if (errors.containsKey("patronymic")) {
                    Text(
                        text = errors["patronymic"] ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                DatePickerField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = "Дата рождения*",
                    isError = errors.containsKey("birthDate"),
                    enabled = !isLoading
                )

                if (errors.containsKey("birthDate")) {
                    Text(
                        text = errors["birthDate"] ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Возраст: ${calculateAge(birthDate)}",
                    fontFamily = Theme.fonts.nunito,
                    fontSize = 16.sp,
                    color = Color(0xFF34495E)
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

fun calculateAge(birthDate: String): String {
    if (birthDate.isBlank()) return ""
    return try {
        val (birthYear, birthMonth, birthDay) = parseDate(birthDate)
        val (currentYear, currentMonth, currentDay) = getCurrentDate()

        var age = currentYear - birthYear
        if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
            age--
        }
        age.toString()
    } catch (e: Exception) {
        "Неверная дата"
    }
}

private fun parseDate(dateString: String): Triple<Int, Int, Int> {
    val parts = dateString.split("-")
    if (parts.size != 3) throw IllegalArgumentException()
    return Triple(
        parts[0].toInt(),
        parts[1].toInt().coerceIn(1..12),
        parts[2].toInt().coerceIn(1..31)
    )
}

private fun isValidDate(year: Int, month: Int, day: Int): Boolean {
    val maxDay = when (month) {
        2 -> if (year % 4 == 0) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return day in 1..maxDay
}

private fun isFutureDate(year: Int, month: Int, day: Int): Boolean {
    val (currentYear, currentMonth, currentDay) = getCurrentDate()
    return when {
        year > currentYear -> true
        year == currentYear && month > currentMonth -> true
        year == currentYear && month == currentMonth && day > currentDay -> true
        else -> false
    }
}

private fun getCurrentDate(): Triple<Int, Int, Int> {
    val currentTime = kotlinx.datetime.Clock.System.now()
    val currentDate = currentTime.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
    return Triple(
        currentDate.year,
        currentDate.monthNumber,
        currentDate.dayOfMonth
    )
}

fun getCurrentLocalDate(): LocalDate {
    val currentTime = kotlinx.datetime.Clock.System.now()
    return currentTime.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
}

fun getDaysInMonth(year: Int, month: Int): Int {
    return when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
}

fun isLeapYear(year: Int): Boolean {
    return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
}

fun getFirstDayOfWeekForMonth(year: Int, month: Int): Int {
    val t = arrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)

    var y = year
    if (month < 3) y -= 1

    val dayOfWeek = (y + y/4 - y/100 + y/400 + t[month-1] + 1) % 7
    return (dayOfWeek + 5) % 7
}

fun formatDate(year: Int, month: Int, day: Int): String {
    val formattedMonth = if (month < 10) "0$month" else month.toString()
    val formattedDay = if (day < 10) "0$day" else day.toString()
    return "$year-$formattedMonth-$formattedDay"
}