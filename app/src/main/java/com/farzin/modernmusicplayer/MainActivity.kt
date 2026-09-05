package com.farzin.modernmusicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.farzin.core_ui.common_components.ChangeStatusBarAndNavigationBarColor
import com.farzin.core_ui.theme.BackgroundColor
import com.farzin.modernmusicplayer.navigation.NavGraph
import com.farzin.modernmusicplayer.ui.theme.ModernMusicPlayerTheme
import com.farzin.player.PlayerViewmodel
import com.farzin.playlists.PlaylistViewmodel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val navController = rememberNavController()
            val playerViewmodel: PlayerViewmodel = hiltViewModel()
            val playlistViewmodel: PlaylistViewmodel = hiltViewModel()

            ModernMusicPlayerTheme {

                ChangeStatusBarAndNavigationBarColor(
                    context = this,
                    isDarkMode = isSystemInDarkTheme()
                )

                CompositionLocalProvider(
                    LocalLayoutDirection.provides(LayoutDirection.Ltr)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.BackgroundColor)
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        NavGraph(
                            navHostController = navController,
                            playerViewmodel = playerViewmodel,
                            playlistViewmodel = playlistViewmodel
                        )
                    }
                }
            }
        }
    }
}
