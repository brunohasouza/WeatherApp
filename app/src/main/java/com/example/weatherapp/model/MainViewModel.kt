package com.example.weatherapp.model

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.api.WeatherService
import com.example.weatherapp.api.toForecast
import com.example.weatherapp.api.toWeather
import com.example.weatherapp.db.fb.FBCity
import com.example.weatherapp.db.fb.FBDatabase
import com.example.weatherapp.db.fb.FBUser
import com.example.weatherapp.db.fb.toFBCity
import com.example.weatherapp.monitor.ForecastMonitor
import com.example.weatherapp.repo.Repository
import com.example.weatherapp.ui.nav.Route
import com.google.android.gms.maps.model.LatLng

class MainViewModel (private val repository: Repository, private val service: WeatherService, private val monitor: ForecastMonitor): ViewModel(),
    Repository.Listener {
    private val _cities = mutableStateMapOf<String, City>()
    private var _city = mutableStateOf<City?>(null)
    private var _page = mutableStateOf<Route>(Route.Home)
    private val _user = mutableStateOf<User?> (null)

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

    init {
        repository.setListener(this)
    }

    fun remove(city: City) {
        repository.remove(city)
    }

    fun add(name: String) {
        service.getLocation(name) { lat, lng ->
            if (lat != null && lng != null) {
                repository.add(City(name = name, location = LatLng(lat, lng)))
            }
        }
    }

    fun add(location: LatLng) {
        service.getName(location.latitude, location.longitude) { name ->
            if (name != null) {
                repository.add(City(name = name, location = location))
            }
        }
    }

    override fun onUserLoaded(user: User) {
        _user.value = user
    }

    override fun onUserSignOut() {
        monitor.cancelAll()
    }

    override fun onCityAdded(city: City) {
        _cities[city.name!!] = city
        monitor.updateCity(city)
    }

    override fun onCityUpdated(city: City) {
        val oldCity = _cities[city.name]
        _cities.remove(city.name)
        _cities[city.name!!] = city.copy(
            weather = oldCity?.weather,
            forecast = oldCity?.forecast
        )
        if (_city.value?.name == city.name) {
            _city.value = _cities[city.name]
        }
        monitor.updateCity(city)
    }

    override fun onCityRemoved(city: City) {
        _cities.remove(city.name)
        monitor.cancelCity(city)
        if (_city.value?.name == city.name) { _city.value = null }
    }

    fun loadWeather(name: String) {
        service.getWeather(name) { apiWeather ->
            val newCity = _cities[name]!!.copy( weather = apiWeather?.toWeather() )
            _cities.remove(name)
            _cities[name] = newCity
        }
    }

    fun loadForecast(name: String) {
        service.getForecast(name) { apiForecast ->
            val newCity = _cities[name]!!.copy( forecast = apiForecast?.toForecast() )
            _cities.remove(name)
            _cities[name] = newCity
            city = if (city?.name == name) newCity else city
        }
    }

    fun loadBitmap(name: String) {
        val city = _cities[name]
        service.getBitmap(city?.weather!!.imgUrl) { bitmap ->
            val newCity = city.copy(
                weather = city.weather?.copy(
                    bitmap = bitmap
                )
            )
            _cities.remove(name)
            _cities[name] = newCity
        }
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