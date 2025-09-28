```yaml
title: Making an XML DSL for Kotlin
description: Making an XML DSL for Kotlin in 2 different ways
tags: [kotlin, coho, dsl]
published-date: "2025-09-27T22:00:00Z"
comment-did: "at://did:plc:lkrhpzgaij74fjzqeimfyicc/app.bsky.feed.post/3lzuaqwkbm223"
```

In making [coho](/posts/coho.md), I needed a quick and easy way to generate some XML.
So, a week ago, I put together a pretty simple DSL for appending tags to a `StringBuilder`.
This is especially nice for the Kotlin templates because it means you *almost* get something JSX-like (KTX?) inside your HTML.

I eventually landed on this, seen here generating the RSS feed for my website:

```kotlin
val rss = tag("rss", "version" to "2.0") {
    tag("channel") {
        tag("title") { append("fishnpotatoes' blog") }
        tag("link") { append("https://fishies.dev/posts") }
        tag("description") { append("fishnpotatoes' blog") }
        tag("language") { append("en") }
        tag("ttl") { append(15) }

        for (post in allPosts) {
            tag("item") {
                tag("title") { append(post.title) }
                tag("description") {
                    cdata {
                        append(innerHtmls[post.title])
                    }
                }
                tag("link") {
                    append("https://fishies.dev/posts/${post.source?.nameWithoutExtension}.html")
                }
                tag("pubDate") {
                    append(
                        post.pubDate.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME)
                    )
                }
            }
        }
    }
}
```

`tag` is just an extension function on `StringBuilder`. Here's the entire DSL code (it's really short!):

```kotlin
fun StringBuilder.tag(name: String, vararg attributes: Pair<String, String>, configure: StringBuilder.() -> Unit = {}) {
    val attrs = attributes.map { (key, value) -> "$key=\"$value\"" }
    append("<$name ${attrs.joinToString(" ")}>")
    configure()
    append("</$name>")
}

fun tag(name: String, vararg attributes: Pair<String, String>, configure: StringBuilder.() -> Unit = {}): String {
    val sb = StringBuilder()
    sb.tag(name, *attributes, configure = configure)
    return sb.toString()
}
```

## V2

I wrote the first version of it a week ago.
Since then, I've thought about it, and I realized I'm not entirely happy with how it turned out — ideally, I'd have something closer to [kotlinx.html](https://github.com/Kotlin/kotlinx.html)'s API, although without the DOM-like features.

This is what I finally landed on:

```kotlin
val rss = "rss"("version" to "2.0") {
    "channel" {
        "title" { append("fishnpotatoes' blog") }
        "link" { append("https://fishies.dev/posts") }
        "description" { append("fishnpotatoes' blog") }
        "language" { append("en") }
        "ttl" { append(15) }

        for (post in allPosts) {
            "item" {
                "title" { append(post.title) }
                "description" {
                    cdata {
                        append(innerHtmls[post.title])
                    }
                }
                "link" {
                    append("https://fishies.dev/posts/${post.source?.nameWithoutExtension}.html")
                }
                "pubDate" {
                    append(
                        post.pubDate.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME)
                    )
                }
            }
        }
    }
}
```

To me it reads much more cleanly, with less extraneous stuff and it looks much closer to both the "official" HTML DSL *and* a mix between JSX and Kotlin.
Its implementation is just about as simple:

```kotlin
context(builder: StringBuilder) operator fun String.invoke(
    vararg attributes: Pair<String, Any?>, inner: StringBuilder.() -> Unit = {}
) {
    val attrs = attributes.map { (key, value) -> "$key=\"$value\"" }
    builder.append("<$this ${attrs.joinToString(" ")}>")
    builder.inner()
    builder.append("</$this>")
}

operator fun String.invoke(
    vararg attributes: Pair<String, Any?>, inner: StringBuilder.() -> Unit = {}
) = with(StringBuilder()) {
    this@invoke(*attributes, inner = inner)
    toString()
}
```

> you'll need to enable [context parameters in Gradle](https://kotlinlang.org/docs/context-parameters.html#how-to-enable-context-parameters)

These implementations aren't 100% lore-accurate to coho since I have extra utilities for XML escaping, but they're very close to what was actually used and what is actually being used respectively.

## Bonus: StringBuilder helper functions

Here are also some helper functions for StringBuilder:

```kotlin
fun StringBuilder.cdata(inner: StringBuilder.() -> Unit = {}) {
    append("<![CDATA[")
    inner()
    append("]]>")
}

fun StringBuilder.doctype() {
    append("<!doctype html>")
}

fun StringBuilder.prolog(version: String = "1.0", encoding: String = "UTF-8") {
    append("<?xml version=\"$version\" encoding=\"$encoding\"?>")
}
```
