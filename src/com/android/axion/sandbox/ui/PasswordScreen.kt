/*
 * Copyright (C) 2025-2026 AxionOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.axion.sandbox.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.axion.sandbox.R
import com.android.axion.sandbox.security.SandboxSecurityManager

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PasswordScreen(
    isSetup: Boolean = false,
    promptText: String? = null,
    onUnlock: () -> Unit,
    onPasswordEntered: (String) -> Boolean,
    confirmPassword: String? = null,
    onBack: (() -> Unit)? = null,
    biometricType: SandboxSecurityManager.BiometricType = SandboxSecurityManager.BiometricType.NONE,
    onBiometricClick: () -> Unit = {},
    onForgotPassword: (() -> Unit)? = null,
    isExiting: Boolean = false
) {
    var enteredPassword by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val motionScheme = MaterialTheme.motionScheme
    val errorMismatch = stringResource(R.string.password_error_mismatch)
    val errorIncorrect = stringResource(R.string.password_error_incorrect)

    LaunchedEffect(confirmPassword) {
        enteredPassword = ""
        isError = false
    }

    val shakeOffset by animateFloatAsState(
        targetValue = if (isError) 1f else 0f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "shake",
        finishedListener = {
            if (isError) isError = false
        }
    )

    val shakeTranslation = if (isError) {
        kotlin.math.sin(shakeOffset * 4 * Math.PI.toFloat()) * 16f
    } else 0f

    fun submit() {
        if (enteredPassword.length >= 4) {
            val success = onPasswordEntered(enteredPassword)
            if (success) {
                onUnlock()
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isError = true
                errorMessage = if (isSetup && confirmPassword != null) errorMismatch else errorIncorrect
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
                .widthIn(max = 400.dp)
        ) {
            Text(
                text = if (isSetup) {
                    if (confirmPassword == null) stringResource(R.string.password_title_create)
                    else stringResource(R.string.password_title_confirm)
                } else stringResource(R.string.password_title_enter),
                style = MaterialTheme.typography.headlineMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = promptText ?: if (isSetup) {
                    if (confirmPassword == null) stringResource(R.string.password_prompt_create)
                    else stringResource(R.string.password_prompt_confirm)
                } else stringResource(R.string.password_prompt_unlock),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = enteredPassword,
                onValueChange = {
                    enteredPassword = it
                    if (isError) isError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationX = shakeTranslation },
                label = { Text(stringResource(R.string.password_label)) },
                placeholder = { Text(stringResource(R.string.password_placeholder)) },
                singleLine = true,
                visualTransformation = if (isPasswordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility
                                          else Icons.Default.VisibilityOff,
                            contentDescription = if (isPasswordVisible)
                                stringResource(R.string.action_hide_password)
                            else stringResource(R.string.action_show_password)
                        )
                    }
                },
                isError = isError,
                supportingText = if (isError) {
                    { Text(errorMessage, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                } else null,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { submit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = enteredPassword.length >= 4,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(
                    text = if (isSetup) {
                        if (confirmPassword == null) stringResource(R.string.action_continue)
                        else stringResource(R.string.action_set_password)
                    } else stringResource(R.string.action_unlock),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (!isSetup && biometricType != SandboxSecurityManager.BiometricType.NONE) {
                Spacer(modifier = Modifier.height(24.dp))
                IconButton(
                    onClick = onBiometricClick,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = if (biometricType == SandboxSecurityManager.BiometricType.FACE)
                            Icons.Filled.Face else Icons.Filled.Fingerprint,
                        contentDescription = stringResource(R.string.action_biometric_unlock),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (!isSetup && onForgotPassword != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onForgotPassword) {
                    Text(
                        text = stringResource(R.string.action_forgot_password),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
