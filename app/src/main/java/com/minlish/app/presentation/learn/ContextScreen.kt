package com.minlish.app.presentation.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minlish.app.core.util.AudioPlayer
import com.minlish.app.domain.model.Vocabulary
import com.minlish.app.presentation.common.BunnyLoadingScreen
import com.minlish.app.presentation.common.BunnyAppBar
import com.minlish.app.presentation.common.BunnyBottomNavBar
import com.minlish.app.presentation.common.BunnyTab
import com.minlish.app.presentation.theme.BunnyColors
import com.minlish.app.presentation.theme.BunnyTypography
import com.minlish.app.presentation.theme.ContextScreenColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextScreen(
    viewModel: LearnViewModel,
    deckId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.flashcards.collectAsState()
    val currentIndex by viewModel.currentCardIndex.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val learnedCount by viewModel.learnedCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()

    if (isLoading) {
        BunnyLoadingScreen(
            message = "Preparing learning space...",
            progress = loadingProgress
        )
        return
    }

    val currentVocab = if (items.isNotEmpty() && currentIndex < items.size) {
        items[currentIndex]
    } else null

    Scaffold(
        topBar = { BunnyAppBar(onBackClick = onNavigateBack) },
        containerColor = ContextScreenColors.SurfaceBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Progress Section (Mirror LearnScreen)
            val progress = if (items.isNotEmpty()) learnedCount.toFloat() / items.size else 1f
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Today's progress",
                    style = BunnyTypography.LabelMd,
                    color = ContextScreenColors.Primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$learnedCount/${items.size}",
                    style = BunnyTypography.LabelMd,
                    color = ContextScreenColors.OnSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(ContextScreenColors.SkyBlue.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(ContextScreenColors.Primary, ContextScreenColors.SkyBlue)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            var offsetX by remember { mutableStateOf(0f) }
            val density = LocalDensity.current
            val swipeThreshold = with(density) { 60.dp.toPx() }

            AnimatedContent(
                targetState = (currentIndex to currentVocab) to isCompleted,
                transitionSpec = {
                    if (targetState.first.first > initialState.first.first) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { translationX = offsetX }
                    .pointerInput(isCompleted) {
                        if (isCompleted) return@pointerInput
                        detectHorizontalDragGestures(
                            onDragCancel = { offsetX = 0f },
                            onDragEnd = {
                                when {
                                    offsetX > swipeThreshold -> viewModel.previousFlashcard()
                                    offsetX < -swipeThreshold -> viewModel.nextFlashcard()
                                }
                                offsetX = 0f
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount
                            }
                        )
                    },
                label = "ContextCardTransition"
            ) { (state, completed) ->
                val vocab = state.second
                if (completed) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF356F59),
                            modifier = Modifier.size(84.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Vocabulary set completed!",
                            style = BunnyTypography.HeadlineMd,
                            color = BunnyColors.OnSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You have completed all vocabulary in this set.",
                            textAlign = TextAlign.Center,
                            style = BunnyTypography.BodyMd,
                            color = BunnyColors.Outline,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(2.dp, ContextScreenColors.SkyBlue.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    "Exit",
                                    style = BunnyTypography.LabelMd,
                                    color = ContextScreenColors.Primary
                                )
                            }
                            Button(
                                onClick = { viewModel.restartLearning() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ContextScreenColors.SkyBlue)
                            ) {
                                Text(
                                    "Review",
                                    style = BunnyTypography.LabelMd,
                                    color = ContextScreenColors.Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else if (vocab != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // 1. Feature Header
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "LEARNING MODE",
                                color = ContextScreenColors.Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Context-based Learning",
                                color = BunnyColors.OnSurface,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                        }

                        // 3. Word Card (Bento Style)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(24.dp))
                                .border(
                                    1.dp,
                                    ContextScreenColors.SkyBlue.copy(alpha = 0.2f),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "TARGET WORD",
                                color = ContextScreenColors.Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .background(ContextScreenColors.SkyBlue, CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )

                            Text(
                                text = vocab.word,
                                color = BunnyColors.OnSurface,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 36.sp,
                                letterSpacing = (-0.5).sp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = vocab.phonetic,
                                    color = ContextScreenColors.OnSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                                if (vocab.soundUrl.isNotBlank()) {
                                    IconButton(
                                        onClick = { AudioPlayer.play(vocab.soundUrl) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(ContextScreenColors.Primary, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Pronounce",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (vocab.wordType.isNotBlank()) {
                                Text(
                                    text = vocab.wordType,
                                    style = BunnyTypography.LabelMd.copy(fontStyle = FontStyle.Italic),
                                    color = ContextScreenColors.OnSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // 4. Context Area
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = ContextScreenColors.Primary
                                )
                                Text(
                                    text = "Real-world Context",
                                    fontWeight = FontWeight.Bold,
                                    color = BunnyColors.OnSurface,
                                    fontSize = 16.sp
                                )
                            }

                            vocab.context.split("\n").filter { it.isNotBlank() }.forEach { sentence ->
                                var isScaled by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(if (isScaled) 2.dp else 0.dp, RoundedCornerShape(16.dp))
                                        .background(Color.White, RoundedCornerShape(16.dp))
                                        .border(
                                            1.dp,
                                            ContextScreenColors.SkyBlue.copy(alpha = 0.2f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { isScaled = !isScaled }
                                        .padding(20.dp)
                                ) {
                                    HighlightedText(
                                        text = sentence,
                                        highlightWord = vocab.word
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun HighlightedText(text: String, highlightWord: String) {
    val annotatedString = buildAnnotatedString {
        // Tìm kiếm không phân biệt chữ hoa chữ thường
        val lowerText = text.lowercase()
        val lowerWord = highlightWord.lowercase()
        val startIndex = lowerText.indexOf(lowerWord)

        if (startIndex >= 0) {
            append(text.substring(0, startIndex))
            withStyle(
                style = SpanStyle(
                    color = ContextScreenColors.Primary,
                    fontWeight = FontWeight.Bold,
                    background = ContextScreenColors.MintGreenHighlight.copy(alpha = 0.4f)
                )
            ) {
                append(text.substring(startIndex, startIndex + highlightWord.length))
            }
            append(text.substring(startIndex + highlightWord.length))
        } else {
            append(text)
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = ContextScreenColors.OnSurfaceVariant,
            lineHeight = 24.sp
        )
    )
}

