package com.minlish.app.presentation.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.app.data.UserSession
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.presentation.dashboard.model.UserProgress
import com.minlish.app.presentation.dashboard.model.ActivityReport
import com.minlish.app.presentation.dashboard.model.DailyPlanTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import com.minlish.app.domain.model.ReviewHistory
import com.minlish.app.domain.model.UserSetting
import com.minlish.app.domain.model.enumration.EaseFactor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val userSession: UserSession,
    private val repository: BunnyRepository
) : ViewModel() {

    private val _userProgress = MutableStateFlow(UserProgress())
    val userProgress: StateFlow<UserProgress> = _userProgress.asStateFlow()

    private val _weeklyActivity =
        MutableStateFlow<List<ActivityReport>>(emptyList())

    val weeklyActivity: StateFlow<List<ActivityReport>> =
        _weeklyActivity.asStateFlow()

    private val _userSetting = MutableStateFlow(UserSetting())
    val userSetting: StateFlow<UserSetting> = _userSetting.asStateFlow()

    private fun getUserId(): String {
        return userSession.getUserId() ?: "test_user_001"
    }

    init {
        // Khởi tạo/Đồng bộ cấu trúc database và dữ liệu mẫu dưới nền
        initializeDatabase()
    }

    private fun initializeDatabase() {
        viewModelScope.launch {
            val userId = getUserId()
            // Nạp dữ liệu mẫu siêu đầy đủ
            repository.initializeEverythingWithFullData(userId)
            // Sau đó load lên UI
            fetchDashboardTelemetry()
        }
    }

    fun fetchDashboardTelemetry() {
        viewModelScope.launch {
            val userId = getUserId()

            // Lấy toàn bộ lịch sử ôn tập
            val reviewHistories = repository.getReviewHistories(userId) ?: emptyList()
            
            // Tính số ngày học liên tiếp từ lịch sử ôn tập
            val computedStreak = calculateStreak(reviewHistories)

            val firebaseProgress = repository.getUserProgress(userId)
            if (firebaseProgress != null) {
                _userProgress.value = firebaseProgress.copy(streak = computedStreak)
            } else {
                _userProgress.value = UserProgress(
                    wordsCount = reviewHistories.map { it.vocabId }.distinct().size,
                    streak = computedStreak
                )
            }

            // Truy vấn động số từ mới và số từ cần ôn tập
            val dailyPlan = repository.getDailyPlanTelemetry(userId)
            _userSetting.value = UserSetting(
                dailyNewWordGoal = dailyPlan.newWordsCount,
                dailyReviewGoal = dailyPlan.reviewWordsCount
            )

            // Bản đồ gom nhóm theo thứ tự ngày trong tuần của Java Calendar (MONDAY..SUNDAY)
            val dailyMinutesMap = mutableMapOf<Int, Int>()
            val daysOfWeek = listOf(
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY,
                Calendar.SUNDAY
            )
            for (day in daysOfWeek) {
                dailyMinutesMap[day] = 0
            }

            // Lấy tuần hiện tại
            val calNow = Calendar.getInstance()
            val currentYear = calNow.get(Calendar.YEAR)
            val currentWeek = calNow.get(Calendar.WEEK_OF_YEAR)

            // Quy đổi: Mỗi lượt học/ôn tập tương ứng với 2 phút học thực tế (chỉ tính trong tuần này)
            val cal = Calendar.getInstance()
            for (history in reviewHistories) {
                cal.time = history.learningTime
                val isSameWeek = cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.WEEK_OF_YEAR) == currentWeek
                if (isSameWeek) {
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    if (dayOfWeek in dailyMinutesMap) {
                        dailyMinutesMap[dayOfWeek] = dailyMinutesMap[dayOfWeek]!! + 2
                    }
                }
            }

            // Đóng gói cấu trúc đưa lên UI biểu đồ
            val activityReportList = listOf(
                ActivityReport("T2", dailyMinutesMap[Calendar.MONDAY] ?: 0),
                ActivityReport("T3", dailyMinutesMap[Calendar.TUESDAY] ?: 0),
                ActivityReport("T4", dailyMinutesMap[Calendar.WEDNESDAY] ?: 0),
                ActivityReport("T5", dailyMinutesMap[Calendar.THURSDAY] ?: 0),
                ActivityReport("T6", dailyMinutesMap[Calendar.FRIDAY] ?: 0),
                ActivityReport("T7", dailyMinutesMap[Calendar.SATURDAY] ?: 0),
                ActivityReport("CN", dailyMinutesMap[Calendar.SUNDAY] ?: 0)
            )
            _weeklyActivity.value = activityReportList
        }
    }

    private fun calculateStreak(reviewHistories: List<ReviewHistory>): Int {
        if (reviewHistories.isEmpty()) return 0

        // Chuyển đổi learningTime thành các ngày duy nhất (năm-tháng-ngày) để tránh giờ giấc khác nhau
        val dates = reviewHistories.map { history ->
            val cal = Calendar.getInstance()
            cal.time = history.learningTime
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending() // Sắp xếp giảm dần

        if (dates.isEmpty()) return 0

        val todayCal = Calendar.getInstance()
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)
        val todayMs = todayCal.timeInMillis

        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
        yesterdayCal.set(Calendar.HOUR_OF_DAY, 0)
        yesterdayCal.set(Calendar.MINUTE, 0)
        yesterdayCal.set(Calendar.SECOND, 0)
        yesterdayCal.set(Calendar.MILLISECOND, 0)
        val yesterdayMs = yesterdayCal.timeInMillis

        // Kiểm tra xem lần học gần nhất có phải hôm nay hoặc hôm qua không
        val latestStudyMs = dates[0]
        if (latestStudyMs != todayMs && latestStudyMs != yesterdayMs) {
            return 0
        }

        var streak = 1
        var currentDayMs = latestStudyMs

        for (i in 1 until dates.size) {
            val prevDayMs = dates[i]
            val checkCal = Calendar.getInstance()
            checkCal.timeInMillis = currentDayMs
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
            val expectedPrevDayMs = checkCal.timeInMillis
            
            if (prevDayMs == expectedPrevDayMs) {
                streak++
                currentDayMs = prevDayMs
            } else if (prevDayMs > expectedPrevDayMs) {
                continue
            } else {
                break
            }
        }

        return streak
    }

    fun recordStudySession(duration: Int) {
        viewModelScope.launch {
            val userId = getUserId()
            
            // Ghi nhận một ReviewHistory thực tế của người dùng
            val newHistory = ReviewHistory(
                id = "",
                vocabId = "vocab_001",
                learningTime = Date(),
                rating = EaseFactor.GOOD
            )
            repository.saveReviewHistory(userId, newHistory)

            // Cộng thêm 1 vào số từ vựng người dùng đã học
            _userProgress.value = _userProgress.value.copy(
                wordsCount = _userProgress.value.wordsCount + 1
            )
            repository.saveUserProgress(userId, _userProgress.value)

            // Kéo dữ liệu mới nhất từ Firebase về để cập nhật biểu đồ ngay lập tức
            fetchDashboardTelemetry()
        }
    }
}
