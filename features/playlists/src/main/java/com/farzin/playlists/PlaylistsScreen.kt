package com.farzin.playlists

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.farzin.core_model.Song
import com.farzin.core_model.db.PlaylistSong
import com.farzin.core_model.db.toSong
import com.farzin.core_ui.common_components.WarningAlertDialog
import com.farzin.core_ui.common_components.DetailTopBar
import com.farzin.core_ui.common_components.EmptySectionText
import com.farzin.core_ui.common_components.MenuItem
import com.farzin.core_ui.common_components.SongItem
import com.farzin.core_ui.common_components.convertToPosition
import com.farzin.core_ui.common_components.convertToProgress
import com.farzin.core_ui.theme.BackgroundColor
import com.farzin.core_ui.theme.WhiteDarkBlue
import com.farzin.core_ui.theme.spacing
import com.farzin.player.PlayerViewmodel
import com.farzin.player.player.FullPlayer
import com.farzin.player.player.MiniMusicController
import com.farzin.playlists.components.PlaylistDetailImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    playlistId: Int,
    playlistName: String,
    playlistViewmodel: PlaylistViewmodel,
    playerViewmodel: PlayerViewmodel,
    navController: NavController,
) {

    val currentPosition by playerViewmodel.currentPosition.collectAsStateWithLifecycle(0L)
    val musicState by playerViewmodel.musicState.collectAsStateWithLifecycle()
    val playbackMode by playerViewmodel.playbackMode.collectAsStateWithLifecycle()
    val playingQueueSongs by playlistViewmodel.playingQueueSongs.collectAsStateWithLifecycle()
    val progress by animateFloatAsState(
        targetValue = convertToProgress(currentPosition, musicState.duration), label = "",
    )

    val songsInPlaylist by playlistViewmodel
        .songsInPlaylist(playlistId).collectAsStateWithLifecycle(emptyList())

    var songsToPlay by remember { mutableStateOf<Set<Song>>(emptySet()) }
    var songToDelete by remember { mutableStateOf(PlaylistSong()) }

    LaunchedEffect(songsInPlaylist) {
        songsToPlay = songsInPlaylist.reversed().map { it.song.toSong() }.toSet()
        Log.e("TAG", songsToPlay.toString())
    }


    val sheetState = rememberBottomSheetScaffoldState()
    val isExpanded = when (sheetState.bottomSheetState.targetValue) {
        SheetValue.Hidden -> false
        SheetValue.Expanded -> true
        SheetValue.PartiallyExpanded -> false
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current






    if (playerViewmodel.showWarningDialog) {
        WarningAlertDialog(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            onDismiss = {
                playerViewmodel.showWarningDialog = false
            },
            onConfirm = {
                playlistViewmodel.deleteSongFromPlaylist(songToDelete)
                playerViewmodel.showWarningDialog = false
            }
        )
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
                            navController.navigateUp()
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
                    navController = navController,
                    sheetState = sheetState
                )
            }

        },
        scaffoldState = sheetState,
        sheetPeekHeight = 64.dp,
        sheetDragHandle = null,
        sheetShape = RoundedCornerShape(0.dp),
        content = {


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.BackgroundColor),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DetailTopBar(
                    onBackClicked = {
                        navController.navigateUp()
                    },
                    text = playlistName,
                    shouldHaveMiddleText = true,
                    shouldHaveEndIcon = true,
                    endIcon = {
                        IconButton(
                            onClick = { playlistViewmodel.openPickSongsDialog(playlistId, songsToPlay.toList()) },
                            modifier = Modifier
                                .size(MaterialTheme.spacing.semiLarge24)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "",
                                modifier = Modifier
                                    .fillMaxSize(),
                                tint = MaterialTheme.colorScheme.WhiteDarkBlue
                            )
                        }
                    }
                )

                Spacer(Modifier.height(MaterialTheme.spacing.medium16))


                if (songsInPlaylist.isNotEmpty()) {

                    PlaylistDetailImage(
                        songsInPlaylist = songsInPlaylist,
                        albumName = ""
                    )

                    Spacer(Modifier.height(MaterialTheme.spacing.large32))


                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 64.dp)

                    ) {
                        itemsIndexed(songsInPlaylist.reversed(), key = { _, playlistSong ->
                            playlistSong.id
                        }) { index, playlistSong ->
                            Spacer(Modifier.height(MaterialTheme.spacing.small8))
                            SongItem(
                                onClick = {
                                    playerViewmodel.play(
                                        songs = songsToPlay.toList(),
                                        startIndex = index
                                    )
                                },
                                song = playlistSong.song.toSong(),
                                isPlaying = playlistSong.song.mediaId == musicState.currentMediaId,
                                onToggleFavorite = {
                                    playerViewmodel.setFavorite(
                                        playlistSong.song.mediaId,
                                        it
                                    )
                                },
                                isFavorite = playlistSong.song.isFavorite,
                                modifier = Modifier
                                    .animateItem(),
                                menuItemList = listOf(
                                    MenuItem(
                                        text = stringResource(com.farzin.core_ui.R.string.remove_from_playlist),
                                        onClick = {
                                            scope.launch {
                                                songToDelete = PlaylistSong(
                                                    song = playlistSong.song,
                                                    playlistId = playlistId
                                                )
                                                songsToPlay =
                                                    songsToPlay.filter { it.mediaId != songToDelete.song.mediaId }
                                                        .toSet()
                                                playerViewmodel.showWarningDialog = true
                                            }
                                        },
                                        iconVector = Icons.Default.Delete,
                                    ),
                                )
                            )
                        }
                    }
                } else {
                    EmptySectionText(stringResource(com.farzin.core_ui.R.string.no_songs_in_playlist))
                }

            }

        }
    )


}
