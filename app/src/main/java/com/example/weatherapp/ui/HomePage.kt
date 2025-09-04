package com.example.weatherapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.R
import com.example.weatherapp.model.MainViewModel
import com.example.weatherapp.ui.components.ForecastItem

@Composable
fun HomePage(viewModel: MainViewModel) {
    Column (
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (viewModel.city == null) {
            Column( modifier = Modifier.fillMaxSize()
                .background(Color.Blue).wrapContentSize(Alignment.Center)
            ) {
                Text(
                    text = "Selecione uma cidade!",
                    fontWeight = FontWeight.Bold, color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center, fontSize = 28.sp
                )
            }
        } else {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = if (viewModel.city?.isMonitored == true)
                    Icons.Filled.Notifications
                else
                    Icons.Outlined.Notifications

                AsyncImage( // Substitui o Icon
                    model = viewModel.city?.weather?.imgUrl,
                    modifier = Modifier.size(56.dp),
                    error = painterResource(id = R.drawable.loading),
                    contentDescription = "Imagem"
                )
                Column (
                    modifier = Modifier.weight(1f)
                ) {
                    Text( text = viewModel.city?.name ?: "Selecione uma cidade...",
                        fontSize = 24.sp )
                    Text( text = viewModel.city?.weather?.desc ?: "...",
                        fontSize = 16.sp )
                    Text( text = "Temp: " + viewModel.city?.weather?.temp + "℃",
                        fontSize = 16.sp )
                }
                IconButton(
                    onClick = {
                        viewModel.update(
                            viewModel.city!!.copy(
                                isMonitored = !viewModel.city!!.isMonitored))
                    }
                ) {
                    Icon(
                        imageVector = icon, contentDescription = "Monitorada?",
                    )
                }
            }
            HorizontalDivider(thickness = 1.dp)
            LaunchedEffect(viewModel.city!!.name) {
                if (viewModel.city!!.forecast == null ||
                    viewModel.city!!.forecast!!.isEmpty()
                ) {
                    viewModel.loadForecast(viewModel.city!!)
                }
            }
            if (viewModel.city?.forecast != null) {
                LazyColumn {
                    items(viewModel.city!!.forecast!!) { forecast ->
                        ForecastItem(forecast, onClick = { })
                    }
                }
            }
        }
    }
}