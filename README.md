# The Algorithmic Bridge
A Bridge for transfers of crypto assets between different blockchains
It is based on ideas picked from the [Xclaim paper](https://github.com/brytemorio/Algorithmic-Bride/blob/main/XCLAIM(research%20paper).pdf).

## Usage
```java -jar algo-bridge.jar```

<br/>

The bridge internally uses a default config file in [TOML](https://toml.io) format.
To override the default config file. Use the System Property ***configfile***. That is:

``` java -Dconfigfile="path/to/toml/configfile" -jar algo-bridge.jar```


Ideally you should supply your own config file using the pattern given [here]( https://github.com/brytemorio/Algorithmic-Bride/blob/main/config.example.toml)


<br/>

The following types of blockchains are supported by the bridge:
* Bitcoin based blockchains
* Ethereuem based blockchains
* Waves Blockchain
