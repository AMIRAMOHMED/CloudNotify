import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_weather")
data class DailyWeather(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Auto-generated ID
    val dayOfWeek: String,
    val weatherDescription: String,
    val tempMin: Double,
    val tempMax: Double,
    val icon: String
)
