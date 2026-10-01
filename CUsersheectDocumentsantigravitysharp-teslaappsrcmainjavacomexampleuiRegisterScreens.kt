@Composable
fun SleepManualRegistrationForm(
    baby: BabyProfile,
    activityDateStr: String,
    onDateChange: (String) -> Unit,
    startTime: String,
    onStartTimeChange: (String) -> Unit,
    endTime: String,
    onEndTimeChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Detalles del sueño", style = Typography.labelLarge, color = PrimaryDark)

        PMOutlinedTextField(
            value = startTime,
            onValueChange = onStartTimeChange,
            label = { Text("Hora de inicio (Ej: 20:00)") },
            modifier = Modifier.fillMaxWidth()
        )

        PMOutlinedTextField(
            value = endTime,
            onValueChange = onEndTimeChange,
            label = { Text("Hora de fin (Ej: 07:00)") },
            modifier = Modifier.fillMaxWidth()
        )

        PMOutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notas (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

