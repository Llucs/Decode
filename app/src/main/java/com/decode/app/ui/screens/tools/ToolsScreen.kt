package com.decode.app.ui.screens.tools

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decode.app.ui.components.AppTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ToolItem(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val category: String
)

val tools = listOf(
    ToolItem("analyzer", "APK Analyzer", "Inspect APK structure and metadata", Icons.Filled.Visibility, "Analysis"),
    ToolItem("decompiler", "DEX Decompiler", "Decompile DEX to Java with JADX", Icons.Filled.Code, "Analysis"),
    ToolItem("smali", "Smali Assembler", "Assemble/disassemble DEX bytecode", Icons.Filled.SwapHoriz, "Development"),
    ToolItem("signer", "APK Signer", "Sign APKs with v1/v2/v3 schemes", Icons.Filled.Lock, "Signing"),
    ToolItem("rebuilder", "APK Rebuilder", "Rebuild APK from workspace", Icons.Filled.Transform, "Build"),
    ToolItem("resource", "Resource Editor", "Edit Android resources (ARSC)", Icons.Filled.Description, "Editing"),
    ToolItem("optimizer", "Image Optimizer", "Compress and optimize PNG images", Icons.Filled.Compress, "Optimization"),
    ToolItem("axml", "AXML Viewer", "View parsed Android XML", Icons.Filled.Analytics, "Analysis"),
    ToolItem("svg", "SVG Renderer", "Preview SVG resources", Icons.Filled.Image, "Preview"),
    ToolItem("elf", "ELF Analyzer", "Inspect ELF native libraries", Icons.Filled.Analytics, "Analysis"),
)

@Composable
fun ToolsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Tools",
                showBackButton = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Available Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select a tool to analyze or modify APK components",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            val categories = tools.map { it.category }.distinct()
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    item {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(tools.filter { it.category == category }) { tool ->
                        ToolCard(
                            tool = tool,
                            onClick = {
                                scope.launch {
                                    val text = when (tool.id) {
                                        "elf" -> "ELF Analyzer: Load native .so files from your project workspace to inspect ELF headers, sections, segments, and entry points."
                                        "svg" -> "SVG Renderer: Open SVG files from your project workspace to preview vector graphics."
                                        "analyzer" -> "APK Analyzer: Open an APK file to inspect its structure, manifest, permissions, and signatures."
                                        "decompiler" -> "DEX Decompiler: Open DEX files from your project workspace to decompile them to Java using JADX."
                                        "smali" -> "Smali Assembler: Convert between DEX bytecode and Smali assembly format."
                                        "signer" -> "APK Signer: Sign rebuilt APKs with a generated key or your own keystore."
                                        "rebuilder" -> "APK Rebuilder: Rebuild modified APK from your project workspace."
                                        "resource" -> "Resource Editor: Edit Android resources including ARSC files and XML layouts."
                                        "optimizer" -> "Image Optimizer: Compress PNG images to reduce APK size."
                                        "axml" -> "AXML Viewer: Parse and view Android Binary XML files in readable format."
                                        else -> "Tool selected: ${tool.name}"
                                    }
                                    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    tool: ToolItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                tool.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
