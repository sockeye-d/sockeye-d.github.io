import java.time.*;
import java.time.format.*;

fun tagHtml(tag: Any?): String {
    val tag = tag as String
    return """<a href="/posts?tag=$tag" class="post-tag-button-$tag"><svg class="icon-small"><use href="/tabler.svg#tabler-tag" /></svg>$tag</a>"""
}

fun clickableTag(tag: Any?): String {
    val tag = tag as String
    return """<a href="javascript:setFilterTag('$tag')" class="post-tag-button-$tag"><svg class="icon-small"><use href="/tabler.svg#tabler-tag" /></svg>$tag</a>"""
}

fun Any?.formatDateTime() = (this as? LocalDateTime)!! .format(DateTimeFormatter.ISO_DATE)!!
