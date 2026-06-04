package com.minlish.app.presentation.learn

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.minlish.app.core.util.AudioPlayer
import com.minlish.app.domain.model.*
import com.minlish.app.domain.model.enumration.*
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.usecase.CalculateSrsUseCase
import com.minlish.app.presentation.common.BunnyLoadingScreen
import com.minlish.app.presentation.common.BunnyAppBar
import com.minlish.app.presentation.common.BunnyBottomNavBar
import com.minlish.app.presentation.common.BunnyTab
import com.minlish.app.presentation.theme.BunnyColors
import com.minlish.app.presentation.theme.BunnyTypography
import kotlinx.coroutines.flow.*

enum class LearnMode {
    FLASHCARD, SRS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    viewModel: LearnViewModel,
    mode: LearnMode = LearnMode.FLASHCARD,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items by (if (mode == LearnMode.SRS) viewModel.dueVocabularies else viewModel.flashcards).collectAsState()
    val currentIndex by (if (mode == LearnMode.SRS) viewModel.currentSrsIndex else viewModel.currentCardIndex).collectAsState()
    val isFlipped by (if (mode == LearnMode.SRS) viewModel.isSrsCardFlipped else viewModel.isCardFlipped).collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val learnedCount by viewModel.learnedCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()

    // Hiệu ứng chạy số mượt mà cho thanh progress
    val animatedProgress by animateFloatAsState(
        targetValue = loadingProgress,
        animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing),
        label = "LoadingProgressAnimation"
    )

    Crossfade(
        targetState = isLoading,
        animationSpec = tween(durationMillis = 400),
        label = "LoadingTransition"
    ) { loading ->
        if (loading) {
            BunnyLoadingScreen(
                message = "Preparing vocabulary...",
                progress = animatedProgress
            )
        } else {
            // Đảm bảo dữ liệu đã sẵn sàng hiển thị ngay khi vừa load xong
            val currentVocab = if (items.isNotEmpty() && currentIndex < items.size) {
                items[currentIndex]
            } else null

            Scaffold(
                topBar = { BunnyAppBar(onBackClick = onNavigateBack) },
                containerColor = BunnyColors.Background,
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

                    val progress = if (items.isNotEmpty()) {
                        if (mode == LearnMode.SRS) currentIndex.toFloat() / items.size else learnedCount.toFloat() / items.size
                    } else 1f
                    val progressPercent = (progress * 100).toInt()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (mode == LearnMode.SRS) "Today's progress" else "${items.size - learnedCount} words left",
                            style = BunnyTypography.LabelMd,
                            color = if (mode == LearnMode.SRS) BunnyColors.Primary else BunnyColors.OnSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (mode == LearnMode.SRS) "$currentIndex/${items.size}" else "$progressPercent%",
                            style = BunnyTypography.LabelMd,
                            color = if (mode == LearnMode.SRS) BunnyColors.OnSurfaceVariant else BunnyColors.Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(BunnyColors.PrimaryContainer.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(BunnyColors.ProgressStart, BunnyColors.ProgressEnd)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

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
                                            offsetX > swipeThreshold -> {
                                                if (mode == LearnMode.SRS) viewModel.previousSrsCard() else viewModel.previousFlashcard()
                                            }
                                            offsetX < -swipeThreshold -> {
                                                if (mode == LearnMode.SRS) viewModel.nextSrsCard() else viewModel.nextFlashcard()
                                            }
                                        }
                                        offsetX = 0f
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetX += dragAmount
                                    }
                                )
                            },
                        label = "CardContentTransition"
                    ) { (state, completed) ->
                        val vocab = state.second
                        if (completed || items.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
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
                                    text = if (items.isEmpty()) "Excellent!" else "Vocabulary set completed!",
                                    style = BunnyTypography.HeadlineMd,
                                    color = BunnyColors.OnSurface,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (items.isEmpty()) "No vocabulary to review today." else "You have completed all vocabulary in this set.",
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
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Exit", style = BunnyTypography.LabelMd)
                                    }
                                    if (items.isNotEmpty()) {
                                        Button(
                                            onClick = { viewModel.restartLearning() },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BunnyColors.Primary)
                                        ) {
                                            Text("Review", style = BunnyTypography.LabelMd, color = Color.White)
                                        }
                                    }
                                }
                            }
                        } else if (vocab != null) {
                            LearnFlippableCard(
                                vocab = vocab,
                                isFlipped = isFlipped,
                                onFlip = { if (mode == LearnMode.SRS) viewModel.toggleSrsCardFlip() else viewModel.flipCard() }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentVocab != null && !isCompleted) {
                            if (isFlipped) {
                                var estHard by remember(currentVocab.id) { mutableStateOf("1 day") }
                                var estGood by remember(currentVocab.id) { mutableStateOf("4 days") }
                                var estEasy by remember(currentVocab.id) { mutableStateOf("7 days") }

                                LaunchedEffect(currentVocab.id) {
                                    estHard = viewModel.getButtonIntervalEstimate(currentVocab.id, EaseFactor.HARD)
                                    estGood = viewModel.getButtonIntervalEstimate(currentVocab.id, EaseFactor.GOOD)
                                    estEasy = viewModel.getButtonIntervalEstimate(currentVocab.id, EaseFactor.EASY)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    SrsButton(
                                        title = "Again", subtitle = "1 day",
                                        containerColor = BunnyColors.ErrorContainer, contentColor = BunnyColors.Error,
                                        modifier = Modifier.weight(1f)
                                    ) { viewModel.submitSrsGrade(currentVocab.id, EaseFactor.AGAIN) }

                                    SrsButton(
                                        title = "Hard", subtitle = estHard,
                                        containerColor = Color(0xFFECECFF), contentColor = BunnyColors.OnSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    ) { viewModel.submitSrsGrade(currentVocab.id, EaseFactor.HARD) }

                                    SrsButton(
                                        title = "Good", subtitle = estGood,
                                        containerColor = BunnyColors.PrimaryContainer, contentColor = BunnyColors.Primary,
                                        modifier = Modifier.weight(1f)
                                    ) { viewModel.submitSrsGrade(currentVocab.id, EaseFactor.GOOD) }

                                    SrsButton(
                                        title = "Easy", subtitle = estEasy,
                                        containerColor = BunnyColors.PrimaryContainer.copy(alpha = 0.7f), contentColor = BunnyColors.Primary,
                                        modifier = Modifier.weight(1f)
                                    ) { viewModel.submitSrsGrade(currentVocab.id, EaseFactor.EASY) }
                                }
                            } else {
                                Text(
                                    text = "Tap the card to see the answer",
                                    style = BunnyTypography.BodyMd,
                                    fontStyle = FontStyle.Italic,
                                    color = BunnyColors.Outline,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (mode == LearnMode.SRS) viewModel.toggleSrsCardFlip() else viewModel.flipCard()
                                        }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun SrsButton(
    title: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = modifier.border(1.dp, contentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = BunnyTypography.LabelMd.copy(color = contentColor))
            Text(subtitle, style = BunnyTypography.LabelSm.copy(color = contentColor.copy(alpha = 0.6f)))
        }
    }
}

@Composable
fun LearnFlippableCard(
    vocab: Vocabulary,
    isFlipped: Boolean,
    onFlip: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "CardRotation"
    )
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onFlip() }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density.density
            },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = BunnyColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.dp, BunnyColors.OutlineVariant.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = BunnyColors.PrimaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Target Word",
                            style = BunnyTypography.LabelMd,
                            color = BunnyColors.OnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = vocab.word,
                            style = BunnyTypography.DisplayLg.copy(fontSize = 40.sp, color = BunnyColors.Primary),
                            textAlign = TextAlign.Center
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = vocab.pronunciation, style = BunnyTypography.BodyLg, color = BunnyColors.Outline)
                            if (vocab.wordType.isNotBlank()) {
                                Text(
                                    text = "(${vocab.wordType})",
                                    style = BunnyTypography.BodyMd.copy(fontStyle = FontStyle.Italic),
                                    color = BunnyColors.Outline.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(onClick = { AudioPlayer.play(vocab.voiceUrl) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Audio Sound",
                                    tint = BunnyColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Image(
                        painter = rememberAsyncImagePainter(model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBPb8nhvec0V6qrcRt3wA1HUo0C74kIgX72vUkiMJrSwOIfHhD5eSH9ZdMHZgu6ofWhm60YJW7sKyPBmiTDdmmWsmj8sQYkeNbJKSjgNer6i2I35AKS4XRYk1ChyN2zraQEgzBp0rth0BdjwLNELOXJL1cJnhy7xH4TrT3sJFvQDJ0BpVVCbUuORY_omeehjZEaMQPjInnNhEtzdUAe4Vj1yxsS8G2ycb6yeUuZ0MEAUJ_lRQB-hx0DD8oBsETDOFDDkKs2saUEY8A"),
                        contentDescription = "Bunny Mascot",
                        modifier = Modifier.size(180.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "Tap the card to see the answer",
                        style = BunnyTypography.BodyMd.copy(fontStyle = FontStyle.Italic),
                        color = BunnyColors.Outline
                    )
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = BunnyColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.dp, BunnyColors.OutlineVariant.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(BunnyColors.Primary, BunnyColors.ProgressEnd)
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = vocab.word,
                                style = BunnyTypography.HeadlineMd.copy(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold),
                                color = BunnyColors.OnSurface
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(BunnyColors.PrimaryContainer)
                                    .clickable { AudioPlayer.play(vocab.voiceUrl) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Pronounce Audio",
                                    tint = BunnyColors.Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(text = vocab.pronunciation, style = BunnyTypography.BodyMd, color = BunnyColors.Outline)

                        if (vocab.wordType.isNotBlank()) {
                            Surface(
                                color = BunnyColors.PrimaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = vocab.wordType,
                                    style = BunnyTypography.LabelMd.copy(fontWeight = FontWeight.Bold),
                                    color = BunnyColors.Primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.width(64.dp), color = BunnyColors.OutlineVariant.copy(alpha = 0.4f))

                        Text(text = vocab.meaning, style = BunnyTypography.HeadlineMd, color = BunnyColors.OnSurface, textAlign = TextAlign.Center)

                        if (vocab.descriptionEnglish.isNotBlank()) {
                            Text(
                                text = vocab.descriptionEnglish,
                                style = BunnyTypography.BodyMd.copy(fontStyle = FontStyle.Italic),
                                color = BunnyColors.OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (vocab.example.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "EXAMPLE",
                                    style = BunnyTypography.LabelSm.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                    color = BunnyColors.Primary.copy(alpha = 0.7f)
                                )
                                vocab.example.filter { it.isNotBlank() }.forEach { ex ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(BunnyColors.PrimaryContainer.copy(alpha = 0.3f))
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "\"$ex\"",
                                            style = BunnyTypography.BodyMd.copy(lineHeight = 22.sp),
                                            color = BunnyColors.OnSurface
                                        )
                                    }
                                }
                            }
                        }

                        if (vocab.note.isNotBlank()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "NOTE",
                                    style = BunnyTypography.LabelSm.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                    color = BunnyColors.Primary.copy(alpha = 0.7f)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFFEF9C3)) // Light yellow note background
                                        .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = vocab.note,
                                        style = BunnyTypography.BodyMd.copy(lineHeight = 20.sp),
                                        color = Color(0xFF713F12) // Dark brown text for yellow note
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
