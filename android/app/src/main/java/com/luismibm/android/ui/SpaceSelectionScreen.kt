package com.luismibm.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luismibm.android.api.RetrofitClient
import com.luismibm.android.auth.Space
import com.luismibm.android.auth.SpaceRequest
import com.luismibm.android.auth.UpdateSpaceRequest
import kotlinx.coroutines.launch

@Composable
fun SpaceSelectionScreen(
    token: String,
    onSpaceSelected: () -> Unit,
    onError: (String) -> Unit
) {
    var spaces by remember { mutableStateOf<List<Space>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isCreatingSpace by remember { mutableStateOf(false) }
    var newSpaceName by remember { mutableStateOf("") }
    var showCreateForm by remember { mutableStateOf(false) }
    var isUpdatingUserSpace by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(key1 = token) {
        try {
            spaces = RetrofitClient.authService.getSpaces("Bearer $token")
            isLoading = false
        } catch (e: Exception) {
            onError("Error al cargar espacios: ${e.message}")
            isLoading = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Selecciona un Espacio",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando espacios...",
                        color = Color.White
                    )
                }
            } else if (spaces.isEmpty() && !showCreateForm) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No hay espacios disponibles",
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showCreateForm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1DB954),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Crear un nuevo espacio",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            } else if (showCreateForm) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nombre del espacio",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newSpaceName,
                        onValueChange = { newSpaceName = it },
                        placeholder = {
                            Text(
                                text = "Mi espacio",
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedContainerColor = Color.DarkGray,
                            focusedContainerColor = Color.DarkGray,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showCreateForm = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Cancelar",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        Button(
                            onClick = {
                                isCreatingSpace = true
                                scope.launch {
                                    try {
                                        // Crear nuevo espacio
                                        val spaceRequest = SpaceRequest(name = newSpaceName)
                                        val createdSpace = RetrofitClient.authService.createSpace("Bearer $token", spaceRequest)
                                        
                                        // Asignar el espacio al usuario
                                        val updateRequest = UpdateSpaceRequest(spaceId = createdSpace.id)
                                        RetrofitClient.authService.updateUserSpace("Bearer $token", updateRequest)
                                        
                                        onSpaceSelected()
                                    } catch (e: Exception) {
                                        onError("Error al crear espacio: ${e.message}")
                                        isCreatingSpace = false
                                    }
                                }
                            },
                            enabled = newSpaceName.isNotEmpty() && !isCreatingSpace,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1DB954),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isCreatingSpace) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                Text(
                                    text = "Crear",
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(spaces) { space ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.DarkGray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isUpdatingUserSpace = true
                                    scope.launch {
                                        try {
                                            // Vincular el espacio seleccionado al usuario
                                            val updateRequest = UpdateSpaceRequest(spaceId = space.id)
                                            RetrofitClient.authService.updateUserSpace("Bearer $token", updateRequest)
                                            onSpaceSelected()
                                        } catch (e: Exception) {
                                            onError("Error al vincular espacio: ${e.message}")
                                            isUpdatingUserSpace = false
                                        }
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = space.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                
                                if (isUpdatingUserSpace) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showCreateForm = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1DB954),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Crear un nuevo espacio",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
} 