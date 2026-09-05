package com.farzin.home.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farzin.core_domain.usecases.db.PlaylistUseCases
import com.farzin.core_domain.usecases.media.MediaUseCases
import com.farzin.core_domain.usecases.preferences.PreferencesUseCases
import com.farzin.core_model.Song
import com.farzin.core_model.db.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewmodel @Inject constructor(
    private val preferencesUseCases: PreferencesUseCases,
    private val mediaUseCases: MediaUseCases,
    private val playlistUseCases: PlaylistUseCases
) : ViewModel() {

    val playingQueueSongs = mediaUseCases.getPlayingQueueSongsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val songs = mediaUseCases.getSongsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val favoriteSongs = mediaUseCases.getSongsUseCase()
        .map { songs -> songs.filter { it.isFavorite } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val recentSongs = mediaUseCases.getRecentlyAddedSongsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val playlists = playlistUseCases.getAllPlaylistsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val homeState = combine(
        songs,
        favoriteSongs,
        mediaUseCases.getArtistsUseCase(),
        mediaUseCases.getAlbumsUseCase(),
        mediaUseCases.getFoldersUseCase(),
        recentSongs,
        preferencesUseCases.getUserDataUseCase(),
        playlists,
    ) { songs,favoriteSongs, artists, albums, folders, recentSongs, userData,playlists ->
        HomeState.Success(
            songs = songs,
            recentSongs = recentSongs,
            favoriteSongs=favoriteSongs,
            albums = albums,
            artists = artists,
            folders = folders,
            sortOrder = userData.sortOrder,
            sortBy = userData.sortBy,
            playlists = playlists
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeState.Loading
    )

    // --- Selection State Management ---
    private val _selectedSongs = MutableStateFlow<Set<Song>>(emptySet())
    val selectedSongs: StateFlow<Set<Song>> = _selectedSongs.asStateFlow()

    fun toggleSelection(song: Song) {
        _selectedSongs.update { currentSet ->
            if (song in currentSet) {
                currentSet - song
            } else {
                currentSet + song
            }
        }
    }

    fun clearSelection() {
        _selectedSongs.update { emptySet() }
    }
    // -----------------------------------

    fun createPlaylist(playlist: Playlist){
        viewModelScope.launch(Dispatchers.IO) {
            playlistUseCases.createPlaylistUseCase(playlist)
        }
    }

    fun deletePlaylist(playlist: Playlist){
        viewModelScope.launch(Dispatchers.IO) {
            playlistUseCases.deletePlaylistUseCase(playlist)
        }
    }



}

inline fun <T1, T2, T3, T4, T5, T6,T7,T8, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6,T7,T8) -> R
): Flow<R> {
    return kotlinx.coroutines.flow.combine(flow, flow2, flow3, flow4, flow5, flow6,flow7,flow8) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
            args[6] as T7,
            args[7] as T8
        )
    }
}