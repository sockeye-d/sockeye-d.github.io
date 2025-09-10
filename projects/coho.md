```yaml
meta:
  title: coho
  description: Static site generator written in Kotlin
```
# coho

Coho is a static site generator written in Kotlin. It supports
* live reload
* simple Kotlin-based configuration

[link](/projects/godl.md)

```nu
#! /usr/bin/nu

let bad_chars = [
    '"', '*', '/', ':', '<', '>', '?', '\', '|',
]

def scan-dir [base_dir: path = .] {
    ls --full-paths $base_dir | par-each {|path|
        if ($path.name | path type) == dir {
            scan-dir $path.name
        }

        let filename: string = $path.name | path basename
        if ($bad_chars | any {|test_char| $filename | str contains $test_char}) {
            notify-send -a "check-filenames.nu" -i "syncthing" $"($filename) has bad characters" $"($path.name)" -t 10000
        }
    }
}

scan-dir ~/synced/
null

let closure = {|a, b: int, ...c|
    print "hi"
}

if (git remote get-url origin | complete | get exit_code) == 0 {

} else {

}

```

Here is some inline highlighted code `#!nu ls -a` hahaha
