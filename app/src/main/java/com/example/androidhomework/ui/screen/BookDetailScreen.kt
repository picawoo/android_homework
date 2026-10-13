package com.example.androidhomework.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.androidhomework.domain.model.Book

private const val MAX_VISIBLE_SUBJECTS = 5
private const val DESCRIPTION_COLLAPSED_LINES = 6

private val HeroCoverWidth = 160.dp
private val HeroCoverHeight = 235.dp

private enum class DetailState { Loading, Error, NotFound, Content }

private val Book.largeCoverUrl: String
    get() = coverUrl.replace("-S.jpg", "-L.jpg")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: Book?,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit
) {
    var isCoverPreviewOpen by rememberSaveable { mutableStateOf(false) }

    val state = when {
        isLoading -> DetailState.Loading
        errorMessage != null -> DetailState.Error
        book == null -> DetailState.NotFound
        else -> DetailState.Content
    }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        FilledTonalIconButton(
                            onClick = onBackClick,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Crossfade(
                targetState = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                label = "detailState"
            ) { target ->
                when (target) {
                    DetailState.Loading -> DetailSkeleton(Modifier.fillMaxSize())
                    DetailState.Error -> DetailErrorState(errorMessage.orEmpty(), Modifier.fillMaxSize())
                    DetailState.NotFound -> DetailNotFoundState(Modifier.fillMaxSize())
                    DetailState.Content -> if (book != null) {
                        BookDetailContent(
                            book = book,
                            onCoverClick = { isCoverPreviewOpen = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    if (isCoverPreviewOpen && book != null) {
        CoverPreviewDialog(
            coverUrl = book.largeCoverUrl,
            title = book.title,
            onDismiss = { isCoverPreviewOpen = false }
        )
    }
}

@Composable
private fun BookDetailContent(
    book: Book,
    onCoverClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        BookHero(book = book, onCoverClick = onCoverClick)

        StatsCard(book = book)

        if (book.subjects.isNotEmpty()) {
            SubjectsSection(subjects = book.subjects.take(MAX_VISIBLE_SUBJECTS))
        }

        AboutSection(description = book.description)
    }
}

@Composable
private fun BookHero(
    book: Book,
    onCoverClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BookCover(
            coverUrl = book.largeCoverUrl,
            title = book.title,
            modifier = Modifier
                .width(HeroCoverWidth)
                .height(HeroCoverHeight),
            elevation = 16.dp,
            onClick = onCoverClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = book.author,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StatsCard(book: Book) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = "Year",
                value = book.publishYear.takeIf { it > 0 }?.toString() ?: "N/A",
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.height(32.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            StatItem(
                label = "Editions",
                value = book.editionCount.takeIf { it > 0 }?.toString() ?: "N/A",
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.height(32.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            StatItem(
                label = "Language",
                value = book.language.takeIf { it.isNotBlank() }?.uppercase() ?: "N/A",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Composable
private fun SubjectsSection(subjects: List<String>) {
    DetailSection(title = "Subjects") {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subjects.forEach { subject -> SubjectChip(text = subject) }
        }
    }
}

@Composable
private fun AboutSection(description: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }

    DetailSection(title = "About this book") {
        Text(
            text = description.ifBlank { "Description is not available for this book." },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp,
            maxLines = if (expanded) Int.MAX_VALUE else DESCRIPTION_COLLAPSED_LINES,
            onTextLayout = { result ->
                if (!expanded) hasOverflow = result.hasVisualOverflow
            },
            modifier = Modifier.animateContentSize()
        )

        if (hasOverflow || expanded) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(text = if (expanded) "Show less" else "Read more")
            }
        }
    }
}

@Composable
private fun DetailSkeleton(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()

    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(HeroCoverWidth)
                .height(HeroCoverHeight)
                .clip(CoverShape)
                .background(brush)
        )
        SkeletonLine(brush, widthFraction = 0.7f, height = 26.dp)
        SkeletonLine(brush, widthFraction = 0.45f, height = 18.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(CardShape)
                .background(brush)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SkeletonLine(brush, widthFraction = 0.3f, height = 18.dp)
            SkeletonLine(brush, widthFraction = 1f, height = 14.dp)
            SkeletonLine(brush, widthFraction = 1f, height = 14.dp)
            SkeletonLine(brush, widthFraction = 0.8f, height = 14.dp)
        }
    }
}

@Composable
private fun DetailErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    CenteredColumn(modifier) {
        StateIcon(
            icon = Icons.Default.ErrorOutline,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun DetailNotFoundState(modifier: Modifier = Modifier) {
    CenteredColumn(modifier) {
        StateIcon(
            icon = Icons.Default.SearchOff,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Book not found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "It may have been removed from the catalog",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun CoverPreviewDialog(
    coverUrl: String,
    title: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = coverUrl,
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.75f)
                    .clip(CoverShape)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}
