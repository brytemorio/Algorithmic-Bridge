# The Algorithmic Bridge
A Bridge for transfers of crypto assets between different blockchains
It is based on ideas picked from the [Xclaim paper](https://github.com/brytemorio/Algorithmic-Bride/blob/main/XCLAIM(research%20paper).pdf).

##Usage
```java -jar algo-bride.jar```

<br/>

The bridge internally uses a default config file in [TOML](https://toml.io) format.
To override the default config file. Use the System Property ***config***. That is:

``` java -Dconfigfile="path/to/toml/configfile```

<br/>

Ideally you should supply your own config file using the pattern