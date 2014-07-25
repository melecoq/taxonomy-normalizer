taxonomy-normalizer
===================

A repackage of "old portal" GBIF code that produces a parent child taxonomy from a source DwC-A which contains s denormalized taxonomy.

Please note that this code is provided as is, with no official support (although I'll try and answer questions) and it expects the input file to have species and subspecies to be complete (e.g. subspecies as "Aus bus cus" and not the epithet only "cus").  In short, this requires a little work to be ready to use, but it is very close and has been used in production in the old GBIF portal code.

To run this using the sample [classpath is your responsibility, but I'd suggest using your IDE]:

```
java org.gbif.taxonomy.norm.DwcaNormalizer -file sample/passeriformes.csv
```

Running this should produce a ```/tmp/normalized.txt```

Good luck!
