package com.farzin.modernmusicplayer.navigation

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.farzin.album.AlbumScreen
import com.farzin.artist.ArtistScreen
import com.farzin.core_ui.Screens
import com.farzin.core_ui.common_components.AddToPlaylistDialogContent
import com.farzin.core_ui.common_components.convertToPosition
import com.farzin.core_ui.common_components.convertToProgress
import com.farzin.core_ui.theme.BackgroundColor
import com.farzin.folder.FolderScreen
import com.farzin.home.home.HomeScreen
import com.farzin.player.PlayerViewmodel
import com.farzin.player.player.FullPlayer
import com.farzin.player.player.MiniMusicController
import com.farzin.playlists.PlaylistViewmodel
import com.farzin.playlists.PlaylistsScreen
import com.farzin.playlists.components.AddSongToPlaylistDialog
import com.farzin.search.search.SearchScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navHostController: NavHostController,
    playerViewmodel: PlayerViewmodel,
    playlistViewmodel: PlaylistViewmodel
) {
    val activity = LocalContext.current as Activity
    val scope = rememberCoroutineScope()
    val playlists by playlistViewmodel.playlists.collectAsStateWithLifecycle()
    val songs by playlistViewmodel.songs.collectAsStateWithLifecycle()

    val currentPosition by playerViewmodel.currentPosition.collectAsStateWithLifecycle(0L)
    val musicState by playerViewmodel.musicState.collectAsStateWithLifecycle()
    val playbackMode by playerViewmodel.playbackMode.collectAsStateWithLifecycle()
    val playingQueueSongs by playerViewmodel.playingQueueSongs.collectAsStateWithLifecycle()
    val progress by animateFloatAsState(
        targetValue = convertToProgress(currentPosition, musicState.duration), label = "",
    )

    val sheetState = rememberBottomSheetScaffoldState()
    val isExpanded = when (sheetState.bottomSheetState.targetValue) {
        SheetValue.Hidden -> false
        SheetValue.Expanded -> true
        SheetValue.PartiallyExpanded -> false
    }

    BottomSheetScaffold(
        sheetContent = {
            AnimatedVisibility(
                visible = !isExpanded,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.BackgroundColor)
                            .clickable {
                                scope.launch {
                                    sheetState.bottomSheetState.expand()
                                }
                            }
                    ) {
                        if (playingQueueSongs.isNotEmpty()) {
                            MiniMusicController(
                                progress = progress,
                                song = playingQueueSongs[musicState.currentSongIndex],
                                onNextClicked = {
                                    playerViewmodel.skipNext()
                                },
                                onPrevClicked = {
                                    playerViewmodel.skipPrevious()
                                },
                                onPlayPauseClicked = {
                                    playerViewmodel.pausePlay(!musicState.playWhenReady)
                                },
                                musicState = musicState,
                                modifier = Modifier
                                    .weight(1f)
                            )
                        }
                    }

                }
            }

            if (playingQueueSongs.isNotEmpty()) {
                FullPlayer(
                    musicState = musicState,
                    songs = playingQueueSongs,
                    onSkipToIndex = {
                        playerViewmodel.skipToIndex(it)
                    },
                    onBackClicked = {
                        if (isExpanded) {
                            scope.launch {
                                sheetState.bottomSheetState.partialExpand()
                            }
                        } else {
                            activity.moveTaskToBack(true)
                        }
                    },
                    currentPosition = currentPosition,
                    onToggleLikeButton = { id, isFavorite ->
                        playerViewmodel.setFavorite(id, isFavorite)
                    },
                    onPlaybackModeClicked = {
                        playerViewmodel.onTogglePlaybackMode()
                    },
                    onSeekTo = {
                        playerViewmodel.seekTo(convertToPosition(it, musicState.duration))
                    },
                    onPrevClicked = {
                        playerViewmodel.skipPrevious()
                    },
                    onNextClicked = {
                        playerViewmodel.skipNext()
                    },
                    onPlayPauseClicked = {
                        playerViewmodel.pausePlay(!musicState.playWhenReady)
                    },
                    playbackMode = playbackMode,
                    navController = navHostController,
                    sheetState = sheetState
                )
            }
        },
        scaffoldState = sheetState,
        sheetPeekHeight = 64.dp,
        sheetDragHandle = null,
        sheetShape = RoundedCornerShape(0.dp),
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navHostController,
                    startDestination = Screens.Home
                ) {

                    composable<Screens.Home> {
                        HomeScreen(
                            navController = navHostController,
                            playerViewmodel = playerViewmodel,
                            playlistViewmodel = playlistViewmodel
                        )
                    }

                    composable<Screens.Album> {
                        val args = it.toRoute<Screens.Album>()
                        AlbumScreen(
                            albumId = args.albumId,
                            navController = navHostController,
                            playerViewmodel = playerViewmodel,
                            playlistViewmodel = playlistViewmodel
                        )
                    }


                    composable<Screens.Artist> {
                        val args = it.toRoute<Screens.Artist>()
                        ArtistScreen(
                            artistId = args.artistId,
                            navController = navHostController,
                            playerViewmodel = playerViewmodel,
                            playlistViewmodel = playlistViewmodel
                        )
                    }



                    composable<Screens.Folder> {
                        val args = it.toRoute<Screens.Folder>()
                        FolderScreen(
                            folderName = args.folderName,
                            navController = navHostController,
                            playerViewmodel = playerViewmodel,
                            playlistViewmodel = playlistViewmodel
                        )
                    }


                    composable<Screens.Search> {
                        SearchScreen(
                            navController = navHostController,
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


                if (playlistViewmodel.isAddSongToPlaylistsVisible) {
                    Dialog(onDismissRequest = { playlistViewmodel.closeAddSongDialog() }) {
                        AddToPlaylistDialogContent(
                            playlists = playlists,
                            onConfirm = {
                                when {
                                    playlistViewmodel.selectedSingleSongForPlaylist != null -> {
                                        playlistViewmodel.addSongToPlaylists(it)
                                    }

                                    playlistViewmodel.selectedMultipleSongForPlaylist != null -> {
                                        playlistViewmodel.insertMultipleSongsToPlaylistsSongs(
                                            playlistViewmodel.selectedMultipleSongForPlaylist,
                                            it
                                        )
                                    }
                                }

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
    )
}
