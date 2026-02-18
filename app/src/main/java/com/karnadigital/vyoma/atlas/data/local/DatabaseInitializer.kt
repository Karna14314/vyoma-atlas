package com.karnadigital.vyoma.atlas.data.local

import android.content.Context
import com.karnadigital.vyoma.atlas.data.local.dao.AstronomicalObjectDao
import com.karnadigital.vyoma.atlas.data.local.entity.AstronomicalObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.InputStreamReader

class DatabaseInitializer(
    private val context: Context,
    private val daoProvider: () -> AstronomicalObjectDao
) {
    fun initialize() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = context.assets.open("initial_data.json")
                val reader = InputStreamReader(inputStream)
                val jsonString = reader.readText()
                val jsonArray = JSONArray(jsonString)
                
                val objects = mutableListOf<AstronomicalObject>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    objects.add(
                        AstronomicalObject(
                            id = jsonObject.getString("id"),
                            name = jsonObject.getString("name"),
                            type = jsonObject.getString("type"),
                            description = jsonObject.optString("description", null),
                            distanceAu = if (jsonObject.has("distanceAu")) jsonObject.getDouble("distanceAu") else null,
                            distanceLy = if (jsonObject.has("distanceLy")) jsonObject.getDouble("distanceLy") else null,
                            radiusKm = if (jsonObject.has("radiusKm")) jsonObject.getDouble("radiusKm") else null,
                            magnitude = if (jsonObject.has("magnitude")) jsonObject.getDouble("magnitude") else null,
                            constellation = jsonObject.optString("constellation", null),
                            imageUrl = jsonObject.optString("imageUrl", null),
                            rightAscension = if (jsonObject.has("rightAscension")) jsonObject.getDouble("rightAscension") else null,
                            declination = if (jsonObject.has("declination")) jsonObject.getDouble("declination") else null,
                            parentId = jsonObject.optString("parentId", null)
                        )
                    )
                }
                
                daoProvider().insertAll(objects)
                android.util.Log.d("DatabaseInitializer", "Successfully loaded ${objects.size} objects from JSON")
            } catch (e: Exception) {
                android.util.Log.e("DatabaseInitializer", "Failed to load from JSON, using fallback data", e)
                // Fallback: Insert hardcoded minimal data
                val fallbackObjects = listOf(
                    AstronomicalObject(
                        id = "sun",
                        name = "Sun",
                        type = "STAR",
                        description = "The star at the center of our Solar System",
                        radiusKm = 696350.0,
                        magnitude = -26.74,
                        rightAscension = 0.0,
                        declination = 0.0,
                        distanceAu = null,
                        distanceLy = null,
                        constellation = null,
                        imageUrl = null,
                        parentId = null
                    ),
                    AstronomicalObject(
                        id = "mercury",
                        name = "Mercury",
                        type = "PLANET",
                        description = "Smallest planet, closest to the Sun",
                        radiusKm = 2439.5,
                        distanceAu = 0.39,
                        magnitude = -0.4,
                        rightAscension = null,
                        declination = null,
                        distanceLy = null,
                        constellation = null,
                        imageUrl = null,
                        parentId = "sun"
                    ),
                    AstronomicalObject(
                        id = "venus",
                        name = "Venus",
                        type = "PLANET",
                        description = "Hottest planet due to greenhouse effect",
                        radiusKm = 6052.0,
                        distanceAu = 0.72,
                        magnitude = -4.6,
                        rightAscension = null,
                        declination = null,
                        distanceLy = null,
                        constellation = null,
                        imageUrl = null,
                        parentId = "sun"
                    ),
                    AstronomicalObject(
                        id = "earth",
                        name = "Earth",
                        type = "PLANET",
                        description = "Only known planet to support life",
                        radiusKm = 6371.0,
                        distanceAu = 1.0,
                        magnitude = -3.99,
                        rightAscension = null,
                        declination = null,
                        distanceLy = null,
                        constellation = null,
                        imageUrl = null,
                        parentId = "sun"
                    ),
                    AstronomicalObject(
                        id = "mars",
                        name = "Mars",
                        type = "PLANET",
                        description = "The Red Planet",
                        radiusKm = 3389.5,
                        distanceAu = 1.52,
                        magnitude = -2.94,
                        rightAscension = null,
                        declination = null,
                        distanceLy = null,
                        constellation = null,
                        imageUrl = null,
                        parentId = "sun"
                    )
                )
                daoProvider().insertAll(fallbackObjects)
                android.util.Log.d("DatabaseInitializer", "Inserted ${fallbackObjects.size} fallback objects")
            }
        }
    }
}
