package com.astrodir.kvas.ui.screens

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astrodir.kvas.KvasState
import com.astrodir.kvas.Msg
import com.astrodir.kvas.R
import com.astrodir.kvas.ui.components.IconBtn
import com.astrodir.kvas.ui.components.KIcon
import com.astrodir.kvas.ui.components.LOGOS
import com.astrodir.kvas.ui.components.press
import com.astrodir.kvas.ui.theme.K
import com.astrodir.kvas.ui.theme.KvasGrad
import kotlinx.coroutines.CoroutineScope

@Composable
fun ChatScreen(
    state: KvasState,
    plusOpen: Boolean,
    onMenu: () -> Unit,
    onTerm: () -> Unit,
    onPlus: () -> Unit,
) {
    val c = K.c
    val chat = state.current
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val typingNow = state.typingChat === chat

    LaunchedEffect(chat, chat.msgs.size, typingNow) {
        val n = chat.msgs.size + if (typingNow) 1 else 0
        if (n > 0) listState.animateScrollToItem(n - 1)
    }

    Column(
        Modifier.fillMaxSize().background(c.bg)
            .windowInsetsPadding(WindowInsets.systemBars).imePadding(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBtn(R.drawable.ic_menu, onMenu)
            Text(
                chat.name, Modifier.weight(1f), color = c.t1, fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            IconBtn(R.drawable.ic_term, onTerm)
        }
        HorizontalDivider(color = c.bd)

        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (chat.msgs.isEmpty()) {
                Hero(state) { text = it }
            } else {
                val lastBot = chat.msgs.indexOfLast { !it.user }
                LazyColumn(
                    state = listState,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    itemsIndexed(chat.msgs, key = { _, m -> m.id }) { i, m ->
                        if (m.user) UserBubble(m) else BotMessage(m, i == lastBot, state, scope)
                    }
                    if (typingNow) item(key = "typing") { TypingDots() }
                }
            }
        }

        InputBar(
            text = text, onText = { text = it }, plusOpen = plusOpen, onPlus = onPlus,
            onSend = {
                val v = text.trim()
                if (v.isNotEmpty()) { text = ""; state.send(v, scope) }
            },
        )
    }
}

@Composable
private fun UserBubble(m: Msg) {
    val c = K.c
    val maxW = (LocalConfiguration.current.screenWidthDp * 0.8f).dp
    val alpha = remember(m.id) { Animatable(if (m.fresh) 0f else 1f) }
    LaunchedEffect(m.id) { if (m.fresh) alpha.animateTo(1f, tween(350)) }
    Row(Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha.value }, horizontalArrangement = Arrangement.End) {
        Box(
            Modifier.widthIn(max = maxW)
                .clip(RoundedCornerShape(18.dp, 18.dp, 5.dp, 18.dp))
                .background(KvasGrad)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(m.text, color = c.onAc, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun BotMessage(m: Msg, showDisc: Boolean, state: KvasState, scope: CoroutineScope) {
    val c = K.c
    val alpha = remember(m.id) { Animatable(if (m.fresh) 0f else 1f) }
    LaunchedEffect(m.id) { if (m.fresh) alpha.animateTo(1f, tween(450)) }
    val clipboard = LocalClipboardManager.current
    val ctx = LocalContext.current

    Column(Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha.value }) {
        if (m.loading) {
            TypingDots()
        } else {
            Text(m.text, color = c.t1, fontSize = 15.5.sp, lineHeight = 24.sp, modifier = Modifier.padding(horizontal = 2.dp))
            Row(Modifier.offset(x = (-8).dp)) {
                ActBtn(R.drawable.ic_copy, false) { clipboard.setText(AnnotatedString(m.text)); state.toast("Скопировано") }
                ActBtn(R.drawable.ic_share, false) {
                    val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, m.text) }
                    ctx.startActivity(Intent.createChooser(i, null))
                }
                ActBtn(R.drawable.ic_play, false) { state.toast("Озвучка — скоро") }
                ActBtn(R.drawable.ic_thumb, m.vote == 1) { m.vote = if (m.vote == 1) 0 else 1 }
                ActBtn(R.drawable.ic_thumb, m.vote == 2, flip = true) { m.vote = if (m.vote == 2) 0 else 2 }
                ActBtn(R.drawable.ic_redo, false) { state.redo(m, scope) }
            }
            if (showDisc) {
                Text("Квас — это ИИ, который может ошибаться.", color = c.t3, fontSize = 12.5.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun ActBtn(icon: Int, on: Boolean, flip: Boolean = false, onClick: () -> Unit) {
    val c = K.c
    Box(Modifier.press(onClick).size(38.dp, 36.dp), contentAlignment = Alignment.Center) {
        KIcon(icon, Modifier.rotate(if (flip) 180f else 0f), tint = if (on) c.ac else c.t3, size = 19.dp)
    }
}

@Composable
fun TypingDots() {
    val c = K.c
    val t = rememberInfiniteTransition(label = "dots")
    Row(Modifier.padding(horizontal = 2.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { i ->
            val y by t.animateFloat(
                0f, 0f,
                infiniteRepeatable(
                    keyframes<Float> { durationMillis = 1100; 0f at 0; -5f at 330; 0f at 660; 0f at 1100 },
                    RepeatMode.Restart, StartOffset(150 * i),
                ),
                label = "dot$i",
            )
            Box(
                Modifier.size(7.dp)
                    .graphicsLayer { translationY = y * density; alpha = 0.5f + 0.5f * (-y / 5f) }
                    .background(c.t3, CircleShape),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Hero(state: KvasState, onChip: (String) -> Unit) {
    val c = K.c
    val t = rememberInfiniteTransition(label = "hero")
    val f by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1750), RepeatMode.Reverse), label = "float")
    Column(
        Modifier.fillMaxSize().padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.graphicsLayer { translationY = -6f * density * f; rotationZ = 3f * f }
                .size(64.dp).clip(RoundedCornerShape(20.dp)).background(KvasGrad),
            contentAlignment = Alignment.Center,
        ) { KIcon(LOGOS[state.logo], tint = c.onAc, size = 30.dp) }
        Text("Чем помочь?", color = c.t1, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
        Text("Спросите что угодно или начните с подсказки", color = c.t2, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp, bottom = 18.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Сделай картинку кваса", "Что нового в чате?", "Напомни через час").forEach { s ->
                Box(
                    Modifier.press { onChip(s) }.clip(RoundedCornerShape(999.dp)).background(c.s1)
                        .border(1.dp, c.bd, RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                ) { Text(s, color = c.t1, fontSize = 13.5.sp) }
            }
        }
    }
}

@Composable
private fun InputBar(text: String, onText: (String) -> Unit, plusOpen: Boolean, onPlus: () -> Unit, onSend: () -> Unit) {
    val c = K.c
    var focused by remember { mutableStateOf(false) }
    val ready = text.isNotBlank()
    val rot by animateFloatAsState(if (plusOpen) 45f else 0f, label = "plusRot")
    val shape = RoundedCornerShape(24.dp)
    Box(Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 10.dp)) {
        Row(
            Modifier.fillMaxWidth().clip(shape).background(c.s1)
                .border(1.dp, if (focused) c.ac else c.bd, shape).padding(6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                Modifier.press(onPlus).size(38.dp).clip(CircleShape).background(if (plusOpen) c.acSoft else c.s2),
                contentAlignment = Alignment.Center,
            ) { KIcon(R.drawable.ic_plus, Modifier.rotate(rot), tint = if (plusOpen) c.ac else c.t1) }

            BasicTextField(
                value = text, onValueChange = onText,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp, vertical = 9.dp).onFocusChanged { focused = it.isFocused },
                textStyle = TextStyle(color = c.t1, fontSize = 15.sp, lineHeight = 21.sp),
                cursorBrush = SolidColor(c.ac),
                maxLines = 4,
                decorationBox = { inner ->
                    Box {
                        if (text.isEmpty()) Text("Напишите Квасу…", color = c.t3, fontSize = 15.sp)
                        inner()
                    }
                },
            )

            Box(
                Modifier.press(onSend).size(38.dp).clip(CircleShape)
                    .then(if (ready) Modifier.background(KvasGrad) else Modifier.background(c.s3)),
                contentAlignment = Alignment.Center,
            ) { KIcon(R.drawable.ic_up, tint = if (ready) c.onAc else c.t3) }
        }
    }
}
