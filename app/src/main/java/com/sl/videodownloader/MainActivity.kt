package com.sl.videodownloader

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import org.json.JSONObject
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.sl.videodownloader.data.database.AppDatabase
import com.sl.videodownloader.data.database.DownloadEntity
import com.sl.videodownloader.data.database.DownloadStatus
import com.sl.videodownloader.data.repository.DownloadRepository
import com.sl.videodownloader.domain.HomeViewModel
import com.sl.videodownloader.presentation.theme.SLVideoDownloaderTheme
import com.sl.videodownloader.presentation.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.P &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST)
        }
        val database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "sl-video-downloader.db").build()
        val repository = DownloadRepository(applicationContext, database.downloadDao())
        val sharedText = if (intent?.action == Intent.ACTION_SEND) intent.getStringExtra(Intent.EXTRA_TEXT) else null
        setContent {
            val factory = remember { HomeViewModelFactory(repository) }
            val homeViewModel: HomeViewModel = viewModel(factory = factory)
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.cacheMode = WebSettings.LOAD_NO_CACHE
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        webViewClient = WebViewClient()
                        webChromeClient = WebChromeClient()
                        addJavascriptInterface(DownloadBridge(homeViewModel, context), "SLAndroid")
                        loadUrl("file:///android_asset/index.html")
                    }
                },
                update = { webView ->
                    if (!sharedText.isNullOrBlank()) {
                        val safeText = JSONObject.quote(sharedText)
                        webView.evaluateJavascript("window.setSharedUrl($safeText)", null)
                    }
                }
            )
        }
    }

    override fun onBackPressed() {
        val webView = findViewById<WebView>(android.R.id.content)
        if (webView?.canGoBack() == true) webView.goBack() else super.onBackPressed()
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST = 7001
    }
}

private class DownloadBridge(
    private val viewModel: HomeViewModel,
    private val context: android.content.Context
) {
    @JavascriptInterface
    fun paste(): String = context.getSystemService(android.content.ClipboardManager::class.java)
        ?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()

    @JavascriptInterface
    fun copy(value: String) {
        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText("Video URL", value))
    }

    @JavascriptInterface
    fun share(value: String) {
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, value)
        }, "Share video URL"))
    }

    @JavascriptInterface
    fun download(url: String) {
        viewModel.setUrl(url)
        viewModel.addDownload()
    }

    @JavascriptInterface
    fun openGithub() {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/surjolive")))
    }

    @JavascriptInterface
    fun openRepositories() {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/surjolive?tab=repositories")))
    }
}

/*
 * The Compose implementation remains below as a migration fallback while the
 * bundled WebView surface is used by the main activity.
 */

private class HomeViewModelFactory(private val repository: DownloadRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repository) as T
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SLVideoDownloaderApp(viewModel: HomeViewModel, sharedText: String?, themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val url by viewModel.url.collectAsState()
    val message by viewModel.message.collectAsState()
    val downloads by viewModel.items.collectAsState()
    val clipboard = LocalClipboardManager.current
    if (!sharedText.isNullOrBlank() && url.isBlank()) viewModel.setUrl(sharedText)
    Scaffold(
        topBar = { TopAppBar(title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark()
                Spacer(Modifier.size(10.dp))
                Text("SL Downloader", fontWeight = FontWeight.Bold)
            }
        }) },
        bottomBar = {
            NavigationBar {
                val tabs = listOf(Icons.Default.Home to "Home", Icons.Default.Download to "Downloads", Icons.Default.History to "History", Icons.Default.Settings to "Settings")
                tabs.forEachIndexed { index, pair ->
                    NavigationBarItem(selected = selectedTab == index, onClick = { selectedTab = index }, icon = { Icon(pair.first, pair.second) }, label = { Text(pair.second) })
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeScreen(Modifier.padding(padding), url, message, downloads, viewModel, clipboard)
            1, 2 -> DownloadsScreen(Modifier.padding(padding), downloads, selectedTab == 2)
            else -> SettingsScreen(Modifier.padding(padding), themeMode, onThemeChange)
        }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier, url: String, message: String?, downloads: List<DownloadEntity>, viewModel: HomeViewModel, clipboard: androidx.compose.ui.platform.ClipboardManager) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp).animateContentSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnimatedVisibility(visible = true, enter = fadeIn() + scaleIn()) {
            Text("SL DOWNLOADER", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Text("Download your authorized videos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Direct MP4 and WebM files only. Use downloads you own or have permission to save.", color = MaterialTheme.colorScheme.secondary)
        OutlinedTextField(value = url, onValueChange = viewModel::setUrl, modifier = Modifier.fillMaxWidth(), label = { Text("Video URL") }, singleLine = true, trailingIcon = {
            Row {
                IconButton(onClick = { clipboard.getText()?.text?.let(viewModel::setUrl) }) { Icon(Icons.Default.ContentPaste, "Paste URL") }
                IconButton(onClick = viewModel::clearUrl) { Icon(Icons.Default.Clear, "Clear URL") }
            }
        })
        AnimatedVisibility(visible = message != null, enter = fadeIn(), exit = fadeOut()) {
            Text(message.orEmpty(), color = MaterialTheme.colorScheme.error)
        }
        Button(onClick = viewModel::addDownload, modifier = Modifier.fillMaxWidth().height(52.dp)) { Icon(Icons.Default.Download, null); Spacer(Modifier.size(8.dp)); Text("Analyze and download") }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Active", downloads.count { it.status == DownloadStatus.DOWNLOADING }.toString(), Modifier.weight(1f))
            StatCard("Completed", downloads.count { it.status == DownloadStatus.COMPLETED }.toString(), Modifier.weight(1f))
        }
        Text("Recent downloads", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        downloads.take(3).forEach { DownloadRow(it) }
    }
}

@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier.size(34.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("SL", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(modifier) { Column(Modifier.padding(16.dp)) { Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text(label, color = Color(0xFF52615E)) } }
}

@Composable
private fun DownloadsScreen(modifier: Modifier, downloads: List<DownloadEntity>, history: Boolean) {
    var query by rememberSaveable { mutableStateOf("") }
    val filteredDownloads = downloads.filter { it.fileName.contains(query, ignoreCase = true) }
    Column(modifier.fillMaxSize().padding(20.dp).animateContentSize()) {
        Text(if (history) "History" else "Downloads", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search videos") },
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Clear, "Clear search") } }
        )
        Spacer(Modifier.height(16.dp))
        AnimatedVisibility(visible = filteredDownloads.isEmpty(), enter = fadeIn() + scaleIn()) {
            Column(Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.VideoLibrary, "No videos", Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text(if (query.isEmpty()) "No videos found." else "No matching videos.", color = MaterialTheme.colorScheme.secondary)
            }
        }
        AnimatedVisibility(visible = filteredDownloads.isNotEmpty(), enter = fadeIn()) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { items(filteredDownloads, key = { it.id }) { DownloadRow(it) } }
        }
    }
}

@Composable
private fun DownloadRow(item: DownloadEntity) {
    val progress = if (item.totalBytes > 0) (item.downloadedBytes.toFloat() / item.totalBytes).coerceIn(0f, 1f) else 0f
    Card(Modifier.fillMaxWidth().animateContentSize()) { Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.VideoLibrary, "Video", Modifier.size(36.dp), tint = Color(0xFF0B6E69))
        Column(Modifier.padding(start = 12.dp).weight(1f)) { Text(item.fileName, fontWeight = FontWeight.SemiBold); Text(item.status.name.lowercase().replace('_', ' '), color = Color(0xFF52615E)) }
        if (item.totalBytes > 0) Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold)
        }
        if (item.status == DownloadStatus.DOWNLOADING || item.totalBytes > 0) {
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
    } }
}

@Composable
private fun SettingsScreen(modifier: Modifier, themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    val context = LocalContext.current
    var dialog by rememberSaveable { mutableStateOf<String?>(null) }
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Appearance", style = MaterialTheme.typography.titleLarge)
        Text("Theme", color = MaterialTheme.colorScheme.secondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ThemeButton("System", Icons.Default.Brightness6, ThemeMode.SYSTEM, themeMode, onThemeChange, Modifier.weight(1f))
            ThemeButton("Light", Icons.Default.LightMode, ThemeMode.LIGHT, themeMode, onThemeChange, Modifier.weight(1f))
            ThemeButton("Dark", Icons.Default.DarkMode, ThemeMode.DARK, themeMode, onThemeChange, Modifier.weight(1f))
        }
        Text("Download", style = MaterialTheme.typography.titleLarge)
        Text("Files are saved to Movies / SL Downloader")
        Text("Only authorized direct media URLs are supported.")
        Text("About", style = MaterialTheme.typography.titleLarge)
        Text("SL Downloader 1.0\nBuilt with Kotlin, Jetpack Compose, Material 3 and Media3.")
        Text("Support", style = MaterialTheme.typography.titleLarge)
        SupportLink("GitHub profile", "https://github.com/surjolive", Icons.Default.Code) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/surjolive")))
        }
        SupportLink("My repositories", "github.com/surjolive?tab=repositories", Icons.Default.Code) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/surjolive?tab=repositories")))
        }
        SupportLink("Report a problem", "Open GitHub to report an issue", Icons.Default.Code) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/surjolive?tab=repositories")))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { dialog = "privacy" }) { Text("Privacy") }
            TextButton(onClick = { dialog = "licenses" }) { Text("Licenses") }
        }
    }
    when (dialog) {
        "privacy" -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("Privacy") },
            text = { Text("SL Downloader works locally. Downloaded files, URLs, and history stay on your device. The app does not collect analytics, cookies, tokens, or personal information.") },
            confirmButton = { TextButton(onClick = { dialog = null }) { Text("Close") } }
        )
        "licenses" -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("Open-source licenses") },
            text = { Text("This app uses Kotlin, Jetpack Compose, Material 3, Room, AndroidX Media3, and AndroidX libraries. Their licenses are available from their respective open-source projects on Android Developers and GitHub.") },
            confirmButton = { TextButton(onClick = { dialog = null }) { Text("Close") } }
        )
    }
}

@Composable
private fun SupportLink(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, title, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.padding(start = 12.dp)) {
                Text(title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ThemeButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, mode: ThemeMode, selected: ThemeMode, onSelect: (ThemeMode) -> Unit, modifier: Modifier) {
    Button(
        onClick = { onSelect(mode) },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (mode == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (mode == selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(icon, label, Modifier.size(18.dp))
        Spacer(Modifier.size(4.dp))
        Text(label)
    }
}
