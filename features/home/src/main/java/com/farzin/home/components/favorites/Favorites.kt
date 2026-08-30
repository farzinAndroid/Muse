package com.farzin.home.components.favorites

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.farzin.core_model.Song
import com.farzin.core_ui.common_components.EmptySectionText
import com.farzin.core_ui.common_components.MenuItem
import com.farzin.core_ui.common_components.SongItem
import com.farzin.core_ui.theme.spacing

@Composable
fun Favorites(
    favoriteSongs:List<Song>,
    currentPlayingSongId: String,
    onClick: (Int,List<Song>) -> Unit,
    onToggleFavorite: (id:String,isFavorite:Boolean) -> Unit,
    onDeleteClicked:(song:Song)->Unit,
    onAddToPlaylistClicked:(song:Song)->Unit,
    modifier: Modifier = Modifier
) {

    if (favoriteSongs.isNotEmpty()){
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
        ) {
            itemsIndexed(favoriteSongs, key = {_,song->song.mediaId}){ index, song ->
                Spacer(Modifier.height(MaterialTheme.spacing.small8))
                SongItem(
                    song = song,
                    onClick = { onClick(index,favoriteSongs) },
                    onToggleFavorite = { onToggleFavorite(song.mediaId,it) },
                    isPlaying = song.mediaId == currentPlayingSongId,
                    isFavorite = song.isFavorite,
                    modifier =Modifier
                        .animateItem(),
                    menuItemList = listOf(
                        MenuItem(
                            text = stringResource(com.farzin.core_ui.R.string.delete),
                            onClick = {
                                onDeleteClicked(song)
                            },
                            iconVector = Icons.Default.Delete,
                        ),
                        MenuItem(
                            text = if (!song.isFavorite) stringResource(com.farzin.core_ui.R.string.add_to_fav) else stringResource(
                                com.farzin.core_ui.R.string.remove_from_fav
                            ),
                            onClick = { onToggleFavorite(song.mediaId,!song.isFavorite) },
                            iconVector = if (!song.isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                        ),
                        MenuItem(
                            text = stringResource(com.farzin.core_ui.R.string.add_to_playlist),
                            onClick = { onAddToPlaylistClicked(song) },
                            iconVector = Icons.Default.AddCircle,
                        ),
                    )
                )

            }
        }
    }else{
        EmptySectionText(stringResource(com.farzin.core_ui.R.string.no_favorite_songs))
    }

}