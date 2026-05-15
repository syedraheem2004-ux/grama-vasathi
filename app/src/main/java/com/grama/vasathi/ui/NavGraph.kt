package com.grama.vasathi.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.grama.vasathi.ui.onboarding.OnboardingScreen
import com.grama.vasathi.ui.roleselection.RoleSelectionScreen
import com.grama.vasathi.ui.roleselection.UserRole
import com.grama.vasathi.ui.auth.AuthScreen
import com.grama.vasathi.ui.home.GuestHomeScreen
import com.grama.vasathi.ui.booking.StayDetailScreen
import com.grama.vasathi.ui.host.HostHomeScreen
import com.grama.vasathi.ui.host.ChecklistScreen
import com.grama.vasathi.ui.booking.MyBookingsScreen
import com.grama.vasathi.ui.profile.ProfileScreen
import com.grama.vasathi.ui.explore.ExploreScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object RoleSelection : Screen("role_selection")
    object Login : Screen("login/{role}") {
        fun createRoute(role: UserRole) = "login/${role.name}"
    }
    object HostHome : Screen("host_home")
    object GuestHome : Screen("guest_home")
    object Explore : Screen("explore")
    object StayDetail : Screen("stay_detail/{stayId}") {
        fun createRoute(stayId: String) = "stay_detail/$stayId"
    }
    object Checklist : Screen("checklist")
    object MyBookings : Screen("my_bookings")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onContinue = { role ->
                    navController.navigate(Screen.Login.createRoute(role))
                }
            )
        }

        composable(Screen.Login.route) { backStackEntry ->
            val roleStr = backStackEntry.arguments?.getString("role")
            val role = try { UserRole.valueOf(roleStr ?: "GUEST") } catch (e: Exception) { UserRole.GUEST }
            
            AuthScreen(
                role = role,
                onNavigateBack = { navController.popBackStack() },
                onAuthSuccess = {
                    val target = if (role == UserRole.HOST) Screen.HostHome.route else Screen.GuestHome.route
                    navController.navigate(target) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.HostHome.route) {
            HostHomeScreen(
                onChecklistClick = {
                    navController.navigate(Screen.Checklist.route)
                },
                onBookingsClick = {
                    navController.navigate(Screen.MyBookings.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Checklist.route) {
            ChecklistScreen(
                onBackClick = { navController.popBackStack() },
                onDashboardClick = { 
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.GuestHome.route) {
            GuestHomeScreen(
                onStayClick = { stayId: String ->
                    navController.navigate(Screen.StayDetail.createRoute(stayId))
                },
                onViewBookingsClick = {
                    navController.navigate(Screen.MyBookings.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onExploreClick = {
                    navController.navigate(Screen.Explore.route)
                }
            )
        }

        composable(Screen.Explore.route) {
            ExploreScreen(
                onStayClick = { stayId ->
                    navController.navigate(Screen.StayDetail.createRoute(stayId))
                },
                onHomeClick = { navController.popBackStack() },
                onBookingsClick = { navController.navigate(Screen.MyBookings.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.StayDetail.route) { backStackEntry ->
            val stayId = backStackEntry.arguments?.getString("stayId") ?: ""
            StayDetailScreen(
                stayId = stayId,
                onBackClick = { navController.popBackStack() },
                onViewBookingsClick = {
                    navController.navigate(Screen.MyBookings.route)
                }
            )
        }

        composable(Screen.MyBookings.route) {
            MyBookingsScreen(
                role = UserRole.GUEST, 
                onBackClick = { navController.popBackStack() },
                onExploreStaysClick = { 
                    navController.navigate(Screen.GuestHome.route) {
                        popUpTo(Screen.MyBookings.route) { inclusive = true }
                    }
                },
                onDashboardClick = {
                    navController.navigate(Screen.HostHome.route) {
                        popUpTo(Screen.MyBookings.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogOut = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeleteAccount = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
