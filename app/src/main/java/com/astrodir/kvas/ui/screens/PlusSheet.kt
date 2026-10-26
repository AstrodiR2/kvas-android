package com.astrodir.kvas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astrodir.kvas.KvasState
import com.astrodir.kvas.R
import com.astrodir.kvas.ui.components.KIcon
import com.astrodir.kvas.ui.components.KToggle
import com.astrodir.kvas.ui.components.Tile
import com.astrodir.kvas.ui.components.TileBlue
import com.astrodir.kvas.ui.components.TileGreen
import com.astrodir.kvas.ui.components.TileOrange
import com.astrodir.kvas.ui.components.TilePink
import com.astrodir.kvas.ui.components.TilePurple
import com.astrodir.kvas.ui.components.press
import com.astrodir.kvas.ui.theme.K
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlusSheet(state: KvasState, onDismiss: () -> Unit, onConnectors: () -> Unit) {
    val c = K.c
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun close(after: () -> Unit = {}) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss(); after() }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.s1,
        scrimColor = c.scrim,
        shape = RoundedCornerShape(24.dp),
        dragHandle = {
            Box(Modifier.padding(vertical = 10.dp).size(38.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(c.s3))
        },
    ) {
        Column(Modifier.padding(horizontal = 8.dp).navigationBarsPadding().padding(bottom = 10.dp)) {
            SheetRow(R.drawable.ic_img, TileBlue, "Фото и файлы", "добавить вложение", onClick = { close { state.toast("Выбор файла — скоро") } }) {
                KIcon(R.drawable.ic_chev, tint = c.t3, size = 18.dp)
            }
            Sep()
            SheetRow(R.drawable.ic_globe, TileGreen, "Поиск в интернете", "свежие ответы", onClick = { state.searchOn = !state.searchOn }) {
                KToggle(state.searchOn)
            }
            SheetRow(R.drawable.ic_layers, TilePurple, "Память", "учитывать другие чаты", onClick = { state.memoryOn = !state.memoryOn }) {
                KToggle(state.memoryOn)
            }
            Sep()
            SheetRow(R.drawable.ic_plug, TileOrange, "Коннекторы", "подключить сервисы", onClick = { close { onConnectors() } }) {
                KIcon(R.drawable.ic_chev, tint = c.t3, size = 18.dp)
            }
            SheetRow(R.drawable.ic_fplus, TilePink, "Добавить в проект", "перенести чат", onClick = { close { state.toast("Проекты — скоро") } }) {
                KIcon(R.drawable.ic_chev, tint = c.t3, size = 18.dp)
            }
        }
    }
}

@Composable
private fun Sep() {
    HorizontalDivider(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = K.c.bd)
}

@Composable
private fun SheetRow(icon: Int, tint: Color, title: String, sub: String, onClick: () -> Unit, trailing: @Composable () -> Unit) {
    val c = K.c
    Row(
        Modifier.fillMaxWidth().press(onClick).clip(RoundedCornerShape(14.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Tile(icon, tint)
        Column(Modifier.weight(1f)) {
            Text(title, color = c.t1, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(sub, color = c.t3, fontSize = 12.5.sp)
        }
        trailing()
    }
}
