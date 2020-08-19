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


public class AttachmentCardView extends CardView implements Constants {
    private final int MARGIN=14;
    private final int ELEVATION=12;

    public AttachmentCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater!=null) {
            inflater.inflate(R.layout.view_card_attachment, this, true);
        }else{
            Log.e(LOG_TAG, "inflater is null ");
        }
        // getting the attribute values from xml
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.AttachmentCardView, 0, 0);
        String fileName = a.getString(R.styleable.AttachmentCardView_fileName);
        if (fileName!=null){
            setFileName(fileName);
        }
        // setting some default properties
        setFocusable(true);
        setClickable(true);
        setCardElevation(UIutilities.convertDpToPx(context, ELEVATION));

        // setting some margins. Oterwise elevation is not shown when inside the flex box layout
        CardView.LayoutParams params = new  CardView.LayoutParams(
                CardView.LayoutParams.WRAP_CONTENT,
                CardView.LayoutParams.WRAP_CONTENT
        );
        int px = UIutilities.convertDpToPx(context, MARGIN);
        params.setMargins(px, px, px, px);
        setLayoutParams(params);
        // setting some margins ** ends

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

    public void setSizeOrStatus(String sizeStatus){
        TextView title = (TextView) findViewById(R.id.view_attachment_file_size);
        title.setText(sizeStatus);
    }

    private String getFileExtension(String filename) {
        if (filename!=null) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        }
        return "";
    }

}
