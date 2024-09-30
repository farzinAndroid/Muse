package com.farzin.home.home

import com.farzin.core_model.Album
import com.farzin.core_model.Song
import com.farzin.core_model.SortBy
import com.farzin.core_model.SortOrder


sealed class HomeState {

    data object Loading : HomeState()
    data class Success(
        val songs: List<Song> = emptyList(),
        val albums:List<Album> = emptyList(),
        val sortOrder: SortOrder,
        val sortBy: SortBy,
    ) : HomeState()

}