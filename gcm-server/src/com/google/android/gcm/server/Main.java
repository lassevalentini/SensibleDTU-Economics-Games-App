
package com.google.android.gcm.server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Sender sender = new Sender("AIzaSyDt66UAtAQ4DowgMSJ3ZaH3-XI1VaF7msk");
        Message.Builder b = new Message.Builder();
        b.addData("title", "New economics game");
        b.addData("timestamp", ""+(System.currentTimeMillis()/1000));
        b.addData("body", "Win stuff!");
        b.addData("type", "economics-pgg-init");
        try {
            sender.send(b.build(), "APA91bEE22wgdY_DWNC2mG9Y3IYwxSLCmb4hs2TZNxVGd1mEwd_76BxWCs_Uf3OFqM7M254CHjFG6r2x0MLHLEnoArVHsj-XlbPyiLEHrhB9jABRpAoV8QXHjwPP4MXD0HpGYk6mjY_3gQR0TAlpOl9FSRPtlWF91xvPBoVltkjj2ZNvYJbYdVk", 1);
        } catch (IOException e) {}
    }
}
