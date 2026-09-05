package com.farzin.playlists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farzin.core_domain.usecases.db.PlaylistUseCases
import com.farzin.core_domain.usecases.media.MediaUseCases
import com.farzin.core_model.SearchDetails
import com.farzin.core_model.Song
import com.farzin.core_model.db.PlaylistSong
import com.farzin.core_model.db.toSongDB
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewmodel @Inject constructor(
    private val playlistUseCases: PlaylistUseCases,
    private val mediaUseCases: MediaUseCases,
) : ViewModel() {

    val query = MutableStateFlow("")


    @OptIn(ExperimentalCoroutinesApi::class)
    val searchDetails = query
        .flatMapLatest { mediaUseCases.searchUseCase(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = SearchDetails(
                songs = emptyList(),
                albums = emptyList(),
                folders = emptyList(),
                artists = emptyList(),
            )
        )

    fun changeQuery(newQuery: String) = query.update { newQuery }

    fun clear() = query.update { "" }


    var isAddSongToPlaylistsVisible by mutableStateOf(false)
        private set

    var selectedSingleSongForPlaylist by mutableStateOf<Song?>(null)
        private set


    var selectedMultipleSongForPlaylist by mutableStateOf<List<Song>?>(null)
        private set

    fun openAddSingleSongDialog(song: Song) {
        selectedSingleSongForPlaylist = song
        isAddSongToPlaylistsVisible = true
    }


    fun openAddMultipleSongDialog(songs: List<Song>) {
        selectedMultipleSongForPlaylist = songs
        isAddSongToPlaylistsVisible = true
    }

    fun closeAddSongDialog() {
        isAddSongToPlaylistsVisible = false
        selectedSingleSongForPlaylist = null
        selectedMultipleSongForPlaylist = null
    }






    var isPickSongsForPlaylistVisible by mutableStateOf(false)
        private set

    var targetPlaylistIdForSongs by mutableStateOf<Int?>(null)
        private set

    var existingSongsInTargetPlaylist by mutableStateOf<List<Song>>(emptyList())
        private set

    fun openPickSongsDialog(playlistId: Int, currentSongs: List<Song>) {
        targetPlaylistIdForSongs = playlistId
        existingSongsInTargetPlaylist = currentSongs
        isPickSongsForPlaylistVisible = true
    }

    fun closePickSongsDialog() {
        isPickSongsForPlaylistVisible = false
        targetPlaylistIdForSongs = null
        existingSongsInTargetPlaylist = emptyList()
        clear()
    }


    val playlists = playlistUseCases.getAllPlaylistsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    fun addSongToPlaylists(playlistIds: List<Int>) {
        val song = selectedSingleSongForPlaylist ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val playlistSongs = playlistIds.map { playlistId ->
                PlaylistSong(
                    song = song.toSongDB(),
                    playlistId = playlistId,
                    id = "${song.mediaId}_$playlistId"
                )
            }
            playlistUseCases.insertPlaylistSongUseCase(playlistSongs)
            closeAddSongDialog()
        }
    }

    fun insertPlaylistSongs(playlistSongs: List<PlaylistSong>, playlistId: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            val playlistSongsToInsert = playlistSongs.map { originalPlaylistSong ->
                PlaylistSong(
                    song = originalPlaylistSong.song,
                    playlistId = playlistId,
                    id = "${originalPlaylistSong.song.mediaId}_$playlistId"
                )
            }
            playlistUseCases.insertPlaylistSongUseCase(playlistSongsToInsert)
            closePickSongsDialog()
        }


    fun insertMultipleSongsToPlaylistsSongs(playlistSongs: List<Song>?, playlistIds: List<Int>) =
        viewModelScope.launch(Dispatchers.IO) {
            playlistIds.forEach {playlistId->
                val playlistSongsToInsert = playlistSongs?.map { originalPlaylistSong ->
                    PlaylistSong(
                        song = originalPlaylistSong.toSongDB(),
                        playlistId = playlistId,
                        id = "${originalPlaylistSong.toSongDB().mediaId}_$playlistId"
                    )
                }
                playlistUseCases.insertPlaylistSongUseCase(playlistSongsToInsert!!)
            }
            closeAddSongDialog()
        }

    val playingQueueSongs = mediaUseCases.getPlayingQueueSongsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val songs = mediaUseCases.getSongsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )


    val allSongsInAllPlaylists = playlistUseCases.getSongsInAllPlaylistsUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )


    fun isSongInPlaylist(song: Song): Boolean {
        return allSongsInAllPlaylists.value.any { it.song.mediaId == song.mediaId }
    }


    fun songsInPlaylist(playlistId: Int) = playlistUseCases.getSongsInPlaylistUseCase(playlistId)


    fun deleteSongFromPlaylist(playlistSong: PlaylistSong) =
        viewModelScope.launch(Dispatchers.IO) {
            val playlistSongToDelete = PlaylistSong(
                song = playlistSong.song,
                playlistId = playlistSong.playlistId,
                id = "${playlistSong.song.mediaId}_${playlistSong.playlistId}"
            )
            playlistUseCases.deleteSongInPlaylistUseCase(playlistSongToDelete)
        }

}
