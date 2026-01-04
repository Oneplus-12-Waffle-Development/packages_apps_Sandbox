package com.android.axion.sandbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.axion.sandbox.security.SandboxSecurityManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityQuestionSetupScreen(
    onComplete: (question: String, answer: String) -> Unit,
    onSkip: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    var selectedQuestion by remember { mutableStateOf<String?>(null) }
    var answer by remember { mutableStateOf("") }
    var showQuestionPicker by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = "Recovery Option",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Set up a security question in case you forget your password",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Security Question",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showQuestionPicker = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedQuestion ?: "Select a question",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedQuestion != null) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text("Your Answer") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = selectedQuestion != null,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    selectedQuestion?.let { q ->
                        onComplete(q, answer)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedQuestion != null && answer.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            if (onSkip != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Skip for now",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    if (showQuestionPicker) {
        AlertDialog(
            onDismissRequest = { showQuestionPicker = false },
            title = { Text("Select a Question") },
            text = {
                Column {
                    SandboxSecurityManager.SECURITY_QUESTIONS.forEach { question ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { 
                                    selectedQuestion = question
                                    showQuestionPicker = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = question,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (selectedQuestion == question) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQuestionPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
