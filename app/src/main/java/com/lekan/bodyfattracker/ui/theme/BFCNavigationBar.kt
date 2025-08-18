//package com.lekan.bodyfattracker.ui.theme
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.unit.dp
//import coil3.compose.AsyncImage
//
//@Composable
//fun BFCNavigationBar(
//    navigationViewModel: NavViewModel,
//    navItems: List<Screen>
//) {
//    NavigationBar { // Using NavigationBar for Material 3
//        val currentScreen = navigationViewModel.backStack.last() // Observe current screen
//
//        navItems.forEach { screen ->
//            NavigationBarItem(
//                icon = {
//                    if (screen is Screen.Profile) {
//                        val photo = navigationViewModel.user?.photoPath
//                        if (photo != null) {
//                            Box(
//                                modifier = Modifier.Companion
//                                    .size(24.dp)
//                                    .clip(CircleShape)
//                                    .background(MaterialTheme.colorScheme.surfaceVariant),
//                                contentAlignment = Alignment.Companion.Center
//                            ) {
//                                AsyncImage(
//                                    model = photo,
//                                    contentScale = ContentScale.Companion.Crop,
//                                    contentDescription = "Photo",
//                                    modifier = Modifier.Companion.fillMaxSize()
//                                )
//                            }
//                        } else {
//                            screen.icon?.let { // Ensure icon is not null
//                                Icon(it, contentDescription = screen.label)
//                            }
//                        }
//                    } else {
//                        screen.icon?.let { // Ensure icon is not null
//                            Icon(it, contentDescription = screen.label)
//                        }
//                    }
//                },
//                label = { Text(screen.label) },
//                selected = currentScreen.route == screen.route,
//                onClick = {
//                    // Avoid pushing the same screen multiple times on top of itself
//                    if (currentScreen.route != screen.route) {
//                        navigationViewModel.push(screen) // Or use a popUpTo for better UX
//                    }
//                }
//            )
//        }
//    }
//}