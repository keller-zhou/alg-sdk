package com.slicejobs.algsdk.algtasklibrary.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;


public class DialogUtils {

    /**
     * 适合简单提示
     * @param msgTitle
     * @param msg
     * @param posiText
     */
    public static void showHintDialog(Context context, DialogDefineClick dialogDefineClick, String msgTitle, String msg, String posiText, boolean isCancellab) {
        AlertDialog.Builder builer = new  AlertDialog.Builder(context, R.style.Dialog_Fullscreen);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.dialog_base_hint, null);
        TextView tvTitle = (TextView)view.findViewById(R.id.dialog_title);
        tvTitle.setText(msgTitle);
        TextView tvMsg = (TextView)view.findViewById(R.id.dialog_msg);
        tvMsg.setText(msg);
        TextView tvBt = (TextView)view.findViewById(R.id.dialog_bt_hint);
        tvBt.setText(posiText);
        //builer.setView(view);
        builer.setCancelable(isCancellab);
        AlertDialog dialog = builer.create();
        dialog.show();
        tvBt.setOnClickListener(v -> {
            dialog.dismiss();
            if (dialogDefineClick != null) {
                dialogDefineClick.defineClick();
            }
        });
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setContentView(view);
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        //p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3，根据实际情况调整
        p.width = (int) (DensityUtil.screenWidthInPix(context) * 0.73); // 宽度设置为屏幕的0.7，根据实际情况调整
        dialogWindow.setAttributes(p);
    }

    /**
     *适合有确定按钮的提示对话框
     * @param msgTitle
     * @param msg
     * @param
     */
    public static void showHintDialog(Context context, DialogClickLinear linear, String msgTitle, String msg, String cancelText, String defineText, boolean isCancellab) {

        AlertDialog.Builder builer = new  AlertDialog.Builder(context,R.style.Dialog_Fullscreen);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.dialog_base_hint, null);
        TextView tvTitle = (TextView)view.findViewById(R.id.dialog_title);
        tvTitle.setText(msgTitle);
        TextView tvMsg = (TextView)view.findViewById(R.id.dialog_msg);
        tvMsg.setText(msg);
        TextView tvBt = (TextView)view.findViewById(R.id.dialog_bt_hint);
        tvBt.setVisibility(View.GONE);
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.dialog_bt_layout);
        linearLayout.setVisibility(View.VISIBLE);
        Button btCancel = (Button) view.findViewById(R.id.dialog_cancel);
        btCancel.setText(cancelText);
        Button btDefine = (Button) view.findViewById(R.id.dialog_define);
        btDefine.setText(defineText);
        builer.setCancelable(isCancellab);
        //builer.setView(view);
        AlertDialog dialog = builer.create();
        dialog.show();
        btCancel.setOnClickListener(v -> {//点击取消按钮,调用接口
            dialog.dismiss();
            if (linear != null) {
                linear.cancelClick();
            }
        });
        btDefine.setOnClickListener(v -> {//点击确定按钮,调用接口
            dialog.dismiss();
            if (linear != null) {
                linear.defineClick();
            }
        });
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setContentView(view);
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        //p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3，根据实际情况调整
        p.width = (int) (DensityUtil.screenWidthInPix(context) * 0.73); // 宽度设置为屏幕的0.7，根据实际情况调整
        dialogWindow.setAttributes(p);
    }

    public interface  DialogClickLinear {

        public void cancelClick();

        public void defineClick();

    }

    public interface  DialogDefineClick {
        public void defineClick();
    }
}
