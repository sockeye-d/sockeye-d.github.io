import java.time.LocalDateTime
import java.net.URI

fun tagHtml(tag: Any?): String {
    val tag = tag as String
    return """<a href="/posts?tag=$tag" class="post-tag-button-$tag"><svg class="icon-small"><use href="/tabler.svg#tabler-tag" /></svg>$tag</a>"""
}

fun clickableTag(tag: Any?): String {
    val tag = tag as String
    return """<a href="javascript:setFilterTag('$tag')" class="post-tag-button-$tag"><svg class="icon-small"><use href="/tabler.svg#tabler-tag" /></svg>$tag</a>"""
}

fun Any?.formatDateTime() = (this as? LocalDateTime)!!.run { """${month.name.lowercase().replaceFirstChar { it.titlecaseChar() }} $dayOfMonth $year""" }

fun URI?.toBskyAppLink() =
    if (this == null) "null" else
        "https://bsky.app/profile/${authority}/post/${path.split("/").last()}"
