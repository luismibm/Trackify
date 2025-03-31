package com.luismibm.trackify

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.luismibm.trackify.screen.HomeScreen
import com.luismibm.trackify.screen.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun MainLayout(navController: NavHostController, userEmail: String) {

    var drawerState = remember { DrawerState(initialValue = DrawerValue.Closed) }
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column (
                    modifier= Modifier.padding(vertical = 20.dp, horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Trackify",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                                navController.navigate("settings")
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.gear),
                                contentDescription = "Settings",
                                Modifier.size(24.dp)
                            )
                        }
                    }
                }

                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navController.navigate("home")
                    }
                )
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") { HomeScreen(navController = navController, email = userEmail, drawerState = drawerState, coroutineScope = coroutineScope) }
            composable("settings") { SettingsScreen(navController = navController, email = userEmail, drawerState = drawerState, coroutineScope = coroutineScope) }
        }
    }

}