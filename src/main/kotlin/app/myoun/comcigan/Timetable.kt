package app.myoun.comcigan

import kotlinx.coroutines.runBlocking

class School(val schoolCode: String) {

    companion object {
        suspend fun search(name: String): List<SearchedSchool> {
            return ComciganUtil.searchSchool(name).schoolSearch
        }

        suspend fun search(name: String, region: String): List<SearchedSchool> {
            return ComciganUtil.searchSchool(name, region).schoolSearch
        }
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