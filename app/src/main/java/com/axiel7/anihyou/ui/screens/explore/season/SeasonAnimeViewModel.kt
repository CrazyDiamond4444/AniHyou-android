package com.axiel7.anihyou.ui.screens.explore.season

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.axiel7.anihyou.SeasonalAnimeQuery
import com.axiel7.anihyou.data.model.PagedResult
import com.axiel7.anihyou.data.model.media.AnimeSeason
import com.axiel7.anihyou.data.repository.MediaRepository
import com.axiel7.anihyou.type.MediaSeason
import com.axiel7.anihyou.ui.common.viewmodel.PagedUiStateViewModel
import com.axiel7.anihyou.utils.StringUtils.removeFirstAndLast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SeasonAnimeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val mediaRepository: MediaRepository,
) : PagedUiStateViewModel<SeasonAnimeUiState>() {

    private val initialYear =
        savedStateHandle.getStateFlow<Int?>(YEAR_ARGUMENT.removeFirstAndLast(), null)
    private val initialSeason =
        savedStateHandle.getStateFlow<String?>(SEASON_ARGUMENT.removeFirstAndLast(), null)

    override val mutableUiState = MutableStateFlow(SeasonAnimeUiState())
    override val uiState = mutableUiState.asStateFlow()

    fun setSeason(value: AnimeSeason) = mutableUiState.update {
        it.copy(season = value, page = 1, hasNextPage = true, isLoading = true)
    }

    val animeSeasonal = mutableStateListOf<SeasonalAnimeQuery.Medium>()

    init {
        combine(
            initialYear.filterNotNull(),
            initialSeason.filterNotNull()
        ) { year, season ->
            mutableUiState.update {
                it.copy(
                    season = AnimeSeason(season = MediaSeason.valueOf(season), year = year)
                )
            }
        }.launchIn(viewModelScope)

        uiState
            .filter { it.season != null && it.hasNextPage }
            .distinctUntilChanged { old, new ->
                old.page == new.page
                        && old.season == new.season
            }
            .flatMapLatest {
                mediaRepository.getSeasonalAnimePage(
                    animeSeason = it.season!!,
                    page = it.page
                )
            }
            .onEach { result ->
                mutableUiState.update {
                    if (result is PagedResult.Success) {
                        if (it.page == 1) animeSeasonal.clear()
                        animeSeasonal.addAll(result.list)
                        it.copy(
                            hasNextPage = result.hasNextPage,
                            isLoading = false,
                        )
                    } else {
                        result.toUiState(loadingWhen = it.page == 1)
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}