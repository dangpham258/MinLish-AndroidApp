package com.minlish.app.presentation.learn

import androidx.compose.animation.*
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import com.minlish.app.domain.model.*
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.usecase.CalculateSrsUseCase
import com.minlish.app.presentation.common.component.BunnyAppBar
import com.minlish.app.presentation.common.component.BunnyBottomNavBar
import com.minlish.app.presentation.common.component.BunnyTab
import com.minlish.app.ui.theme.BunnyColors
import com.minlish.app.ui.theme.BunnyTypography
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

    val currentVocab = if (items.isNotEmpty() && currentIndex < items.size) {
        items[currentIndex]
    } else null

    LaunchedEffect(currentVocab?.id) {
        currentVocab?.id?.let { viewModel.markAsLearned(it) }
    }

    Scaffold(
        topBar = { BunnyAppBar(onBackClick = onNavigateBack) },
        bottomBar = { 
            BunnyBottomNavBar(
                selectedTab = BunnyTab.LESSONS,
                onTabSelected = { /* Handle navigation if needed */ }
            ) 
        },
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

            val wordsLeft = items.size - learnedCount
            val progress = if (items.isNotEmpty()) learnedCount.toFloat() / items.size else 1f
            val progressPercent = (progress * 100).toInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if (mode == LearnMode.SRS) "Tiến độ hôm nay" else "$wordsLeft words left",
                    style = BunnyTypography.LabelMd,
                    color = if (mode == LearnMode.SRS) BunnyColors.Primary else BunnyColors.OnSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (mode == LearnMode.SRS) "$learnedCount/${items.size}" else "$progressPercent%",
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
                if (completed) {
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
                            text = "Hoàn thành bộ từ!",
                            style = BunnyTypography.HeadlineMd,
                            color = BunnyColors.OnSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bạn đã hoàn thành tất cả từ vựng trong bộ này.",
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
                                Text("Thoát", style = BunnyTypography.LabelMd)
                            }
                            Button(
                                onClick = { viewModel.restartLearning() },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BunnyColors.Primary)
                            ) {
                                Text("Xem lại", style = BunnyTypography.LabelMd, color = Color.White)
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
                        var estHard by remember(currentVocab.id) { mutableStateOf("1 ngày") }
                        var estGood by remember(currentVocab.id) { mutableStateOf("4 ngày") }
                        var estEasy by remember(currentVocab.id) { mutableStateOf("7 ngày") }

                        LaunchedEffect(currentVocab.id) {
                            estHard = viewModel.getButtonIntervalEstimate(currentVocab.id, EaseFactor.Hard)
                            estGood = viewModel.getButtonIntervalEstimate(currentVocab.id, EaseFactor.Good)
                            estEasy = viewModel.getButtonIntervalEstimate(currentVocab.id, EaseFactor.Easy)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SrsButton(
                                title = "Lặp lại", subtitle = "1 ngày",
                                containerColor = BunnyColors.ErrorContainer, contentColor = BunnyColors.Error,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.submitSrsGrade(currentVocab.id, EaseFactor.Again) }

                            SrsButton(
                                title = "Khó", subtitle = estHard,
                                containerColor = Color(0xFFECECFF), contentColor = BunnyColors.OnSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.submitSrsGrade(currentVocab.id, EaseFactor.Hard) }

                            SrsButton(
                                title = "Tốt", subtitle = estGood,
                                containerColor = BunnyColors.PrimaryContainer, contentColor = BunnyColors.Primary,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.submitSrsGrade(currentVocab.id, EaseFactor.Good) }

                            SrsButton(
                                title = "Dễ", subtitle = estEasy,
                                containerColor = BunnyColors.PrimaryContainer.copy(alpha = 0.7f), contentColor = BunnyColors.Primary,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.submitSrsGrade(currentVocab.id, EaseFactor.Easy) }
                        }
                    } else {
                        Text(
                            text = "Chạm vào thẻ để xem đáp án",
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
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Audio Sound",
                                tint = BunnyColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Image(
                        painter = rememberAsyncImagePainter(model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBPb8nhvec0V6qrcRt3wA1HUo0C74kIgX72vUkiMJrSwOIfHhD5eSH9ZdMHZgu6ofWhm60YJW7sKyPBmiTDdmmWsmj8sQYkeNbJKSjgNer6i2I35AKS4XRYk1ChyN2zraQEgzBp0rth0BdjwLNELOXJL1cJnhy7xH4TrT3sJFvQDJ0BpVVCbUuORY_omeehjZEaMQPjInnNhEtzdUAe4Vj1yxsS8G2ycb6yeUuZ0MEAUJ_lRQB-hx0DD8oBsETDOFDDkKs2saUEY8A"),
                        contentDescription = "Bunny Mascot",
                        modifier = Modifier.size(180.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "Chạm vào thẻ để xem đáp án",
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
                                    .clickable { },
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
                                    text = "VÍ DỤ",
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

                        if (vocab.synonyms.isNotEmpty() || vocab.antonyms.isNotEmpty() || vocab.relatedWords.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "LIÊN QUAN",
                                    style = BunnyTypography.LabelSm.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                    color = BunnyColors.Primary.copy(alpha = 0.7f)
                                )
                                
                                if (vocab.synonyms.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(text = "Đồng nghĩa", style = BunnyTypography.LabelSm, color = BunnyColors.Outline)
                                        @OptIn(ExperimentalLayoutApi::class)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            vocab.synonyms.forEach { syn ->
                                                Surface(
                                                    color = Color(0xFFDCFCE7),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                                                ) {
                                                    Text(
                                                        text = syn,
                                                        style = BunnyTypography.LabelMd.copy(fontWeight = FontWeight.Bold),
                                                        color = Color(0xFF166534),
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (vocab.antonyms.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(text = "Trái nghĩa", style = BunnyTypography.LabelSm, color = BunnyColors.Outline)
                                        @OptIn(ExperimentalLayoutApi::class)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            vocab.antonyms.forEach { ant ->
                                                Surface(
                                                    color = Color(0xFFFEE2E2),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                                                ) {
                                                    Text(
                                                        text = ant,
                                                        style = BunnyTypography.LabelMd.copy(fontWeight = FontWeight.Bold),
                                                        color = Color(0xFF991B1B),
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (vocab.relatedWords.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(text = "Từ liên quan khác", style = BunnyTypography.LabelSm, color = BunnyColors.Outline)
                                        @OptIn(ExperimentalLayoutApi::class)
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            vocab.relatedWords.forEach { rw ->
                                                SuggestionChip(
                                                    onClick = { },
                                                    label = { Text(rw.word) },
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                            }
                                        }
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
                                    text = "GHI CHÚ",
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



@Preview(showBackground = true, name = "1. Flashcard Mode - Front")
@Composable
fun LearnScreenFrontPreview() {
    val mockViewModel = remember { MockLearnViewModel(LearnMode.FLASHCARD) }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LearnScreen(
                viewModel = mockViewModel,
                mode = LearnMode.FLASHCARD,
                onNavigateBack = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2. Flashcard Mode - Back")
@Composable
fun LearnScreenBackPreview() {
    val mockViewModel = remember {
        MockLearnViewModel(LearnMode.FLASHCARD).apply {
            setFlippedState(true)
        }
    }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LearnScreen(
                viewModel = mockViewModel,
                mode = LearnMode.FLASHCARD,
                onNavigateBack = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "3. SRS Mode - Front")
@Composable
fun SrsScreenFrontPreview() {
    val mockViewModel = remember { MockLearnViewModel(LearnMode.SRS) }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LearnScreen(
                viewModel = mockViewModel,
                mode = LearnMode.SRS,
                onNavigateBack = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "4. SRS Mode - Back")
@Composable
fun SrsScreenBackPreview() {
    val mockViewModel = remember {
        MockLearnViewModel(LearnMode.SRS).apply {
            setFlippedState(true)
        }
    }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LearnScreen(
                viewModel = mockViewModel,
                mode = LearnMode.SRS,
                onNavigateBack = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "5. Completed State (Inbox Zero)")
@Composable
fun LearnScreenCompletedPreview() {
    val mockViewModel = remember {
        MockLearnViewModel(LearnMode.SRS).apply {
            setEmptyState()
        }
    }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LearnScreen(
                viewModel = mockViewModel,
                mode = LearnMode.SRS,
                onNavigateBack = {}
            )
        }
    }
}

class MockLearnViewModel(mode: LearnMode) : LearnViewModel(
    repository = MockPreviewBunnyRepository(),
    calculateSrsUseCase = CalculateSrsUseCase()
) {
    private val sampleVocabs = listOf(
        Vocabulary(
            id = 1,
            word = "Persistence",
            pronunciation = "/pərˈsɪs.təns/",
            meaning = "Sự kiên trì, bền bỉ",
            descriptionEnglish = "The quality that allows someone to continue doing something even though it is difficult.",
            example = listOf("Her persistence finally paid off."),
            synonyms = listOf("Endurance", "Tenacity", "Diligence"),
            antonyms = listOf("Laziness", "Apathy", "Indifference"),
            relatedWords = listOf(
                Vocabulary(
                    id = 101, word = "Endurance", pronunciation = "/ɪnˈdjʊə.rəns/",
                    meaning = "Sự chịu đựng", descriptionEnglish = "", example = emptyList(), 
                    synonyms = emptyList(), antonyms = emptyList(), note = ""
                ),
                Vocabulary(
                    id = 102, word = "Tenacity", pronunciation = "/təˈnæs.ə.ti/",
                    meaning = "Sự bền bỉ", descriptionEnglish = "", example = emptyList(), 
                    synonyms = emptyList(), antonyms = emptyList(), note = ""
                )
            ),
            note = "IELTS core"
        )
    )

    private val _mockFlashcards = MutableStateFlow(if (mode == LearnMode.FLASHCARD) sampleVocabs else emptyList())
    override val flashcards: StateFlow<List<Vocabulary>> = _mockFlashcards.asStateFlow()

    private val _mockDueVocabularies = MutableStateFlow(if (mode == LearnMode.SRS) sampleVocabs else emptyList())
    override val dueVocabularies: StateFlow<List<Vocabulary>> = _mockDueVocabularies.asStateFlow()

    private val _mockCardIndex = MutableStateFlow(0)
    override val currentCardIndex: StateFlow<Int> = _mockCardIndex.asStateFlow()

    private val _mockSrsIndex = MutableStateFlow(0)
    override val currentSrsIndex: StateFlow<Int> = _mockSrsIndex.asStateFlow()

    private val _mockIsFlipped = MutableStateFlow(false)
    override val isCardFlipped: StateFlow<Boolean> = _mockIsFlipped.asStateFlow()

    private val _mockIsSrsFlipped = MutableStateFlow(false)
    override val isSrsCardFlipped: StateFlow<Boolean> = _mockIsSrsFlipped.asStateFlow()

    override fun flipCard() { _mockIsFlipped.value = !_mockIsFlipped.value }
    override fun nextFlashcard() {
        if (_mockFlashcards.value.isNotEmpty()) {
            _mockCardIndex.value = (_mockCardIndex.value + 1) % _mockFlashcards.value.size
        }
    }
    override fun previousFlashcard() {
        if (_mockFlashcards.value.isNotEmpty()) {
            val nextIndex = if (_mockCardIndex.value - 1 < 0) _mockFlashcards.value.size - 1 else _mockCardIndex.value - 1
            _mockCardIndex.value = nextIndex
        }
    }
    override fun nextSrsCard() {
        if (_mockDueVocabularies.value.isNotEmpty()) {
            _mockSrsIndex.value = (_mockSrsIndex.value + 1) % _mockDueVocabularies.value.size
        }
    }
    override fun previousSrsCard() {
        if (_mockDueVocabularies.value.isNotEmpty()) {
            val nextIndex = if (_mockSrsIndex.value - 1 < 0) _mockDueVocabularies.value.size - 1 else _mockSrsIndex.value - 1
            _mockSrsIndex.value = nextIndex
        }
    }
    override fun toggleSrsCardFlip() { _mockIsSrsFlipped.value = !_mockIsSrsFlipped.value }

    fun setFlippedState(flipped: Boolean) {
        _mockIsFlipped.value = flipped
        _mockIsSrsFlipped.value = flipped
    }

    fun setEmptyState() {
        _mockFlashcards.value = emptyList()
        _mockDueVocabularies.value = emptyList()
    }

    override suspend fun getButtonIntervalEstimate(vocabularyId: Int, button: EaseFactor): String {
        return "4 ngày"
    }

    override fun submitSrsGrade(vocabularyId: Int, button: EaseFactor) {
    }
}

class MockPreviewBunnyRepository : BunnyRepository {
    override fun getDecks(): Flow<List<Deck>> = flowOf(emptyList())
    override suspend fun insertDeck(deck: Deck): Long = 0
    override suspend fun getDeckById(deckId: Int): Deck? = null
    override fun getDeckByIdFlow(deckId: Int): Flow<Deck?> = flowOf(null)
    override fun getVocabularyByDeck(deckId: Int): Flow<List<Vocabulary>> = flowOf(emptyList())
    override fun getVocabularyById(id: Int): Flow<Vocabulary?> = flowOf(null)
    override suspend fun insertVocabulary(vocabulary: Vocabulary): Long = 0
    override suspend fun getVocabularyByIdDirect(id: Int): Vocabulary? = null
    override fun getActiveUserVocabularyStates(): Flow<List<UserVocabularyState>> = flowOf(emptyList())
    override fun getVocabularyDueForReview(currentTime: Long, deckId: Int?): Flow<List<Vocabulary>> = flowOf(emptyList())
    override fun getNewVocabularyForLearning(deckId: Int?, limit: Int): Flow<List<Vocabulary>> = flowOf(emptyList())
    override suspend fun getUserVocabularyState(vocabularyId: Int): UserVocabularyState? = null
    override suspend fun saveUserVocabularyState(state: UserVocabularyState, previousEaseFactor: EaseFactor?) {}
    override fun getUserName(): Flow<String> = flowOf("User")
    override suspend fun updateUserName(name: String) {}
    override fun getLearningGoal(): Flow<LearningGoal> = flowOf(LearningGoal.IELTS)
    override suspend fun updateLearningGoal(goal: LearningGoal) {}
    override fun getInitialLevel(): Flow<InitialLevel> = flowOf(InitialLevel.B1)
    override suspend fun updateInitialLevel(level: InitialLevel) {}
    override fun getUser(userId: Int): Flow<User?> = flowOf(null)
    override fun getLearnedWordsCount(): Flow<Int> = flowOf(0)
    override fun getStreakDaysCount(): Flow<Int> = flowOf(0)
    override suspend fun incrementStreak() {}
    override fun getReviewHistory(vocabularyId: Int): Flow<List<ReviewHistory>> = flowOf(emptyList())
    override suspend fun addReviewHistory(history: ReviewHistory) {}
    override fun getNotifications(): Flow<List<Notification>> = flowOf(emptyList())
    override suspend fun addNotification(notification: Notification) {}
    override suspend fun prepopulateInitialData() {}
}
