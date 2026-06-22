package com.decode.app.ui.screens.tools

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decode.app.ui.components.AppTopBar

data class ToolItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val category: String
)

val tools = listOf(
    ToolItem("APK Analyzer", "Inspect APK structure and metadata", Icons.Filled.Visibility, "Analysis"),
    ToolItem("DEX Decompiler", "Decompile DEX to Java with JADX", Icons.Filled.Code, "Analysis"),
    ToolItem("Smali Assembler", "Assemble/disassemble DEX bytecode", Icons.Filled.SwapHoriz, "Development"),
    ToolItem("APK Signer", "Sign APKs with v1/v2/v3 schemes", Icons.Filled.Lock, "Signing"),
    ToolItem("APK Rebuilder", "Rebuild APK from workspace", Icons.Filled.Transform, "Build"),
    ToolItem("Resource Editor", "Edit Android resources (ARSC)", Icons.Filled.Description, "Editing"),
    ToolItem("Image Optimizer", "Compress and optimize PNG images", Icons.Filled.Compress, "Optimization"),
    ToolItem("AXML Viewer", "View parsed Android XML", Icons.Filled.Analytics, "Analysis"),
    ToolItem("SVG Renderer", "Preview SVG resources", Icons.Filled.Image, "Preview"),
    ToolItem("ELF Analyzer", "Inspect ELF native libraries", Icons.Filled.Analytics, "Analysis"),
)

@Composable
fun ToolsScreen(
    onNavigateBack: () -> Unit
) {
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
                        ToolCard(tool)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCard(tool: ToolItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
