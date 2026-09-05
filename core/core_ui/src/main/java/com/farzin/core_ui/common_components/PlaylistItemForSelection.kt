package com.farzin.core_ui.common_components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farzin.core_model.db.Playlist
import com.farzin.core_ui.R
import com.farzin.core_ui.theme.Gray
import com.farzin.core_ui.theme.WhiteDarkBlue
import com.farzin.core_ui.theme.spacing

@Composable
fun PlaylistItemForSelection(
    playlist: Playlist,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(if (isSelected) MaterialTheme.colorScheme.Gray else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = MaterialTheme.spacing.medium16),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = isSelected,
            enter = slideInHorizontally() + expandHorizontally() + fadeIn(),
            exit = slideOutHorizontally() + shrinkHorizontally() + fadeOut()
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(MaterialTheme.spacing.semiLarge24)
                    .padding(end = MaterialTheme.spacing.small8),
                tint = MaterialTheme.colorScheme.WhiteDarkBlue
            )
        }

        Image(
            painter = if (isSystemInDarkTheme())
                painterResource(R.drawable.vinyl_white)
            else
                painterResource(R.drawable.vinyl_blue),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(50.dp)
        )

        Spacer(Modifier.width(MaterialTheme.spacing.medium16))

        Column(modifier = Modifier.weight(1f)) {
            TextBold(
                text = playlist.name,
                color = MaterialTheme.colorScheme.WhiteDarkBlue,
                fontSize = 16.sp,
                maxLine = 1,
                overflow = TextOverflow.Ellipsis
            )
            TextRegular(
                text = stringResource(R.string.playlist),
                color = MaterialTheme.colorScheme.Gray,
                fontSize = 12.sp
            )
        }
    }
}