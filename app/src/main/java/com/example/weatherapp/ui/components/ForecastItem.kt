package com.example.weatherapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.R
import com.example.weatherapp.model.Forecast
import java.text.DecimalFormat

@Composable
fun ForecastItem(
    forecast: Forecast,
    onClick: (Forecast) -> Unit,
    modifier: Modifier = Modifier
) {
    val format = DecimalFormat("#.0")
    val tempMin = format.format(forecast.tempMin)
    val tempMax = format.format(forecast.tempMax)
    Row (
        modifier = modifier.fillMaxWidth().padding(12.dp)
            .clickable( onClick = { onClick(forecast) }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = forecast.imgUrl,
            modifier = Modifier.size(32.dp),
            error = painterResource(id = R.drawable.loading),
            contentDescription = "Imagem"
        )
        Column {
            Text(modifier = Modifier, text = forecast.weather, fontSize = 20.sp)
            Row (
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(modifier = Modifier, text = forecast.date, fontSize = 16.sp)
                Text(modifier = Modifier, text = "Min: $tempMin℃", fontSize = 12.sp)
                Text(modifier = Modifier, text = "Max: $tempMax℃", fontSize = 12.sp)
            }
        }
    }
}