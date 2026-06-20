# モジュール構成
以下のようにする。

- `compiler-plugin`: KCP実装
- `gradle-plugin`: `compiler-plugin`をプラグインとして動作させるためのモジュール
- `runtime-api`: アノテーションや生成コードが依存する基底クラス等、実行時に必要な依存
- `integration-test`: プラグインのカスタム実装等のテストのためのモジュール。検証内容毎に適切に子モジュールを作成する
