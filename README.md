# The Algorithmic Bridge [Incomplete]
A Bridge for transfers of crypto assets between different blockchains
It is based on ideas picked from the [Xclaim paper](https://github.com/brytemorio/Algorithmic-Bride/blob/main/XCLAIM(research%20paper).pdf).

## Overview
![image](https://user-images.githubusercontent.com/61395900/212017691-86c4c9df-c96e-43e5-b8a7-258d8d6f00f4.png)


## Usage
```java -jar algo-bridge.jar```

<br/>

The bridge internally uses a default config file in [TOML](https://toml.io) format.
To override the default config file. Use the System Property ***configfile***. That is:

``` java -Dconfigfile="path/to/toml/configfile" -jar algo-bridge.jar```


Ideally you should supply your own config file using the pattern given [here]( https://github.com/brytemorio/Algorithmic-Bridge/blob/main/app/src/main/resources/config.json)


<br/>

The following types of blockchains are supported by the bridge:
* Bitcoin based blockchains
* Ethereuem based blockchains
* Waves Blockchain
