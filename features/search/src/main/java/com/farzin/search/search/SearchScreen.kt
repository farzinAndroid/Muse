package com.farzin.search.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.farzin.core_model.Song
import com.farzin.core_ui.Screens
import com.farzin.core_ui.common_components.WarningAlertDialog
import com.farzin.core_ui.common_components.LinearAlbumItem
import com.farzin.core_ui.common_components.MediaItem
import com.farzin.core_ui.common_components.MenuItem
import com.farzin.core_ui.common_components.SongItem
import com.farzin.core_ui.common_components.deleteLauncher
import com.farzin.core_ui.theme.BackgroundColor
import com.farzin.core_ui.theme.spacing
import com.farzin.player.PlayerViewmodel
import com.farzin.playlists.PlaylistViewmodel
import com.farzin.search.components.HeaderText
import com.farzin.search.components.SearchTextField
import kotlinx.coroutines.launch


@Composable
fun SearchScreen(
    navController: NavController,
    searchViewmodel: SearchViewmodel = hiltViewModel(),
    playerViewmodel: PlayerViewmodel = hiltViewModel(),
    playlistViewmodel: PlaylistViewmodel = hiltViewModel(),
) {

    val scope = rememberCoroutineScope()
    val uiState by searchViewmodel.uiState.collectAsStateWithLifecycle()
    val musicState by playerViewmodel.musicState.collectAsStateWithLifecycle()



    val allSongsInAllPlaylists by playlistViewmodel.allSongsInAllPlaylists
        .collectAsStateWithLifecycle(emptyList())
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

    when (val state = uiState) {
        SearchUiState.Loading -> {}
        is SearchUiState.Success -> {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.BackgroundColor)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchTextField(
                    onValueChange = { searchViewmodel.changeQuery(it) },
                    onClearClicked = { searchViewmodel.clear() },
                    value = state.query
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (state.searchDetails.songs.isNotEmpty()) {
                        item {
                            HeaderText(stringResource(com.farzin.core_ui.R.string.songs))
                        }

                        itemsIndexed(
                            state.searchDetails.songs,
                            key = { _, song ->
                                song.mediaId
                            }
                        ) { index, song ->
                            Spacer(Modifier.height(MaterialTheme.spacing.small8))
                            SongItem(
                                song = song,
                                onClick = {
                                    playerViewmodel.play(
                                        songs = state.searchDetails.songs,
                                        startIndex = index
                                    )
                                },
                                isPlaying = musicState.currentMediaId == song.mediaId,
                                onToggleFavorite = {
                                    playerViewmodel.setFavorite(
                                        song.mediaId,
                                        it
                                    )
                                },
                                isFavorite = song.isFavorite,
                                modifier = Modifier
                                    .animateItem(),
                                searchText = state.query,
                                menuItemList = listOf(
                                    MenuItem(
                                        text = stringResource(com.farzin.core_ui.R.string.delete),
                                        onClick = {
                                            scope.launch {
                                                songToDelete = song
                                                playerViewmodel.showWarningDialog = true
                                            }
                                        },
                                        iconVector = null,
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

                    if (state.searchDetails.albums.isNotEmpty()) {
                        item {
                            HeaderText(stringResource(com.farzin.core_ui.R.string.albums))
                        }

                        itemsIndexed(
                            state.searchDetails.albums,
                            key = { _, album ->
                                album.id
                            }
                        ) { index, album ->
                            Spacer(Modifier.height(MaterialTheme.spacing.small8))
                            LinearAlbumItem(
                                album = album,
                                onClick = {
                                    navController.navigate(Screens.Album(album.id))
                                },
                                modifier = Modifier
                                    .animateItem()
                            )
                        }
                    }

                    if (state.searchDetails.artists.isNotEmpty()) {
                        item {
                            HeaderText(stringResource(com.farzin.core_ui.R.string.artists))
                        }

                        itemsIndexed(
                            state.searchDetails.artists,
                            key = { _, artist ->
                                artist.id
                            }
                        ) { index, artist ->
                            Spacer(Modifier.height(MaterialTheme.spacing.small8))
                            MediaItem(
                                title = artist.name,
                                subTitle = stringResource(com.farzin.core_ui.R.string.artist),
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(Screens.Artist(artist.id))
                                    }
                                    .animateItem(),
                                darkModePic = painterResource(com.farzin.core_ui.R.drawable.artist_white),
                                lightModePic = painterResource(com.farzin.core_ui.R.drawable.artist_blue),
                            )
                        }
                    }

                    if (state.searchDetails.folders.isNotEmpty()) {
                        item {
                            HeaderText(stringResource(com.farzin.core_ui.R.string.folders))
                        }

                        itemsIndexed(
                            state.searchDetails.folders,
                            key = { _, folder ->
                                folder.name
                            }
                        ) { index, folder ->
                            Spacer(Modifier.height(MaterialTheme.spacing.small8))
                            MediaItem(
                                title = folder.name,
                                subTitle = "",
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(Screens.Folder(folder.name))
                                    }
                                    .animateItem(),
                                darkModePic = painterResource(com.farzin.core_ui.R.drawable.folder_white),
                                lightModePic = painterResource(com.farzin.core_ui.R.drawable.folder_blue),
                            )
                        }
                    }
                }

            }

        }
    }
}


