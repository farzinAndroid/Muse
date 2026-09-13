package com.farzin.player

import android.app.RecoverableSecurityException
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farzin.core_domain.usecases.preferences.PreferencesUseCases
import com.farzin.core_domain.usecases.remote.GetLyricsUseCase
import com.farzin.core_media_service.MusicServiceConnection
import com.farzin.core_model.PlaybackMode
import com.farzin.core_model.Song
import com.farzin.core_model.SortBy
import com.farzin.core_model.SortOrder
import com.farzin.core_model.remote.Lyric
import com.farzin.core_model.remote.LyricResult
import com.farzin.core_model.remote.NetworkResult
import com.farzin.core_ui.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewmodel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    private val preferencesUseCases: PreferencesUseCases,
    private val getLyricsUseCase: GetLyricsUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val musicState = musicServiceConnection.musicState
    val currentPosition = musicServiceConnection.currentPosition.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0L
    )

    val lyrics : MutableStateFlow<NetworkResult<Lyric>> = MutableStateFlow(NetworkResult.Loading())

    fun getLyrics(songTitle:String,artist:String){
        viewModelScope.launch {
            lyrics.emit(NetworkResult.Success("",Lyric(listOf(LyricResult()))))
            lyrics.emit(getLyricsUseCase(songTitle,artist))
        }
    }

    fun play(
        songs: List<Song>,
        startIndex: Int = 0,
    ) = musicServiceConnection.playSongs(
        songs, startIndex
    )

    fun pausePlay(isPlaying: Boolean) =
        if (isPlaying) {
            musicServiceConnection.play()
        } else {
            musicServiceConnection.pause()
        }


    fun skipNext() = musicServiceConnection.skipNext()
    fun skipPrevious() = musicServiceConnection.skipPrevious()

    fun skipToIndex(index: Int) = musicServiceConnection.skipToIndex(index)

    fun seekTo(position: Long) = musicServiceConnection.seekTo(position)


    val playbackMode = preferencesUseCases.getPlaybackModeUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PlaybackMode.REPEAT
    )

    fun onTogglePlaybackMode() = viewModelScope.launch {
        val newPlaybackMode = when (playbackMode.value) {
            PlaybackMode.REPEAT -> PlaybackMode.REPEAT_ONE
            PlaybackMode.REPEAT_ONE -> PlaybackMode.SHUFFLE
            PlaybackMode.SHUFFLE -> PlaybackMode.REPEAT
        }
        preferencesUseCases.setPlaybackModeUseCase(newPlaybackMode)
    }

    fun onChangeSortOrder(sortOrder: SortOrder) =
        viewModelScope.launch { preferencesUseCases.setSortOrderUseCase(sortOrder) }

    fun onChangeSortBy(sortBy: SortBy) =
        viewModelScope.launch { preferencesUseCases.setSortByUseCase(sortBy) }

    fun setFavorite(id: String, isFavorite: Boolean) = viewModelScope.launch {
        preferencesUseCases.setFavoriteUseCase(id, isFavorite)
    }


    var showWarningDialog by mutableStateOf(false)
    fun deleteSong(
        songs: List<Song>,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    ) {
        val contentResolver = context.contentResolver
        val uris = songs.map {
            it.mediaUri
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = MediaStore.createDeleteRequest(contentResolver, uris)
            launcher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
        } else {
            kotlin.runCatching {
                for (uri in uris) contentResolver.delete(uri, null, null)
            }.fold(
                onSuccess = {
                    viewModelScope.launch {
//                        skipNext()
                        context.showToast(context.getString(com.farzin.core_ui.R.string.song_deleted))
                    }
                },
                onFailure = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && it is RecoverableSecurityException) {
                        launcher.launch(IntentSenderRequest.Builder(it.userAction.actionIntent.intentSender).build())
                    } else {
                        context.showToast(context.getString(com.farzin.core_ui.R.string.sth_went_wrong))
                    }
                },
            )
        }
    }


}