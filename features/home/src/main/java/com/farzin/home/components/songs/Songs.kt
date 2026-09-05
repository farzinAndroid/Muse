package com.farzin.home.components.songs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.farzin.core_model.Song
import com.farzin.core_ui.R
import com.farzin.core_ui.common_components.EmptySectionText
import com.farzin.core_ui.common_components.MenuItem
import com.farzin.core_ui.common_components.SongItem
import com.farzin.core_ui.theme.spacing

@Composable
fun Songs(
    selectedSongs: Set<Song>,
    onToggleSelection: (Song) -> Unit,
    songs: List<Song>,
    currentPlayingSongId: String,
    onClick: (Int, List<Song>) -> Unit,
    onDeleteClicked: (song: Song) -> Unit,
    onAddToPlaylistClicked: (song: Song) -> Unit,
    onToggleFavorite: (id: String, isFavorite: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {

    val isInSelectionMode = selectedSongs.isNotEmpty()

    if (songs.isNotEmpty()) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
        ) {
            itemsIndexed(songs, key = { _, song -> song.mediaId }) { index, song ->
                val isSelected = song in selectedSongs
                Spacer(Modifier.height(MaterialTheme.spacing.small8))
                SongItem(
                    song = song,
                    onClick = {
                        if (isInSelectionMode) {
                            onToggleSelection(song)
                        } else {
                            onClick(index, songs)
                        }
                    },
                    onLongClick = {
                        onToggleSelection(song)
                    },
                    isSelected = isSelected,
                    onToggleFavorite = { onToggleFavorite(song.mediaId, it) },
                    isPlaying = song.mediaId == currentPlayingSongId,
                    isFavorite = song.isFavorite,
                    modifier = Modifier.animateItem(),
                    menuItemList = listOf(
                        MenuItem(
                            text = stringResource(R.string.delete),
                            onClick = { onDeleteClicked(song) },
                            iconVector = Icons.Default.Delete,
                        ),
                        MenuItem(
                            text = if (!song.isFavorite) stringResource(R.string.add_to_fav) else stringResource(
                                R.string.remove_from_fav
                            ),
                            onClick = { onToggleFavorite(song.mediaId, !song.isFavorite) },
                            iconVector = if (!song.isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                        ),
                        MenuItem(
                            text = stringResource(R.string.add_to_playlist),
                            onClick = { onAddToPlaylistClicked(song) },
                            iconVector = Icons.Default.AddCircle,
                        ),
                    )
                )

            }
        }
    } else {
        EmptySectionText(stringResource(R.string.no_songs))
    }

}
