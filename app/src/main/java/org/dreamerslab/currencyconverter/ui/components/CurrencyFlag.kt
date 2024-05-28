package org.dreamerslab.currencyconverter.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import org.dreamerslab.currencyconverter.R

@Composable
fun CurrencyFlag(
    flagUrl: String,
    contentDescription: String? = null,
) {
    val painter = when {
        LocalInspectionMode.current -> painterResource(R.drawable.pk)
        else -> rememberAsyncImagePainter(flagUrl)
    }

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = Modifier
            .size(width = 36.dp, height = 24.dp)
            .clip(RoundedCornerShape(4.dp)),
        contentScale = ContentScale.Crop,
    )
}