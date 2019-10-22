package com.example.tg.scout.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.example.tg.scout.MainActivity;
import com.example.tg.scout.R;


public class WebFragment extends Fragment implements View.OnClickListener{

    private WebView webView;
    private Button backButton, indexButton;
    private MainActivity activity;
    private String webUrl = "http://119.23.8.24/front/#/table";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //activity = (MainActivity) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, null);

        webView = view.findViewById(R.id.webView);
        backButton = view.findViewById(R.id.webBack);
        indexButton = view.findViewById(R.id.webIndex);

        initWebView();

        backButton.setOnClickListener(this);
        indexButton.setOnClickListener(this);

        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    private void initWebView(){
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setInitialScale(140);
        webView.getSettings().setSupportZoom(true);
        //wvResult.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 在APP内部打开链接，不要调用系统浏览器
                view.loadUrl(url);
                return true;
            }
        });

        webView.loadUrl(webUrl);
    }




    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.webBack) {
            if (webView.canGoBack())
                webView.goBack();
        } else if(view.getId() == R.id.webIndex) {
            webView.loadUrl(webUrl);
        }
    }

}
