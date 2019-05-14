package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Util.HttpCallbackListener;
import com.example.myapplication.Util.HttpUtil;
import com.example.myapplication.Util.Utility;
import com.example.myapplication.db.City;
import com.example.myapplication.db.County;
import com.example.myapplication.db.Province;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private EditText editText;
//    private ImageView imageView;
//    private ProgressBar progressBar;
//    private WebView webView;

    private Button back_button;
    private Button button_send_request;
    private TextView title_text;
    private ProgressDialog progressDialog;
    private TextView show_message;
    private TextView responseText;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    private String[] data = {"Apple", "Banana", "Orange", "Watermelon",
        "Pear", "Grape", "Pineapple", "Strawberry", "Cherry", "Mango",
        "Apple", "Banana", "Orange", "Watermelon", "Pear", "Grape",
        "Pineapple", "Strawberry", "Cherry", "Mango"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "hi", Toast.LENGTH_SHORT).show();
                queryProvinces();

                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, data);
                adapter = new ArrayAdapter<String>(
                        MainActivity.this, android.R.layout.simple_list_item_1, dataList);
                ListView listView = (ListView) findViewById(R.id.list_view);
                listView.setAdapter(adapter);
            }
        });

        back_button = (Button) findViewById(R.id.back_button);
        button_send_request = (Button) findViewById(R.id.button_send_request);
        title_text = (TextView) findViewById(R.id.title_text);
        show_message = (TextView) findViewById(R.id.show_message);


        responseText = (TextView) findViewById(R.id.response_text);

        listView = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });

        back_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });

        button_send_request.setOnClickListener(this);
        //queryProvinces();



    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.button:
                String inputText = editText.getText().toString();
                Toast.makeText(MainActivity.this, inputText, Toast.LENGTH_SHORT).show();
                break;

            case R.id.button_dialog:
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("This is Dialog");
                dialog.setMessage("Something important.");
                dialog.setCancelable(false);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
                break;

            case R.id.button_send_request:
                //sendRequestWithHttpURLConnection();
                //sendRequestWithOkHttp();
                //HttpUtil.sendHttpRequest("http://192.168.1.110/SmartConnect/public/index/show_message", new HttpCallbackListener() {
//                HttpUtil.sendHttpRequest("http://guolin.tech/api/weather?cityid=CN101091001&key=a27f737673af451ab27c128ec2f99eeb", new HttpCallbackListener() {
//                    @Override
//                    public void onFinish(String response) {
//                        showResponse(response.toString());
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        e.printStackTrace();
//                    }
//                });
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void sendRequestWithHttpURLConnection(){
        // 开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("http://192.168.1.110/SmartConnect/public/index/show_message");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
//                    connection.setConnectTimeout(8000);
//                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    // 下面对获取到的输入流进行读取
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    showResponse(response.toString());
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    if(reader != null){
                        try{
                            reader.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if(connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void showResponse(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                //responseText.setText(response);
                show_message.setText(response);
            }
        });
    }

    private void sendRequestWithOkHttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    //Request request = new Request.Builder().url("http://www.baidu.com").build();
                    Request request = new Request.Builder().url("http://192.168.1.110/SmartConnect/public/index/show_message").build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    showResponse(responseData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        title_text.setText("中国");
        back_button.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }

            //return dataList;
            //adapter.notifyDataSetChanged();

            //currentLevel = LEVEL_PROVINCE;

//            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, dataList);
//            //ListView listView = (ListView) findViewById(R.id.list_view);
//            listView.setAdapter(adapter);
            //listView.setSelection(0);
        } else {
            //String address = "http://guolin.tech/api/china";
            String address = "http://192.168.1.110/test_android/public/index/show_weather";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities() {
        title_text.setText(selectedProvince.getProvinceName());
        back_button.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0) {
            dataList.clear();
            for(City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties() {
        title_text.setText(selectedCity.getCityName());
        back_button.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size() > 0) {
            dataList.clear();
            for(County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                String responseText = response;
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.hanleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if(result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }

            }

            @Override
            public void onError(Exception e) {

                // 通过runOnUiThread() 方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(MainActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
