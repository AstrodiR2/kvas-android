package com.astrodir.kvas.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astrodir.kvas.R
import com.astrodir.kvas.terminal.TerminalState
import com.astrodir.kvas.ui.components.IconBtn
import com.astrodir.kvas.ui.theme.K
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ConsolePanel(visible: Boolean, term: TerminalState, onClose: () -> Unit) {
    val c = K.c
    val panelW = LocalConfiguration.current.screenWidthDp.dp - 52.dp
    var dx by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(visible) { dx = 0f }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier.fillMaxSize().background(c.scrim)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
            )
        }
        AnimatedVisibility(
            visible,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = slideInHorizontally(tween(380)) { it },
            exit = slideOutHorizontally(tween(300)) { it },
        ) {
            val shape = RoundedCornerShape(topStart = 26.dp, bottomStart = 26.dp)
            Column(
                Modifier.offset { IntOffset(dx.roundToInt(), 0) }
                    .width(panelW).fillMaxHeight()
                    .shadow(16.dp, shape).clip(shape).background(c.s1)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (dx > size.width * 0.3f) onClose()
                                dx = 0f
                            },
                            onDragCancel = { dx = 0f },
                        ) { _, delta -> dx = (dx + delta).coerceAtLeast(0f) }
                    }
                    .windowInsetsPadding(WindowInsets.systemBars).imePadding(),
            ) {
                ConsoleBody(term, onClose)
            }
        }
    }
}

@Composable
private fun ConsoleBody(term: TerminalState, onClose: () -> Unit) {
    val c = K.c
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val focus = remember { FocusRequester() }
    val fm = LocalFocusManager.current
    val mono = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, lineHeight = 22.sp, color = c.t2)

    LaunchedEffect(term.lines.size, term.partial) { listState.scrollToItem(term.lines.size) }

    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBtn(R.drawable.ic_back, onClose)
        Text("Консоль", color = c.t1, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp))
    }
    HorizontalDivider(color = c.bd)

    LazyColumn(
        state = listState,
        modifier = Modifier.weight(1f).fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focus.requestFocus() },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
    ) {
        items(term.lines.size) { i ->
            val l = term.lines[i]
            Text(l, style = mono, color = if (l.startsWith("\$ ")) c.t1 else c.t2)
        }
        item {
            Column {
                if (term.partial.isNotEmpty()) Text(term.partial, style = mono)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("\$ ", style = mono.copy(color = c.ac))
                    BasicTextField(
                        value = term.input,
                        onValueChange = term::onInput,
                        modifier = Modifier.weight(1f).focusRequester(focus),
                        textStyle = mono.copy(color = c.t1),
                        cursorBrush = SolidColor(c.ac),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send, autoCorrectEnabled = false),
                        keyboardActions = KeyboardActions(onSend = { term.run() }),
                    )
                }
            }
        }
    }

    // дополнительные клавиши, как в Termux
    val rows = listOf(
        listOf("ESC", "/", "—", "HOME", "↑", "END", "PGUP"),
        listOf("⇥", "CTRL", "ALT", "←", "↓", "→", "PGDN"),
    )
    Column(Modifier.fillMaxWidth().background(c.s2).padding(horizontal = 2.dp, vertical = 4.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { k ->
                    val active = (k == "CTRL" && term.ctrl) || (k == "ALT" && term.alt)
                    Box(
                        Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(10.dp))
                            .background(if (active) c.acSoft else c.s2)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                when (k) {
                                    "ESC" -> fm.clearFocus()
                                    "/" -> term.insert("/")
                                    "—" -> term.insert("-")
                                    "⇥" -> term.insert("  ")
                                    "HOME" -> term.moveTo(0)
                                    "END" -> term.moveTo(term.input.text.length)
                                    "↑" -> term.historyUp()
                                    "↓" -> term.historyDown()
                                    "←" -> term.cursorLeft()
                                    "→" -> term.cursorRight()
                                    "PGUP" -> scope.launch { listState.animateScrollBy(-listState.layoutInfo.viewportSize.height * 0.8f) }
                                    "PGDN" -> scope.launch { listState.animateScrollBy(listState.layoutInfo.viewportSize.height * 0.8f) }
                                    "CTRL" -> term.ctrl = !term.ctrl
                                    "ALT" -> term.alt = !term.alt
                                }
                                if (k != "CTRL" && k != "ALT") { term.ctrl = false; term.alt = false }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(k, color = if (active) c.ac else c.t1, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
