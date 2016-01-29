package com.example.baidu.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.baidu.myapplication.jsbridge.BridgeHandler;
import com.example.baidu.myapplication.jsbridge.BridgeWebView;
import com.example.baidu.myapplication.jsbridge.CallBackFunction;
import com.example.baidu.myapplication.jsbridge.DefaultHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    private BridgeWebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (BridgeWebView) findViewById(R.id.webview);
        mWebView.setDefaultHandler(new DefaultHandler());
        mWebView.loadUrl("http://wb145230.github.io/");

        mWebView.registerHandler("testObjcCallback", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e("wb", data);

                try {
                    JSONObject json = new JSONObject(data);
                    String imageUrl = json.getString("url");
                    loadImage(imageUrl);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 加载图片
     * @param imageUrl
     */
    private void loadImage(final String imageUrl) {
        Glide.with(MainActivity.this).load(imageUrl).into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource,
                                        GlideAnimation<? super GlideDrawable> glideAnimation) {

                Bitmap bitmap = drawable2Bitmap(resource);
                if (bitmap != null) {

                    JSONObject json = new JSONObject();
                    try {
                        json.put("imageUrl", imageUrl);
                        json.put("imagePaths", "data:image/png;base64," + getImgStr(bitmap));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // 把相关的图片信息以json格式传递给H5,方便H5解析
                    mWebView.send(json.toString());

                }
            }
        });
    }

    // Drawable转换成Bitmap
    private Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ?
                        Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    //将Bitmap转换成Base64
    private String getImgStr(Bitmap bit) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 100, bos);//参数100表示不压缩
        byte[] bytes = bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

}
