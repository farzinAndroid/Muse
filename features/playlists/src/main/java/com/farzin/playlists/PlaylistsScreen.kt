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
import com.farzin.core_model.db.toSongDB
import com.farzin.core_ui.R
import com.farzin.core_ui.common_components.SelectionTopBar
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
    var songsToDelete by remember { mutableStateOf<List<PlaylistSong>>(emptyList()) }
    var selectedSongs by remember { mutableStateOf<Set<Song>>(emptySet()) }
    val isInSelectionMode = selectedSongs.isNotEmpty()

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
                songsToDelete.forEach { playlistSong ->
                    playlistViewmodel.deleteSongFromPlaylist(playlistSong)
                }
                selectedSongs = emptySet()
                songsToDelete = emptyList()
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
                if (isInSelectionMode) {
                    SelectionTopBar(
                        selectedCount = selectedSongs.size,
                        onCloseClicked = { selectedSongs = emptySet() },
                        onDeleteClicked = {
                            songsToDelete = selectedSongs.map { song ->
                                PlaylistSong(
                                    song = song.toSongDB(),
                                    playlistId = playlistId
                                )
                            }
                            playerViewmodel.showWarningDialog = true
                        },
                        onAddToPlaylistClicked = {
                            playlistViewmodel.openAddMultipleSongDialog(selectedSongs.toList())
                        }
                    )
                } else {
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
                }

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
                            val currentSong = playlistSong.song.toSong()
                            val isSelected = currentSong in selectedSongs
                            Spacer(Modifier.height(MaterialTheme.spacing.small8))
                            SongItem(
                                song = currentSong,
                                onClick = {
                                    if (isInSelectionMode) {
                                        selectedSongs = if (isSelected) {
                                            selectedSongs - currentSong
                                        } else {
                                            selectedSongs + currentSong
                                        }
                                    } else {
                                        playerViewmodel.play(
                                            songs = songsToPlay.toList(),
                                            startIndex = index
                                        )
                                    }
                                },
                                onLongClick = {
                                    selectedSongs = if (isSelected) {
                                        selectedSongs - currentSong
                                    } else {
                                        selectedSongs + currentSong
                                    }
                                },
                                isSelected = isSelected,
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
                                        text = stringResource(R.string.remove_from_playlist),
                                        onClick = {
                                            scope.launch {
                                                val pSong = PlaylistSong(
                                                    song = playlistSong.song,
                                                    playlistId = playlistId
                                                )
                                                songsToDelete = listOf(pSong)
                                                songsToPlay =
                                                    songsToPlay.filter { it.mediaId != pSong.song.mediaId }
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
                    EmptySectionText(stringResource(R.string.no_songs_in_playlist))
                }

            }

        }
    )


}
