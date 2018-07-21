# MessagePack Template Library [![Build Status](https://travis-ci.org/ksgwr/MessagePackTemplate.svg?branch=master)](https://travis-ci.org/ksgwr/MessagePackTemplate)

## 概要

MessagePackTemplateを含むライブラリです。
javaの実装ではフィールド名がシリアライズされないことに注目し、
一部フィールドをMap<String,Object>としてフィールド名をシリアライズするためのObjectTemplateと
普通のオブジェクトをフィールド名を含めてシリアライズするためのMapObjectTemplateを用意してあります


## サンプル

各テストケースを参照してください。


## ライセンス

Apache License, Version 2.0


## 使い方

pom.xmlに以下を記述

```
<repositories>
  <repository>
    <id>ksgwr-repo</id>
    <url>http://ksgwr.github.io/mvn-repo/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>jp.ksgwr</groupId>
    <artifactId>msgpack-template</artifactId>
    <version>0.0.1</version>
  </dependency>
</dependencies>
```
