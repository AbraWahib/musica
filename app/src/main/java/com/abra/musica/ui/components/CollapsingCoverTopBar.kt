package com.abra.musica.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.abra.musica.R

@Composable
fun CollapsingCoverTopBar(
    title: String,
    subtitle: String,
    coverArtUri: Uri,
    collapsedFraction: Float,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    expandedHeight: Dp = 280.dp,
    collapsedHeight: Dp = 64.dp
) {
    val fraction = collapsedFraction.coerceIn(0f, 1f)
    val currentHeight = expandedHeight - ((expandedHeight - collapsedHeight) * fraction)
    val expandedAlpha = 1f - fraction

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(currentHeight),
        shadowElevation = if (fraction == 1f) 4.dp else 0.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = coverArtUri.takeUnless { it == Uri.EMPTY },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(expandedAlpha),
                contentScale = ContentScale.Crop,
                placeholder = rememberAsyncImagePainter(R.drawable.music_placeholder),
                error = rememberAsyncImagePainter(R.drawable.music_placeholder)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(expandedAlpha)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.32f),
                                Color.Black.copy(alpha = 0.72f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(collapsedHeight)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (fraction < 1f) {
                                    Color.Black.copy(alpha = 0.28f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                            tint = if (fraction < 1f) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(fraction)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .alpha(expandedAlpha)
                    .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
