package com.farzin.core_ui.common_components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.farzin.core_ui.theme.WhiteDarkBlue
import com.farzin.core_ui.theme.spacing

@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onCloseClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onAddToPlaylistClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(top = MaterialTheme.spacing.large32)
            .padding(horizontal = MaterialTheme.spacing.semiLarge24),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.SpaceBetween
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close Selection",
                tint = MaterialTheme.colorScheme.WhiteDarkBlue,
                modifier = Modifier
                    .size(MaterialTheme.spacing.semiLarge24)
                    .clickable { onCloseClicked() }
            )

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium16))

            TextBold(
                text = "$selectedCount Selected",
                color = MaterialTheme.colorScheme.WhiteDarkBlue,
                fontSize = 20.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Add to Playlist",
                tint = MaterialTheme.colorScheme.WhiteDarkBlue,
                modifier = Modifier
                    .size(MaterialTheme.spacing.semiLarge24)
                    .clickable { onAddToPlaylistClicked() }
            )

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium16))

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Selected",
                tint = MaterialTheme.colorScheme.WhiteDarkBlue,
                modifier = Modifier
                    .size(MaterialTheme.spacing.semiLarge24)
                    .clickable { onDeleteClicked() }
            )
        }
    }
}
