# State Based Pattern Implementation

## Organization Service

### Only Setup
``` sh
sudo chmod +x ./init.sh ./down.sh 
```

起動
``` sh
./init.sh
```

停止
``` sh
./down.sh
```

### Environment
- CUSTOM_SECRET: 適当な文字列

### Prerequire
- [asdf](./setup_asdf.md)

### Migrationファイル生成
``` sh
./gradlew generateMigrationFile -PmigrationName={ファイル内容} -Pdir={相対ディレクトリ}
```

具体例
``` sh
./gradlew generateMigrationFile -PmigrationName=create_user_profiles_table -Pdir=ddl
```

### Setup
#### コマンドセットアップ
``` sh
asdf plugin add grpcurl
asdf plugin add kafka
asdf install
```

## Test
``` sh
grpcurl --plaintext -d '{"user_profile_id": "0820a8820081410efd3e1b0305045141"}' localhost:9080 userprofile.v1.UserProfileService/FindUserProfile
```

```sh
grpcurl --plaintext -d '{"user_profile_id": "user:12345"}' localhost:9080 userprofile.v1.UserProfileService/FindUserProfileByUserId
```

### App
- Gatewayからの受付けは常に`gRPC`を利用した`handler`パッケージで行う.
- Eventのサブスクライバ定義は, `event/subscriber`がエントリポイントとなり, 実際のコンシューマは`event/handler`, イベントクラスは`event/model`で定義.
- `handler`や`event/consumer`のロジック部分は`service`が担当する.ただし, `service`は`entity`で定義されたデータをもとに動作するので, 各自`mapper`での変換を行う必要がある.
- `service`は`EventPublisher`や`repository`パッケージ, `SagaManager`などを保持しており, クエリであれば, `Repository`を利用した応答, コマンドであれば, 必要に応じて`Event`の
  パブリッシュとSagaState作成後, `Saga`オーケストラを生成.
- `SimpleSaga`を実装して, オーケストレータの振る舞いを定義したものを`saga/model`パッケージに格納.
- コマンドの定義は`saga/command`, コマンドのエンドポイントを生成するプロキシは, `saga/proxy`, sagaの状態の保持と応答によって決定するコマンド作成などを担当する`SagaState`は`saga/state`に格納.
- 実装したSaga定義をコンストラクタに取る`SagaManagerImpl`をBean定義しておくことで, `service`内の`SagaManager`からの`Saga`生成時に呼ばれることになる.
- 自身の処理だけは`local`で行うのではなく, 他と同様にParticipantとして参加する.
- saga参加者としての振る舞いは, `saga/handler`に格納し, これが`service`を呼び出すことになるため
- 作成した`SagaCommandHandler`は`saga/dispatcher`パッケージにて, `SagaCommandDispatcher`にBean登録を行う.