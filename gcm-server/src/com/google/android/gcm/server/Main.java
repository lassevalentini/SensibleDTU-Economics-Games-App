
package com.google.android.gcm.server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Sender sender = new Sender("AIzaSyDt66UAtAQ4DowgMSJ3ZaH3-XI1VaF7msk");
        Message.Builder b = new Message.Builder();
        b.addData("title", "New economics game");
        b.addData("timestamp", ""+(System.currentTimeMillis()/1000));
        b.addData("body", "Win stuff!");
        b.addData("type", "economics-game-init");
        
        b.addData("game-id", "321");
        b.addData("game-type", "game-pgg");
        b.addData("game-started", "timestamp");
        b.addData("game-participants", "4");
        String regid = "APA91bH2I17tM87IBby4xVTe2yOzL13e7BJU91gkRyEfaw8voj_D_o4b7HkSH1qAk-FH1G7OC3oNoq1W0DwO4EksZAiX27ufjx6EYYWj8kxkyXXAEM7kkziOhOcxc_XT3ZpDha5q-AXsFF5DXSzcb_d6f_ckPjaGkfsriXCBfg1tUUjiSIQ2EJ8";
        // String regid = "APA91bEE22wgdY_DWNC2mG9Y3IYwxSLCmb4hs2TZNxVGd1mEwd_76BxWCs_Uf3OFqM7M254CHjFG6r2x0MLHLEnoArVHsj-XlbPyiLEHrhB9jABRpAoV8QXHjwPP4MXD0HpGYk6mjY_3gQR0TAlpOl9FSRPtlWF91xvPBoVltkjj2ZNvYJbYdVk";

        try {
            sender.send(b.build(), regid, 1);
        } catch (IOException e) {}
    }
}
