package com.sathesh.corporatemail.customui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.ui.util.UIutilities;


public class AttachmentsCardView extends CardView implements Constants {
    private final int MARGIN=14;
    private final int ELEVATION=12;

    public AttachmentsCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater!=null) {
            inflater.inflate(R.layout.view_card_attachment, this, true);
        }else{
            Log.e(LOG_TAG, "inflater is null ");
        }
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.AttachmentsCardView, 0, 0);
        String titleText = a.getString(R.styleable.AttachmentsCardView_fileName);

        setFileName(titleText);
        setFocusable(true);
        setClickable(true);
        setCardElevation(UIutilities.convertDpToPx(context, ELEVATION));

        /*FrameLayout.LayoutParams params= new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(UIutilities.convertDpToPx(context, MARGIN),
                UIutilities.convertDpToPx(context, MARGIN),
                        UIutilities.convertDpToPx(context, MARGIN),
                                UIutilities.convertDpToPx(context, MARGIN));
        setLayoutParams(params);*/
        a.recycle();
    }

    public void setFileName(String fileName){
        TextView title = (TextView) findViewById(R.id.view_card_attachment_file_name);
        title.setText(fileName);
        updateImageIcon(getFileExtension(fileName));
    }

    public void setIcon(Drawable icon) {
        ImageView iconView = findViewById(R.id.view_card_attachment_icon);
        iconView.setImageDrawable(icon);
    }

    private void updateImageIcon(String fileExtension) {
        if (fileExtension.equalsIgnoreCase("pdf")) {
            setIcon(getResources().getDrawable(R.drawable.attachment_pdf));
        } else if (fileExtension.equalsIgnoreCase("ppt") || fileExtension.equalsIgnoreCase("pptx")) {
            setIcon(getResources().getDrawable(R.drawable.attachment_ppt));
        }else if (fileExtension.equalsIgnoreCase("gif")) {
            setIcon(getResources().getDrawable(R.drawable.attachment_gif));
        }else if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")) {
            setIcon(getResources().getDrawable(R.drawable.attachment_jpg));
        }else if (fileExtension.equalsIgnoreCase("mp3") || fileExtension.equalsIgnoreCase("aac") || fileExtension.equalsIgnoreCase("wav")) {
            setIcon(getResources().getDrawable(R.drawable.attachment_mp3));
        }else if (fileExtension.equalsIgnoreCase("mpg") || fileExtension.equalsIgnoreCase("mpeg") || fileExtension.equalsIgnoreCase("wmv")|| fileExtension.equalsIgnoreCase("3gp")) {
            setIcon(getResources().getDrawable(R.drawable.attachment_mpg));
        }else if (fileExtension.equalsIgnoreCase("txt")) {
            setIcon(getResources().getDrawable(R.drawable.attachment_txt));
        }else if (fileExtension.equalsIgnoreCase("xls") || fileExtension.equalsIgnoreCase("xlsx")) {
            setIcon(getResources().getDrawable(R.drawable.attachment_xls));
        }else if (fileExtension.equalsIgnoreCase("exe") || fileExtension.equalsIgnoreCase("apk") || fileExtension.equalsIgnoreCase("dmg") || fileExtension.equalsIgnoreCase("pkg")
                || fileExtension.equalsIgnoreCase("sh")) {
            setIcon(getResources().getDrawable(R.drawable.attachment_warn));
        }else {
            setIcon(getResources().getDrawable(R.drawable.attachment_attach));
        }
    }

    public void setSizeStatus(String sizeStatus){
        TextView title = (TextView) findViewById(R.id.view_attachment_file_size);
        title.setText(sizeStatus);
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

}
