# 制作物の手引き
#### M210786 小林翔

ここでは、同ディレクトリ内のobnizを利用したBLEのプログラムについて説明します。
4つのプログラムについて、それぞれの動作を説明してあるので参考にしてください。
なお、obnizのSDKやメソッドについてはobnizのドキュメントを参照してください。

* BLE_congestion

    論文内の一つ目の実装例である混雑状況把握のプログラムです。ここでは2つのobnizを操作し、一方をアドバタイズパケットを通じて周囲に情報を発信するペリフェラル、もう一方を情報を取得するセントラルとして利用しています。ユーザーは、このパケットを取得することで混雑状況を把握することができます。

    * 12~13:操作するobnizの設定。
    * 20:sleep関数。javascriptにはsleep関数が標準で用意されていないため、自分で作成している。
    * 22~28:混雑状況やそれに付随する情報の保存場所である配列の宣言と初期化。
    * 30~32:10進数から16進数へ変換する関数。
    * 34~69:別デバイスが取得した混雑状況をペリフェラルとして周囲に発信するプログラム。37~45行でBLE通信に必要な情報を設定している。その後47~68行でループを行い、一定時間パケットの発信、発信停止、情報の更新の順で動作する。情報は既に宣言してある配列から取得する。
    * 71~113:混雑状況を取得するセントラル役のobnizのプログラム。対象となるデバイス(ユーザーのスマートフォンなど)が共通の名前を持っていることを前提としており、一定時間スキャンをおこなって周囲にあるデバイス数を取得する。スキャン終了後予め宣言してあった配列の方に必要は情報を保存する。その後一定時間sleepしまたスキャンを再度実施する。

* blecentral_experiment1

    評価実験用のプログラムです。ここではセントラルがペリフェラルを見つけるまでの時間における距離の影響を測っています。本プログラムはobnizをセントラルとして利用しており、一定時間(目標のペリフェラルが見つけられるよう余裕のある時間)スキャンし続けます。ここでは目標の名前を"target"で指定することで他の関係のないペリフェラルをスキャン結果から除外しています。また、スキャン開始時と対象発見時に時間を計測しており、この結果の差から目標であった時間を導出しています。

* blecentral_experiment2

    評価実験用のプログラムです。ここではセントラルにおけるペリフェラルのパケット発信時間の差によるペリフェラルの見つけやすさを測っています。本プログラムはobnizをセントラルとして利用しており、一定時間(目標のペリフェラルが見つけられるよう余裕のある時間)スキャンし続けます。ここでは目標の名前を"target"で指定することで他の関係のないペリフェラルをスキャン結果から除外しています。また、スキャン開始時と対象発見時に時間を計測しており、この結果の差から目標であった時間を導出しています。

* Peripheral Collect

    論文内の二つ目の実装例であるスタンプラリーのプログラムです。ここではobnizをペリフェラルとして利用しており、ユーザーデバイス(セントラル)からのアクセス、操作でアクションを行います。

    * 14~23:スタンプラリー内のペリフェラル名と紐づけられた番号の集合。まだ訪れていない場所などの導出に利用。
    * 24~27:必要な定数の宣言。
    * 30~33:10進数を16進数に変換するメソッド。
    * 35~59:obnizにおけるBLEの設定。デバイス名やサービス、キャラクタリスティック、そのプロパティなど必要な事項について宣言や設定を行なっている。
    * 61~105:セントラルからキャラスタリスティックに値が書き込まれた時のアクションをおこなっている。書き込まれる値のフォーマットは共通できていれば自由に設定が可能。"newvalue"に値が書き込まれているため、ここから読み出す。ここでは書き込まれる値の種類に応じてラベルがついているため、ラベルに応じた処理が行われる。
    * 107~112:アドバタイズパケットに必要な情報を設定している。