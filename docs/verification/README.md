# 検証手順

`Kotlin Compiler Plugin`（KCP）のIRフェーズで生成・差し替えを行う際、インクリメンタルコンパイルによる処理スキップで発生する不具合と、その回避策を検証する手順を示す。

## 仕組みの概要

- 共通: プラグインはFIRフェーズで`${group}.aics.GeneratedContext`（`internal object`）を生成し、IRフェーズで`targetCall()`呼び出し毎に「呼び出し元FQN」を返すアクセサ関数を`GeneratedContext`へ蓄積生成し、呼び出しをそのアクセサ呼び出しへ置き換える。
- 従来版（`legacy-sample`）: `targetCall`を`runtime-api`の通常関数として参照し、プラグインのソース生成を無効化する（`aics { generateTargetFunction = false }`）。`ClassA`のインターフェースが変化しないため、`ClassB`追加時に`ClassA`はインクリメンタルコンパイルでスキップされ、再生成された`GeneratedContext`から`ClassA`用アクセサが欠落し、実行時に`NoSuchMethodError`となる。
- 改善版（`improved-sample`）: `targetCall`をビルド毎にランダムな`marker`を持つ`internal inline`関数として生成する。`inline`関数の本体は呼び出し側のABIに含まれるため、再生成により全呼び出し元（`ClassA`含む）がダーティ扱いとなり再コンパイルされ、`GeneratedContext`に全アクセサが揃うため不具合が起きない。

## 前提条件

- JDK 17以上
- Gradle（付属の`gradlew`を使用）

## 検証1: 従来版（legacy-sample）の正常動作確認

```powershell
cd integration-test\legacy-sample
..\..\gradlew.bat clean test
```

**期待結果**: 2テストがパスする。

## 検証2: 改善版（improved-sample）の正常動作確認

```powershell
cd integration-test\improved-sample
..\..\gradlew.bat clean test
```

**期待結果**: 2テストがパスする。

## 検証3: 従来版のインクリメンタルコンパイル不具合の再現

`ClassB`を一旦隠して`ClassA`のみビルド→`ClassA`を変更せず`ClassB`を復元してテスト、という操作を自動化する。

```powershell
cd integration-test\legacy-sample
powershell -ExecutionPolicy Bypass -File ..\..\docs\verification\verify-legacy.ps1
```

**期待結果**: `ClassA`のテストが`NoSuchMethodError`で失敗し、`ClassB`のテストはパスする。
`EXPECTED: Incremental compilation issue reproduced ...` と表示され、終了コードは`0`（＝再現成功）。

## 検証4: 改善版でのインクリメンタルコンパイル不具合の回避確認

```powershell
cd integration-test\improved-sample
powershell -ExecutionPolicy Bypass -File ..\..\docs\verification\verify-improved.ps1
```

**期待結果**: `marker`がビルド毎に変化し`ClassA`も再コンパイルされるため、両テストがパスする。
`SUCCESS: Both tests passed ...` と表示され、終了コードは`0`（＝回避成功）。

> 各スクリプトは内部で`clean`を実行し、`ClassB.kt`の退避・復元も自動で行う（途中で失敗しても`finally`で復元する）。
> 判定はJUnitの`build/test-results/test/*.xml`を解析して行い、結果は終了コードでも返す。
