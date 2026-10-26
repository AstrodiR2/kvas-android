package com.astrodir.kvas.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astrodir.kvas.R
import com.astrodir.kvas.ui.components.IconBtn
import com.astrodir.kvas.ui.components.KIcon
import com.astrodir.kvas.ui.components.Tile
import com.astrodir.kvas.ui.components.TileBlue
import com.astrodir.kvas.ui.components.TileGreen
import com.astrodir.kvas.ui.components.TileOrange
import com.astrodir.kvas.ui.components.TilePurple
import com.astrodir.kvas.ui.components.press
import com.astrodir.kvas.ui.theme.K

private class Conn(val icon: Int, val color: Color, val name: String, val sub: String)

@Composable
fun ConnectorsPage(visible: Boolean, onClose: () -> Unit) {
    val c = K.c
    val items = remember {
        listOf(
            Conn(R.drawable.ic_send, TileBlue, "Telegram", "чаты и память"),
            Conn(R.drawable.ic_folder, TileGreen, "Файлы телефона", "папки Termux"),
            Conn(R.drawable.ic_cal, TileOrange, "Календарь", "события и напоминания"),
            Conn(R.drawable.ic_cloud, TilePurple, "Погода", "прогноз по городу"),
        )
    }
    val on = remember { mutableStateListOf(true, false, false, false) }

    AnimatedVisibility(
        visible,
        enter = slideInHorizontally(tween(380)) { it },
        exit = slideOutHorizontally(tween(300)) { it },
    ) {
        Column(
            Modifier.fillMaxSize().background(c.bg)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconBtn(R.drawable.ic_back, onClose)
                Text("Коннекторы", color = c.t1, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp))
            }
            HorizontalDivider(color = c.bd)
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(items) { i, it ->
                    val shape = RoundedCornerShape(16.dp)
                    Row(
                        Modifier.fillMaxWidth().clip(shape).background(c.s1).border(1.dp, c.bd, shape).padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(13.dp),
                    ) {
                        Tile(it.icon, it.color, 42.dp)
                        Column(Modifier.weight(1f)) {
                            Text(it.name, color = c.t1, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                            Text(it.sub, color = c.t3, fontSize = 12.5.sp)
                        }
                        Row(
                            Modifier.press { on[i] = !on[i] }.clip(RoundedCornerShape(999.dp))
                                .background(if (on[i]) c.acSoft else c.s2)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            if (on[i]) KIcon(R.drawable.ic_ok, tint = c.ac, size = 14.dp)
                            Text(if (on[i]) "Подключено" else "Подключить", color = if (on[i]) c.ac else c.t1, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
