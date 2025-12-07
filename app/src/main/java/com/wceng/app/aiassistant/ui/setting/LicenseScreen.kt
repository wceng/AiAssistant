package com.wceng.app.aiassistant.ui.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.component.AiaLargeTopBar
import com.wceng.app.aiassistant.component.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    onNavigationClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LicenseTopAppBar(onNavigationClick, scrollBehavior)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

            LibrariesContainer(Modifier.fillMaxSize())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicenseTopAppBar(
    onNavigationClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    AiaLargeTopBar(
        titleRes = R.string.license,
        navigationIcon = {
            BackButton(onNavigationClick)
        },
        scrollBehavior = scrollBehavior
    )
}