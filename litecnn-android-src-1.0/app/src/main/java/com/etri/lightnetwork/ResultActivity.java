package com.etri.lightnetwork;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.etri.lightnetwork.tensorflow.Classifier;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResultActivity extends AppCompatActivity {
    ImageCache imageCache;
    @BindView(R.id.iv_resultImage)
    ImageView resultImage;
    @BindViews({R.id.tv_result1, R.id.tv_result2, R.id.tv_result3, R.id.tv_result4, R.id.tv_result5})
    TextView[] tvResults;
    @BindView(R.id.tv_performance)
    TextView tvPerformance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);
        imageCache = ImageCache.getInstance();
        resultImage.setImageBitmap(imageCache.getBitmap());

        setResult();
    }

    @OnClick(R.id.btn_back) void onClickBack() {
        finish();
    }

    private void setResult() {
        List<Classifier.Recognition> results = imageCache.getResult();
        int resultSize = results.size();

        tvPerformance.setText("Performance "+imageCache.getTime()+" ms");

        if(resultSize == 0) {
            tvResults[0].setText("Unknown");
        }

        for(int i=0; i < 5; i++) {
            if(i < resultSize) {
                tvResults[i].setText(i+". "+results.get(i).getTitle() + " ("+results.get(i).getConfidence()+")");
            }else{
                tvResults[i].setText("");
            }
        }
    }
}
