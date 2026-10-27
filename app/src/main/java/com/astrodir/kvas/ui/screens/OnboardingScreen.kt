package com.astrodir.kvas.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astrodir.kvas.R
import com.astrodir.kvas.ui.components.KIcon
import com.astrodir.kvas.ui.components.press
import com.astrodir.kvas.ui.theme.K
import com.astrodir.kvas.ui.theme.KvasGrad
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val LAST = 3
private val Amber2 = Color(0xFFF0B429)

/**
 * Онбординг: приветствие → имя → цель → установка терминала.
 * ВАЖНО: шаг установки пока СИМУЛИРУЕТ прогресс (как в HTML-макете).
 * Реальную загрузку/распаковку подключим, когда решим, как запускать бинарники.
 */
@Composable
fun OnboardingScreen(onFinish: (name: String, purpose: Int) -> Unit) {
    val c = K.c
    val scope = rememberCoroutineScope()
    val pager = rememberPagerState(pageCount = { 4 })
    var pos by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var purpose by remember { mutableIntStateOf(-1) }
    var progress by remember { mutableFloatStateOf(0f) }
    var started by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }

    fun go(n: Int) { pos = n; scope.launch { pager.animateScrollToPage(n) } }

    LaunchedEffect(pos) {
        if (pos == LAST && !started) {
            started = true
            while (progress < 100f) {
                delay(220)
                val step = when {
                    progress < 40f -> 2f + Random.nextFloat() * 5f
                    progress < 85f -> 1f + Random.nextFloat() * 4f
                    else -> 0.6f + Random.nextFloat() * 2.2f
                }
                progress = minOf(100f, progress + step)
            }
            delay(450)
            done = true
        }
    }

    val disabled = (pos == LAST && !done) || (pos == 1 && name.isBlank()) || (pos == 2 && purpose < 0)
    val btnText = if (pos == LAST) (if (done) "Начать пользоваться" else "Устанавливаем…") else "Далее"

    Column(
        Modifier.fillMaxSize().background(c.bg)
            .windowInsetsPadding(WindowInsets.systemBars).imePadding(),
    ) {
        // верх: назад + сегменты
        Row(Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(44.dp).alpha(if (pos == 0 || pos == LAST) 0f else 1f)) {
                if (pos != 0 && pos != LAST) {
                    Text("‹", color = c.t2, fontSize = 28.sp, modifier = Modifier.press { go(pos - 1) }.padding(end = 12.dp))
                }
            }
            Row(Modifier.weight(1f).widthIn(max = 176.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { i ->
                    val f by animateFloatAsState(if (i <= pos) 1f else 0f, tween(400), label = "seg$i")
                    Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(c.s3)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(f).background(Brush.horizontalGradient(listOf(c.ac, Amber2))))
                    }
                }
            }
            Spacer(Modifier.width(44.dp))
        }

        HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth(), userScrollEnabled = false) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> NamePage(name) { if (it.length <= 24) name = it }
                2 -> PurposePage(purpose) { purpose = it }
                else -> InstallPage(name, progress, done)
            }
        }

        Box(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 20.dp)) {
            Box(
                Modifier.fillMaxWidth()
                    .alpha(if (disabled) 0.4f else 1f)
                    .press { if (!disabled) { if (pos == LAST) onFinish(name.trim(), purpose) else go(pos + 1) } }
                    .clip(RoundedCornerShape(18.dp)).background(KvasGrad).padding(vertical = 17.dp),
                contentAlignment = Alignment.Center,
            ) { Text(btnText, color = c.onAc, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun WelcomePage() {
    val c = K.c
    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, bottom = 48.dp), verticalArrangement = Arrangement.Center) {
        val t = rememberInfiniteTransition(label = "mark")
        val f by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1750), RepeatMode.Reverse), label = "f")
        Box(
            Modifier.padding(bottom = 36.dp)
                .graphicsLayer { translationY = -6f * density * f }
                .shadow(14.dp, RoundedCornerShape(26.dp), ambientColor = c.ac, spotColor = c.ac)
                .size(84.dp).clip(RoundedCornerShape(26.dp)).background(KvasGrad),
            contentAlignment = Alignment.Center,
        ) { KIcon(R.drawable.ic_lg0, tint = Color.White, size = 46.dp) } // «Кружка»
        Text("Привет, разработчик", color = c.t1, fontSize = 44.sp, lineHeight = 46.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            "Заходите, смотрите своё приложение. Почту привязывать не нужно.",
            color = c.t2, fontSize = 17.sp, lineHeight = 25.sp, modifier = Modifier.padding(top = 16.dp).widthIn(max = 320.dp),
        )
    }
}

@Composable
private fun PageHead(title: String, lead: String) {
    val c = K.c
    Text(title, color = c.t1, fontSize = 26.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold)
    Text(lead, color = c.t2, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 10.dp).widthIn(max = 320.dp))
}

@Composable
private fun NamePage(name: String, onName: (String) -> Unit) {
    val c = K.c
    var focused by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 28.dp)) {
        PageHead("Как вас называть, разработчик?", "Так Квас будет обращаться к вам в чате")
        BasicTextField(
            value = name, onValueChange = onName, singleLine = true,
            textStyle = TextStyle(color = c.t1, fontSize = 30.sp, fontWeight = FontWeight.SemiBold),
            cursorBrush = SolidColor(c.ac),
            modifier = Modifier.fillMaxWidth().padding(top = 36.dp).onFocusChanged { focused = it.isFocused },
            decorationBox = { inner ->
                Column {
                    Box(Modifier.padding(top = 10.dp, bottom = 12.dp)) {
                        if (name.isEmpty()) Text("Ваше имя", color = c.t3, fontSize = 30.sp, fontWeight = FontWeight.Medium)
                        inner()
                    }
                    Box(Modifier.fillMaxWidth().height(2.dp).background(if (focused) c.ac else c.bd))
                }
            },
        )
        val a by animateFloatAsState(if (name.isNotBlank()) 1f else 0f, tween(350), label = "greet")
        Text(
            if (name.isNotBlank()) "Приятно познакомиться, ${name.trim()}." else "",
            color = c.t2, fontSize = 16.sp, modifier = Modifier.padding(top = 18.dp).alpha(a),
        )
    }
}

@Composable
private fun PurposePage(selected: Int, onSelect: (Int) -> Unit) {
    val c = K.c
    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 28.dp)) {
        PageHead("Для чего будете использовать?", "Разработчик, выберите, что ближе всего")
        Column(Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val items = listOf(
                Triple(R.drawable.ic_code, "Код и разработка", "Скрипты, отладка, терминал"),
                Triple(R.drawable.ic_task, "Повседневные задачи", "Тексты, планы, быстрые ответы"),
                Triple(R.drawable.ic_chat, "Просто пообщаться", "Разговор без задач"),
            )
            items.forEachIndexed { i, (icon, title, sub) ->
                val sel = selected == i
                val border by animateColorAsState(if (sel) c.ac else c.bd, label = "b$i")
                val shape = RoundedCornerShape(20.dp)
                Row(
                    Modifier.fillMaxWidth().press { onSelect(i) }.clip(shape)
                        .background(if (sel) c.s2 else c.s1).border(1.5.dp, border, shape).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(if (sel) c.ac else c.s2),
                        contentAlignment = Alignment.Center,
                    ) { KIcon(icon, tint = if (sel) c.onAc else c.ac, size = 22.dp) }
                    Column {
                        Text(title, color = c.t1, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(sub, color = c.t2, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InstallPage(name: String, progress: Float, done: Boolean) {
    val c = K.c
    val termBg = if (c.dark) Color(0xFF080705) else Color(0xFF1C160B)
    val termText = if (c.dark) Color(0xFFEFE6D2) else Color(0xFFEADFC6)
    val termDim = if (c.dark) Color(0xFF7C705A) else Color(0xFF8C7E62)
    val termLine = Amber2.copy(alpha = 0.15f)
    val q = progress.toInt().coerceIn(0, 100)
    // 0 ждёт, 1 идёт, 2 готово
    val st = when {
        q < 40 -> intArrayOf(1, 0, 0)
        q < 85 -> intArrayOf(2, 1, 0)
        q < 100 -> intArrayOf(2, 2, 1)
        else -> intArrayOf(2, 2, 2)
    }
    val det = listOf(
        when { q < 40 -> "%.1f / 46 МБ".format(q / 40f * 46f); else -> "46 МБ" },
        when { q < 40 -> ""; q < 85 -> "${((q - 40) / 45f * 600).toInt()}/600"; else -> "600/600" },
        "",
    )
    val labels = listOf("Скачивание пакета", "Распаковка файлов", "Настройка окружения")

    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 28.dp)) {
        PageHead(
            if (done) (if (name.isNotBlank()) "Готово, ${name.trim()}!" else "Готово, разработчик!") else "Готовим терминал",
            if (done) "Окружение установлено, разработчик. Можно начинать." else "Разработчик, устанавливаем окружение один раз — дальше всё будет мгновенно",
        )
        Column(Modifier.padding(top = 28.dp).fillMaxWidth().shadow(12.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(termBg)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                val mono = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Text("$ ", style = mono, color = Amber2)
                Text("kvas setup", style = mono, color = termDim)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(termLine))
            Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                labels.forEachIndexed { i, label ->
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(termLine))
                    Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StepIcon(st[i], termLine)
                        Text(label, Modifier.weight(1f), color = if (st[i] == 0) termDim else termText, fontSize = 14.5.sp)
                        Text(det[i], color = if (st[i] == 0) termDim else termText, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.5.sp), textAlign = TextAlign.End)
                    }
                }
            }
        }
        Row(Modifier.padding(top = 24.dp).height(36.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            val frac by animateFloatAsState(q / 100f, tween(300), label = "bar")
            Box(Modifier.weight(1f).height(10.dp).clip(CircleShape).background(c.s3)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(frac).clip(CircleShape).background(Brush.horizontalGradient(listOf(c.ac, Amber2))))
            }
            Text("$q%", color = c.t1, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
        }
        val ha by animateFloatAsState(if (done) 0f else 1f, tween(400), label = "hint")
        Text("Это нужно только один раз", color = c.t3, fontSize = 12.5.sp, modifier = Modifier.padding(top = 14.dp).alpha(ha))
    }
}

@Composable
private fun StepIcon(state: Int, line: Color) {
    val t = rememberInfiniteTransition(label = "spin")
    val spin by t.animateFloat(0f, 360f, infiniteRepeatable(tween(800, easing = LinearEasing)), label = "s")
    val fill by animateFloatAsState(if (state == 2) 1f else 0f, spring(dampingRatio = 0.55f), label = "fill")
    Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = 1.8.dp.toPx()
            if (state < 2) drawCircle(line, radius = size.minDimension / 2 - sw, style = Stroke(sw))
            if (state == 1) {
                rotate(spin) {
                    drawArc(
                        Amber2, startAngle = 0f, sweepAngle = 90f, useCenter = false,
                        topLeft = Offset(sw, sw), size = Size(size.width - 2 * sw, size.height - 2 * sw),
                        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
            }
            if (fill > 0f) drawCircle(Amber2, radius = size.minDimension / 2 * fill)
        }
        if (state == 2) KIcon(R.drawable.ic_ok, tint = Color(0xFF1A1304), size = 14.dp)
    }
}
