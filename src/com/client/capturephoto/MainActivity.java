package com.client.capturephoto;

import static com.client.capturephoto.util.Constant.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.client.capturephoto.adapter.ImageAdapter;
import com.client.capturephoto.adapter.ImageInfo;
import com.client.capturephoto.adapter.ImageAdapter.ISelectedChangeListener;
import com.client.capturephoto.adapter.ImageAdapter.Mode;
import com.client.capturephoto.db.MySQLiteOpenHelper;
import com.client.capturephoto.util.BitmapTools;
import com.client.capturephoto.util.Constant;
import com.client.capturephoto.util.DensityUtil;
import com.client.capturephoto.util.FileOperationHelper;
import com.client.capturephoto.util.ProgressAsyncTask;
import com.client.capturephoto.util.SPUtil;
import com.client.capturephoto.util.Util;
import com.google.zxing.client.android.CaptureActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MainActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, ISelectedChangeListener
{
    
    private static final String TAG = "MainActivity";
    
    private static final int CONFIG = Menu.FIRST;
    
    private static final int EXIT = Menu.FIRST + 1;
    
    /**
     * 条码区域
     */
    private EditText mEditTextCode;
    
    /**
     * 按钮
     */
    // 扫码、拍照、上传、多选、设置、返回、删除
    private Button btnCode, btnCapture, btnUpload, btnMultiChoice, btnConfig,
            btnReturn, btnDelete;
    
    protected static ArrayList<ImageInfo> imageInfos = new ArrayList<ImageInfo>();
    
    private LinearLayout llDefaultView, llDeleteView;
    
    private GridView myGridView;   // 图片列表
    
    private ImageAdapter imageAdapter;  // 适配器
    
    private DensityUtil mDensityUtil;
    
    private SPUtil spUtil;
    
    /**
     * 当前每行显示图片数量
     */
    private int currentNumber;
    
    private String currentCode = "";
    
    /**
     * 每张预览图的宽高
     */
    private int pictureWidth, pictureHeight;
    
    /**
     * 提示框
     */
    private AlertDialog.Builder builder;
    
    private MySQLiteOpenHelper sqLiteOpenHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        spUtil = SPUtil.getInstance(MainActivity.this);
        mDensityUtil = new DensityUtil(this);
        currentNumber = spUtil.getInt(NUMBER, DEFAULT_NUMBER_POSITION);
        currentNumber = Integer.parseInt(ARRAY_NUMBER[currentNumber]);
        sqLiteOpenHelper = new MySQLiteOpenHelper(MainActivity.this);
        initView();
        initPicture();
        initGridView();
        
        
        
        Toast.makeText(MainActivity.this, "test", 100*1000).show();
        
        
    }
    
    /**
     * 初始化组件
     */
    private void initView()
    {
        llDefaultView = (LinearLayout) findViewById(R.id.ll_defaultview);
        llDeleteView = (LinearLayout) findViewById(R.id.ll_deleteview);
        mEditTextCode = (EditText) findViewById(R.id.edittext_code);
        btnCode = (Button) findViewById(R.id.button_code);
        btnCapture = (Button) findViewById(R.id.btn_pz);
        btnUpload = (Button) findViewById(R.id.btn_upload);
        btnMultiChoice = (Button) findViewById(R.id.btn_multi_choice);
        btnConfig = (Button) findViewById(R.id.btn_config);
        btnReturn = (Button) findViewById(R.id.btn_return);
        btnDelete = (Button) findViewById(R.id.btn_delete);
        btnCode.setOnClickListener(this);
        btnCapture.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnMultiChoice.setOnClickListener(this);
        btnConfig.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnReturn.setOnClickListener(this);
        
        myGridView = (GridView) findViewById(R.id.gridview);
//        ImageInfo imageInfo;
//        for (int i = 0; i < uris.length; i++)
//        {
//            imageInfo = new ImageInfo();
//            imageInfo.fullName = uris[i];
//            imageInfos.add(imageInfo);
//        }
        
        imageAdapter = new ImageAdapter(MainActivity.this, imageInfos);
    }
    
    /**
     * 初始化图片列表
     */
    private void initGridView()
    {
        myGridView.setAdapter(imageAdapter);
        myGridView.setOnItemClickListener(this);
        imageAdapter.setOnSelectedChangeListener(this);
    }
    
    /**
     * 计算图片的宽高
     */
    private void initPicture()
    {
        pictureWidth = mDensityUtil.getImageViewWidth(currentNumber,
                getResources().getDimensionPixelSize(R.dimen.horizontal_margin),
                getResources().getDimensionPixelSize(R.dimen.grid_Spacing));
        pictureHeight = mDensityUtil.getImageViewHeight(pictureWidth);
        myGridView.setNumColumns(currentNumber);
        imageAdapter.setPictureLayoutParams(pictureWidth, pictureHeight);
        ImageLoader.getInstance().clearMemoryCache();
    }
    
    /**
     * 重新计算每张图片的宽高
     */
    @Override
    protected void onStart()
    {
        int number = spUtil.getInt(NUMBER, DEFAULT_NUMBER_POSITION);
        number = Integer.parseInt(ARRAY_NUMBER[number]);
        if (currentNumber != number)
        {
            currentNumber = number;
            initPicture();
            imageAdapter.notifyDataSetChanged();
        }
        
        super.onStart();
    }
    
    @Override
    protected void onPause()
    {
        /**
         * 清除缓存
         */
        ImageLoader.getInstance().clearMemoryCache();
        super.onPause();
    }
    
    /**
     * 菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //        menu.add(0, EXIT, 0, R.string.menu_exit)/* .setIcon(R.drawable.menu_exit) */;
        //        menu.add(0, CONFIG, 1, R.string.menu_config);
        menu.add(0, CONFIG + 2, 1, R.string.un_upload_picture);
        return true;
    }
    
    /**
     * 菜单点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        
        switch (item.getItemId())
        {
            case CONFIG:
                Intent configIntent = new Intent(this, ConfigActivity.class);
                startActivity(configIntent);
                break;
            
            case EXIT:
                MainActivity.this.finish();
                break;
            case CONFIG + 2:
                Intent intent = new Intent(this, UnUploadActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 按钮点击事件
     */
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_code: // 拍照扫描
                startActivityForResult(new Intent(this, CaptureActivity.class),
                        SCANNIN_REQUEST_CODE);
                break;
            case R.id.btn_pz:  // 拍照 跳转至拍照界面
                if (checkToTakePicture())
                {
                    Intent takePickureIntent = new Intent(this,
                            TackPictureActivity.class);
                    
//                    不能包含特殊字符
//                    \/:*"?<>|
                    String code = currentCode;
                    code.replace("\\", "");
                    code.replace("/", "");
                    code.replace(":", "");
                    code.replace("*", "");
                    code.replace("\"", "");
                    code.replace("?", "");
                    code.replace("<", "");
                    code.replace(">", "");
                    code.replace("|", "");
                    takePickureIntent.putExtra(TEXT_CODE, code);
                    startActivityForResult(takePickureIntent,
                            TAKE_PICTURE_REQUEST_CODE);
                }
                
                //                createPhoto();
                break;
            case R.id.btn_multi_choice: // 多选
                imageAdapter.setMode(Mode.Pick);
                llDefaultView.setVisibility(View.GONE);
                llDeleteView.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_return:  // 返回
                imageAdapter.setMode(Mode.View);
                llDefaultView.setVisibility(View.VISIBLE);
                llDeleteView.setVisibility(View.GONE);
                break;
            case R.id.btn_delete:  // 删除
                if (imageAdapter.getIsSelected().size() > 0)
                    showDeleteDialog();
                break;
            case R.id.btn_config:  // 设置
                Intent configIntent = new Intent(this, ConfigActivity.class);
                startActivity(configIntent);
                break;
            case R.id.btn_upload:  // 上传
                if (checkToUpload())
                    upload();
                break;
            default:
                break;
        }
    }
    
    private String imagePath;
    
    private Uri imageUri = null;
    private String imageName;
    /**
     * 创建图片
     */
    private void createPhoto()
    {
        imageName = simpleDateFormat.format(System.currentTimeMillis())
                + ".jpg";
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        // 存储卡可用 将照片存储在 sdcard
        if (Util.isHasSdcard())
        {
            
            if (imagePath == null)
            {
                imagePath = Util.SDCARD_ROOT_PATH + File.separator
                        + getString(R.string.app_name) + File.separator;
            }
            File file = new File(imagePath);
            if (!file.exists())
                file.mkdirs();
            imageUri = Uri.fromFile(new File(imagePath, imageName));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            // startActivityForResult(intent, REQUEST_CODE);
            
            startActivityForResult(intent, /* TAKE_BIG_PICTURE */
                    CROP_BIG_PICTURE);
        }
        else
        {
            showToast(R.string.toast_nosdcard);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode)
        {
        
        /* 拍照后保存并显示图片 */
            case TAKE_BIG_PICTURE:
                
                // 存储卡可用
                if (Util.isHasSdcard())
                {
                    
                    // bitmap = BitmapTools
                    // .getBitmapBySampleSize(Dispute.this, imagePath);
                    //                    FileInfo fileInfo = new FileInfo();
                    //                    fileInfo.fileName = imageName;
                    //                    fileInfo.filePath = imagePath;
                }
                else
                {
                    // 存储卡不可用直接返回缩略图
                    //                    Bundle extras = data.getExtras();
                    //                    Bitmap bitmap = (Bitmap) extras.get("data");
                }
                
                //                BitmapTools.cropImageUri(MainActivity.this,
                //                        imageUri,
                //                        CROP_BIG_PICTURE);
                break;
            
            case CROP_BIG_PICTURE:// from crop_big_picture
                Log.v(TAG, "CROP_BIG_PICTURE: data = " + data);// it seems to be
                                                               // null
                if (imageUri != null)
                {
                    ImageInfo imageInfo = new ImageInfo();
                    // bitmap = BitmapTools.getBitmapBySampleSize(MainActivity.this,
                    // imagePath + imageName);
                    imageInfo.fullName = imagePath + imageName;
                    imageInfos.add(imageInfo);
                    imageAdapter.notifyDataSetChanged();
                }
                break;
            
                /**
                 * 返回扫描结果
                 */
            case SCANNIN_REQUEST_CODE:
                //显示扫描到的内容
                // 如果两次扫码结果不同，则清除图片
                mEditTextCode.setText(data.getStringExtra(SCAN_RESULT));
                if (!currentCode.equals(mEditTextCode.getText().toString()))
                {
                    clearAllList();
                    imageAdapter.notifyDataSetChanged();
                }
                break;
                
                /**
                 * 拍照界面返回
                 */
            case TAKE_PICTURE_REQUEST_CODE:
                imageAdapter.notifyDataSetChanged();
                break;
        }
    }
    
    /**
     * 按键按下
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                //                System.exit(0);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    /**
     * 按下Back键触发
     */
    @Override
    public void onBackPressed()
    {
        ImageLoader.getInstance().stop();
        ImageLoader.getInstance().clearMemoryCache();
        System.exit(0);
        super.onBackPressed();
    }
    
    /**
     * 程序退出
     */
    @Override
    protected void onDestroy()
    {
        ImageLoader.getInstance().stop();
        System.exit(0);
        super.onDestroy();
    }
    
    
    /**
     * GridView点击事件
     * 选择模式和预览模式
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id)
    {
        if (imageAdapter.getMode() == Mode.Pick)
        {
            imageAdapter.setSelected(position, view);
        }
        else
        {
            startImagePagerActivity(position);
        }
    }
    
    /**
     * 点击小图显示大图
     * 
     * @param position
     */
    private void startImagePagerActivity(int position)
    {
        Intent intent = new Intent(this, ImagePagerActivity.class);
        intent.putParcelableArrayListExtra(Extra.IMAGES, imageInfos);
        intent.putExtra(Extra.IMAGE_POSITION, position);
        startActivity(intent);
    }
    
    /**
     * 图片多选监听
     */
    @Override
    public void onSelectedChange(int count)
    {
        if (count == 0)
            btnDelete.setText(R.string.button_delete);
        else if (count < 10)
            btnDelete.setText(getString(R.string.button_delete) + " 0" + count);
        else
            btnDelete.setText(getString(R.string.button_delete) + " " + count);
    }
    
    /**
     * 显示删除图片提示框
     */
    private void showDeleteDialog()
    {
        if (builder == null)
        {
            builder = new AlertDialog.Builder(this).setTitle(R.string.dialog_title)
                    .setMessage(R.string.dialog_message);
            builder.setPositiveButton(R.string.button_sure,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            delete();
                            btnDelete.setText(R.string.button_delete);
                        }
                    });
            
            builder.setNegativeButton(R.string.button_cancel, null);
        }
        builder.show();
    }
    
    /**
     * 删除所选图片 
     */
    private void delete()
    {
        initDeleteTask();
        //        showProgress(getString(R.string.operation_deleting));
        //        
        //        ArrayList<ImageInfo> selectedFiles = imageAdapter.getIsSelected();
        //        if (mFileOperationHelper.delete(selectedFiles)) {
        //            
        //        }
        //        clearSelection(selectedFiles, imageInfos);
        //        selectedFiles.clear();
        //        imageAdapter.notifyDataSetChanged();
    }
    
    /**
     * 创建删除图片任务
     */
    private void initDeleteTask()
    {
        ProgressAsyncTask asyncTask = new ProgressAsyncTask(MainActivity.this,
                getString(R.string.dialog_title),
                getString(R.string.operation_deleting))
        {
            
            @Override
            protected void onPostExecute(Object obj)
            {
                super.onPostExecute(obj);
                imageAdapter.notifyDataSetChanged();
            }
            
            @Override
            protected Object doInBackground(Object... params)
            {
                ArrayList<ImageInfo> selectedFiles = imageAdapter.getIsSelected();
                if (FileOperationHelper.deleteFiles(FileOperationHelper.copyFileList(selectedFiles)))
                {
                    if (DEBUG)
                    {
                    }
                }
                clearSelection(selectedFiles, imageInfos);
                selectedFiles.clear();
                return super.doInBackground(params);
            }
        };
        asyncTask.execute();
    }
    
    /**
     * 清除列表
     * @param files
     * @param allfiles
     */
    private void clearSelection(final List<ImageInfo> files,
            final ArrayList<ImageInfo> allfiles)
    {
        for (ImageInfo f : files)
        {
            allfiles.remove(f);
        }
    }
    
    /**
     * 检查拍照条件
     * @return
     */
    private boolean checkToTakePicture()
    {
        currentCode = mEditTextCode.getText().toString();
        if (TextUtils.isEmpty(currentCode))
        {
            showToast(R.string.message_empty_code);
            return false;
        }
        
        int limitPosition = spUtil.getInt(LIMIT, DEFAULT_LIMIT_POSITION);
        // TODO
        //        if (imageInfos.size() >= Integer.parseInt(ARRAY_LIMIT[limitPosition]))
        //        {
        //            showToast(R.string.message_limit);
        //            return false;
        //        }
        return true;
    }
    
    /**
     * 检查上传条件
     * 列表不为空
     * @return
     */
    private boolean checkToUpload()
    {
        if (imageInfos == null || imageInfos.size() <= 0)
        {
            return false;
        }
        return true;
    }
    
    private UploadProgressAsyncTask uploadProgressAsyncTask;
    
    private void upload()
    {
        uploadProgressAsyncTask = new UploadProgressAsyncTask(
                MainActivity.this, false);
        uploadProgressAsyncTask.execute();
    }
    
    /**
     * 压缩图片
     * 
     * @throws Exception
     */
    private void compressBitmap() throws Exception
    {
        final int trans = spUtil.getInt(TRANS, DEFAULT_TRANS_POSITION);
        final int width = spUtil.getInt(Constant.PICTURE_WIDTH,
                mDensityUtil.getScreenWidth());
        final int height = spUtil.getInt(Constant.PICTURE_HEIGHT,
                mDensityUtil.getScreenHeight());
        int i = 0;
        for (ImageInfo imageInfo : FileOperationHelper.copyFileList(imageInfos))
        {
            if (uploadProgressAsyncTask.getCancel())
            {
                return;
            }
            if (trans == 0) // 直接上传
            {
                
            }
            else
            {
                /**
                 * 压缩上传
                 */
                int result[] = getScale(trans, width, height);
                Bitmap bitmap = BitmapTools.getResizedBitmap(imageInfo.fullName, result[0], result[1]);
                
                if (bitmap != null)
                {
                    BitmapTools.saveMyBitmap(imageInfo.fullName, bitmap);
                    if(Constant.DEBUG){
                        Log.e(TAG, "======= bitmap   " + bitmap.getWidth() + "  " + bitmap.getHeight());
                    }
                    bitmap.recycle();
                    bitmap = null;
                }
            }
            i++;
            upload(imageInfo, i);
        }
    }
    
    private static int[] getScale(int trans, int width, int height)
    {
        int result[] = new int[2];
        switch (trans)
        {
            case 0:
                result[0] = width;
                result[1] = height;
                break;
            case 1:
                result[0] = width / 2;
                result[1] = height / 2; // 宽高1:2 
                break;
            case 2:
                result[0] = width / 3;
                result[1] = height / 3; // 宽高1:3 
                break;
            case 3:
                result[0] = width / 4;
                result[1] = height / 4; // 宽高1:4 
                break;
            default:
                result[0] = width;
                result[1] = height;
                break;
        }
        return result;
    }
    
    /**
     * 图片上传类
     * @author Administrator
     *
     */
    private final class UploadProgressAsyncTask extends ProgressAsyncTask
    {
        private boolean isCancel = false;
        public UploadProgressAsyncTask(Context context, boolean isShow)
        {
            super(context, isShow);
        }
        
        @Override
        protected void onPreExecute()
        {
            isCancel = false;
            // 显示进度条
            showProgressDialog(mContext, imageInfos.size());
            super.onPreExecute();
        }
        
        @Override
        protected void onPostExecute(Object obj)
        {
            super.onPostExecute(obj);
            if(Constant.DEBUG){
                
            }
            // 
            dismissProgressDialog();
            dismissProgress();
            imageAdapter.notifyDataSetChanged();
        }
        
        @Override
        protected Object doInBackground(Object... params)
        {
            
            // 添加至数据库
            MySQLiteOpenHelper.insert(sqLiteOpenHelper, imageInfos);
            
            if (!Util.isConnectingToInternet(mContext))
            {
                showToastInUI(R.string.network_error);
                return 0;
            }
            try
            {
                compressBitmap();
                return uploadLength;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                showToastInUI(R.string.upload_failed);
            }
            return super.doInBackground(params);
        }
        
        public void updateProgress(int value)
        {
            publishProgress(value + "");
        }
        
        @Override
        protected void onProgressUpdate(String... values)
        {
            MainActivity.this.updateProgress(Integer.parseInt(values[0]));
            super.onProgressUpdate(values);
        }
        
        @Override
        protected void onCancelled()
        {
            super.onCancelled();
        }
        
        public void setCancel(boolean isCancel)
        {
            this.isCancel = isCancel;
        }
        
        public boolean getCancel()
        {
            return isCancel;
        }
    }
    
    /**
     * 上传提示框
     */
    private AlertDialog uploadDialog;
    
    /**
     * 上传进度
     */
    private ProgressBar progressBar;
    
    private TextView tvDownLoadProgress;
    
    private int uploadLength;
    
    /**
     * 显示进度条对话框
     * 
     * @param mContext
     * @param max
     */
    private void showProgressDialog(final Context mContext, final int max)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext).setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.dialog_title);
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.upload_dialog_progress, null);
        
        tvDownLoadProgress = (TextView) view.findViewById(R.id.text_progress);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        uploadLength = max;
        progressBar.setMax(uploadLength);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.button_cancel,
                new DialogInterface.OnClickListener()
                {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (dialog != null)
                        {
                            dialog.dismiss();
                            showProgress(getString(R.string.canceling));
                            uploadProgressAsyncTask.setCancel(true);
                        }
                    }
                });
        
        uploadDialog = builder.create();
        uploadDialog.show();
    }
    
    private void updateProgress(int progress)
    {
        tvDownLoadProgress.setText(progress + "/" + uploadLength);
        progressBar.setProgress(progress);
    }
    
    /**
     * 隐藏对话框
     */
    private void dismissProgressDialog()
    {
        if (uploadDialog != null)
        {
            uploadDialog.dismiss();
            uploadDialog = null;
        }
    }
    
    
    /**
     * 上传图片
     * 上传成功则删除文件，删除数据库，更新进度条
     * @param imageInfo
     * @param value
     * @throws Exception
     */
    private void upload(ImageInfo imageInfo, int value) throws Exception
    {
        Util.uploadFile(MainActivity.this, spUtil.getString(URL, getString(R.string.default_url)), imageInfo.fullName, null);
        FileOperationHelper.deleteFile(imageInfo);
        MySQLiteOpenHelper.delete(sqLiteOpenHelper, imageInfo);
        imageInfos.remove(imageInfo);
        uploadProgressAsyncTask.updateProgress(value);
    }
    
    /**
     * 清除所有列表
     */
    private void clearAllList()
    {
        imageInfos.clear();
        imageAdapter.getIsSelected().clear();
    }
    
}
