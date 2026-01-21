package com.android.axion.sandbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.axion.sandbox.security.LockedAppBehavior
import com.android.axion.sandbox.security.PrivateSectionBehavior
import com.android.axion.sandbox.security.SecurityType

private object SettingsShapes {
    val card = RoundedCornerShape(24.dp)
    val item = RoundedCornerShape(16.dp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentSecurityType: SecurityType,
    currentLockedAppBehavior: LockedAppBehavior,
    currentLockedAppTimeout: Int,
    currentPrivateBehavior: PrivateSectionBehavior,
    currentPrivateTimeout: Int,
    isBiometricAvailable: Boolean,
    isBiometricEnabled: Boolean,
    isPreferBiometric: Boolean,
    hasSecurityQuestion: Boolean,
    onBackClick: () -> Unit,
    onChangeSecurityType: (SecurityType) -> Unit,
    onChangeLockedAppBehavior: (LockedAppBehavior) -> Unit,
    onChangeLockedAppTimeout: (Int) -> Unit,
    onChangePrivateBehavior: (PrivateSectionBehavior) -> Unit,
    onChangePrivateTimeout: (Int) -> Unit,
    onChangeBiometricEnabled: (Boolean) -> Unit,
    onChangePreferBiometric: (Boolean) -> Unit,
    onSetupRecovery: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var showLockedAppTimeoutDialog by remember { mutableStateOf(false) }
    var showPrivateTimeoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Private Apps Security",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SettingsShapes.card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    SettingsItem(
                        icon = when (currentSecurityType) {
                            SecurityType.PIN -> Icons.Outlined.Pin
                            SecurityType.PASSWORD -> Icons.Outlined.Password
                            SecurityType.PATTERN -> Icons.Outlined.Pattern
                            SecurityType.NONE -> Icons.Outlined.Lock
                        },
                        title = "Current Lock Type",
                        subtitle = when (currentSecurityType) {
                            SecurityType.PIN -> "PIN (4-6 digits)"
                            SecurityType.PASSWORD -> "Password"
                            SecurityType.PATTERN -> "Pattern"
                            SecurityType.NONE -> "Not set"
                        },
                        onClick = null
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Pin,
                        title = "Use PIN",
                        subtitle = "4-6 digit code",
                        isSelected = currentSecurityType == SecurityType.PIN,
                        onClick = { onChangeSecurityType(SecurityType.PIN) }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Password,
                        title = "Use Password",
                        subtitle = "Alphanumeric password",
                        isSelected = currentSecurityType == SecurityType.PASSWORD,
                        onClick = { onChangeSecurityType(SecurityType.PASSWORD) }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Pattern,
                        title = "Use Pattern",
                        subtitle = "Draw pattern to unlock",
                        isSelected = currentSecurityType == SecurityType.PATTERN,
                        onClick = { onChangeSecurityType(SecurityType.PATTERN) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isBiometricAvailable) {
                Text(
                    text = "Biometrics",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SettingsShapes.card,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Fingerprint,
                            title = "Unlock with Biometrics",
                            subtitle = "Use fingerprint or face unlock",
                            checked = isBiometricEnabled,
                            onCheckedChange = onChangeBiometricEnabled
                        )

                        if (isBiometricEnabled) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            SettingsSwitchItem(
                                icon = Icons.Outlined.AutoMode,
                                title = "Auto-show Biometric Prompt",
                                subtitle = "Automatically show biometric prompt when opening locked apps",
                                checked = isPreferBiometric,
                                onCheckedChange = onChangePreferBiometric
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            if (currentSecurityType != SecurityType.NONE) {
                Text(
                    text = "Recovery Options",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SettingsShapes.card,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        if (hasSecurityQuestion) {
                            SettingsItem(
                                icon = Icons.Outlined.LockReset,
                                title = "Forgot Password",
                                subtitle = "Reset your password using security question",
                                onClick = onForgotPassword
                            )
                        } else {
                            SettingsItem(
                                icon = Icons.Outlined.Help,
                                title = "Set Up Security Question",
                                subtitle = "Required for password recovery",
                                onClick = onSetupRecovery
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            Text(
                text = "Locked App Behavior",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
            
            Text(
                text = "Controls when unlocked apps re-lock",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SettingsShapes.card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Outlined.ExitToApp,
                        title = "Re-lock when leaving app",
                        subtitle = "Re-lock immediately when leaving the unlocked app",
                        isSelected = currentLockedAppBehavior == LockedAppBehavior.ON_LEAVE,
                        onClick = { onChangeLockedAppBehavior(LockedAppBehavior.ON_LEAVE) }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Timer,
                        title = "Re-lock after timeout",
                        subtitle = "Re-lock after ${currentLockedAppTimeout}s of inactivity",
                        isSelected = currentLockedAppBehavior == LockedAppBehavior.TIMEOUT,
                        onClick = { 
                            onChangeLockedAppBehavior(LockedAppBehavior.TIMEOUT)
                            showLockedAppTimeoutDialog = true
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Smartphone,
                        title = "Re-lock on screen off",
                        subtitle = "Keep unlocked when switching apps, lock on screen off",
                        isSelected = currentLockedAppBehavior == LockedAppBehavior.ON_SCREEN_OFF,
                        onClick = { onChangeLockedAppBehavior(LockedAppBehavior.ON_SCREEN_OFF) }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Close,
                        title = "Re-lock only when killed",
                        subtitle = "Keep unlocked until Sandbox is force closed",
                        isSelected = currentLockedAppBehavior == LockedAppBehavior.ON_KILL,
                        onClick = { onChangeLockedAppBehavior(LockedAppBehavior.ON_KILL) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Private Section Behavior",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
            
            Text(
                text = "Controls when the private apps section in this app locks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SettingsShapes.card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Outlined.ExitToApp,
                        title = "Collapse when leaving",
                        subtitle = "Collapse and lock when leaving Sandbox app",
                        isSelected = currentPrivateBehavior == PrivateSectionBehavior.ON_LEAVE,
                        onClick = { onChangePrivateBehavior(PrivateSectionBehavior.ON_LEAVE) }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Timer,
                        title = "Collapse after timeout",
                        subtitle = "Collapsed after ${currentPrivateTimeout}s of leaving the app",
                        isSelected = currentPrivateBehavior == PrivateSectionBehavior.TIMEOUT,
                        onClick = { 
                            onChangePrivateBehavior(PrivateSectionBehavior.TIMEOUT)
                            showPrivateTimeoutDialog = true
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "About",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SettingsShapes.card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = "Version",
                        subtitle = "1.0.0",
                        onClick = null
                    )
                }
            }
        }
    }
    
    if (showLockedAppTimeoutDialog) {
        TimeoutPickerDialog(
            currentTimeout = currentLockedAppTimeout,
            onDismiss = { showLockedAppTimeoutDialog = false },
            onConfirm = { timeout ->
                onChangeLockedAppTimeout(timeout)
                showLockedAppTimeoutDialog = false
            }
        )
    }
    
    if (showPrivateTimeoutDialog) {
        TimeoutPickerDialog(
            currentTimeout = currentPrivateTimeout,
            onDismiss = { showPrivateTimeoutDialog = false },
            onConfirm = { timeout ->
                onChangePrivateTimeout(timeout)
                showPrivateTimeoutDialog = false
            }
        )
    }
}

@Composable
private fun TimeoutPickerDialog(
    currentTimeout: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val timeoutOptions = listOf(15, 30, 60, 120, 300)
    var selectedTimeout by remember { mutableStateOf(currentTimeout) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lock Timeout") },
        text = {
            Column {
                timeoutOptions.forEach { seconds ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedTimeout = seconds }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTimeout == seconds,
                            onClick = { selectedTimeout = seconds }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (seconds < 60) "$seconds seconds" 
                                   else "${seconds / 60} minute${if (seconds >= 120) "s" else ""}"
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedTimeout) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean = false,
    isDestructive: Boolean = false,
    onClick: (() -> Unit)?
) {
    val contentColor = when {
        isDestructive -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SettingsShapes.item)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else if (isDestructive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceContainerHigh
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SettingsShapes.item)
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(checked.let { 
                    if (it) MaterialTheme.colorScheme.primaryContainer 
                    else MaterialTheme.colorScheme.surfaceContainerHigh 
                }),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
    }
}

