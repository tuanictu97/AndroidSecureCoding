/*
 * Copyright (C) 2012-2014 Japan Smartphone Security Association
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jssec.android.activity.inhouseuser;

import org.jssec.android.shared.PkgCert;
import org.jssec.android.shared.SigPerm;
import org.jssec.android.shared.Utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class InhouseUserActivity extends Activity {

    // 利用先のActivity情報
    private static final String TARGET_PACKAGE =  "org.jssec.android.activity.inhouseactivity";
    private static final String TARGET_ACTIVITY = "org.jssec.android.activity.inhouseactivity.InhouseActivity";

    // 自社のSignature Permission
    private static final String MY_PERMISSION = "org.jssec.android.activity.inhouseactivity.MY_PERMISSION";

    // 自社の証明書のハッシュ値
    private static String sMyCertHash = null;
    private static String myCertHash(Context context) {
        if (sMyCertHash == null) {
            if (Utils.isDebuggable(context)) {
                // debug.keystoreの"androiddebugkey"の証明書ハッシュ値
                sMyCertHash = "0EFB7236 328348A9 89718BAD DF57F544 D5CCB4AE B9DB34BC 1E29DD26 F77C8255";
            } else {
                // keystoreの"my company key"の証明書ハッシュ値
                sMyCertHash = "D397D343 A5CBC10F 4EDDEB7C A10062DE 5690984F 1FB9E88B D7B3A7C2 42E142CA";
            }
        }
        return sMyCertHash;
    }

    private static final int REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void onUseActivityClick(View view) {

        // ★ポイント11★ 独自定義Signature Permissionが自社アプリにより定義されていることを確認する
        if (!SigPerm.test(this, MY_PERMISSION, myCertHash(this))) {
            Toast.makeText(this, "独自定義Signature Permissionが自社アプリにより定義されていない。", Toast.LENGTH_LONG).show();
            return;
        }

        // ★ポイント12★ 利用先アプリの証明書が自社の証明書であることを確認する
        if (!PkgCert.test(this, TARGET_PACKAGE, myCertHash(this))) {
            Toast.makeText(this, "利用先アプリは自社アプリではない。", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Intent intent = new Intent();

            // ★ポイント13★ 利用先アプリは自社アプリであるから、センシティブな情報をputExtra()を使う場合に限り送信してもよい
            intent.putExtra("PARAM", "センシティブな情報");

            // ★ポイント14★ 明示的Intentにより自社限定Activityを呼び出す
            intent.setClassName(TARGET_PACKAGE, TARGET_ACTIVITY);
            startActivityForResult(intent, REQUEST_CODE);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(this, "利用先Activityが見つからない。", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
        case REQUEST_CODE:
            String result = data.getStringExtra("RESULT");

            // ★ポイント15★ 自社アプリからの結果情報であっても、受信Intentの安全性を確認する
            // サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
            Toast.makeText(this, String.format("結果「%s」を受け取った。", result), Toast.LENGTH_LONG).show();
            break;
        }
    }
}