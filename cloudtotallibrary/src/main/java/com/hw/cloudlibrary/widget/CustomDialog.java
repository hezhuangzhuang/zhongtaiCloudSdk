package com.hw.cloudlibrary.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hw.cloudlibrary.R;
import com.hw.cloudlibrary.widget.dialog.BaseDialog;
import com.hw.cloudlibrary.widget.dialog.OnDialogClickListener;

public class CustomDialog extends BaseDialog {
    private TextView title;
    private EditText etAccessCode;
    private Button btCancle;
    private Button btConfirm;

    //释放主席
    private boolean releaseChair = true;

    //0:申请主席，1:释放主席，2:结束会议，3:离开会议
    private int controlType;

    public CustomDialog(@NonNull Context context) {
        super(context);
    }

    public CustomDialog(@NonNull Context context, int controlType) {
        super(context);
        this.controlType = controlType;
    }

    @Override
    public int bindLayout() {
        return R.layout.dialog_request_chair;
    }

    @Override
    public void initView(final BaseDialog dialog, View contentView) {
        title = (TextView) contentView.findViewById(R.id.title);
        etAccessCode = (EditText) contentView.findViewById(R.id.et_accessCode);
        btCancle = (Button) contentView.findViewById(R.id.bt_cancle);
        btConfirm = (Button) contentView.findViewById(R.id.bt_confirm);


        switch (controlType) {
            case 0:
                title.setText("主席密码");
                etAccessCode.setHint("请输入主席密码");
                break;

            //释放主席
            case 1:
                title.setText("是否释放主席?");
                etAccessCode.setVisibility(View.GONE);
                break;

            //结束会议
            case 2:
                title.setText("请选择你的操作");
                etAccessCode.setVisibility(View.GONE);
                btCancle.setText("离开会议");
                btConfirm.setText("结束会议");
                break;

            //离开会议
            case 3:
                title.setText("是否离开会议");
                etAccessCode.setVisibility(View.GONE);
                btConfirm.setText("离开会议");
                break;
        }

        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != dialogClickListener) {
                    if (0 == controlType) {
                        etAccessCode.setText("");
                        dialogClickListener.onConfirmClickListener(etAccessCode.getText().toString().trim());
                    } else {
                        dialogClickListener.onConfirmClickListener();
                    }
                    dismiss();
                }
            }
        });

        btCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != dialogClickListener) {
                    dialogClickListener.onCancleClickListener();
                }
                dismiss();
            }
        });
    }

    @Override
    public void setWindowStyle(Window window) {

    }

    public OnDialogClickListener dialogClickListener;

    public void setDialogClickListener(OnDialogClickListener dialogClickListener) {
        this.dialogClickListener = dialogClickListener;
    }
}
