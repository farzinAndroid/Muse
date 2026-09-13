package com.farzin.home.home

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.farzin.core_model.Album
import com.farzin.core_model.Artist
import com.farzin.core_model.Folder
import com.farzin.core_model.Song
import com.farzin.core_model.SortBy
import com.farzin.core_model.SortOrder
import com.farzin.core_model.db.Playlist
import com.farzin.core_ui.R
import com.farzin.core_ui.Screens
import com.farzin.core_ui.common_components.WarningAlertDialog
import com.farzin.core_ui.common_components.Loading
import com.farzin.core_ui.common_components.convertToPosition
import com.farzin.core_ui.common_components.convertToProgress
import com.farzin.core_ui.common_components.deleteLauncher
import com.farzin.core_ui.theme.BackgroundColor
import com.farzin.core_ui.utils.showToast
import com.farzin.home.components.FilterSection
import com.farzin.home.components.HomePager
import com.farzin.home.components.HomeTopBar
import com.farzin.core_ui.common_components.SelectionTopBar
import com.farzin.home.permission.AudioPermission
import com.farzin.home.permission.PermissionScreen
import com.farzin.player.PlayerViewmodel
import com.farzin.player.player.FullPlayer
import com.farzin.player.player.MiniMusicController
import com.farzin.playlists.PlaylistViewmodel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    playerViewmodel: PlayerViewmodel,
    playlistViewmodel: PlaylistViewmodel,
) {

    val context = LocalContext.current

    val permissionState = rememberPermissionState(
        permission = AudioPermission,
        onPermissionResult = { result ->
            if (!result) {
                context.showToast(context.getString(R.string.grant_permission))
            }
        }
    )

    when (permissionState.status.isGranted) {
        true -> {
            Home(
                navController = navController,
                playerViewmodel = playerViewmodel,
                playlistViewmodel = playlistViewmodel
            )
        }

        false -> {
            PermissionScreen(
                onButtonClick = {
                    permissionState.launchPermissionRequest()
                }
            )

        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun Home(
    homeViewmodel: HomeViewmodel = hiltViewModel(),
    playerViewmodel: PlayerViewmodel,
    playlistViewmodel: PlaylistViewmodel,
    navController: NavController,
) {
    val activity = LocalContext.current as Activity

    var showFilter by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetScaffoldState()
    val isExpanded = when (sheetState.bottomSheetState.targetValue) {
        SheetValue.Hidden -> false
        SheetValue.Expanded -> true
        SheetValue.PartiallyExpanded -> false
    }


    val allSongsInAllPlaylists by playlistViewmodel.allSongsInAllPlaylists
        .collectAsStateWithLifecycle(emptyList())

    val currentPosition by playerViewmodel.currentPosition.collectAsStateWithLifecycle(0L)
    val musicState by playerViewmodel.musicState.collectAsStateWithLifecycle()
    val playbackMode by playerViewmodel.playbackMode.collectAsStateWithLifecycle()
    val playingQueueSongs by homeViewmodel.playingQueueSongs.collectAsStateWithLifecycle()
    val progress by animateFloatAsState(
        targetValue = convertToProgress(currentPosition, musicState.duration), label = "",
    )


    var loading by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf(SortOrder.DESCENDING) }
    var sortBy by remember { mutableStateOf(SortBy.DATE_ADDED) }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var recentSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var favoriteSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var albums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var artists by remember { mutableStateOf<List<Artist>>(emptyList()) }
    var folders by remember { mutableStateOf<List<Folder>>(emptyList()) }
    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    val homeState by homeViewmodel.homeState.collectAsStateWithLifecycle()
    val selectedSongs by homeViewmodel.selectedSongs.collectAsStateWithLifecycle()

    when (val state = homeState) {
        HomeState.Loading -> {
            loading = true
        }

        is HomeState.Success -> {
            loading = false
            songs = state.songs
            albums = state.albums
            artists = state.artists
            folders = state.folders
            sortBy = state.sortBy
            sortOrder = state.sortOrder
            recentSongs = state.recentSongs
            favoriteSongs = state.favoriteSongs
            playlists = state.playlists
        }
    }


    var songsToDelete by remember { mutableStateOf<List<Song>>(emptyList()) }
    val launcher = deleteLauncher(
        songsToDelete = songsToDelete,
        onSuccess = {
            scope.launch {
                songsToDelete.forEach {song->
                    if (playlistViewmodel.isSongInPlaylist(song)){
                        allSongsInAllPlaylists.forEach {
                            if (it.song.mediaId == song.mediaId) {
                                playlistViewmodel.deleteSongFromPlaylist(it)
                            }
                        }
                    }
                }
                homeViewmodel.clearSelection()
                songsToDelete = emptyList()
            }
        }
    )




    if (playerViewmodel.showWarningDialog) {
        WarningAlertDialog(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            onDismiss = {
                playerViewmodel.showWarningDialog = false
            },
            onConfirm = {
                scope.launch {
                    playerViewmodel.deleteSong(
                        songs = songsToDelete,
                        launcher = launcher,
                    )

                    homeViewmodel.clearSelection()
                    songsToDelete = emptyList()

                    playerViewmodel.showWarningDialog = false
                }
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
                        if (songs.isNotEmpty() && playingQueueSongs.isNotEmpty()) {
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




            if (songs.isNotEmpty() && playingQueueSongs.isNotEmpty()) {
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
            Box(){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.BackgroundColor)
                ) {
                    if (selectedSongs.isNotEmpty()) {
                        SelectionTopBar(
                            selectedCount = selectedSongs.size,
                            onCloseClicked = { homeViewmodel.clearSelection() },
                            onDeleteClicked = {
                                songsToDelete = selectedSongs.toList()
                                playerViewmodel.showWarningDialog = true
                            },
                            onAddToPlaylistClicked = {
                                playlistViewmodel.openAddMultipleSongDialog(selectedSongs.toList())
                            }
                        )
                    } else {
                        HomeTopBar(
                            onSearchClicked = {
                                navController.navigate(Screens.Search)
                            },
                            onFilterClicked = {
                                showFilter = !showFilter
                            },
                            showFilter = showFilter
                        )
                    }


                    FilterSection(
                        showFilter = showFilter,
                        sortOrder = sortOrder,
                        sortBy = sortBy,
                        onSortOrderClicked = {
                            playerViewmodel.onChangeSortOrder(it)
                        },
                        onSortByClicked = {
                            playerViewmodel.onChangeSortBy(it)
                        }
                    )

                    if (loading) {
                        Loading()
                    } else {
                        HomePager(
                            currentPlayingSongId = musicState.currentMediaId,
                            songs = songs,
                            favoriteSongs = favoriteSongs,
                            albums = albums,
                            selectedSongs = selectedSongs,
                            onToggleSelection = { homeViewmodel.toggleSelection(it) },
                            onClearSelection = { homeViewmodel.clearSelection() },
                            onSongClick = { index, songsList ->
                                playerViewmodel.play(songsList, index)

                                Log.e("TAG",playlistViewmodel.isSongInPlaylist(songsList[index]).toString())
                            },
                            onAlbumClick = { albumId ->
                                navController.navigate(Screens.Album(albumId))
                            },
                            musicState = musicState,
                            artists = artists,
                            onArtistClick = { artistId ->
                                navController.navigate(Screens.Artist(artistId))
                            },
                            folders = folders,
                            onFolderClick = { name ->
                                navController.navigate(Screens.Folder(name))
                            },
                            onFavoriteClick = { id: String, isFavorite: Boolean ->
                                playerViewmodel.setFavorite(id, isFavorite)
                            },
                            recentSongs = recentSongs,
                            onDeleteClicked = {
                                songsToDelete = listOf(it)
                                playerViewmodel.showWarningDialog = true
                            },
                            playlists = playlists,
                            onPlaylistClicked = { playlist ->
                                navController.navigate(
                                    Screens.Playlists(
                                        playlistId = playlist.id,
                                        playlistName = playlist.name
                                    )
                                )
                            },
                            onAddToPlaylistClicked = {
                                playlistViewmodel.openAddSingleSongDialog(it)
                            }
                        )
                    }
                }
            }
        }
    )
}
