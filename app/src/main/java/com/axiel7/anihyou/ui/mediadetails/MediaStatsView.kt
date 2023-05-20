package com.axiel7.anihyou.ui.mediadetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.anihyou.R
import com.axiel7.anihyou.data.model.localized
import com.axiel7.anihyou.type.MediaRankType
import com.axiel7.anihyou.ui.composables.InfoTitle
import com.axiel7.anihyou.ui.composables.defaultPlaceholder
import com.axiel7.anihyou.ui.composables.stats.HorizontalStatsBar
import com.axiel7.anihyou.ui.theme.AniHyouTheme

@Composable
fun MediaStatsView(
    mediaId: Int,
    viewModel: MediaDetailsViewModel,
) {
    LaunchedEffect(mediaId) {
        viewModel.getMediaStats(mediaId)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Rankings
        if (viewModel.isLoadingStats || viewModel.mediaRankings.isNotEmpty()) {
            InfoTitle(text = stringResource(R.string.rankings))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                viewModel.mediaRankings.forEach {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(text = buildString {
                                append("#${it.rank} ${it.context.capitalize(Locale.current)}")
                                it.season?.let { season ->
                                    append(" ${season.localized()}")
                                }
                                it.year?.let { year ->
                                    append(" $year")
                                }
                            })
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(
                                    if (it.type == MediaRankType.POPULAR) R.drawable.favorite_24
                                    else R.drawable.star_24
                                ),
                                contentDescription = "rank"
                            )
                        }
                    )
                }
                if (viewModel.isLoadingStats) {
                    for (i in 1..3) {
                        Text(
                            text = "This is a loading placeholder",
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .defaultPlaceholder(visible = true)
                        )
                    }
                }
            }//: Column
        }

        // Status distribution
        InfoTitle(text = stringResource(R.string.status_distribution))
        HorizontalStatsBar(
            stats = viewModel.mediaStatusDistribution,
            horizontalPadding = 16.dp,
            isLoading = viewModel.isLoadingStats
        )
    }
}

@Preview
@Composable
fun MediaStatsViewPreview() {
    AniHyouTheme {
        Surface {
            MediaStatsView(
                mediaId = 1,
                viewModel = viewModel()
            )
        }
    }
}