package com.example.androidhomework.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.example.androidhomework.data.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: Book?,
    onBackClick: () -> Unit
) {
    if (book == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Book not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                val (
                    cover, title, author, yearBadge,
                    ratingRow, statsCard, chips, descTitle, description
                ) = createRefs()

                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(130.dp)
                        .height(190.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .constrainAs(cover) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                        }
                )

                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.constrainAs(title) {
                        top.linkTo(parent.top)
                        start.linkTo(cover.end, margin = 16.dp)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )

                Text(
                    text = "by ${book.author}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.constrainAs(author) {
                        top.linkTo(title.bottom, margin = 4.dp)
                        start.linkTo(title.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )

                SuggestionChip(
                    onClick = {},
                    label = { Text("Year: ${book.publishYear}") },
                    modifier = Modifier.constrainAs(yearBadge) {
                        top.linkTo(author.bottom, margin = 8.dp)
                        start.linkTo(title.start)
                    }
                )

                Row(
                    modifier = Modifier.constrainAs(ratingRow) {
                        top.linkTo(yearBadge.bottom, margin = 8.dp)
                        start.linkTo(title.start)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${book.rating}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = " (${book.ratingsCount})",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.constrainAs(statsCard) {
                        top.linkTo(cover.bottom, margin = 24.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(label = "Pages", value = "${book.pageCount}")
                        StatItem(label = "Language", value = book.language)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(chips) {
                            top.linkTo(statsCard.bottom, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    book.subjects.take(3).forEach { subject ->
                        AssistChip(onClick = {}, label = { Text(subject) })
                    }
                }

                Text(
                    text = "About this book",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.constrainAs(descTitle) {
                        top.linkTo(chips.bottom, margin = 20.dp)
                        start.linkTo(parent.start)
                    }
                )

                Text(
                    text = book.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    lineHeight = 22.sp,
                    modifier = Modifier.constrainAs(description) {
                        top.linkTo(descTitle.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}