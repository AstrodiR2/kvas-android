package com.astrodir.kvas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astrodir.kvas.KvasState
import com.astrodir.kvas.R
import com.astrodir.kvas.ui.components.IconBtn
import com.astrodir.kvas.ui.components.KIcon
import com.astrodir.kvas.ui.components.LOGOS
import com.astrodir.kvas.ui.components.press
import com.astrodir.kvas.ui.theme.K
import com.astrodir.kvas.ui.theme.KvasGrad

@Composable
fun SideMenuContent(
    state: KvasState,
    onClose: () -> Unit,
    onConsole: () -> Unit,
    onSoon: () -> Unit,
    onOpenChat: () -> Unit,
) {
    val c = K.c
    val w = (LocalConfiguration.current.screenWidthDp * 0.84f).dp.coerceAtMost(330.dp)
    ModalDrawerSheet(
        modifier = Modifier.width(w),
        drawerContainerColor = c.s1,
        drawerContentColor = c.t1,
    ) {
        Column(Modifier.fillMaxSize()) {
            // шапка
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(Modifier.press { state.cycleLogo() }, verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(30.dp).clip(CircleShape).background(KvasGrad), contentAlignment = Alignment.Center) {
                        KIcon(LOGOS[state.logo], tint = c.onAc, size = 16.dp)
                    }
                    Text("Квас", color = c.t1, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp))
                }
                IconBtn(R.drawable.ic_x, onClose)
            }
            // поиск (декоративный, как в макете)
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 6.dp).fillMaxWidth().height(40.dp)
                    .clip(RoundedCornerShape(12.dp)).background(c.s2).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KIcon(R.drawable.ic_search, tint = c.t3)
                Text("Поиск по чатам", color = c.t3, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
            }
            // навигация
            Column(Modifier.padding(horizontal = 8.dp)) {
                NavItem(R.drawable.ic_chat, "Чаты", active = true, onClick = onClose)
                NavItem(R.drawable.ic_folder, "Проекты", onClick = onSoon)
                NavItem(R.drawable.ic_term, "Консоль", onClick = onConsole)
                NavItem(R.drawable.ic_set, "Настройки", onClick = onSoon)
            }
            Text(
                "НЕДАВНИЕ", color = c.t3, fontSize = 11.5.sp, letterSpacing = 0.7.sp,
                modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 6.dp),
            )
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
                state.chats.forEach { ch ->
                    val last = ch.msgs.lastOrNull()?.text ?: "пока пусто"
                    Column(
                        Modifier.fillMaxWidth().press { state.current = ch; onOpenChat() }
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (ch === state.current) c.s2 else c.s1)
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                    ) {
                        Text(ch.name, color = c.t1, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(last, color = c.t3, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            // низ
            HorizontalDivider(color = c.bd)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(34.dp).clip(CircleShape).background(KvasGrad), contentAlignment = Alignment.Center) {
                        Text((state.userName.ifBlank { "Р" }).take(1).uppercase(), color = c.onAc, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(state.userName.ifBlank { "Разработчик" }, color = c.t1, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 10.dp))
                }
                Box(
                    Modifier.press { state.newChat(); onOpenChat() }.size(44.dp).clip(CircleShape).background(KvasGrad),
                    contentAlignment = Alignment.Center,
                ) { KIcon(R.drawable.ic_pen, tint = c.onAc) }
            }
        }
    }
}

@Composable
private fun NavItem(icon: Int, label: String, active: Boolean = false, onClick: () -> Unit) {
    val c = K.c
    Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().press(onClick).clip(RoundedCornerShape(12.dp))
                .background(if (active) c.acSoft else c.s1)
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KIcon(icon, tint = if (active) c.ac else c.t2)
            Text(
                label, color = if (active) c.t1 else c.t2, fontSize = 15.sp,
                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.padding(start = 13.dp),
            )
        }
        if (active) {
            Box(Modifier.align(Alignment.CenterStart).padding(vertical = 10.dp).width(3.dp).fillMaxHeight().background(c.ac, RoundedCornerShape(3.dp)))
        }
    }
}
