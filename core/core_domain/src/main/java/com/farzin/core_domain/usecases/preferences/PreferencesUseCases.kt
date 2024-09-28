package com.farzin.core_domain.usecases.preferences

data class PreferencesUseCases(
    val getUserDataUseCase: GetUserDataUseCase,
    val setPlaybackModeUseCase: SetPlaybackModeUseCase,
    val setSortOrderUseCase: SetSortOrderUseCase,
    val setSortByUseCase: SetSortByUseCase,
    val setPlayingQueueIndexUseCase: SetPlayingQueueIndexUseCase,
    val setPlayingQueueIdsUseCase: SetPlayingQueueIdsUseCase,
    val getPlaybackModeUseCase: GetPlaybackModeUseCase,
    val getPlayQueueIndexUseCase: GetPlayingQueueIndexUseCase,
    val getPlayingQueueIdsUseCase: GetPlayingQueueIdsUseCase,
    val setShuffleModeUseCase: SetShuffleModeUseCase,
    val getShuffleModeUseCase: GetShuffleModeUseCase,
    val setRepeatModeUseCase: SetRepeatModeUseCase,
    val getRepeatModeUseCase: GetRepeatModeUseCase,
)