package app.myoun.comcigan

import kotlinx.coroutines.runBlocking

class School private constructor(val schoolCode: String) {

    companion object {
        suspend fun search(name: String): List<SearchedSchool> {
            return ComciganUtil.searchSchool(name).schoolSearch
        }

        suspend fun search(name: String, region: String): List<SearchedSchool> {
            return ComciganUtil.searchSchool(name, region).schoolSearch
        }

        fun fromSchoolCode(schoolCode: String): School =
            School(schoolCode)
    }

    var timetable: Timetable
        private set

    init {
        runBlocking {
            timetable = ComciganUtil.getTimetable(schoolCode)
            this@School.refresh()
        }
    }

    suspend fun refresh() {
        timetable = ComciganUtil.getTimetable(schoolCode)
    }

}