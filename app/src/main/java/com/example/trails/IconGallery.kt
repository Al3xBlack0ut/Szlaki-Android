package com.example.trails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class IconItem(val name: String, val icon: ImageVector)

@Preview(showBackground = true)
@Composable
fun IconGalleryPreview() {
    val icons = listOf(
        IconItem("DirectionsWalk", Icons.AutoMirrored.Filled.DirectionsWalk),
        IconItem("Hiking", Icons.Default.Hiking),
        IconItem("DirectionsRun", Icons.AutoMirrored.Filled.DirectionsRun),
        IconItem("TransferWithinAStation", Icons.Default.TransferWithinAStation),
        IconItem("AccessibilityNew", Icons.Default.AccessibilityNew),
        IconItem("Accessibility", Icons.Default.Accessibility),
        IconItem("EmojiPeople", Icons.Default.EmojiPeople),
        IconItem("Elderly", Icons.Default.Elderly),
        IconItem("SelfImprovement", Icons.Default.SelfImprovement),
        IconItem("Person", Icons.Default.Person),
        IconItem("DirectionsBike", Icons.AutoMirrored.Filled.DirectionsBike),
        IconItem("Man", Icons.Default.Man),
        IconItem("Woman", Icons.Default.Woman),
        IconItem("DirectionsBus", Icons.Default.DirectionsBus)
    )

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(icons) { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = item.name, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
