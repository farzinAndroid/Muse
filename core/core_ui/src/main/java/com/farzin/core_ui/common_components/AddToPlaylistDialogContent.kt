package com.farzin.core_ui.common_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.farzin.core_model.db.Playlist
import com.farzin.core_ui.R
import com.farzin.core_ui.theme.LyricDialogColor
import com.farzin.core_ui.theme.WhiteDarkBlue
import com.farzin.core_ui.theme.spacing

@Composable
fun AddToPlaylistDialogContent(
    modifier: Modifier = Modifier,
    playlists: List<Playlist>,
    onConfirm: (playListIds: List<Int>) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedPlaylistIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    Column(
        modifier = modifier
            .clip(Shapes().medium)
            .background(MaterialTheme.colorScheme.LyricDialogColor)
            .fillMaxWidth()
            .fillMaxHeight(0.8f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(MaterialTheme.spacing.medium16))

        TextBold(
            text = stringResource(R.string.add_to_playlist),
            color = MaterialTheme.colorScheme.WhiteDarkBlue,
            fontSize = 18.sp,
            modifier = Modifier
                .fillMaxWidth(),
            textStyle = TextStyle(
                textAlign = TextAlign.Center
            )
        )

        Spacer(Modifier.height(MaterialTheme.spacing.medium16))

        Box(modifier = Modifier.weight(1f)) {
            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptySectionText(stringResource(R.string.no_playlists))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(playlists) { playlist ->
                        val isSelected = playlist.id in selectedPlaylistIds
                        PlaylistItemForSelection(
                            playlist = playlist,
                            isSelected = isSelected,
                            onClick = {
                                selectedPlaylistIds = if (isSelected) {
                                    selectedPlaylistIds - playlist.id
                                } else {
                                    selectedPlaylistIds + playlist.id
                                }
                            }
                        )
                        Spacer(Modifier.height(MaterialTheme.spacing.small8))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.spacing.small8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                TextMedium(
                    text = stringResource(R.string.dismiss),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.WhiteDarkBlue
                )
            }

            TextButton(
                onClick = { onConfirm(selectedPlaylistIds.toList()) },
                enabled = selectedPlaylistIds.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                TextMedium(
                    text = stringResource(R.string.confirm),
                    fontSize = 16.sp,
                    color = if (selectedPlaylistIds.isNotEmpty())
                        MaterialTheme.colorScheme.WhiteDarkBlue
                    else
                        MaterialTheme.colorScheme.WhiteDarkBlue.copy(0.5f)
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AddToPlaylistDialogContentPreview() {
    MaterialTheme {
        AddToPlaylistDialogContent(
            playlists = listOf(
                Playlist(id = 1, name = "My Favorites"),
                Playlist(id = 2, name = "Workout Mix"),
                Playlist(id = 3, name = "Chill Vibes")
            ),
            onConfirm = {},
            onDismiss = {}
        )
    }
}
