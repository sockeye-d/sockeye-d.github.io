import java.time.LocalDateTime
import java.net.URI

fun icon(id: String, size: String = "small") = "svg"("class" to "icon-$size") {
    append("<use href=\"/tabler.svg#tabler-$id\" />")
}

fun tagHtml(tag: Any?): String {
    val tag = tag as String
    return """<a href="/posts?tag=$tag" class="post-tag-button-$tag"><svg class="icon-small"><use href="/tabler.svg#tabler-tag" /></svg>$tag</a>"""
}

fun clickableTag(tag: Any?, end: Any? = null): String {
    val tag = tag as String
//     return """<a href="javascript:setFilterTag('$tag')" class="post-tag-button-$tag"><svg class="icon-small"><use href="/tabler.svg#tabler-tag" /></svg>$tag</a>"""
    return "a"("href" to "javascript:setFilterTag('$tag')", "class" to "post-tag-button-$tag") {
        append(icon("tag"))
        "span" {
            append(tag)
            if (end != null) {
                append(" ")
                "span"("class" to "subtext") {
                    append(end)
                }
            }
        }
    }
}

fun Any?.formatDateTime() = (this as? LocalDateTime)!!.run { """${month.name.lowercase().replaceFirstChar { it.titlecaseChar() }} $dayOfMonth $year""" }

fun Any?.toBskyAppLink() =
    if (this as? URI== null) "null" else
        "https://bsky.app/profile/${authority}/post/${path.split("/").last()}"
