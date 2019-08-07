
package SecuGen.Demo;
import java.io.*;
import java.nio.ByteBuffer;
import SecuGen.Demo.R;
import SecuGen.Driver.Constant;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.widget.Toast;

import SecuGen.FDxSDKPro.*;

public class JSGDActivity extends Activity
        implements View.OnClickListener, java.lang.Runnable, SGFingerPresentEvent {

    private static final String TAG = "Lasyst";
    private static final int IMAGE_CAPTURE_TIMEOUT_MS = 10000;
    private static final int IMAGE_CAPTURE_QUALITY = 50;

    private Button mButtonCapture;
    private Button mButtonRegister;
    private Button mButtonMatch;
    private Button mButtonLed;
    private Button mSDKTest;
    private EditText mEditLog;
    private android.widget.TextView mTextViewResult;
    private android.widget.CheckBox mCheckBoxMatched;
    private android.widget.ToggleButton mToggleButtonSmartCapture;
    private android.widget.ToggleButton mToggleButtonCaptureModeN;
    private android.widget.ToggleButton mToggleButtonAutoOn;
    private android.widget.ToggleButton mToggleButtonNFIQ;
    private android.widget.ToggleButton mToggleButtonUSBBulkMode64;
    private PendingIntent mPermissionIntent;
    private ImageView mImageViewFingerprint;
    private ImageView mImageViewRegister;
    private ImageView mImageViewVerify;
    private byte[] mRegisterImage;
    private byte[] mVerifyImage;
    private byte[] mRegisterTemplate;
    private byte[] mVerifyTemplate;
	private int[] mMaxTemplateSize;
	private int mImageWidth;
	private int mImageHeight;
	private int mImageDPI;
	private int[] grayBuffer;
    private Bitmap grayBitmap;
    private IntentFilter filter; //
    private SGAutoOnEventNotifier autoOn;
    private boolean mLed;
    private boolean mAutoOnEnabled;
    private int nCaptureModeN;
    private Button mButtonSetBrightness0;
    private Button mButtonSetBrightness100;
    private Button mButtonReadSN;
    private boolean bSecuGenDeviceOpened;
    private JSGFPLib sgfplib;
    private boolean usbPermissionRequested;
	private String myData;
	private static int i=0;
	private int index=0;


	private int[] attend= new int[60];
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    private void debugMessage(String message) {
        this.mEditLog.append(message);
        this.mEditLog.invalidate(); //TODO trying to get Edit log to update after each line written
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    //This broadcast receiver is necessary to get user permissions to access the attached USB device
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		//Log.d(TAG,"Enter mUsbReceiver.onReceive()");
    		if (ACTION_USB_PERMISSION.equals(action)) {
    			synchronized (this) {
    				UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
    					if(device != null){
    						//DEBUG Log.d(TAG, "Vendor ID : " + device.getVendorId() + "\n");
    						//DEBUG Log.d(TAG, "Product ID: " + device.getProductId() + "\n");
    						debugMessage("USB BroadcastReceiver VID : " + device.getVendorId() + "\n");
    						debugMessage("USB BroadcastReceiver PID: " + device.getProductId() + "\n");
    					}
    					else
        					Log.e(TAG, "mUsbReceiver.onReceive() Device is null");
    				}
    				else
    					Log.e(TAG, "mUsbReceiver.onReceive() permission denied for device " + device);
    			}
    		}
    	}
    };

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    //This message handler is used to access local resources not
    //accessible by SGFingerPresentCallback() because it is called by
    //a separate thread.
    public Handler fingerDetectedHandler = new Handler(){
    	// @Override
	    public void handleMessage(Message msg) {
	       //Handle the message
			CaptureFingerPrint();
	    	if (mAutoOnEnabled) {
				mToggleButtonAutoOn.toggle();
		    	EnableControls();
	    	}
	    }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
	public void EnableControls(){
		this.mButtonCapture.setClickable(true);
		this.mButtonCapture.setTextColor(getResources().getColor(android.R.color.white));
		this.mButtonRegister.setClickable(true);
		this.mButtonRegister.setTextColor(getResources().getColor(android.R.color.white));
		this.mButtonMatch.setClickable(true);
		this.mButtonMatch.setTextColor(getResources().getColor(android.R.color.white));
	    mButtonSetBrightness0.setClickable(true);
	    mButtonSetBrightness100.setClickable(true);
	    mButtonReadSN.setClickable(true);
		this.mButtonLed.setClickable(true);
		this.mButtonLed.setTextColor(getResources().getColor(android.R.color.white));
	}

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
	public void DisableControls(){
		this.mButtonCapture.setClickable(false);
		this.mButtonCapture.setTextColor(getResources().getColor(android.R.color.black));
		this.mButtonRegister.setClickable(false);
		this.mButtonRegister.setTextColor(getResources().getColor(android.R.color.black));
		this.mButtonMatch.setClickable(false);
		this.mButtonMatch.setTextColor(getResources().getColor(android.R.color.black));
	    mButtonSetBrightness0.setClickable(false);
		mButtonSetBrightness100.setClickable(false);
		mButtonReadSN.setClickable(false);
		this.mButtonLed.setClickable(false);
		this.mButtonLed.setTextColor(getResources().getColor(android.R.color.black));
	}


    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//Log.d(TAG, "onCreate()");
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        mButtonCapture = (Button)findViewById(R.id.buttonCapture);
        mButtonCapture.setOnClickListener(this);
        mButtonRegister = (Button)findViewById(R.id.buttonRegister);
        mButtonRegister.setOnClickListener(this);
        mButtonMatch = (Button)findViewById(R.id.buttonMatch);
        mButtonMatch.setOnClickListener(this);
       	mButtonLed = (Button)findViewById(R.id.buttonLedOn);
        mButtonLed.setOnClickListener(this);
        mSDKTest = (Button)findViewById(R.id.buttonSDKTest);
        mSDKTest.setOnClickListener(this);
        mEditLog = (EditText)findViewById(R.id.editLog);
        mTextViewResult = (android.widget.TextView)findViewById(R.id.textViewResult);
        mCheckBoxMatched = (android.widget.CheckBox) findViewById(R.id.checkBoxMatched);
        mToggleButtonSmartCapture = (android.widget.ToggleButton) findViewById(R.id.toggleButtonSmartCapture);
        mToggleButtonSmartCapture.setOnClickListener(this);
        mToggleButtonCaptureModeN = (android.widget.ToggleButton) findViewById(R.id.toggleButtonCaptureModeN);
        mToggleButtonCaptureModeN.setOnClickListener(this);
        mToggleButtonAutoOn = (android.widget.ToggleButton) findViewById(R.id.toggleButtonAutoOn);
        mToggleButtonAutoOn.setOnClickListener(this);
        mToggleButtonNFIQ = (android.widget.ToggleButton) findViewById(R.id.toggleButtonNFIQ);
        mToggleButtonNFIQ.setOnClickListener(this);
        mToggleButtonUSBBulkMode64 = (android.widget.ToggleButton) findViewById(R.id.ToggleButtonUSBBulkMode64);
        mToggleButtonUSBBulkMode64.setOnClickListener(this);
        mImageViewFingerprint = (ImageView)findViewById(R.id.imageViewFingerprint);
        mImageViewRegister = (ImageView)findViewById(R.id.imageViewRegister);
        mImageViewVerify = (ImageView)findViewById(R.id.imageViewVerify);
	    mButtonSetBrightness0 = (Button)findViewById(R.id.buttonSetBrightness0);
	    mButtonSetBrightness0.setOnClickListener(this);
	    mButtonSetBrightness100 = (Button)findViewById(R.id.buttonSetBrightness100);
	    mButtonSetBrightness100.setOnClickListener(this);
		mButtonSetBrightness0.setClickable(false);
		mButtonSetBrightness100.setClickable(false);
		mButtonSetBrightness0.setTextColor(getResources().getColor(android.R.color.black));
		mButtonSetBrightness100.setTextColor(getResources().getColor(android.R.color.black));
		mButtonReadSN = (Button)findViewById(R.id.buttonReadSN);
		mButtonReadSN.setOnClickListener(this);

        grayBuffer = new int[JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES*JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES];
        for (int i=0; i<grayBuffer.length; ++i)
        	grayBuffer[i] = android.graphics.Color.GRAY;
        grayBitmap = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES, Bitmap.Config.ARGB_8888);
        grayBitmap.setPixels(grayBuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES);
        mImageViewFingerprint.setImageBitmap(grayBitmap);

        int[] sintbuffer = new int[(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2)*(JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES/2)];
        for (int i=0; i<sintbuffer.length; ++i)
        	sintbuffer[i] = android.graphics.Color.GRAY;
        Bitmap sb = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES/2, Bitmap.Config.ARGB_8888);
        sb.setPixels(sintbuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES/2);
        mImageViewRegister.setImageBitmap(grayBitmap);
        mImageViewVerify.setImageBitmap(grayBitmap);
        mMaxTemplateSize = new int[1];

        //USB Permissions
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
       	filter = new IntentFilter(ACTION_USB_PERMISSION);
        sgfplib = new JSGFPLib((UsbManager)getSystemService(Context.USB_SERVICE));
        this.mToggleButtonSmartCapture.toggle();
        bSecuGenDeviceOpened = false;
        usbPermissionRequested = false;

		debugMessage("Starting Activity\n");
		debugMessage("JSGFPLib version: " + sgfplib.GetJSGFPLibVersion() + "\n");
		mLed = false;
		mAutoOnEnabled = false;
		autoOn = new SGAutoOnEventNotifier (sgfplib, this);
		nCaptureModeN = 0;

    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onPause() {
    	//Log.d(TAG, "onPause()");
    	if (bSecuGenDeviceOpened)
    	{
    		autoOn.stop();
    		EnableControls();
    		sgfplib.CloseDevice();
            bSecuGenDeviceOpened = false;
    	}
    	unregisterReceiver(mUsbReceiver);
    	mRegisterImage = null;
    	mVerifyImage = null;
    	mRegisterTemplate = null;
    	mVerifyTemplate = null;
        mImageViewFingerprint.setImageBitmap(grayBitmap);
        mImageViewRegister.setImageBitmap(grayBitmap);
        mImageViewVerify.setImageBitmap(grayBitmap);
        super.onPause();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onResume(){
    	//Log.d(TAG, "onResume()");
        super.onResume();
        DisableControls();
       	registerReceiver(mUsbReceiver, filter);
        long error = sgfplib.Init( SGFDxDeviceName.SG_DEV_AUTO);
        if (error != SGFDxErrorCode.SGFDX_ERROR_NONE){
        	AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        	if (error == SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND)
        		dlgAlert.setMessage("The attached fingerprint device is not supported on Android");
        	else
        		dlgAlert.setMessage("Fingerprint device initialization failed!");
        	dlgAlert.setTitle("SecuGen Fingerprint SDK");
        	dlgAlert.setPositiveButton("OK",
        			new DialogInterface.OnClickListener() {
        		      public void onClick(DialogInterface dialog,int whichButton){
        		        	finish();
        		        	return;
        		      }
        			}
        	);
        	dlgAlert.setCancelable(false);
        	dlgAlert.create().show();
        }
        else {
	        UsbDevice usbDevice = sgfplib.GetUsbDevice();
	        if (usbDevice == null){
	        	AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
	        	dlgAlert.setMessage("SecuGen fingerprint sensor not found!");
	        	dlgAlert.setTitle("SecuGen Fingerprint SDK");
	        	dlgAlert.setPositiveButton("OK",
	        			new DialogInterface.OnClickListener() {
	        		      public void onClick(DialogInterface dialog,int whichButton){
	        		        	finish();
	        		        	return;
	        		      }
	        			}
	        	);
	        	dlgAlert.setCancelable(false);
	        	dlgAlert.create().show();
	        }
	        else {
	        	boolean hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
		        if (!hasPermission) {
			        if (!usbPermissionRequested)
			        {
			    		debugMessage("Requesting USB Permission\n");
			        	//Log.d(TAG, "Call GetUsbManager().requestPermission()");
			        	usbPermissionRequested = true;
			        	sgfplib.GetUsbManager().requestPermission(usbDevice, mPermissionIntent);
			        }
			        else
			        {
			        	//wait up to 20 seconds for the system to grant USB permission
			        	hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
			    		debugMessage("Waiting for USB Permission\n");
			        	int i=0;
				        while ((hasPermission == false) && (i <= 40))
				        {
				        	++i;
				            hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
				        	try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
				        	//Log.d(TAG, "Waited " + i*50 + " milliseconds for USB permission");
				        }
			        }
		        }
		        if (hasPermission) {
		    		debugMessage("Opening SecuGen Device\n");
			        error = sgfplib.OpenDevice(0);
					debugMessage("OpenDevice() ret: " + error + "\n");
					if (error == SGFDxErrorCode.SGFDX_ERROR_NONE)
					{
				        bSecuGenDeviceOpened = true;
						SecuGen.FDxSDKPro.SGDeviceInfoParam deviceInfo = new SecuGen.FDxSDKPro.SGDeviceInfoParam();
				        error = sgfplib.GetDeviceInfo(deviceInfo);
						debugMessage("GetDeviceInfo() ret: " + error + "\n");
				    	mImageWidth = deviceInfo.imageWidth;
				    	mImageHeight= deviceInfo.imageHeight;
				    	mImageDPI = deviceInfo.imageDPI;
						debugMessage("Image width: " + mImageWidth + "\n");
						debugMessage("Image height: " + mImageHeight + "\n");
						debugMessage("Image resolution: " + mImageDPI + "\n");
				    	debugMessage("Serial Number: " + new String(deviceInfo.deviceSN()) + "\n");
				        sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
						sgfplib.GetMaxTemplateSize(mMaxTemplateSize);
						debugMessage("TEMPLATE_FORMAT_ISO19794 SIZE: " + mMaxTemplateSize[0] + "\n");
				        mRegisterTemplate = new byte[mMaxTemplateSize[0]];
				        mVerifyTemplate = new byte[mMaxTemplateSize[0]];
				        EnableControls();
				        boolean smartCaptureEnabled = this.mToggleButtonSmartCapture.isChecked();
				        if (smartCaptureEnabled)
				        	sgfplib.WriteData(SGFDxConstant.WRITEDATA_COMMAND_ENABLE_SMART_CAPTURE, (byte)1);
				        else
				        	sgfplib.WriteData(SGFDxConstant.WRITEDATA_COMMAND_ENABLE_SMART_CAPTURE, (byte)0);
				        if (mAutoOnEnabled){
				        	autoOn.start();
				        	DisableControls();
				        }
			        }
			        else
			        {
						debugMessage("Waiting for USB Permission\n");
			        }
		        }
		        //Thread thread = new Thread(this);
		        //thread.start();
	        }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onDestroy() {
    	//Log.d(TAG, "onDestroy()");
    	sgfplib.CloseDevice();
    	mRegisterImage = null;
    	mVerifyImage = null;
    	mRegisterTemplate = null;
    	mVerifyTemplate = null;
    	sgfplib.Close();
        super.onDestroy();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    //Converts image to grayscale (NEW)
    public Bitmap toGrayscale(byte[] mImageBuffer, int width, int height)
    {
        byte[] Bits = new byte[mImageBuffer.length * 4];
        for (int i = 0; i < mImageBuffer.length; i++) {
                        Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = mImageBuffer[i]; // Invert the source bits
                        Bits[i * 4 + 3] = -1;// 0xff, that's the alpha.
        }

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmpGrayscale.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
        return bmpGrayscale;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    //Converts image to grayscale (NEW)
    public Bitmap toGrayscale(byte[] mImageBuffer)
    {
        byte[] Bits = new byte[mImageBuffer.length * 4];
        for (int i = 0; i < mImageBuffer.length; i++) {
                        Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = mImageBuffer[i]; // Invert the source bits
                        Bits[i * 4 + 3] = -1;// 0xff, that's the alpha.
        }

        Bitmap bmpGrayscale = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
        bmpGrayscale.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
        return bmpGrayscale;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    //Converts image to grayscale (NEW)
    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int y=0; y< height; ++y) {
            for (int x=0; x< width; ++x){
            	int color = bmpOriginal.getPixel(x, y);
            	int r = (color >> 16) & 0xFF;
            	int g = (color >> 8) & 0xFF;
            	int b = color & 0xFF;
            	int gray = (r+g+b)/3;
            	color = Color.rgb(gray, gray, gray);
            	//color = Color.rgb(r/3, g/3, b/3);
            	bmpGrayscale.setPixel(x, y, color);
            }
        }
        return bmpGrayscale;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    //Converts image to binary (OLD)
    public Bitmap toBinary(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    public void DumpFile(String fileName, byte[] buffer)
    {
    	//Uncomment section below to dump images and templates to SD card
		//debugMessage("HEy fiel\n");
		//Toast.makeText(getApplicationContext(), "Hey File", Toast.LENGTH_SHORT).show();
        try {
            File myDir = new File("/sdcard/Lasyst");
			if(!myDir.exists())
			{
				myDir.mkdirs();
			}
			File myFile = new File("/sdcard/Lasyst/"+fileName);
			myFile.createNewFile();
			Toast.makeText(getApplicationContext(), myFile.toString(), Toast.LENGTH_SHORT).show();
            FileOutputStream fOut = new FileOutputStream(myFile);
            fOut.write(buffer,0,buffer.length);
			//debugMessage("HEy fiel\n");
			//Toast.makeText(getApplicationContext(), "Hey cd File", Toast.LENGTH_SHORT).show();
			fOut.close();
        } catch (Exception e) {
            debugMessage("Exception when writing file" + fileName);
        }
    }
	public void ReadFile(String fileName)
	{
		File myFile;
		//Uncomment section below to dump images and templates to SD card
		//debugMessage("HEy fiel\n");
		//Toast.makeText(getApplicationContext(), "Hey File", Toast.LENGTH_SHORT).show();

		try {


			/*File myDir = new File("/sdcard/Lasyst");
			if(!myDir.exists())
			{
				myDir.mkdirs();
			}*/
			//myFile = new File("/sdcard/Lasyst/"+fileName);
			//myFile.createNewFile();
			//Toast.makeText(getApplicationContext(), myFile.toString(), Toast.LENGTH_SHORT).show();

			//FileInputStream fin = new FileInputStream(myFile);
			//fOut.write(buffer,0,buffer.length);
			//fin.read(mRegisterImage);
			//debugMessage("HEy fiel\n");
			//Toast.makeText(getApplicationContext(), "Hey cd File", Toast.LENGTH_SHORT).show();
			//fin.close();
		}
		catch (Exception e)
		{
			debugMessage("Exception when writing file" + fileName);
		}

		/*try {

			myFile = new File("/sdcard/Lasyst/"+fileName);
			FileInputStream fis = new FileInputStream(myFile);
			DataInputStream in = new DataInputStream(fis);
			BufferedReader br =
					new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				myData = myData + strLine;
			}
			mRegisterImage = myData.getBytes();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		//File file = new File(path);

		myFile = new File("/sdcard/Lasyst/"+fileName);
		//Toast.makeText(getApplicationContext(),fileName, Toast.LENGTH_SHORT).show();
		int size = (int) myFile.length();
		//byte[] bytes = new byte[size];
		mRegisterImage = new byte[size];
		try
		{
			BufferedInputStream buf = new BufferedInputStream(new FileInputStream(myFile));
			buf.read(mRegisterImage, 0, mRegisterImage.length);
			buf.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    public void SGFingerPresentCallback (){
		autoOn.stop();
		fingerDetectedHandler.sendMessage(new Message());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
	public void CaptureFingerPrint(){
		long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;
		this.mCheckBoxMatched.setChecked(false);
	    byte[] buffer = new byte[mImageWidth*mImageHeight];
	    dwTimeStart = System.currentTimeMillis();
	    //long result = sgfplib.GetImage(buffer);
	    long result = sgfplib.GetImageEx(buffer, IMAGE_CAPTURE_TIMEOUT_MS,IMAGE_CAPTURE_QUALITY);

	    String NFIQString;
	    if (this.mToggleButtonNFIQ.isChecked()) {
	    	long nfiq = sgfplib.ComputeNFIQ(buffer, mImageWidth, mImageHeight);
	    	//long nfiq = sgfplib.ComputeNFIQEx(buffer, mImageWidth, mImageHeight,500);
	    	NFIQString = "NFIQ=" + nfiq;
	    }
	    else
		{
			NFIQString = "";
		}
		DumpFile("capture"+i+".raw", buffer);
		i++;
	    dwTimeEnd = System.currentTimeMillis();
	    dwTimeElapsed = dwTimeEnd-dwTimeStart;
	    debugMessage("getImageEx(10000,50) ret:" + result + " [" + dwTimeElapsed + "ms]" + NFIQString +"\n");
		mTextViewResult.setText("getImageEx(10000,50) ret: " + result + " [" + dwTimeElapsed + "ms] " + NFIQString + "\n");
	    mImageViewFingerprint.setImageBitmap(this.toGrayscale(buffer));

		Bitmap bitmap = toGrayscale(buffer);
		//bitmapDrawable .getBitmap();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

		byte[] imageInByte = stream.toByteArray();
		DumpFile("capture.jpeg",imageInByte);
        buffer = null;
	}


    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    public void onClick(View v) {
		long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;
		if (v== mToggleButtonSmartCapture)
		{
			if(mToggleButtonSmartCapture.isChecked()){
	        	sgfplib.WriteData(SGFDxConstant.WRITEDATA_COMMAND_ENABLE_SMART_CAPTURE, (byte)1); //Enable Smart Capture
	       		this.mButtonSetBrightness0.setClickable(false);
        		this.mButtonSetBrightness100.setClickable(false);
        		this.mButtonSetBrightness0.setTextColor(getResources().getColor(android.R.color.black));
        		this.mButtonSetBrightness100.setTextColor(getResources().getColor(android.R.color.black));
			}
	        else {
	        	sgfplib.WriteData(SGFDxConstant.WRITEDATA_COMMAND_ENABLE_SMART_CAPTURE, (byte)0); //Disable Smart Capture
        		this.mButtonSetBrightness0.setClickable(true);
        		this.mButtonSetBrightness100.setClickable(true);
        		this.mButtonSetBrightness0.setTextColor(getResources().getColor(android.R.color.white));
        		this.mButtonSetBrightness100.setTextColor(getResources().getColor(android.R.color.white));
	        }
		}
		if (v== mToggleButtonCaptureModeN)
		{
			if(mToggleButtonCaptureModeN.isChecked())
	        	sgfplib.WriteData((byte)0, (byte)0); //Enable Mode N
	        else
	        	sgfplib.WriteData((byte)0, (byte)1); //Disable Mode N
		}
		if (v== mToggleButtonUSBBulkMode64)
		{
			if(mToggleButtonUSBBulkMode64.isChecked())
	        	sgfplib.WriteData(Constant.WRITEDATA_COMMAND_ENABLE_USB_MODE_64, (byte)1); //Enable 64byte USB bulk mode
	        else
	        	sgfplib.WriteData(Constant.WRITEDATA_COMMAND_ENABLE_USB_MODE_64, (byte)0); //Enable 4096byte USB bulk mode
		}
		if (v == this.mButtonReadSN){
			//Read Serial number
			byte[] szSerialNumber = new byte[15];
	        long result = sgfplib.ReadSerialNumber(szSerialNumber);
	        debugMessage("ReadSerialNumber() ret: " + result + " ["	+ new String(szSerialNumber) + "]\n");
			//Increment last byte and Write serial number      
	        //szSerialNumber[14] += 1;
	        //error = sgfplib.WriteSerialNumber(szSerialNumber);
	        szSerialNumber = null;
		}
        if (v == mButtonCapture) {
        	//DEBUG Log.d(TAG, "Pressed CAPTURE");
        	CaptureFingerPrint();
        }
        if (v == mToggleButtonAutoOn) {
			if(mToggleButtonAutoOn.isChecked()) {
				mAutoOnEnabled = true;
				autoOn.start(); //Enable Auto On
	        	DisableControls();
			}
	        else {
				mAutoOnEnabled = false;
	        	autoOn.stop(); //Disable Auto On
	        	EnableControls();
	        }

        }
        if (v == mButtonLed) {
        	this.mCheckBoxMatched.setChecked(false);
        	mLed = !mLed;
            dwTimeStart = System.currentTimeMillis();
            long result = sgfplib.SetLedOn(mLed);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("setLedOn(" + mLed +") ret:" + result + " [" + dwTimeElapsed + "ms]\n");
        	mTextViewResult.setText("setLedOn(" + mLed + ") ret: " + result + " [" + dwTimeElapsed + "ms]\n");
        }
        if (v == mSDKTest) {
        	//
        	//
        	// SDKTest();
        }
        if (v == this.mButtonRegister) {
        /*	//DEBUG Log.d(TAG, "Clicked REGISTER");
			debugMessage("Clicked REGISTER\n");
            if (mRegisterImage != null)
            	mRegisterImage = null;
            mRegisterImage = new byte[mImageWidth*mImageHeight];

			this.mCheckBoxMatched.setChecked(false);
            dwTimeStart = System.currentTimeMillis();
    	    long result = sgfplib.GetImageEx(mRegisterImage, IMAGE_CAPTURE_TIMEOUT_MS,IMAGE_CAPTURE_QUALITY);
            DumpFile("register.raw", mRegisterImage);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("GetImageEx() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
    	    mImageViewFingerprint.setImageBitmap(this.toGrayscale(mRegisterImage));
            dwTimeStart = System.currentTimeMillis();
            result = sgfplib.SetTemplateFormat(SecuGen.FDxSDKPro.SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("SetTemplateFormat(ISO19794) ret:" + result + " [" + dwTimeElapsed + "ms]\n");

            int quality1[] = new int[1];
            result = sgfplib.GetImageQuality(mImageWidth, mImageHeight, mRegisterImage, quality1);
            debugMessage("GetImageQuality() ret:" +  result + "quality [" + quality1[0] + "]\n");

            SGFingerInfo fpInfo = new SGFingerInfo();
            fpInfo.FingerNumber = 1;
            fpInfo.ImageQuality = quality1[0];
            fpInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
            fpInfo.ViewNumber = 1;

            for (int i=0; i< mRegisterTemplate.length; ++i)
            	mRegisterTemplate[i] = 0;
            dwTimeStart = System.currentTimeMillis();
            result = sgfplib.CreateTemplate(fpInfo, mRegisterImage, mRegisterTemplate);
            DumpFile("register.min", mRegisterTemplate);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("CreateTemplate() ret:" + result + " [" + dwTimeElapsed + "ms]\n");

            int[] size = new int[1];
            result = sgfplib.GetTemplateSize(mRegisterTemplate, size);
			debugMessage("GetTemplateSize() ret:" + result + " size [" + size[0] + "]\n");


            mImageViewRegister.setImageBitmap(this.toGrayscale(mRegisterImage));
			//mTextViewResult.setText("Click Verify");

			mRegisterImage = null;
    	    fpInfo = null;
			mRegisterTemplate=null;
        */}
        if (v == this.mButtonMatch) {

        	//DEBUG Log.d(TAG, "Clicked MATCH");
            //debugMessage("Clicked MATCH\n");
            if (mVerifyImage != null)
            	mVerifyImage = null;
            mVerifyImage = new byte[mImageWidth*mImageHeight];
            dwTimeStart = System.currentTimeMillis();
    	    long result = sgfplib.GetImageEx(mVerifyImage, IMAGE_CAPTURE_TIMEOUT_MS,IMAGE_CAPTURE_QUALITY);

			//DumpFile("verify.raw", mVerifyImage);

			dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("GetImageEx() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
    	   // mImageViewFingerprint.setImageBitmap(this.toGrayscale(mVerifyImage));
    	    mImageViewVerify.setImageBitmap(this.toGrayscale(mVerifyImage));
            dwTimeStart = System.currentTimeMillis();
            result = sgfplib.SetTemplateFormat(SecuGen.FDxSDKPro.SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("SetTemplateFormat(ISO19794) ret:" +  result + " [" + dwTimeElapsed + "ms]\n");

			int[] quality = new int[1];
            result = sgfplib.GetImageQuality(mImageWidth, mImageHeight, mVerifyImage, quality);
            debugMessage("GetImageQuality() ret:" +  result + "quality [" + quality[0] + "]\n");

            SGFingerInfo fpInfo = new SGFingerInfo();
            fpInfo.FingerNumber = 0x00;
            fpInfo.ImageQuality = quality[0];
            fpInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
            fpInfo.ViewNumber = 1;


            for (int i=0; i< mVerifyTemplate.length; ++i)
            	mVerifyTemplate[i] = 0;
            dwTimeStart = System.currentTimeMillis();
            result = sgfplib.CreateTemplate(fpInfo, mVerifyImage, mVerifyTemplate);
            //DumpFile("verify.min", mVerifyTemplate);
			dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("CreateTemplate() ret:" + result+ " [" + dwTimeElapsed + "ms]\n");

            int[] size = new int[1];
            result = sgfplib.GetTemplateSize(mVerifyTemplate, size);
            debugMessage("GetTemplateSize() ret:" + result+ " size [" + size[0] + "]\n");

            boolean[] matched = new boolean[1];
            dwTimeStart = System.currentTimeMillis();

			//////////

			int arraySize=3;
			while(index<arraySize) {
				ReadFile("capture"+index+".raw");
				//Toast.makeText(getApplicationContext(), "hey", Toast.LENGTH_SHORT).show();
				//Toast.makeText(getApplicationContext(), index, Toast.LENGTH_SHORT).show();
				mImageViewRegister.setImageBitmap(this.toGrayscale(mRegisterImage));
				int[] quality1 = new int[1];
				result = sgfplib.GetImageQuality(mImageWidth, mImageHeight, mRegisterImage, quality1);

				//debugMessage("GetImageQuality() ret:" + result + "quality [" + quality1[0] + "]\n");

				/*SGFingerInfo fpInfo1 = new SGFingerInfo();
				fpInfo1.FingerNumber = 0x00;
				fpInfo1.ImageQuality = quality1[0];
				fpInfo1.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
				fpInfo1.ViewNumber = 1;
*/
				for (int i = 0; i < mRegisterTemplate.length; ++i)
					mRegisterTemplate[i] = 0;
				dwTimeStart = System.currentTimeMillis();
				result = sgfplib.CreateTemplate(fpInfo, mRegisterImage, mRegisterTemplate);
				DumpFile("capture"+index+".min", mRegisterTemplate);
				dwTimeEnd = System.currentTimeMillis();
				dwTimeElapsed = dwTimeEnd - dwTimeStart;
//				debugMessage("CreateTemplate() ret:" + result + " [" + dwTimeElapsed + "ms]\n");

				int[] size1 = new int[1];
				result = sgfplib.GetTemplateSize(mRegisterTemplate, size);
//				debugMessage("GetTemplateSize() ret:" + result + " size [" + size1[0] + "]\n");


				//mImageViewRegister.setImageBitmap(this.toGrayscale(mRegisterImage));
				//mTextViewResult.setText("Click Verify");
				//////////
				//mImageViewRegister.setImageBitmap(this.toGrayscale(mRegisterImage));
				result = sgfplib.MatchTemplate(mRegisterTemplate, mVerifyTemplate, SGFDxSecurityLevel.SL_NORMAL, matched);
				mImageViewRegister.setImageBitmap(this.toGrayscale(mRegisterImage));
				dwTimeEnd = System.currentTimeMillis();
				dwTimeElapsed = dwTimeEnd - dwTimeStart;
				debugMessage("MatchTemplate() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
				if (matched[0]) {
					mTextViewResult.setText("MATCHED!!\n");
					//this.mCheckBoxMatched.setChecked(true);
					debugMessage("MATCHED!!\n");
					break;
				}
				//mRegisterImage=null;
				//mRegisterTemplate=null;
				index++;
			}
			if (!matched[0])
			{
            	mTextViewResult.setText("NOT MATCHED!!");
            	this.mCheckBoxMatched.setChecked(false);
                debugMessage("NOT MATCHED!!\n");
            }
			//DumpFile("capture"+index+".jpg", mVerifyImage);
            mVerifyImage = null;
    	    fpInfo = null;
    	    matched = null;
		}
        if (v == this.mButtonSetBrightness0) {
        	this.sgfplib.SetBrightness(0);
        	debugMessage("SetBrightness(0)\n");
        }
        if (v == this.mButtonSetBrightness100) {
        	this.sgfplib.SetBrightness(100);
        	debugMessage("SetBrightness(100)\n");
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    public void run() {

    	//Log.d(TAG, "Enter run()");
        //ByteBuffer buffer = ByteBuffer.allocate(1);
        //UsbRequest request = new UsbRequest();
        //request.initialize(mSGUsbInterface.getConnection(), mEndpointBulk);
        //byte status = -1;
        while (true) {


            // queue a request on the interrupt endpoint
            //request.queue(buffer, 1);
            // send poll status command
          //  sendCommand(COMMAND_STATUS);
            // wait for status event
            /*
            if (mSGUsbInterface.getConnection().requestWait() == request) {
                byte newStatus = buffer.get(0);
                if (newStatus != status) {
                    Log.d(TAG, "got status " + newStatus);
                    status = newStatus;
                    if ((status & COMMAND_FIRE) != 0) {
                        // stop firing
                        sendCommand(COMMAND_STOP);
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            } else {
                Log.e(TAG, "requestWait failed, exiting");
                break;
            }
            */
        }
    }
}