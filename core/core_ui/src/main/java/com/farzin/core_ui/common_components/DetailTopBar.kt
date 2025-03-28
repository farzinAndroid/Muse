package com.farzin.core_ui.common_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.farzin.core_ui.theme.WhiteDarkBlue
import com.farzin.core_ui.theme.spacing

@Composable
fun DetailTopBar(
    onBackClicked: () -> Unit,
    text: String,
    shouldHaveMiddleText: Boolean,
    endIcon:@Composable ()->Unit = {},
    shouldHaveEndIcon: Boolean = false
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(top = MaterialTheme.spacing.semiLarge24)
            .padding(horizontal = MaterialTheme.spacing.semiLarge24),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ){

            IconButton(
                onClick = { onBackClicked() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(MaterialTheme.spacing.semiLarge24)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize(),
                    tint = MaterialTheme.colorScheme.WhiteDarkBlue
                )
            }



            if (shouldHaveMiddleText) {

                TextMedium(
                    text = text,
                    color = MaterialTheme.colorScheme.WhiteDarkBlue,
                    maxLine = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = MaterialTheme.spacing.semiLarge24)
                )

            }


            if (shouldHaveEndIcon){
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                ) {
                    endIcon()
                }
            }

        }





    }

}