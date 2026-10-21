package com.farzin.search.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farzin.core_domain.usecases.media.MediaUseCases
import com.farzin.core_domain.usecases.preferences.PreferencesUseCases
import com.farzin.core_model.SearchDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SearchViewmodel @Inject constructor(
    private val preferencesUseCases: PreferencesUseCases,
    mediaUseCases: MediaUseCases,
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

    val uiState = combine(
        query,
        searchDetails,
    ) { query, searchDetails ->
        SearchUiState.Success(
            query = query,
            searchDetails = searchDetails,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SearchUiState.Loading
    )

    fun changeQuery(newQuery: String) = query.update { newQuery }

    fun clear() = query.update { "" }

}