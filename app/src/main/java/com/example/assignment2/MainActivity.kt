package com.example.assignment2

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.assignment2.ui.theme.Assignment2Theme
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Assignment2Theme {
                CameraApp()
            }
        }
    }
}

@Composable
fun CameraApp() {

    val context = LocalContext.current

    var capturedPhotoPath by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var pendingPhotoPath by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    // Launch the Android system camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->

        if (success) {
            capturedPhotoPath = pendingPhotoPath
        } else {
            pendingPhotoPath?.let { File(it).delete() }
        }
    }

    // Main application interface
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Simple Camera App",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Display captured photograph
            val bitmap = remember(capturedPhotoPath) {
                capturedPhotoPath?.let { path ->
                    BitmapFactory.decodeFile(path)?.asImageBitmap()
                }
            }

            if (bitmap != null) {

                Image(
                    bitmap = bitmap,
                    contentDescription = "Captured Photograph",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    contentScale = ContentScale.Fit
                )

            } else {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No photograph captured yet")
                    }
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Open Camera button
            Button(
                onClick = {

                    val directory = context.getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES
                    )

                    if (directory != null) {

                        val photoFile = File.createTempFile(
                            "IMG_",
                            ".jpg",
                            directory
                        )

                        pendingPhotoPath = photoFile.absolutePath

                        val photoUri: Uri =
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )

                        cameraLauncher.launch(photoUri)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Camera")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Share photograph button
            Button(
                onClick = {

                    capturedPhotoPath?.let { path ->

                        val photoFile = File(path)

                        if (photoFile.exists()) {

                            val photoUri =
                                FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )

                            val shareIntent = Intent(
                                Intent.ACTION_SEND
                            ).apply {

                                type = "image/jpeg"

                                putExtra(
                                    Intent.EXTRA_STREAM,
                                    photoUri
                                )

                                clipData = android.content.ClipData.newRawUri(
                                    "Captured Photo",
                                    photoUri
                                )

                                addFlags(
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            }

                            val chooserIntent = Intent.createChooser(
                                shareIntent,
                                "Share Photograph"
                            ).apply {
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            context.startActivity(chooserIntent)
                        }
                    }
                },
                enabled = capturedPhotoPath != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Share Photo")
            }
        }
    }
}
