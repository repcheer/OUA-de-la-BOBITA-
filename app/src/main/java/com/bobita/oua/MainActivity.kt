package com.bobita.oua

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BobitaApp() }
    }
}

@Composable
fun BobitaApp() {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2200)
        showSplash = false
    }
    if (showSplash) {
        SplashBobita()
    } else {
        val items = listOf(BottomDest.Shop, BottomDest.Orders, BottomDest.Cart)
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val route = navBackStackEntry?.destination?.route
                    items.forEach { dest ->
                        NavigationBarItem(
                            selected = route == dest.route,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        ) { padding ->
            var one by rememberSaveable { mutableStateOf(0) }
            var two by rememberSaveable { mutableStateOf(0) }
            var c10 by rememberSaveable { mutableStateOf(0) }
            var c30 by rememberSaveable { mutableStateOf(0) }
            val orders = remember { mutableStateListOf<String>() }
            var targetPhone by rememberSaveable { mutableStateOf("07xxxxxxxx") }

            NavHost(
                navController,
                startDestination = BottomDest.Shop.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(BottomDest.Shop.route) {
                    ShopScreen(
                        one = one, onOne = { one = it },
                        two = two, onTwo = { two = it },
                        c10 = c10, onC10 = { c10 = it },
                        c30 = c30, onC30 = { c30 = it },
                        onAddToCart = {
                            navController.navigate("processing")
                        }
                    )
                }
                composable("processing") {
                    ProcessingScreen(
                        onDone = {
                            val summary = cartSummary(one, two, c10, c30)
                            if (summary.isNotBlank()) {
                                orders.add("Comanda se procesează… Găinile sunt motivate! ➜ $summary")
                            }
                            navController.navigate(BottomDest.Orders.route) {
                                popUpTo(BottomDest.Shop.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(BottomDest.Orders.route) {
                    OrdersScreen(orders = orders)
                }
                composable(BottomDest.Cart.route) {
                    CartScreen(
                        one, two, c10, c30,
                        targetPhone = targetPhone,
                        onPhoneChange = { targetPhone = it },
                        onCheckout = { ctx ->
                            val body = "Comanda ouă Bobiță: " + cartSummary(one, two, c10, c30)
                            if (body.isNotBlank()) {
                                openSms(ctx, targetPhone, body)
                            }
                        },
                        onClear = { one = 0; two = 0; c10 = 0; c30 = 0 }
                    )
                }
            }
        }
    }
}

sealed class BottomDest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Shop : BottomDest("shop", "Magazin", Icons.Default.Home)
    data object Orders : BottomDest("orders", "Comenzi", Icons.Default.List)
    data object Cart : BottomDest("cart", "Coș", Icons.Default.ShoppingCart)
}

@Composable
fun SplashBobita() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9C4)),
        contentAlignment = Alignment.Center
    ) {
        val infinite = rememberInfiniteTransition(label = "scooter")
        val x by infinite.animateFloat(
            initialValue = -220f, targetValue = 220f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Restart
            ), label = "x"
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Ouole lui Bobiță!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().height(80.dp), verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .offset(x = x.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("🛵💨  🥚🥚🥚", fontSize = 32.sp)
                }
                Spacer(Modifier.weight(1f))
            }
            Text("La Bobiță, la ouă!", fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun ShopScreen(
    one: Int, onOne: (Int) -> Unit,
    two: Int, onTwo: (Int) -> Unit,
    c10: Int, onC10: (Int) -> Unit,
    c30: Int, onC30: (Int) -> Unit,
    onAddToCart: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Alege cantitățile:", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        QuantityRow("1 ou", one, onOne)
        QuantityRow("2 ouă", two, onTwo)
        QuantityRow("Carton 10 ouă", c10, onC10)
        QuantityRow("Carton 30 ouă", c30, onC30)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAddToCart, modifier = Modifier.fillMaxWidth()) {
            Text("Adaugă în coș")
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Glumă: livrare în 5 minute dacă găinile cooperează!",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun QuantityRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), fontSize = 18.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { onChange((value - 1).coerceAtLeast(0)) }) { Text("-") }
                Text(value.toString(), modifier = Modifier.padding(horizontal = 12.dp), fontSize = 18.sp)
                Button(onClick = { onChange(value + 1) }) { Text("+") }
            }
        }
    }
}

@Composable
fun ProcessingScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2200)
        onDone()
    }
    val infinite = rememberInfiniteTransition(label = "proc")
    val scale by infinite.animateFloat(0.9f, 1.1f, animationSpec = infiniteRepeatable(
        animation = tween(400, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse
    ), label = "scale")
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Comanda se procesează…", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFE082))
        ) {
            Text(
                "👨‍🌾 🔨  🐔🐔",
                fontSize = 36.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("Bobiță zice: azi ouăm record! 🥚", color = Color.Gray)
    }
}

@Composable
fun OrdersScreen(orders: List<String>) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Status comenzi", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        if (orders.isEmpty()) {
            Text("Nicio comandă încă. Plasează una din ecranul Magazin.")
        } else {
            orders.forEach { o ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(o, Modifier.padding(12.dp))
                }
            }
        }
    }
}

@Composable
fun CartScreen(
    one: Int, two: Int, c10: Int, c30: Int,
    targetPhone: String,
    onPhoneChange: (String) -> Unit,
    onCheckout: (android.content.Context) -> Unit,
    onClear: () -> Unit
) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Coș", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        val summary = cartSummary(one, two, c10, c30)
        Text(if (summary.isBlank()) "Coșul e gol" else summary)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = targetPhone,
            onValueChange = onPhoneChange,
            label = { Text("Număr telefon pentru SMS") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Row {
            Button(
                enabled = summary.isNotBlank(),
                onClick = { onCheckout(ctx) }
            ) { Text("Trimite SMS") }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(onClick = onClear) { Text("Golește coșul") }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Notă: se deschide aplicația SMS cu textul precompletat. Android cere acțiunea utilizatorului pentru trimitere.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

fun cartSummary(one: Int, two: Int, c10: Int, c30: Int): String {
    val parts = mutableListOf<String>()
    if (one > 0) parts += "${one} x 1 ou"
    if (two > 0) parts += "${two} x 2 ouă"
    if (c10 > 0) parts += "${c10} x carton 10"
    if (c30 > 0) parts += "${c30} x carton 30"
    return parts.joinToString(", ")
}

fun openSms(ctx: android.content.Context, phone: String, body: String) {
    val uri = Uri.parse("smsto:${phone}")
    val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
        putExtra("sms_body", body)
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    ctx.startActivity(intent)
}