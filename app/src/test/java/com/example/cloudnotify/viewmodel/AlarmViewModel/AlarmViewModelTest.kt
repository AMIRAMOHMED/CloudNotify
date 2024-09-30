package com.example.cloudnotify.viewmodel.AlarmViewModel
import com.example.cloudnotify.data.model.local.AlertNotification
import com.example.cloudnotify.data.repo.ALertNotificationRepo
import com.example.cloudnotify.data.repo.FakeAlertNotificationDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AlarmViewModelTest {

    /*



      * This test class is for testing the AlarmViewModel.


      *PLEASE NOTE:

      * This test should be run independently to ensure isolation.
      * It modifies the state and should not affect other tests.



      */

    private val currentTime = System.currentTimeMillis()

    private val alert1 = AlertNotification(id = 1, title = "Test Alert", calendar = currentTime + 10000, type = "alarm")
    private val alert2 = AlertNotification(id = 2, title = "Test Alert 2", calendar = currentTime + 20000, type = "alarm")
    private val alert3 = AlertNotification(id = 3, title = "Test Alert 3", calendar = currentTime - 10000, type = "alarm")
    private val alert4 = AlertNotification(id = 4, title = "Test Alert 4", calendar = currentTime - 20000, type = "alarm")

    private lateinit var alertNotificationRepo: ALertNotificationRepo
    private lateinit var alarmViewModel: AlarmViewModel
    private lateinit var fakeDao: FakeAlertNotificationDao

    @Before
    fun setUp() {
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher) // Set the main dispatcher for coroutines
        fakeDao = FakeAlertNotificationDao()
        alertNotificationRepo = ALertNotificationRepo(fakeDao)
        alarmViewModel = AlarmViewModel(alertNotificationRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        fakeDao.clear() // Implement a method in FakeAlertNotificationDao to reset state

    }


    @Test
    fun `test insertAlertNotification adds notification`() = runTest {
        alarmViewModel.insertAlertNotification(alert1)

        // Check that the notification is in the repository
        val notifications: List<AlertNotification> = fakeDao.getAllAlertNotifications().first()
        assertTrue(notifications.contains(alert1))
    }

    @Test
    fun `test deleteAlertNotificationById removes notification`() = runTest {
        // Insert alert to delete
        fakeDao.insertAlertNotification(alert1)

        // Delete the alert
        alarmViewModel.deleteAlertNotificationById(alert1.id)

        // Check that the alert is deleted
        val notifications: List<AlertNotification> = fakeDao.getAllAlertNotifications().first()
        assertFalse(notifications.contains(alert1))
    }

    @Test
    fun `test getAllAlertNotifications returns future notifications`() = runTest {
        // Insert alerts into the fake DAO
        fakeDao.insertAlertNotification(alert1)
        fakeDao.insertAlertNotification(alert2) // Future notifications
        fakeDao.insertAlertNotification(alert3) // Past notification
        fakeDao.insertAlertNotification(alert4) // Past notification

        // Call the method
        alarmViewModel.getAllAlertNotifications()

        // Use a delay or observe the StateFlow to ensure it's updated
        // If you have a delay, make sure it's enough for the coroutine to finish processing
        advanceUntilIdle()  // This will let all coroutines finish

        // Check that only future notifications are present
        val notifications: List<AlertNotification> = alarmViewModel.alertNotifications.first()
        assertEquals(2, notifications.size) // Only two alerts should be in the future
        assertTrue(notifications.contains(alert1))
        assertTrue(notifications.contains(alert2))
        assertFalse(notifications.contains(alert3))
        assertFalse(notifications.contains(alert4))
    }



    @Test
    fun `test getAllAlertNotifications returns empty list when none exist`() = runTest {
        // Call the method with no notifications added
        alarmViewModel.getAllAlertNotifications()

        // Check that the notifications list is empty
        val notifications: List<AlertNotification> = alarmViewModel.alertNotifications.first()
        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `test deleteNonExistentAlertNotificationById does not crash`() = runTest {
        // Attempt to delete a non-existent alert
        alarmViewModel.deleteAlertNotificationById(999) // ID that doesn't exist

        // Ensure no exception is thrown and state remains unchanged
        val notifications: List<AlertNotification> = fakeDao.getAllAlertNotifications().first()
        assertEquals(0, notifications.size) // Ensure that no notifications were added
    }

    @Test
    fun `test insertMultipleAlertNotifications adds notifications`() = runTest {
        alarmViewModel.insertAlertNotification(alert1)
        alarmViewModel.insertAlertNotification(alert2)

        // Check that both notifications are in the repository
        val notifications: List<AlertNotification> = fakeDao.getAllAlertNotifications().first()
        assertTrue(notifications.contains(alert1))
        assertTrue(notifications.contains(alert2))
        assertEquals(2, notifications.size)
    }


}