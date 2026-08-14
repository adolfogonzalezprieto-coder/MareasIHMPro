package com.adolfogonzalez.mareasihmpro.data
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
interface IhmApi {
 @GET("api-ihm/getmarea") suspend fun stations(@Query("request") request:String="getlist",@Query("format") format:String="json"):StationEnvelope
 @GET("api-ihm/getmarea") suspend fun tides(@Query("request") request:String="gettide",@Query("id") id:String,@Query("format") format:String="json",@Query("date") date:String):IhmResponse
}
interface WeatherApi {
 @GET("v1/forecast") suspend fun forecast(@Query("latitude") latitude:Double,@Query("longitude") longitude:Double,@Query("current") current:String="temperature_2m,apparent_temperature,relative_humidity_2m,surface_pressure,uv_index,wind_speed_10m,wind_direction_10m,wind_gusts_10m",@Query("hourly") hourly:String="temperature_2m,uv_index,wind_speed_10m,wind_gusts_10m",@Query("forecast_days") days:Int=2,@Query("timezone") timezone:String="Europe/Madrid"):WeatherResponse
}
interface MarineApi {
 @GET("v1/marine") suspend fun current(@Query("latitude") latitude:Double,@Query("longitude") longitude:Double,@Query("current") current:String="wave_height,wave_period,wave_direction,swell_wave_height,sea_surface_temperature",@Query("timezone") timezone:String="Europe/Madrid"):MarineResponse
}
object Apis {
 private fun retrofit(url:String)=Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build()
 val ihm=retrofit("https://ideihm.covam.es/").create(IhmApi::class.java)
 val weather=retrofit("https://api.open-meteo.com/").create(WeatherApi::class.java)
 val marine=retrofit("https://marine-api.open-meteo.com/").create(MarineApi::class.java)
}
