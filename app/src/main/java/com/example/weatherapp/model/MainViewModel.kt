package com.example.weatherapp.model

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.api.WeatherService
import com.example.weatherapp.api.toForecast
import com.example.weatherapp.api.toWeather
import com.example.weatherapp.monitor.ForecastMonitor
import com.example.weatherapp.repo.Repository
import com.example.weatherapp.ui.nav.Route
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel (
    private val repository: Repository,
    private val service: WeatherService,
    private val monitor: ForecastMonitor): ViewModel() {

    private val _cities = mutableStateMapOf<String, City>()
    private var _city = mutableStateOf<City?>(null)
    private var _page = mutableStateOf<Route>(Route.Home)
    private val _user = mutableStateOf<User?> (null)

    init {
        viewModelScope.launch (Dispatchers.Main) {
            repository.user.collect { user ->
                _user.value = user.copy()
            }
        }
        viewModelScope.launch (Dispatchers.Main) {
            repository.cities.collect { list ->
                val names = list.map { it.name }
                val newCities = list.filter { it.name !in _cities.keys }
                val oldCities = list.filter { it.name in _cities.keys }
                _cities.keys.removeIf { it !in names } // remove cidades deletadas
                newCities.forEach { _cities[it.name] = it } // adiciona cidades novas
                oldCities.forEach { refresh(it) }
            }
        }
    }

    val cities : List<City>
        get() = _cities.values.toList()

    var city: City?
        get() = _city.value
        set(tmp) { _city.value = tmp?.copy() }

    var page: Route
        get() = _page.value
        set(tmp) { _page.value = tmp }

    val user : User?
        get() = _user.value

    fun remove(city: City) {
        repository.remove(city)
    }
    fun add(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val location = service.getLocation(name)?:return@launch
        repository.add(City( name = name, location = location))
    }
    fun add(location: LatLng) = viewModelScope.launch(Dispatchers.IO) {
        val name = service.getName(location.latitude, location.longitude)?:return@launch
        repository.add(City(name = name, location = location))
    }

    fun loadWeather(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val weather = service.getWeather(name)?.toWeather()
        _cities[name]?.let { refresh(it.copy(weather = weather)) }
    }
    fun loadForecast(city: City) = viewModelScope.launch(Dispatchers.IO) {
        val forecast = service.getForecast(city.name)?.toForecast()
        _cities[city.name]?.let { refresh(it.copy(forecast = forecast)) }
    }
    fun loadBitmap(city: City) = viewModelScope.launch(Dispatchers.IO) {
        val bitmap = service.getBitmap(city.weather!!.imgUrl)?:return@launch
        _cities[city.name]?.let { refresh(it.copy(
            weather =  city.weather?.copy(bitmap = bitmap)
        )) }
    }

    suspend fun refresh(city: City) = withContext(Dispatchers.Main) {
        val oldCity = _cities[city.name]
        _cities.remove(city.name)
        _cities[city.name] = city.copy(
            weather = city.weather?:oldCity?.weather, // se novo = null, reusa antigo
            forecast = city.forecast?:oldCity?.forecast, // se novo = null, reusa antigo
        )
        if (_city.value?.name == city.name) {
            _city.value = _cities[city.name]
        }
        monitor.updateCity(_cities[city.name]!!)
    }

    fun update(city: City) {
        this.repository.update(city)
    }
}

class MainViewModelFactory(private val repository: Repository, private val service: WeatherService, private val monitor: ForecastMonitor) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, service, monitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}