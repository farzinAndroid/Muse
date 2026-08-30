package com.farzin.artist

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.farzin.core_ui.common_components.WarningAlertDialog
import com.farzin.core_ui.common_components.DetailTopBar
import com.farzin.core_ui.common_components.EmptySectionText
import com.farzin.core_ui.common_components.MenuItem
import com.farzin.core_ui.common_components.SongItem
import com.farzin.core_ui.common_components.convertToPosition
import com.farzin.core_ui.common_components.convertToProgress
import com.farzin.core_ui.common_components.deleteLauncher
import com.farzin.core_ui.common_components.Loading
import com.farzin.core_ui.theme.BackgroundColor
import com.farzin.core_ui.theme.spacing
import com.farzin.player.PlayerViewmodel
import com.farzin.player.player.FullPlayer
import com.farzin.player.player.MiniMusicController
import com.farzin.playlists.PlaylistViewmodel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    artistId: Long,
    navController: NavController,
    playerViewmodel: PlayerViewmodel,
    artistViewmodel: ArtistViewmodel,
    playlistViewmodel: PlaylistViewmodel,
) {

    val scope = rememberCoroutineScope()


    val allSongsInAllPlaylists by playlistViewmodel.allSongsInAllPlaylists
        .collectAsStateWithLifecycle(emptyList())
    val currentPosition by playerViewmodel.currentPosition.collectAsStateWithLifecycle(0L)
    val musicState by playerViewmodel.musicState.collectAsStateWithLifecycle()
    val playbackMode by playerViewmodel.playbackMode.collectAsStateWithLifecycle()
    val playingQueueSongs by artistViewmodel.playingQueueSongs.collectAsStateWithLifecycle()
    val progress by animateFloatAsState(
        targetValue = convertToProgress(currentPosition, musicState.duration), label = "",
    )

    LaunchedEffect(Unit) {
        artistViewmodel.getArtistById(artistId)
    }
    val artist by artistViewmodel.artist.collectAsStateWithLifecycle()

    val sheetState = rememberBottomSheetScaffoldState()
    val isExpanded = when (sheetState.bottomSheetState.targetValue) {
        SheetValue.Hidden -> false
        SheetValue.Expanded -> true
        SheetValue.PartiallyExpanded -> false
    }

    var songToDelete by remember { mutableStateOf(Song()) }
    val context = LocalContext.current
    val launcher = deleteLauncher(
        songToDelete = songToDelete,
        onSuccess = {
            scope.launch {
                if (playlistViewmodel.isSongInPlaylist(songToDelete)){
                    allSongsInAllPlaylists.forEach {
                        if (it.song.mediaId == songToDelete.mediaId) {
                            playlistViewmodel.deleteSongFromPlaylist(it)
                        }
                    }
                }
            }
        }
    )

    if (playerViewmodel.showWarningDialog){
        WarningAlertDialog(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            onDismiss = {
                playerViewmodel.showWarningDialog = false
            },
            onConfirm = {
                playerViewmodel.deleteSong(
                    song = songToDelete,
                    launcher = launcher,
                )
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
                    text = artist?.name ?: "",
                    shouldHaveMiddleText = true
                )

                Spacer(Modifier.height(MaterialTheme.spacing.medium16))

                if (artist == null) {
                    Loading()
                } else if (!artistViewmodel.error) {
                    artist?.let {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 64.dp)

                        ) {
                            itemsIndexed(
                                it.songs,
                                key = { _, song ->
                                    song.mediaId
                                }
                            ) { index, song ->
                                Spacer(Modifier.height(MaterialTheme.spacing.small8))
                                SongItem(
                                    onClick = {
                                        playerViewmodel.play(
                                            it.songs,
                                            index
                                        )
                                    },
                                    song = song,
                                    onToggleFavorite = {
                                        playerViewmodel.setFavorite(
                                            song.mediaId,
                                            it
                                        )
                                    },
                                    isFavorite = song.isFavorite,
                                    shouldUseDefaultPic = true,
                                    isPlaying = song.mediaId == musicState.currentMediaId,
                                    modifier = Modifier
                                        .animateItem(),
                                    menuItemList = listOf(
                                        MenuItem(
                                            text = stringResource(com.farzin.core_ui.R.string.delete),
                                            onClick = {
                                                scope.launch {
                                                    songToDelete = song
                                                    playerViewmodel.showWarningDialog = true
                                                }
                                            },
                                            iconVector = Icons.Default.Delete,
                                        ),
                                        MenuItem(
                                            text = stringResource(com.farzin.core_ui.R.string.add_to_playlist),
                                            onClick = { playlistViewmodel.openAddSongDialog(song) },
                                            iconVector = Icons.Default.AddCircle,
                                        ),
                                        MenuItem(
                                            text = if (!song.isFavorite) stringResource(com.farzin.core_ui.R.string.add_to_fav) else stringResource(
                                                com.farzin.core_ui.R.string.remove_from_fav
                                            ),
                                            onClick = { playerViewmodel.setFavorite(song.mediaId, !song.isFavorite) },
                                            iconVector = if (!song.isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                                        ),
                                    )
                                )
                            }
                        }
                    }
                } else {
                    EmptySectionText(stringResource(com.farzin.core_ui.R.string.no_songs))
                }

            }
        }
    )

}