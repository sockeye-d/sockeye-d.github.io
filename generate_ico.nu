#!/usr/bin/nu

def main [] {
    let _ = magick convert -background transparent "favicon.png" -define icon:auto-resize=16,32 "favicon.ico"
    print "Done"
}
