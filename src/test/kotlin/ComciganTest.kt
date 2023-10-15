import app.myoun.comcigan.ComciganUtil
import app.myoun.comcigan.School
import app.myoun.comcigan.Timetable
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class ComciganTest {

    @Test
    fun `학교 검색 테스트`() = runBlocking {
        val searchByName = School.search("컴시간고등학교")
        val searchByNameAndRegion = School.search("컴시간고등학교", "강원")

        assert(
            searchByName.size == 1 &&
            searchByNameAndRegion.size == 1 &&
            searchByNameAndRegion[0].region == "강원"
        )
    }

    @Test
    fun `시간표 가져오기 테스트`() = runBlocking {
        val schoolCode = ComciganUtil.searchSchool("컴시간고등학교").schoolSearch.first().schoolCode

        val timetable = School(schoolCode)

        assert(timetable.timetable.size == 3)
    }

    @Test
    fun `시간표 출력 길이 테스트`() = runBlocking {
        val schoolCode = ComciganUtil.searchSchool("컴시간고등학교").schoolSearch.first().schoolCode

        val timetable = School(schoolCode)

        assert(timetable.timetable[1][1][1][1].subject.length == 2)
    }
}