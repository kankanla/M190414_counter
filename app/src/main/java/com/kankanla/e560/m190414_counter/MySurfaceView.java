package com.kankanla.e560.m190414_counter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private final String T = "###  MySurfaceView --";
    private Context context;
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    protected Thread reThred;                    //リフレシュスレット
    private boolean re_flag;                    //スレットチャック
    protected final int re_speed = 50;            //リフレットスピート
    public static int scre_X, scre_Y;                 //画面のサイズ、高さ、幅

    private MySurfaceView.CallBack callBack;        // 広告表示、未表示コントロール用
    protected HandlerThread handlerThreadM;        //
    private Handler handlerM;                   //
    protected Handler.Callback handlercallbackM;  //
    protected Message messageM;                  //

    private Bitmap bitmap_Window;               //カウント窓の枠Bitmap
    private Bitmap bitmap_digital;              //数字ビットマップ
    private Paint digitalPaint;                        //ペン
    public static Bitmap[] bitmapsICON;         //アイコンビットマープ
    protected List<Point>[] iConPoint;          //アイコン位置記録
    protected List<Integer>[] iConSpeed;        //アイコン移動速度の記録
    private int iConSpeedOffset = 0;            //モニターサイズによって、速度のオフセット
    public static int[] iconid;                    //アイコンR.ID のリスト

    private Matrix[] matrixS;                       //カウント窓
    private RectF[] rectFS;                         //窓の四角
    private float contH;                          // 四つの窓の高さ； x/4;
    private final int contS = 5;                          //窓の数
    private final int contNo = 12;                        //文字数(デジダルビットマックの数字の数）
    protected final int contSS = 12;                         //カウンタ最大の位数
    private int[] counters;                            //カウンター記録

    private Rect[] rectsNo;                       //数字の読み取りFect
    private RectF[] rectFsNo;                       //数字の表示Fect

    private final String spName = "Counter";        //記録
    public static SharedPreferences[] sharedPreferences;    //記録
    private SharedPreferences sharedPreferencesAll;        //すべてのクリック数を記録、メインActivityで消す
    private int commentSize = 30;                           //コメントの文字サイズ
    private SharedPreferences.Editor spEditor;              //カウント記録用

    private Paint paintComment = new Paint(Paint.ANTI_ALIAS_FLAG);   //コメントペン
    private Paint paintComment2 = new Paint(Paint.ANTI_ALIAS_FLAG); //コメント影のペン
    private StringBuffer CommentBuffer;

    public MySurfaceView(Context context, final MySurfaceView.CallBack callBack) {
        super(context);
        this.context = context;
        this.callBack = callBack;

        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);

        //記録
        sharedPreferencesAll = context.getSharedPreferences("allcont", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editorAll = sharedPreferencesAll.edit();
        sharedPreferences = new SharedPreferences[5];
        for (int i = 0; i < contS; i++) {
            sharedPreferences[i] = context.getSharedPreferences(spName + i, Context.MODE_PRIVATE);
        }
        init();

        handlerThreadM = new HandlerThread("newThread");
        handlercallbackM = new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    //カウンタした値を記録
                    case 22:
                        int temp = sharedPreferencesAll.getInt("allcont", 0);
                        editorAll.putInt("allcont", temp + 1);
                        editorAll.apply();
                        spEditor = sharedPreferences[msg.arg1].edit();
                        int step = sharedPreferences[msg.arg1].getInt("step", 1);
                        if (sharedPreferences[msg.arg1].getBoolean("mode", false)) {
                            spEditor.putInt("No", sharedPreferences[msg.arg1].getInt("No", 0) - step);
                        } else {
                            spEditor.putInt("No", sharedPreferences[msg.arg1].getInt("No", 0) + step);
                        }
                        spEditor.apply();
                        break;
                    case 33:
                        //スクリンに出たアイコンアイテムを削除する
                        int ii = messageM.arg2;
                        for (int j = 0; j < iConPoint[ii].size(); j++) {
                            if (iConPoint[ii].get(j).y <= -100) {
                                iConPoint[ii].remove(j);
                                iConSpeed[ii].remove(j);
                                break;
                            }
                        }
                        break;
                }
                return true;
            }
        };
        handlerThreadM.start();
        handlerM = new Handler(handlerThreadM.getLooper(), handlercallbackM);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
    }

    private void init() {
        /*初期化Paint、ビットマック読み込み*/
        digitalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmap_Window = BitmapFactory.decodeResource(getResources(), R.mipmap.path3272);    //Window背景
        bitmap_digital = BitmapFactory.decodeResource(getResources(), R.mipmap.path4661);   //数字

        bitmapsICON = new Bitmap[12];
        iConSpeed = new ArrayList[bitmapsICON.length];
        iConPoint = new ArrayList[bitmapsICON.length];

        /*http://icooon-mono.com/*/
        iconid = new int[]{R.mipmap.icon0, R.mipmap.icon1, R.mipmap.icon2, R.mipmap.icon3, R.mipmap.icon4, R.mipmap.icon5,
                R.mipmap.icon6, R.mipmap.icon7, R.mipmap.icon8, R.mipmap.icon9, R.mipmap.icon10, R.mipmap.icon11};

        for (int i = 0; i < bitmapsICON.length; i++) {
            bitmapsICON[i] = BitmapFactory.decodeResource(getResources(), iconid[i]);
            iConPoint[i] = new ArrayList<>();
            iConSpeed[i] = new ArrayList<>();
        }
    }

    /*
    プログラムの初期化
     */
    private void game_init() {
        //画面サイズ
        scre_X = this.getWidth();       //画面のサイズ
        scre_Y = this.getHeight();      //画面のサイズ
        contH = (float) (scre_Y / contS);   //窓の高さを割計算

        /*画面サイズによって、動画の速度を変更*/
        /*1440 x 2960,1440 x 2560,1080 x 2160,1080 x 1920,800 x 1280,480 x 800*/
        /*画面サイズによってアイコンの移動速とと文字のサイズを決める*/
        if (scre_Y > 700) {
            iConSpeedOffset = 0;
            commentSize = 20;
        }
        if (scre_Y > 1180) {
            iConSpeedOffset = 1;
            commentSize = 40;
        }
        if (scre_Y > 1820) {
            iConSpeedOffset = 3;
            commentSize = 60;
        }
        if (scre_Y > 2060) {
            iConSpeedOffset = 6;
            commentSize = 80;
        }
        if (scre_Y > 2460) {
            iConSpeedOffset = 9;
            commentSize = 100;
        }
        if (scre_Y > 2860) {
            iConSpeedOffset = 11;
            commentSize = 120;
        }

        //窓のマットリクスの初期化
        matrixS = new Matrix[contS];
        counters = new int[contS];
        for (int i = 0; i < contS; i++) {
            matrixS[i] = new Matrix();
            counters[i] = sharedPreferences[i].getInt("No" + i, 0);
        }

        //窓の表示位置のRectF初期化
        rectFS = new RectF[contS];
        for (int i = 0; i < contS; i++) {
            rectFS[i] = new RectF(0, 0, bitmap_Window.getWidth(), bitmap_Window.getHeight());
        }

        /*数字表示用RectFの初期化*/
        rectsNo = new Rect[contNo];
        rectFsNo = new RectF[contNo];
        int x = bitmap_digital.getWidth() / contNo;
        int y = bitmap_digital.getHeight();

        /*数字表示用RectFの初期化
        表示数字の拡大率*/
        Matrix temp = new Matrix();
        float xb = (float) scre_X / (float) bitmap_digital.getWidth();               //拡大率
        float yb = (float) scre_Y / (float) bitmap_digital.getHeight() / contS / 2;     //拡大率

        /*数字表示用RectFの初期化*/
        for (int i = 0; i < contNo; i++) {
            int left = i * x;
            int top = 0;
            int right = left + x;
            rectsNo[i] = new Rect(left, top, right, y);
            rectFsNo[i] = new RectF(left, top, right, y);
            temp.setScale(xb, yb);
            temp.mapRect(rectFsNo[i]);
        }
    }

    /*長押しの場合、カウントしません。*/
    /*upTime - downTime > 設定のタイム*/
    protected long downTime = 0, upTime = 0, conTime = 500;
    private boolean isdown;      //長押し判断
    private long isdownMillis = 0;

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Log.i(T, "KeyEvent");

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                messageM = handlerM.obtainMessage();
                messageM.what = 22;
                messageM.arg1 = rectFS.length - 2;
                handlerM.sendMessage(messageM);
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                messageM = handlerM.obtainMessage();
                messageM.what = 22;
                messageM.arg1 = rectFS.length - 1;
                handlerM.sendMessage(messageM);
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isdown = true;          //押した状態
                isdownMillis = System.currentTimeMillis();
                //長押した時、とアイテムのI 取得
                for (int i = 0; i < rectFS.length; i++) {
                    if (rectFS[i].contains(x, y)) {
                        //アイテムのIをメニューに渡す。
                        isOpenMenu(i);
                    }
                }
                downTime = System.currentTimeMillis();      //押したときのタイプ
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                isdown = false;         //押した状態の解除
                // !isMenuopen  メニュー開いてときに、カウントしない。
                // !drawStop Draw停止したときはカウントしない。
                if (!isMenuOpen) {
                    upTime = System.currentTimeMillis();           //一定時間超えの長押しはカウントしないための値
                    if (upTime - downTime < conTime) {
                        for (int i = 0; i < rectFS.length; i++) {
                            if (rectFS[i].contains(x, y)) {
                                //カウント記録
                                messageM = handlerM.obtainMessage();
                                messageM.what = 22;
                                messageM.arg1 = i;
                                handlerM.sendMessage(messageM);

                                //アイコンの開始位置とスピート設定
                                iConPoint[i].add(new Point((int) x, (int) y));
                                iConSpeed[i].add((int) (Math.random() * (0 - 3)) + 3);

                                //一定条件でAdmob表示する。
                                int temp = sharedPreferencesAll.getInt("allcont", 0);
                                // Log.i(T, temp + "    temp");
                                if (temp > 200) {
                                    callBack.showadmob();
                                    // Log.i(T, "callBack.showadmob()");
                                } else {
                                    callBack.hideadmob();
                                }
                            }
                        }
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Log.i(T, "#### surfaceCreated");
        game_init();
        re_flag = true;
        reThred = new Thread(this);
        reThred.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Log.i(T, "#### surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Log.i(T, "#### surfaceDestroyed");
        re_flag = false;
    }

    /*
       数字の表示
     */


    {
        paintComment.setAntiAlias(true);
        paintComment2.setAlpha(50);
    }

    private void digital(int no, int position, Canvas canvas) {
        int x = no;         //表示数
        int j = contNo;    //文字数(デジダルビットマックの数字の数）
        int i = contSS;     //カウンタ最大の位数

        //オフセット設定。
        float oftx = (scre_X - (rectFsNo[1].right - rectFsNo[1].left) * (contSS + 1)) / 2;
        float ofty = rectFS[position].top + (contH - (rectFsNo[1].bottom - rectFsNo[1].top)) / 2;

        CommentBuffer = new StringBuffer();
        CommentBuffer.append(sharedPreferences[position].getString("Comment", ""));
        CommentBuffer.append(",");
        if (sharedPreferences[position].getBoolean("mode", false)) {
            CommentBuffer.append("-");
        } else {
            CommentBuffer.append("+");
        }
        CommentBuffer.append(sharedPreferences[position].getInt("step", 1));
        String temp = CommentBuffer.toString();
        paintComment.setTextSize(commentSize);
        paintComment2.setTextSize(commentSize);
        canvas.drawText(temp, (scre_X / 16) * 1, ofty + (scre_Y / contS / 16 * 1), paintComment);
        canvas.drawText(temp, (scre_X / 16) * 1 + 5, ofty + (scre_Y / contS / 16 * 1 + 5), paintComment2);

        //数字の影
        while (i != 1) {
            i--;
            canvas.save();
            canvas.translate(oftx, ofty);
            canvas.drawBitmap(bitmap_digital, rectsNo[10], rectFsNo[i], digitalPaint);
            canvas.restore();
        }
        //数字
        if (x == 0) {
            j--;
            canvas.save();
            canvas.translate(oftx, ofty);
            canvas.drawBitmap(bitmap_digital, rectsNo[0], rectFsNo[j], digitalPaint);
            canvas.restore();

        } else {
            while (x != 0 && j != 0) {
                j--;
                canvas.save();
                canvas.translate(oftx, ofty);
                canvas.drawBitmap(bitmap_digital, rectsNo[x % 10], rectFsNo[j], digitalPaint);
                canvas.restore();
                x = (x - x % 10) / 10;
            }
        }
    }

    /*
    4つのカウント窓の表示
     */
    private void count4(Canvas canvas) {
        canvas.save();
        float xb = (float) scre_X / (float) bitmap_Window.getWidth();               //拡大率
        float yb = (float) scre_Y / (float) bitmap_Window.getHeight() / contS;         //拡大率

        /*
        初期化書くMatrix
        contS 窓の数
         */
        for (int i = 0; i < contS; i++) {
            matrixS[i].setScale(xb, yb);        //拡大率設定
            matrixS[i].postTranslate(0, contH * i);     //窓位置の設定、
            paintComment.setTextSize(100);
            canvas.drawBitmap(bitmap_Window, matrixS[i], digitalPaint);    //イメージの書き込み
            //四角
            rectFS[i].set(0, 0, bitmap_Window.getWidth(), bitmap_Window.getHeight());
            matrixS[i].mapRect(rectFS[i]);      //四角の再設定
        }
        canvas.restore();
    }

    //メニュー開くの中押し時間
    //メニュー開くのカウンター
    protected long sleepMillis = 1000;
    public static boolean isMenuOpen = false;
    protected int isOpenMenu_Item = 99;

    protected void isOpenMenu(final int Item) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isdown) {
                    if ((System.currentTimeMillis() - isdownMillis) > sleepMillis) {
                        isOpenMenu_Item = Item;
                        openMenu();
                    }
                }
            }
        }).start();
    }

    private void openMenu() {
        ItemDialog itemDialog = new ItemDialog();
        ItemDialog.itemNO = spName + isOpenMenu_Item;

        if (!isMenuOpen) {
            isMenuOpen = true;
            itemDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "ttt");
        }
    }

    /*
    アイコンの表示
    */

    private Matrix matrix = new Matrix();
    private Paint iConShowPaint = new Paint();
    private boolean rl = false;   //アイコンの移動方向を判断する

    {
        iConShowPaint.setAlpha(50);
        iConShowPaint.setAntiAlias(true);
    }

    /**
     * アイコンを表示する。
     *
     * @param canvas
     * @param i      カウンタアイテム
     */
    private void iConShow2(Canvas canvas, int i) {
        for (int j = 0; j < iConPoint[i].size(); j++) {
            int x = iConPoint[i].get(j).x;
            int y = iConPoint[i].get(j).y;

            /*Mode1*/
            matrix.setTranslate(x, y = y - (iConSpeed[i].get(j) + iConSpeedOffset));
            /*Mode2 途中*/

            /*アイコンの番号*/
            int temp = sharedPreferences[i].getInt("iConIndex", 0);
            canvas.drawBitmap(bitmapsICON[temp], matrix, iConShowPaint);
            if (rl) {
                if (x++ > getWidth() / 2) {
                    x += 2;
                    x += iConSpeedOffset;
                    rl = false;
                }
            } else {
                if (x-- < getWidth() / 2) {
                    x -= 2;
                    x -= iConSpeedOffset;
                    rl = true;
                }
            }

            iConPoint[i].get(j).set(x, y -= 5);
        }
    }

    /**
     * スクリーンから出たアイコンを削除する。
     * 未使用
     *
     * @param i カウンタアイテム
     */
    private void iConRemove(int i) {
        for (int j = 0; j < iConPoint[i].size(); j++) {
            if (iConPoint[i].get(j).y <= 300) {
                iConPoint[i].remove(j);
                iConSpeed[i].remove(j);
                break;
            }
        }
    }

    /**
     * 画面の書き込み
     */
    private void MyDraw() {
        try {
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);
//                count4(canvas);         /*窓の表示*/
                for (int i = 0; i < counters.length; i++) {
                    /*数字の表示*/
                    if (sharedPreferences[i].getInt("No", 0) < 0) {
                        digital(0, i, canvas);
                    } else {
                        digital(sharedPreferences[i].getInt("No", 0), i, canvas);
                    }
                    iConShow2(canvas, i);
                    //スクリンに出たアイコンアイテムを削除する
                    messageM = handlerM.obtainMessage();
                    messageM.what = 33;
                    messageM.arg2 = i;
                    handlerM.sendMessage(messageM);
                }
                count4(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log.i(T, "MyDraw() try error");
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * 画面更新
     */
    @Override
    public void run() {
        while (re_flag) {
            long start = System.currentTimeMillis();
            if (!isMenuOpen && !MainActivity.isonPause) {
                MyDraw();
            }
            long end = System.currentTimeMillis();
            if (end - start < re_speed) {
                try {
                    Thread.sleep(re_speed - (end - start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // Log.i(T, "run No Speed");
            }
        }
    }

    /**
     *
     */
    interface CallBack {
        void showadmob();

        void hideadmob();

        void onSurfaceCreated();
    }
}
