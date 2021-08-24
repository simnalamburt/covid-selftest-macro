@file:JvmName("AppKt")

package com.lhwdev.selfTestMacro.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.SystemUiController
import com.lhwdev.selfTestMacro.database.PreferenceState
import com.lhwdev.selfTestMacro.database.preferenceState
import com.lhwdev.selfTestMacro.repository.SelfTestManagerImpl
import com.lhwdev.selfTestMacro.ui.pages.splash.Splash
import kotlinx.coroutines.flow.collect


val LocalActivity = compositionLocalOf<Activity> { error("not provided") }
val LocalPreference = compositionLocalOf<PreferenceState> { error("not provided") }
val LocalPreview = staticCompositionLocalOf { false }


@Composable
fun ComposeApp(activity: Activity) {
	val context = LocalContext.current
	val pref = remember(context) { context.preferenceState }
	val navigator = remember {
		val navigator = NavigatorImpl()
		
		navigator.pushRoute { Splash() }
		
		navigator
	}
	val scheduler = remember { SelfTestManagerImpl(context.applicationContext, pref.db) }
	
	LaunchedEffect(pref) {
		snapshotFlow {
			pref.db.testGroups
		}.collect {
			scheduler.onScheduleUpdated(pref.db)
		}
	}
	
	AppTheme {
		CompositionLocalProvider(
			LocalActivity provides activity,
			LocalPreference provides pref,
			LocalGlobalNavigator provides navigator
		) {
			ProvideAutoWindowInsets {
				AnimateListAsComposable(
					navigator.routes,
					isOpaque = { it.isOpaque },
					animation = { route, visible, onAnimationEnd, content ->
						val transition = route as? RouteTransition ?: if(route.isOpaque) {
							DefaultOpaqueRouteTransition
						} else {
							DefaultTransparentRouteTransition
						}
						transition.Transition(
							route = route,
							visibleState = visible,
							onAnimationEnd = onAnimationEnd,
							content = content
						)
					}
				) { index, route ->
					EnabledRoute(enabled = index == navigator.routes.lastIndex) {
						RouteContent(route)
					}
				}
			}
		}
		
		BackHandler(navigator.size > 1) {
			navigator.popLastRoute()
		}
	}
}

@Composable
private fun EnabledRoute(enabled: Boolean, content: @Composable () -> Unit) {
	EnableAutoSystemUi(enabled) {
		content()
	}
}

val LocalPreviewUiController =
	staticCompositionLocalOf<SystemUiController> { error("not provided") }
