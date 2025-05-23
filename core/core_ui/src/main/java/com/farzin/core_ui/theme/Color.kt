package com.farzin.core_ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


val ColorScheme.BackgroundColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xff091227) else Color(0xffF7FAFF)


val ColorScheme.WhiteDarkBlue: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xffF7FAFF) else Color(0xff091227)


val ColorScheme.Gray: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xff8996B8) else Color(0x998996B8)

val ColorScheme.DarkGray: Color
    @Composable
    get() = Color(0xff555B6A)

val ColorScheme.SearchTextFieldColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xffF7FAFF) else Color(0xff555B6A)


val ColorScheme.LyricDialogColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xff555B6A) else Color(0xffF7FAFF)

val ColorScheme.MainBlue: Color
    @Composable
    get() = Color(0xff091227)

val ColorScheme.LyricHighLight: Color
    @Composable
    get() =  if (isSystemInDarkTheme()) Color(0xFFF53232) else Color(0xFFA90000)