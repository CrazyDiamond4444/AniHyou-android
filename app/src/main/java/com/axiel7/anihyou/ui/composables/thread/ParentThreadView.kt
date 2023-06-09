package com.axiel7.anihyou.ui.composables.thread

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.anihyou.R
import com.axiel7.anihyou.fragment.BasicThreadDetails
import com.axiel7.anihyou.ui.composables.TextIconHorizontal
import com.axiel7.anihyou.ui.composables.defaultPlaceholder
import com.axiel7.anihyou.ui.theme.AniHyouTheme
import com.axiel7.anihyou.utils.ContextUtils.openActionView
import com.axiel7.anihyou.utils.DateUtils.timestampToDateString
import com.axiel7.anihyou.utils.MarkdownUtils.formatImageTags
import com.axiel7.anihyou.utils.NumberUtils.format
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ParentThreadView(
    thread: BasicThreadDetails,
    navigateToFullscreenImage: (String) -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = thread.title ?: "",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp
        )
        Text(
            text = thread.createdAt.toLong().timestampToDateString(format = "MMM d, YYYY") ?: "",
            color = MaterialTheme.colorScheme.outline,
            fontSize = 15.sp
        )

        MarkdownText(
            markdown = thread.body
                ?.formatImageTags()
                ?: "",
            modifier = Modifier.padding(vertical = 8.dp),
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface,
            onLinkClicked = {
                if (it.startsWith("anihyouimage")) {
                    navigateToFullscreenImage(
                        it.removePrefix("anihyouimage")
                    )
                }
                else context.openActionView(it)
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextIconHorizontal(
                text = thread.likeCount.format(),
                icon = R.drawable.favorite_20
            )
            Text(text = thread.user?.name ?: "")
        }
    }
}

@Composable
fun ParentThreadViewPlaceholder() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "This is a loading placeholder",
            modifier = Modifier
                .padding(bottom = 4.dp)
                .defaultPlaceholder(visible = true),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp
        )
        Text(
            text = "Jan 1, 2010",
            modifier = Modifier.defaultPlaceholder(visible = true),
            color = MaterialTheme.colorScheme.outline,
            fontSize = 15.sp
        )

        Text(
            text = "This is a loading placeholder of a thread view, the content is loading so please wait until it finished loading. Thank you.",
            modifier = Modifier
                .padding(vertical = 8.dp)
                .defaultPlaceholder(visible = true),
            fontSize = 20.sp,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextIconHorizontal(
                text = "17",
                modifier = Modifier.defaultPlaceholder(visible = true),
                icon = R.drawable.favorite_20
            )
            Text(
                text = "Username",
                modifier = Modifier.defaultPlaceholder(visible = true),
            )
        }
    }
}

@Preview
@Composable
fun ParentThreadViewPreview() {
    val thread = BasicThreadDetails(
        id = 1,
        title = "[Spoilers] Oshi no Ko - Episode 8 Discussion",
        body = "Great episode as expected. Reality Dating arc near to end and Akane was full on fire dem full of confidence and new personality. That kissing scene was soo good. But for sec i feel bad for Arima. Also finally we have 3rd member of b-komachi group i love to see new b-komachi on stage very hyped for that.",
        viewCount = 102,
        replyCount = 12,
        likeCount = 17,
        user = BasicThreadDetails.User(
            id = 1,
            name = "KOMBRAT",
            __typename = "User"
        ),
        createdAt = 1293823000
    )
    AniHyouTheme {
        Surface {
            Column {
                ParentThreadView(
                    thread = thread,
                    navigateToFullscreenImage = {}
                )
                ParentThreadViewPlaceholder()
            }
        }
    }
}