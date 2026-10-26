package com.astrodir.kvas

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astrodir.kvas.terminal.TerminalState
import com.astrodir.kvas.ui.screens.ChatScreen
import com.astrodir.kvas.ui.screens.ConnectorsPage
import com.astrodir.kvas.ui.screens.ConsolePanel
import com.astrodir.kvas.ui.screens.PlusSheet
import com.astrodir.kvas.ui.screens.SideMenuContent
import com.astrodir.kvas.ui.theme.K
import com.astrodir.kvas.ui.theme.KvasTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("kvas", MODE_PRIVATE)
        val home = filesDir
        setContent { KvasTheme { KvasApp(prefs, home) } }
    }
}

@Composable
fun KvasApp(prefs: SharedPreferences, home: File) {
    val c = K.c
    val state = remember { KvasState(prefs) }
    val term = remember { TerminalState(home) }
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var sheet by remember { mutableStateOf(false) }
    var console by remember { mutableStateOf(false) }
    var connectors by remember { mutableStateOf(false) }
    var shownToast by remember { mutableStateOf("") }

    BackHandler(enabled = drawer.isOpen) { scope.launch { drawer.close() } }
    BackHandler(enabled = console) { console = false }
    BackHandler(enabled = connectors) { connectors = false }

    LaunchedEffect(state.toastTick) {
        state.toastText?.let { shownToast = it }
        if (state.toastText != null) {
            delay(1800)
            state.toastText = null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawer,
        gesturesEnabled = !console && !connectors,
        drawerContent = {
            SideMenuContent(
                state = state,
                onClose = { scope.launch { drawer.close() } },
                onConsole = { scope.launch { drawer.close(); console = true } },
                onSoon = { scope.launch { drawer.close(); state.toast("Этот раздел скоро появится") } },
                onOpenChat = { scope.launch { drawer.close() } },
            )
        },
    ) {
        Box(Modifier.fillMaxSize().background(c.bg)) {
            ChatScreen(
                state = state,
                plusOpen = sheet,
                onMenu = { scope.launch { drawer.open() } },
                onTerm = { console = true },
                onPlus = { sheet = !sheet },
            )
            ConsolePanel(console, term) { console = false }
            ConnectorsPage(connectors) { connectors = false }

            AnimatedVisibility(
                state.toastText != null,
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 92.dp),
                enter = fadeIn(), exit = fadeOut(),
            ) {
                Text(
                    shownToast, color = c.bg, fontSize = 13.5.sp,
                    modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(c.t1).padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }

    if (sheet) {
        PlusSheet(state, onDismiss = { sheet = false }, onConnectors = { connectors = true })
    }
}
