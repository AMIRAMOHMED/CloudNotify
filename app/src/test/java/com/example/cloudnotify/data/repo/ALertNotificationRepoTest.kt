import com.example.cloudnotify.data.model.local.AlertNotification
import com.example.cloudnotify.data.repo.ALertNotificationRepo
import com.example.cloudnotify.data.repo.FakeAlertNotificationDao
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class AlertNotificationRepoTest {

    private val fakeDao = FakeAlertNotificationDao()
    private val repo = ALertNotificationRepo(fakeDao)

    @Test
    fun insertAlertNotification_addsNotification() = runTest {
        // Arrange
        val alert = AlertNotification(id = 1, title = "Test Alert", calendar = 7878, type = "alarm")

        // Act
        repo.insertAlertNotification(alert)

        // Assert
        val notifications = repo.getAllAlertNotifications().first()
        assertEquals(1, notifications.size)
        assertEquals("Test Alert", notifications[0].title)
    }

    @Test
    fun getAllAlertNotifications_returnsNotifications() = runTest {
        // Arrange
        val alert1 = AlertNotification(id = 1, title = "Test Alert 1", calendar = 7878, type = "alarm")
        fakeDao.insertAlertNotification(alert1)

        // Act: Collect the flow using first()
        val notifications = repo.getAllAlertNotifications().first() //  collects the first emitted value from the flow

        // Assert
        assertEquals(1, notifications.size)
        assertEquals("Test Alert 1", notifications[0].title)
    }

    @Test
    fun deleteAlertNotificationById_removesNotification() = runTest {
        // Arrange
        val alert = AlertNotification(id = 1, title = "Test Alert", calendar = 7878, type = "alarm")
        fakeDao.insertAlertNotification(alert)

        // Act
        repo.deleteAlertNotificationById(1)

        // Assert
        val notifications = repo.getAllAlertNotifications().first()
        assertEquals(0, notifications.size)
    }
}