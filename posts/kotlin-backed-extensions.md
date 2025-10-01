```yaml
title: How to make an extension property with a backing field in Kotlin
description: >
    A cursed episode for the start of October: making an extension property with a backing field in Kotlin
tags: [kotlin, cursed]
published-date: "2025-10-01T20:12:00Z"
comment-did: "at://did:plc:lkrhpzgaij74fjzqeimfyicc/app.bsky.feed.post/3m25udn7ey22f"
```

It's October now, and that means the Spooky Season™ is upon us.
(although, if you're a large retailer, spooky season started 3 months ago).
To "celebrate," I want to do something spooky in Kotlin.

## Giving extension properties their backing fields back

If you've used Kotlin for more than 3 seconds, I can almost guarantee you've heard of an [extension property](https://kotlinlang.org/docs/extensions.html#scope-of-extensions).
They're like extension functions, but for properties:

```kotlin
val Double.formattedToThreePlaces
    get() = String.format("%.3f", this)

/* ...later */

println(0.1234567.formattedToThreePlaces) // 0.123
```

Or,

```kotlin
val String.int get() = this.toInt()
```

Now, these come with some limitations:
* You *must* compute the value on-demand, you can't store it anywhere
* You can't use initializers
* You can't feed them after midnight.

You can't, for example, do

```kotlin
val Double.five = 5.0
    set(value) {
        field = 10.0
    }
    get() = field / 2.0
```

But like, what if you could?

> Also, now that I think about it, that last one might be from *Gremlins*.

## HashMap?

Okay, let's just store them in a HashMap. A HashMap is sort of like a property backing field. Surely nothing will go wrong here:

```kotlin
private val backing = mutableMapOf<String, String>()
var String.hello
    set(value) {
        backing[this] = value
    }
    get() = backing[this] ?: error("not found")

/* ...later */
repeat(Int.MAX_VALUE) {
    it.toString().hello = "5"
}
```

If you actually try this, it might work for a while. In fact, your program might not run long enough for this to be a problem!
But, there's still a somewhat subtle issue here: a HashMap stores **strong** references to its keys.
Every time you insert a key into that map, it stops being able to be garbage collected.
This won't make your program crash instantly — the worst type of bug — instead, it'll probably work for a little while, limping along as the heap fills up with uncollectable references.

Eventually, the heap will fill, and you'll get a

```
Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "main"
```

> I'd like to take a second to acknowledge that just because the JVM is a managed, garbage-collected VM,
> it's still possible to "leak" memory like this.
> While not technically a *real* memory leak, you can just stuff all your references in a never-collectable list,
> and they'll never be collected.

## Weak references

As you may have guessed, if the problem is that the HashMap stores **strong** references, why not store **weak** references instead?

A weak reference is a reference to an object that doesn't prevent it from being garbage collected. You can normally query the weak reference for whether the object still exists or not, like a [`#!cpp std::shared_ptr`](https://en.cppreference.com/w/cpp/memory/shared_ptr.html).

> Of course, since we're in managed land, you don't get pointers, you get "references," whatever those are

In fact, Java already provides a data structure to do this: the [`WeakHashMap`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/WeakHashMap.html)!

Let's try this again, but with a `WeakHashMap` instead:

```kotlin
import java.util.WeakHashMap

private val backing = WeakHashMap<String, String>()
var String.hello
    set(value) {
        backing[this] = value
    }
    get() = backing[this] ?: error("not found")

/* ...later */
repeat(Int.MAX_VALUE) {
    it.toString().hello = "5"
}
```

And finally, it doesn't crash.
We've fixed the issue.

## "Interesting" properties

Since strings on the JVM (and many other runtimes, like the CLR) are [interned](https://en.wikipedia.org/wiki/Interning_(computer_science)), you can do some pretty cursed stuff:

```kotlin
private val backing = WeakHashMap<String, String>()
var String.hello
    set(value) { backing[this] = value }
    get() = backing[this] ?: error("not found")

fun main() {
    "a".hello = "hi"
    "b".hello = "hello"
    println("a".hello) // "hi"
    println("b".hello) // "hello"
}
```

You can do even *more* cursed things:

```kotlin
private val backing = WeakHashMap<Int, String>()
var Int.hello
    set(value) { backing[this] = value }
    get() = backing[this] ?: error("not found")

5.hello = "hihihihihi"
10.hello = "hihihihihihihihihihi"
println(5.hello) // "hihihihihi"
println(10.hello) // "hihihihihihihihihihi"
```

## Property delegation

Kotlin has this neat feature called ["property delegation"](https://kotlinlang.org/docs/delegated-properties.html),
where you can delegate gets and sets to any object with the `getValue` (required) and `setValue` (if omitted, can only be applied to `val`s) functions:

```kotlin
var String.hi by object {
    operator fun getValue(thisRef: String, property: KProperty<*>) = "hi"

    operator fun setValue(thisRef: String, property: KProperty<*>, value: String) {
        println(value)
    }
}
```

Using this, you can wrap the cursed backed extension fields into a nice little function:

```kotlin
fun <K, V> backed() = object : ReadWriteProperty<K, V> {
    val backing = WeakHashMap<K, V>()
    override fun getValue(thisRef: K, property: KProperty<*>): V = backing[thisRef] ?: error("Key $thisRef not found")
    override fun setValue(thisRef: K, property: KProperty<*>, value: V) {
        backing[thisRef] = value
    }
}
```

> The interface `ReadWriteProperty` is used here so that the type checker "knows" that `backed` returns a type that implements the necessary functions.
> You could substitute it for an entire class, but a function is nicer, since it doesn't need to participate in a hierarchy.

Now, you can use it as simply as

```kotlin
var String.hello: String by backed()
```

Honestly, it feels like something so illegal shouldn't be able to be written in 7 lines of code, and used in just 1.
The type inference algorithm even figures out what the two type parameters should be from the property definition!
It's almost *too* convenient, and now I want to use this everywhere.
