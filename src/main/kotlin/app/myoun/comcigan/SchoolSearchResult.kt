@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
package app.myoun.comcigan

import kotlinx.serialization.*
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.JsonNames
import java.lang.IllegalArgumentException

@Serializable
data class SchoolSearchResult(@JsonNames("학교검색") val schoolSearch: List<SearchedSchool>)

@Serializable(with = SearchedSchoolSerializer::class)
data class SearchedSchool(val schoolCode: String, val region: String, val name: String)


class SearchedSchoolSerializer : KSerializer<SearchedSchool> {

    companion object {
        val stringDescriptor = String.serializer().descriptor
    }

    override val descriptor: SerialDescriptor = buildSerialDescriptor("app.myoun.SearchedSchool", StructureKind.LIST, stringDescriptor) {
        element("schoolCode", stringDescriptor)
        element("region", stringDescriptor)
        element("name", stringDescriptor)
    }

    override fun deserialize(decoder: Decoder): SearchedSchool {
        return decoder.decodeStructure(descriptor) {
            var schoolCode: Int? = null
            var region: String? = null
            var name: String? = null
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> decodeIntElement(descriptor, 0)
                    1 -> region = decodeStringElement(descriptor, 1)
                    2 -> name = decodeStringElement(descriptor, 2)
                    3 -> schoolCode = decodeIntElement(descriptor, 3)
                    DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            SearchedSchool(schoolCode.toString(), region ?: "null", name ?: "null")
        }
    }

    override fun serialize(encoder: Encoder, value: SearchedSchool) {
        TODO("Not yet implemented")
    }


}