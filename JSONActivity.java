package com.example.studypart2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONActivity extends AppCompatActivity {
    private TextView txt1, txt2;
    private ListView listView;
    private Button parse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json);

        txt1 = findViewById(R.id.txt1);
        txt2 = findViewById(R.id.txt2);
        listView = findViewById(R.id.list_view);

        parse = findViewById(R.id.parse);
        parse.setOnClickListener(view -> {
            parseByJSONObject();
            //         parseByGSON();
        });
    }

    public void parseByGSON() {
        //1.添加依赖
        //2.实例化一个GSON对象(工具对象)
        final Gson gson = new Gson();
        //3.toJson:将对象变为json字符串
        Book b = new Book("imooc讲义", "P", "imooc开发宝典，你值得拥有");
        String str = gson.toJson(b);
        Log.e("TAG", str);

        //4.fromJson
        Book b2 = gson.fromJson(str, Book.class);
        Log.e("TAG", b2 + "====");
        Log.e("TAG", "标题：" + b2.getTitle() + ",内容：" + b2.getContent());

        new Thread() {
            @Override
            public void run() {
                super.run();
                String msg = get();
                Test t = gson.fromJson(msg, Test.class);
                Log.e("TAG", t + "===");
                Log.e("TAG", t.getStatus() + "---" + t.getMsg() + "---" + t.getData().getContent());
            }
        }.start();
    }

    public void parseByJSONObject() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String str = get();
                //解析
                //JSONObject ：json对象   JSONArray：json数组
                try {
                    //参数：满足json格式要求的字符串
                    JSONObject jo = new JSONObject(str);
                    //getJSONObject(String name)
                    int status = jo.getInt("status");
                    final String msg = jo.getString("msg");
                    Log.e("TAG", status + msg + "===");

//                    JSONObject data = jo.getJSONObject("data");
//                    final String title = data.getString("title");

                    //为ListView准备数据源
                    List<Map<String, String>> list = new ArrayList<>();
                    //=====================
                    //解析JSON数组
                    JSONArray ary = jo.getJSONArray("data");
                    //遍历数组
                    for (int i = 0; i < ary.length(); i++) {
                        //取出对应索引上的JSON对象
                        JSONObject obj = ary.getJSONObject(i);
                        String name = obj.getString("name");
                        String id = obj.getString("id");
                        Log.e("TAG", "id=" + id + ",name=" + name);

                        Map<String, String> map = new HashMap<>();
                        map.put("name", name);
                        map.put("id", id);

                        list.add(map);
                    }

                    //=====================

                    //创建SimpleAdapter(数据源 List<Map<String,Object>> , 布局资源  R.layout.item , from,to)
                    String[] from = {"name", "id"};
                    int[] to = {R.id.item_name, R.id.item_id};

                    final SimpleAdapter adapter = new SimpleAdapter(JSONActivity.this, list, R.layout.item, from, to);

                    //显示到界面上
                    //此方法在子线程中调用，可以在内部处理界面的显示问题
                    //因为它相当于在此刻将操作权由子线程移交给了主线程
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            txt1.setText(msg);
//                            txt2.setText(title);

                            listView.setAdapter(adapter);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    private String get() {
        try {
            //HttpURLConnection
            //1.实例化一个URL对象
//            URL url = new URL("http://www.imooc.com/api/teacher?type=2&cid=1");
            URL url = new URL("http://www.imooc.com/api/teacher?type=3&cid=1");

            //2.获取HttpURLConnection实例
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //3.设置和请求相关的属性
            //请求方式
            conn.setRequestMethod("GET");
            //请求超时时长
            conn.setConnectTimeout(6000);

            //4.获取响应码       200:成功   404：未请求到指定资源  500：服务器异常
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                //5.判断响应码并获取响应数据(响应的正文)
                //获取响应的流
                InputStream in = conn.getInputStream();
                byte[] b = new byte[1024];
                int len = 0;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //在循环中读取输入流
                // in.read(b);   // 该方法返回值是int类型数据，代表的是实际读到的数据长度
                while ((len = in.read(b)) > -1) {
                    //将字节数组里面的内容存/写入缓存流
                    //参数1：待写入的数组
                    //参数2：起点
                    //参数3：长度
                    baos.write(b, 0, len);
                }

                String msg = new String(baos.toByteArray());
                Log.e("TAG", msg + "========");
                return msg;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
