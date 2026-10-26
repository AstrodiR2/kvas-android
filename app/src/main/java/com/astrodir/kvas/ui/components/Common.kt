package com.astrodir.kvas.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.astrodir.kvas.ui.theme.K

/** Аналог .press из CSS: лёгкое сжатие при нажатии. Ставить первым в цепочке. */
@Composable
fun Modifier.press(onClick: () -> Unit): Modifier {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val s by animateFloatAsState(if (pressed) 0.94f else 1f, label = "press")
    return this
        .graphicsLayer { scaleX = s; scaleY = s }
        .clickable(interactionSource = src, indication = null, onClick = onClick)
}

@Composable
fun KIcon(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    tint: Color = K.c.t1,
    size: Dp = 20.dp,
) {
    Icon(painterResource(id), contentDescription = null, modifier = modifier.size(size), tint = tint)
}

@Composable
fun IconBtn(@DrawableRes id: Int, onClick: () -> Unit) {
    Box(
        Modifier.press(onClick).size(40.dp).clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) { KIcon(id) }
}

@Composable
fun Tile(@DrawableRes id: Int, color: Color, size: Dp = 40.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) { KIcon(id, tint = color) }
}

@Composable
fun KToggle(on: Boolean) {
    val c = K.c
    val bg by animateColorAsState(if (on) c.ac else c.s3, label = "tgBg")
    val x by animateDpAsState(if (on) 23.dp else 3.dp, spring(dampingRatio = 0.6f), label = "tgX")
    Box(Modifier.size(48.dp, 28.dp).clip(RoundedCornerShape(14.dp)).background(bg)) {
        Box(
            Modifier.offset(x = x, y = 3.dp).size(22.dp)
                .shadow(2.dp, CircleShape).background(Color.White, CircleShape),
        )
    }
}

val TileBlue = Color(0xFF4299E1)
val TileGreen = Color(0xFF3FB36F)
val TilePurple = Color(0xFFA06EE6)
val TileOrange = Color(0xFFE8913A)
val TilePink = Color(0xFFE65A78)
