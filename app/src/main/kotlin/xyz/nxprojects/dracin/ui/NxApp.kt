package xyz.nxprojects.dracin.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import xyz.nxprojects.dracin.ui.components.BottomNavBar
import xyz.nxprojects.dracin.ui.detail.DetailScreen
import xyz.nxprojects.dracin.ui.home.HomeScreen
import xyz.nxprojects.dracin.ui.player.PlayerScreen
import xyz.nxprojects.dracin.ui.search.SearchScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NxApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var currentRoute by remember { mutableStateOf("home") }

    Scaffold(
        containerColor = Color(0xFF09090B),
        topBar = {
            when (currentRoute) {
                "home" -> {
                    HomeTopBar()
                }
                "search" -> {
                    SearchTopBar()
                }
                "detail" -> {
                    // Toolbar untuk detail screen sudah dihandle di DetailScreen
                }
                "player" -> {
                    // Toolbar untuk player screen sudah dihandle di PlayerScreen
                }
                else -> {
                    DefaultTopBar(
                        title = currentRoute.replaceFirstChar { it.uppercase() },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        },
        bottomBar = {
            if (currentRoute !in listOf("detail", "player")) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        currentRoute = route
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    currentRoute = "home"
                    HomeScreen(
                        onDramaClick = { bookId ->
                            navController.navigate("detail/$bookId")
                        }
                    )
                }

                composable("search") {
                    currentRoute = "search"
                    SearchScreen(
                        onDramaClick = { bookId ->
                            navController.navigate("detail/$bookId")
                        }
                    )
                }

                composable(
                    route = "detail/{bookId}",
                    arguments = listOf(navArgument("bookId") { type = NavType.StringType })
                ) { backStackEntry ->
                    currentRoute = "detail"
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
                    DetailScreen(
                        bookId = bookId,
                        onVideoClick = { videoId ->
                            navController.navigate("player/$videoId?bookId=$bookId")
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = "player/{videoId}?bookId={bookId}",
                    arguments = listOf(
                        navArgument("videoId") { type = NavType.StringType },
                        navArgument("bookId") { 
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    currentRoute = "player"
                    val videoId = backStackEntry.arguments?.getString("videoId") ?: return@composable
                    val bookId = backStackEntry.arguments?.getString("bookId")
                    
                    PlayerScreen(
                        videoId = videoId,
                        bookId = bookId,
                        context = context,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onNextEpisode = { nextVideoId ->
                            navController.navigate("player/$nextVideoId?bookId=$bookId") {
                                popUpTo("player/$videoId?bookId=$bookId") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
