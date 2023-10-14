import app.myoun.comcigan.ComciganUtil
import app.myoun.comcigan.School
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class ComciganTest {

    @Test
    fun `학교 검색 테스트`() = runBlocking {
        val searchByName = School.search("컴시간고등학교")
        val searchByNameAndRegion = School.search("컴시간고등학교", "경기")

        assert(
            searchByName.size == 1 &&
            searchByNameAndRegion.size == 1 &&
            searchByNameAndRegion[0].region == "경기"
        )
    }

    @Test
    fun `시간표 가져오기 테스트`() = runBlocking {
        val schoolCode = ComciganUtil.searchSchool("컴시간고등학교").schoolSearch.first().schoolCode

        val school = School.fromSchoolCode(schoolCode)

        assert(school.timetable.size == 3)
    }
}