package com.example.diceroller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.diceroller.ui.theme.DiceRollerTheme
import kotlinx.coroutines.launch

const val CHANNEL_ID = "dice_channel"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FEATURE 4 (setup): create the notification channel
        createNotificationChannel(this)

        // FEATURE 4 (setup): ask permission to show notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100
            )
        }

        setContent {
            DiceRollerTheme {
                DiceRollerApp()
            }
        }
    }
}

@Preview
@Composable
fun DiceRollerApp() {
    // FEATURE 2 (setup): the Scaffold holds the Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        DiceWithButtonAndImage(
            snackbarHostState = snackbarHostState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .wrapContentSize(Alignment.Center)
        )
    }
}

@Composable
fun DiceWithButtonAndImage(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var result by remember { mutableStateOf(1) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val imageResource = when (result) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }

    fun rollDice() {
        result = (1..6).random()

        // FEATURE 1: Toast message about the rolled number
        val toastMessage = when (result) {
            1 -> "🎲 You rolled a 1. A slow start, but keep rolling!"
            2 -> "🎲 You rolled a 2. Not your luckiest roll!"
            3 -> "🎲 You rolled a 3. Right in the middle!"
            4 -> "🎲 You rolled a 4. Nice roll!"
            5 -> "🎲 You rolled a 5. So close to the top!"
            else -> "🎲 You rolled a 6. The highest number!"
        }
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()

        when (result) {
            // FEATURE 2: Snackbar with UNDO when the dice rolls 2
            2 -> scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val snackResult = snackbarHostState.showSnackbar(
                    message = "Oops! You rolled a 2. Tap UNDO to roll again.",
                    actionLabel = "UNDO",
                    duration = SnackbarDuration.Long
                )
                if (snackResult == SnackbarResult.ActionPerformed) {
                    rollDice() // Undo = reroll automatically
                }
            }

            // FEATURE 3: Dialog box when the dice rolls 4
            4 -> showDialog = true

            // FEATURE 4: Notification when the dice rolls 6
            6 -> showSixNotification(context)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(imageResource),
            contentDescription = result.toString()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { rollDice() }) {
            Text(stringResource(R.string.roll))
        }
    }

    // FEATURE 3: the dialog with Continue, Exit and Reroll
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("You rolled a 4! 🎲") },
            text = { Text("That's a good roll! What would you like to do next?") },
            confirmButton = {
                Row {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Continue")
                    }
                    TextButton(onClick = { (context as? Activity)?.finishAffinity() }) {
                        Text("Exit")
                    }
                    TextButton(onClick = {
                        showDialog = false
                        rollDice() // Reroll automatically
                    }) {
                        Text("Reroll")
                    }
                }
            }
        )
    }
}

// FEATURE 4: notification channel (needed on Android 8+)
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Dice Roller Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Tells you when you roll a 6"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

// FEATURE 4: the "Congrats! you rolled 6!" notification
@SuppressLint("MissingPermission")
fun showSixNotification(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(context, "Please allow notifications to see your prize!", Toast.LENGTH_SHORT).show()
        return
    }

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.dice_6)
        .setContentTitle("🎉 Lucky Roll!")
        .setContentText("Congrats! you rolled 6!")
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText("Congrats! you rolled 6! That's the highest number on the dice. Keep the lucky streak going!")
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(6, notification)
}
