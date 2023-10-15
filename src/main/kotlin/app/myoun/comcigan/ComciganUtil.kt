package app.myoun.comcigan

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okio.ByteString.Companion.encode
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.nio.charset.Charset

object ComciganUtil {

    private const val BASEURL = "http://comci.kr:4082"

    private val client = HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(true)
            }
        }
        Charsets {
            register(Charset.forName("UTF-8"))
            register(Charset.forName("EUC-KR"))
        }
        install(ContentNegotiation) {
            json()
        }
        install(HttpTimeout)
    }

    private val comciHtml = Jsoup.connect("$BASEURL/st").get()
    private val script = comciHtml.select("script[language=\"Javascript\"]")[0].dataNodes()[0].toString()

    private val SCSEARCH_REGEX = """(?<=url:['\"]\.).*?(?=['\"])""".toRegex()
    private val TIMEGET_REGEX = """(?<=var sc3=['\"]\.).*?(?=['\"])""".toRegex()
    private val PREFIX_REGEX = """(?<=sc_data\(['\"]).*?(?=['\"])""".toRegex()

    private val TEACHER_NUM_REGEX = """성명=자료\.자료\d+""".toRegex()
    private val SUBJECT_NUM_REGEX = """자료.자료\d+\[sb\]""".toRegex()
    private val DAY_DATA_NUM_REGEX = """일일자료=Q자료\(자료\.자료\d+""".toRegex()

    private val SUBJECT_NUM = extractint(regsearch(SUBJECT_NUM_REGEX, script))
    private val TEACHER_NUM = extractint(regsearch(TEACHER_NUM_REGEX, script))
    private val DAY_DATA_NUM = extractint(regsearch(DAY_DATA_NUM_REGEX, script))

    fun regsearch(reg: Regex, org: String): String? {
        return reg.find(org)?.groupValues?.get(0)
    }

    fun extractint(org: String?): Int? = org?.filter { it.isDigit() }?.toInt()


    suspend fun searchSchool(schoolName: String): SchoolSearchResult {
        val SEARCHPATH = SCSEARCH_REGEX.findAll(script).first().value

        val schoolCodeSearch = client.get("${BASEURL}${SEARCHPATH}${URLEncoder.encode(schoolName, "EUC-KR")}") {
            timeout {
                requestTimeoutMillis = 60 * 1000
            }
        }.bodyAsText()
        val trimmed = schoolCodeSearch.filter { it.code != 0 }
        val schoolSearchResult: SchoolSearchResult = Json.decodeFromString(trimmed)
        return schoolSearchResult
    }

    suspend fun searchSchool(schoolName: String, region: String): SchoolSearchResult {
        val raw_search = searchSchool(schoolName)
        val result = SchoolSearchResult(raw_search.schoolSearch.filter { it.region == region })
        return result
    }

    suspend fun getTimetable(schoolCode: String): Timetable {
        val TIMEPATH = TIMEGET_REGEX.findAll(script).first().value
        val PREFIX = PREFIX_REGEX.findAll(script).first().value
        val timeUrl = "$BASEURL$TIMEPATH${"$PREFIX${schoolCode}_0_1".encode(Charsets.UTF_8).toByteArray().encodeBase64()}"

        val timeResponse = client.get(timeUrl) {
            timeout { requestTimeoutMillis = 60 * 1000 }
        }

        val rawTimetable: JsonObject = Json.decodeFromString(timeResponse.bodyAsText().filter { it.code != 0 })
        val subjects = rawTimetable["자료${SUBJECT_NUM}"]!!.jsonArray
        val teachers = rawTimetable["자료${TEACHER_NUM}"]!!.jsonArray
        val dayData = rawTimetable["자료${DAY_DATA_NUM}"]!!.jsonArray

        val timetable = mutableListOf<List<List<List<Period>>>>()

        for (igrade in dayData.drop(1)) {
            val classList = mutableListOf<List<List<Period>>>()
            for (iclass in igrade.jsonArray.drop(1)) {
                val dayList = mutableListOf<List<Period>>()
                for (iday in iclass.jsonArray.slice(1..5)) {
                    val periodList = mutableListOf<Period>()
                    for (iperiod in iday.jsonArray.drop(1).filter { it.toString().dropLast(2).isNotEmpty() }) {
                        val tn = iperiod.toString().dropLast(2).toInt()
                        val sn = iperiod.toString().padStart(4, '0').drop(2).toInt()
                        val teacher = teachers[tn]
                        val subject = subjects[sn]
                        periodList.add(Period(subject = subject.jsonPrimitive.content, teacher = teacher.jsonPrimitive.content))
                    }
                    dayList.add(periodList)
                }
                classList.add(dayList)
            }
            timetable.add(classList)
        }

        return timetable
    }
}