package com.axiel7.anihyou.ui.screens.usermedialist.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.anihyou.R
import com.axiel7.anihyou.UserMediaListQuery
import com.axiel7.anihyou.data.model.media.duration
import com.axiel7.anihyou.data.model.media.exampleMediaList
import com.axiel7.anihyou.type.MediaListStatus
import com.axiel7.anihyou.type.ScoreFormat
import com.axiel7.anihyou.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.anihyou.ui.composables.media.MediaPoster
import com.axiel7.anihyou.ui.composables.scores.BadgeScoreIndicator
import com.axiel7.anihyou.ui.theme.AniHyouTheme
import com.axiel7.anihyou.utils.NumberUtils.isGreaterThanZero

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactUserMediaListItem(
    item: UserMediaListQuery.MediaList,
    status: MediaListStatus,
    scoreFormat: ScoreFormat,
    isMyList: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickPlus: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Max)
        ) {
            Box(
                contentAlignment = Alignment.BottomStart
            ) {
                MediaPoster(
                    url = item.media?.coverImage?.large,
                    showShadow = false,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .heightIn(min = MEDIA_POSTER_SMALL_WIDTH.dp)
                        .fillMaxHeight()
                        .width(MEDIA_POSTER_SMALL_WIDTH.dp)
                )

                BadgeScoreIndicator(
                    score = item.basicMediaListEntry.score,
                    scoreFormat = scoreFormat
                )
            }//: Box

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.media?.basicMediaDetails?.title?.userPreferred ?: "",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (item.media?.nextAiringEpisode != null) 1 else 2
                )

                AiringScheduleText(
                    item = item,
                    fontSize = 16.sp,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${item.basicMediaListEntry.progress ?: 0}/${item.media?.basicMediaDetails?.duration() ?: 0}",
                        fontSize = 15.sp,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (item.basicMediaListEntry.repeat.isGreaterThanZero()) {
                            Icon(
                                painter = painterResource(R.drawable.replay_20),
                                contentDescription = stringResource(R.string.repeat_count),
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                        if (!item.basicMediaListEntry.notes.isNullOrBlank()) {
                            Icon(
                                painter = painterResource(R.drawable.notes_20),
                                contentDescription = stringResource(R.string.notes),
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                        if (isMyList && (status == MediaListStatus.CURRENT
                                    || status == MediaListStatus.REPEATING)
                        ) {
                            FilledTonalButton(onClick = onClickPlus) {
                                Text(text = "+1")
                            }
                        }
                    }
                }//:Row
            }//:Column
        }//:Row
    }//:Card
}

@Preview
@Composable
fun CompactUserMediaListItemPreview() {
    AniHyouTheme {
        Surface {
            CompactUserMediaListItem(
                item = exampleMediaList,
                status = MediaListStatus.CURRENT,
                scoreFormat = ScoreFormat.POINT_100,
                isMyList = true,
                onClick = { },
                onLongClick = { },
                onClickPlus = { }
            )
        }
    }
}