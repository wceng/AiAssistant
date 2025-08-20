package com.wceng.app.aiassistant.component

import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.wceng.app.aiassistant.navigation.TopLevelDestination
import com.wceng.app.aiassistant.ui.DevicePreview

@Composable
fun AiaNavigationSuiteScaffold(
    navigationSuiteItems: NavigationSuiteScope.() -> Unit,
    modifier: Modifier = Modifier,
    hideBottomNavBar: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = navigationSuiteItems,
        layoutType = if (hideBottomNavBar) {
            NavigationSuiteType.None
        } else {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
                currentWindowAdaptiveInfo()
            )
        }
    ) {
        content()
    }
}

fun NavigationSuiteScope.AiaNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    selectedIcon: ImageVector,
    @StringRes label: Int
) {
    item(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) selectedIcon else icon,
                contentDescription = null
            )
        },
        label = { Text(stringResource(label)) },
    )
}

@DevicePreview
@Composable
private fun AiaNavigationSuiteScaffoldPreview() {
    Surface {
        AiaNavigationSuiteScaffold(
            navigationSuiteItems = {
                TopLevelDestination.entries.forEachIndexed { index, destination ->
                    AiaNavigationItem(
                        selected = index == 0,
                        onClick = {},
                        icon = destination.unselectedIcon,
                        selectedIcon = destination.selectedIcon,
                        label = destination.iconText
                    )
                }
            },
        ) {}
    }
}