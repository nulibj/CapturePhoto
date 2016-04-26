package com.client.capturephoto;

import com.client.capturephoto.util.SPUtil;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import static com.client.capturephoto.util.Constant.*;

/**
 * ���ý���
 * @author Administrator
 *
 */
public class ConfigActivity extends BaseActivity implements
        View.OnClickListener
{
    
    private Spinner mSpinnerTran, mSpinnerLimit, mSpinnerNumber, mSpinnerSize;
    
    // ���桢�ָ�Ĭ�ϡ�ȡ��
    private Button btnOK, btnDefault, btnCancel;
    
    private EditText userName, password, url;
    
    private SPUtil spUtil;
    
    private SharedPreferences.Editor editor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);
        spUtil = SPUtil.getInstance(ConfigActivity.this);
        editor = SPUtil.sp.edit();
        initSpinner();
        initView();
        initData();
    }
    
    private void initView()
    {
        userName = (EditText) findViewById(R.id.txtUsername);
        password = (EditText) findViewById(R.id.txtPassword);
        url = (EditText) findViewById(R.id.txtURL);
        btnOK = (Button) findViewById(R.id.btnOK);
        btnDefault = (Button) findViewById(R.id.btnDefault);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnOK.setOnClickListener(this);
        btnDefault.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }
    
    private void initSpinner()
    {
        mSpinnerTran = (Spinner) findViewById(R.id.spinner_tran);
        mSpinnerLimit = (Spinner) findViewById(R.id.spinner_limit);
        mSpinnerNumber = (Spinner) findViewById(R.id.spinner_number);
        mSpinnerSize = (Spinner) findViewById(R.id.spinner_size);
        
        // ����ѡ������ArrayAdapter��������
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ARRAY_TRANS);
        // ���������б��ķ��
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // ��adapter ���ӵ�spinner��
        mSpinnerTran.setAdapter(adapter);
        
        // ����ѡ������ArrayAdapter��������
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ARRAY_LIMIT);
        // ���������б��ķ��
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // ��adapter ���ӵ�spinner��
        mSpinnerLimit.setAdapter(adapter);
        
        // ����ѡ������ArrayAdapter��������
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ARRAY_NUMBER);
        // ���������б��ķ��
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerNumber.setAdapter(adapter);
        
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ARRAY_SIZE);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSize.setAdapter(adapter);
        
    }
    
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnOK:
                if (check())
                {
                    saveSP();
                }
                break;
            case R.id.btnDefault:
                restoreSP();
                initData();
                break;
            case R.id.btnCancel:
                finish();
                break;
            
            default:
                break;
        }
    }
    
    /**
     * ����Ƿ��������
     * @return
     */
    private boolean check()
    {
        if (TextUtils.isEmpty(userName.getText()))
        {
            showToast(R.string.empty_user_name);
            return false;
        }
        if (TextUtils.isEmpty(password.getText()))
        {
            showToast(R.string.empty_password);
            return false;
        }
        
        if (TextUtils.isEmpty(password.getText()))
        {
            showToast(R.string.empty_url);
            return false;
        }
        return true;
    }
    
    /**
     * ����
     */
    public void saveSP()
    {
        editor.putString(USER_NAME, userName.getText().toString().trim());
        editor.putString(PASSWORD, password.getText().toString().trim());
        editor.putString(URL, url.getText().toString().trim());
        editor.putInt(TRANS, mSpinnerTran.getSelectedItemPosition());
        editor.putInt(LIMIT, mSpinnerLimit.getSelectedItemPosition());
        editor.putInt(NUMBER, mSpinnerNumber.getSelectedItemPosition());
        editor.putInt(SIZE, mSpinnerSize.getSelectedItemPosition());
        editor.commit();
        editor.clear();
    }
    
    /**
     * �ָ�Ĭ��
     */
    private void restoreSP()
    {
        editor.putString(USER_NAME, getString(R.string.default_user_name));
        editor.putString(PASSWORD, getString(R.string.default_password));
        editor.putString(URL, getString(R.string.default_url));
        editor.putInt(TRANS, DEFAULT_TRANS_POSITION);
        editor.putInt(LIMIT, DEFAULT_LIMIT_POSITION);
        editor.putInt(NUMBER, DEFAULT_NUMBER_POSITION);
        editor.putInt(SIZE, DEFAULT_SIZE_POSITION);
        editor.commit();
        editor.clear();
    }
    
    /**
     * ��ʾ����
     */
    private void initData()
    {
        String userName = spUtil.getString(USER_NAME, "");
        if (!TextUtils.isEmpty(userName))
        {
            this.userName.setText(userName);
        }
        
        String password = spUtil.getString(PASSWORD, "");
        if (!TextUtils.isEmpty(password))
        {
            this.password.setText(password);
        }
        
        String url = spUtil.getString(URL, "");
        if (!TextUtils.isEmpty(url))
        {
            this.url.setText(url);
        }
        mSpinnerTran.setSelection(spUtil.getInt(TRANS, DEFAULT_TRANS_POSITION));
        mSpinnerLimit.setSelection(spUtil.getInt(LIMIT, DEFAULT_LIMIT_POSITION));
        mSpinnerNumber.setSelection(spUtil.getInt(NUMBER,
                DEFAULT_NUMBER_POSITION));
        mSpinnerSize.setSelection(spUtil.getInt(SIZE, DEFAULT_SIZE_POSITION));
    }
}