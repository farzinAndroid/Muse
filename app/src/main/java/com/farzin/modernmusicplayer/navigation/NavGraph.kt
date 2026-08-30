package com.farzin.modernmusicplayer.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.farzin.album.AlbumScreen
import com.farzin.album.AlbumViewmodel
import com.farzin.artist.ArtistScreen
import com.farzin.artist.ArtistViewmodel
import com.farzin.core_ui.Screens
import com.farzin.core_ui.common_components.AddToPlaylistDialogContent
import com.farzin.folder.FolderScreen
import com.farzin.folder.FolderViewmodel
import com.farzin.home.home.HomeScreen
import com.farzin.home.home.HomeViewmodel
import com.farzin.player.PlayerViewmodel
import com.farzin.playlists.PlaylistViewmodel
import com.farzin.playlists.PlaylistsScreen
import com.farzin.playlists.components.AddSongToPlaylistDialog
import com.farzin.search.search.SearchScreen
import com.farzin.search.search.SearchViewmodel
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navHostController: NavHostController,
    homeViewmodel: HomeViewmodel,
    playerViewmodel: PlayerViewmodel,
    playlistViewmodel: PlaylistViewmodel,
    albumViewmodel: AlbumViewmodel,
    artistViewmodel: ArtistViewmodel,
    folderViewmodel: FolderViewmodel,
    searchViewmodel: SearchViewmodel
) {
    val playlists by playlistViewmodel.playlists.collectAsStateWithLifecycle()
    val songs by playlistViewmodel.songs.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navHostController,
            startDestination = Screens.Home
        ) {

            composable<Screens.Home> {
                HomeScreen(
                    navController = navHostController,
                    homeViewmodel = homeViewmodel,
                    playerViewmodel = playerViewmodel,
                    playlistViewmodel = playlistViewmodel
                )
            }

            composable<Screens.Album> {
                val args = it.toRoute<Screens.Album>()
                AlbumScreen(
                    albumId = args.albumId,
                    navController = navHostController,
                    albumViewModel = albumViewmodel,
                    playerViewmodel = playerViewmodel,
                    playlistViewmodel = playlistViewmodel
                )
            }


            composable<Screens.Artist> {
                val args = it.toRoute<Screens.Artist>()
                ArtistScreen(
                    artistId = args.artistId,
                    navController = navHostController,
                    artistViewmodel = artistViewmodel,
                    playerViewmodel = playerViewmodel,
                    playlistViewmodel = playlistViewmodel
                )
            }



            composable<Screens.Folder> {
                val args = it.toRoute<Screens.Folder>()
                FolderScreen(
                    folderName = args.folderName,
                    navController = navHostController,
                    folderViewmodel = folderViewmodel,
                    playerViewmodel = playerViewmodel,
                    playlistViewmodel = playlistViewmodel
                )
            }


            composable<Screens.Search> {
                SearchScreen(
                    navController = navHostController,
                    searchViewmodel = searchViewmodel,
                    playerViewmodel = playerViewmodel,
                    playlistViewmodel = playlistViewmodel
                )
            }

            composable<Screens.Playlists> {
                val args = it.toRoute<Screens.Playlists>()
                PlaylistsScreen(
                    playlistId = args.playlistId,
                    playlistName = args.playlistName,
                    navController = navHostController,
                    playlistViewmodel = playlistViewmodel,
                    playerViewmodel = playerViewmodel
                )
            }


        }

        // Add 1 song to N playlists
        if (playlistViewmodel.isAddSongToPlaylistsVisible) {
            Dialog(onDismissRequest = { playlistViewmodel.closeAddSongDialog() }) {
                AddToPlaylistDialogContent(
                    playlists = playlists,
                    onConfirm = {
                        playlistViewmodel.addSongToPlaylists(it)
                    },
                    onDismiss = {
                        playlistViewmodel.closeAddSongDialog()
                    }
                )
            }
        }

        // Add N songs to 1 playlist
        if (playlistViewmodel.isPickSongsForPlaylistVisible) {
            val targetId = playlistViewmodel.targetPlaylistIdForSongs
            if (targetId != null) {
                AddSongToPlaylistDialog(
                    onDismiss = { playlistViewmodel.closePickSongsDialog() },
                    onConfirm = { playlistSongs ->
                        scope.launch {
                            playlistViewmodel.insertPlaylistSongs(
                                playlistSongs = playlistSongs,
                                playlistId = targetId
                            )
                        }
                    },
                    songs = songs,
                    playlistViewmodel = playlistViewmodel,
                    playlistId = targetId,
                    songsToPlay = playlistViewmodel.existingSongsInTargetPlaylist
                )
            }
        }
    }
}
