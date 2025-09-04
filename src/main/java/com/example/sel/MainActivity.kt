package com.example.sel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sel.ui.theme.MyApplicationTheme

// 1) 데이터 모델
data class Emotion(val id: Int, val name: String, val emoji: String)
data class SelEntry(
    val preEmotion: Emotion,
    val preLevel: Int,
    val activity: String,
    val postEmotion: Emotion,
    val postLevel: Int
)

// 2) 기본 데이터
private val EMOTIONS = listOf(
    Emotion(1,"행복","😀"), Emotion(2,"편안","🙂"), Emotion(3,"보통","😐"), Emotion(4,"지루","🥱"),
    Emotion(5,"슬픔","😢"), Emotion(6,"걱정","😟"), Emotion(7,"분노","😡"), Emotion(8,"짜증","😖"),
    Emotion(9,"놀람","😮"), Emotion(10,"긴장","😬")
)
private val ACTIVITIES = listOf("그림 그리기","체육하기","레고 만들기","호흡 운동","음악 듣기")

// 3) 단계 정의
private enum class Step { PRE, ACTIVITY, POST, SUMMARY }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MyApplicationTheme { SelApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelApp() {
    // 화면 상태(간단히 한 파일에서 관리)
    var step by rememberSaveable { mutableStateOf(Step.PRE) }

    var preEmotion by rememberSaveable { mutableStateOf<Emotion?>(null) }
    var preLevel by rememberSaveable { mutableIntStateOf(5) }
    var chosenActivity by rememberSaveable { mutableStateOf<String?>(null) }
    var postEmotion by rememberSaveable { mutableStateOf<Emotion?>(null) }
    var postLevel by rememberSaveable { mutableIntStateOf(5) }

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("SEL for All - MVP") }) }) { inner ->
        Box(Modifier.padding(inner)) {
            when (step) {
                Step.PRE -> {
                    EmotionStep(
                        title = "사전 감정 체크",
                        selected = preEmotion,
                        level = preLevel,
                        onSelectEmotion = { preEmotion = it },
                        onLevelChange = { preLevel = it },
                        onNext = { if (preEmotion != null) step = Step.ACTIVITY }
                    )
                }
                Step.ACTIVITY -> {
                    ActivityStep(
                        activities = ACTIVITIES,
                        selected = chosenActivity,
                        onSelect = { chosenActivity = it },
                        onNext = { if (chosenActivity != null) step = Step.POST },
                        onBack = { step = Step.PRE }
                    )
                }
                Step.POST -> {
                    EmotionStep(
                        title = "사후 감정 체크",
                        selected = postEmotion,
                        level = postLevel,
                        onSelectEmotion = { postEmotion = it },
                        onLevelChange = { postLevel = it },
                        onNext = { if (postEmotion != null) step = Step.SUMMARY },
                        onBack = { step = Step.ACTIVITY }
                    )
                }
                Step.SUMMARY -> {
                    val entry = remember(preEmotion, preLevel, chosenActivity, postEmotion, postLevel) {
                        if (preEmotion != null && chosenActivity != null && postEmotion != null)
                            SelEntry(preEmotion!!, preLevel, chosenActivity!!, postEmotion!!, postLevel)
                        else null
                    }
                    SummaryStep(
                        entry = entry,
                        onRestart = {
                            // 다음 기록을 위해 초기화
                            preEmotion = null; preLevel = 5
                            chosenActivity = null
                            postEmotion = null; postLevel = 5
                            step = Step.PRE
                        },
                        onBack = { step = Step.POST }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmotionStep(
    title: String,
    selected: Emotion?,
    level: Int,
    onSelectEmotion: (Emotion) -> Unit,
    onLevelChange: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("이모티콘 10개 중 하나를 선택하세요")
        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            items(EMOTIONS) { item ->
                EmotionChip(
                    emotion = item,
                    selected = item.id == selected?.id,
                    onClick = { onSelectEmotion(item) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("강도(1~10): $level", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = level.toFloat(),
            onValueChange = { onLevelChange(it.toInt().coerceIn(1,10)) },
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "감정 강도 슬라이더" }
        )

        Spacer(Modifier.height(16.dp))
        Row {
            if (onBack != null) {
                OutlinedButton(onClick = onBack) { Text("이전") }
                Spacer(Modifier.width(8.dp))
            }
            Button(
                onClick = onNext,
                enabled = selected != null
            ) { Text("다음") }
        }
    }
}

@Composable
private fun EmotionChip(emotion: Emotion, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) ButtonDefaults.outlinedButtonBorder else null
    OutlinedButton(
        onClick = onClick,
        border = border,
        modifier = Modifier
            .padding(4.dp)
            .size(64.dp)
            .semantics { contentDescription = "${emotion.name} 선택" }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emotion.emoji, textAlign = TextAlign.Center)
            Text(emotion.name, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ActivityStep(
    activities: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("추천 활동 선택", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxWidth()) {
            activities.forEach { act ->
                val isSelected = act == selected
                val style = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                Surface(
                    tonalElevation = if (isSelected) 4.dp else 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .semantics { contentDescription = "$act 선택" }
                        .clickable { onSelect(act) }
                ) {
                    Text(
                        text = if (isSelected) "✔ $act" else act,
                        modifier = Modifier.padding(16.dp),
                        color = style
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row {
            OutlinedButton(onClick = onBack) { Text("이전") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onNext, enabled = selected != null) { Text("다음") }
        }
    }
}

@Composable
private fun SummaryStep(entry: SelEntry?, onRestart: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("요약", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (entry == null) {
            Text("입력이 부족합니다.")
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBack) { Text("이전") }
            return
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("사전 감정: ${entry.preEmotion.name} (${entry.preEmotion.emoji}), 강도 ${entry.preLevel}")
                Text("활동: ${entry.activity}")
                Text("사후 감정: ${entry.postEmotion.name} (${entry.postEmotion.emoji}), 강도 ${entry.postLevel}")
                val diff = entry.postLevel - entry.preLevel
                val result = when {
                    diff < 0 -> "강도가 ${-diff}만큼 낮아졌어요 👍"
                    diff > 0 -> "강도가 ${diff}만큼 높아졌네요 👀"
                    else -> "변화가 없어요."
                }
                Spacer(Modifier.height(8.dp))
                Text(result, style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRestart) { Text("새 기록 시작") }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSelApp() {
    MyApplicationTheme { SelApp() }
}
