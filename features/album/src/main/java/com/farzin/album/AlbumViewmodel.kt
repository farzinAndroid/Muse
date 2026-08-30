package com.farzin.album

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farzin.core_domain.usecases.media.MediaUseCases
import com.farzin.core_model.Album
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AlbumViewmodel @Inject constructor(
    private val mediaUseCases: MediaUseCases
) : ViewModel() {

    val playingQueueSongs = mediaUseCases.getPlayingQueueSongsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    var error by mutableStateOf(false)

    private val _album = MutableStateFlow<Album?>(null)
    val album: StateFlow<Album?> = _album


    fun getAlbumById(albumId: Long) {
        _album.value = null
        viewModelScope.launch {
            try {
                mediaUseCases.getAlbumByIdUseCase(albumId).collectLatest {
                    _album.value = it
                }
                error = false
            }catch (e:Exception){
                Log.e("TAG","error")
                error = true
            }
        }
    }

}