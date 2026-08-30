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
import com.farzin.album.AlbumViewmodel
import com.farzin.artist.ArtistViewmodel
import com.farzin.core_ui.common_components.ChangeStatusBarAndNavigationBarColor
import com.farzin.core_ui.theme.BackgroundColor
import com.farzin.folder.FolderViewmodel
import com.farzin.home.home.HomeViewmodel
import com.farzin.modernmusicplayer.navigation.NavGraph
import com.farzin.modernmusicplayer.ui.theme.ModernMusicPlayerTheme
import com.farzin.player.PlayerViewmodel
import com.farzin.playlists.PlaylistViewmodel
import com.farzin.search.search.SearchViewmodel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val navController = rememberNavController()
            val homeViewmodel: HomeViewmodel = hiltViewModel()
            val playerViewmodel: PlayerViewmodel = hiltViewModel()
            val playlistViewmodel: PlaylistViewmodel = hiltViewModel()
            val albumViewmodel: AlbumViewmodel = hiltViewModel()
            val artistViewmodel: ArtistViewmodel = hiltViewModel()
            val folderViewmodel: FolderViewmodel = hiltViewModel()
            val searchViewmodel: SearchViewmodel = hiltViewModel()

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
                            homeViewmodel = homeViewmodel,
                            playerViewmodel = playerViewmodel,
                            playlistViewmodel = playlistViewmodel,
                            albumViewmodel = albumViewmodel,
                            artistViewmodel = artistViewmodel,
                            folderViewmodel = folderViewmodel,
                            searchViewmodel = searchViewmodel
                        )
                    }
                }
            }
        }
    }
}

