package com.ds.bluetooth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.R.anim;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.bluetoothUtil.BluetoothClientService;
import com.ds.bluetoothUtil.BluetoothTools;
import com.ds.bluetoothUtil.TransmitBean;
/**
 * �ͻ���
 * @author miaowei
 *
 */
public class ClientActivity extends Activity {

	private TextView serversText;
	private EditText chatEditText;
	private EditText sendEditText;
	private Button sendBtn;
	private Button startSearchBtn;
	private Button selectDeviceBtn;
	/**
	 * ������ʾ��������
	 */
	private ListView listView;
	private ArrayAdapter arrayAdapter;
	/**
	 * �����������
	 */
	private List<String> deviceNameList = new ArrayList<String>();
	
	private List<BluetoothDevice> deviceListT = new ArrayList<BluetoothDevice>();
	
	private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
	
	
	
	//�㲥������
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		
		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (BluetoothTools.ACTION_NOT_FOUND_SERVER.equals(action)) {
				//δ�����豸
				serversText.append("not found device\r\n");
				
			} else if (BluetoothTools.ACTION_FOUND_DEVICE.equals(action)) {
				//��ȡ���豸����
				BluetoothDevice device = (BluetoothDevice)intent.getExtras().get(BluetoothTools.DEVICE);
				deviceList.add(device);
				
				serversText.append(device.getName()+" \r\n");
				
				
			} else if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)) {
				//���ӳɹ�
				serversText.append("���ӳɹ�");
				sendBtn.setEnabled(true);
				
			} else if (BluetoothTools.ACTION_DATA_TO_GAME.equals(action)) {
				//��������
				TransmitBean data = (TransmitBean)intent.getExtras().getSerializable(BluetoothTools.DATA);
				String msg = "from remote " + new Date().toLocaleString() + " :\r\n" + data.getMsg() + "\r\n";
				chatEditText.append(msg);
			
			} 
			//����Զ�������豸ʱ��ȡ���豸��MAC��ַ
			else if (BluetoothDevice.ACTION_FOUND.equals(action)) {

	              //����Զ�������������Ķ���ȡ��
	              BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	              
	              //String str= device.getName() + "|" + device.getAddress();
	              String str= device.getName();

	              if (deviceNameList.indexOf(str) == -1){// ��ֹ�ظ����

	            	  deviceNameList.add(str); // ��ȡ�豸���ƺ�mac��ַ
	            	  deviceListT.add(device);
	              }
	              //�𵽸��µ�Ч��
	              arrayAdapter.notifyDataSetChanged();

	           }
			
		}
	};
	
	
	@Override
	protected void onStart() {
		//����豸�б�
		deviceList.clear();
		//deviceListT.clear();
		//deviceNameList.clear();
		//������̨service
		Intent startService = new Intent(ClientActivity.this, BluetoothClientService.class);
		startService(startService);
		
		//ע��BoradcasrReceiver
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothTools.ACTION_NOT_FOUND_SERVER);
		intentFilter.addAction(BluetoothTools.ACTION_FOUND_DEVICE);
		intentFilter.addAction(BluetoothTools.ACTION_DATA_TO_GAME);
		intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);// ��BroadcastReceiver��ȡ���������
		registerReceiver(broadcastReceiver, intentFilter);
		
		super.onStart();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceListT);
		serversText = (TextView)findViewById(R.id.clientServersText);
		serversText.setVisibility(View.GONE);
		chatEditText = (EditText)findViewById(R.id.clientChatEditText);
		sendEditText = (EditText)findViewById(R.id.clientSendEditText);
		sendBtn = (Button)findViewById(R.id.clientSendMsgBtn);
		startSearchBtn = (Button)findViewById(R.id.startSearchBtn);
		selectDeviceBtn = (Button)findViewById(R.id.selectDeviceBtn);
		
		listView = (ListView)findViewById(R.id.listViewDevice);
		listView.setAdapter(arrayAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				Intent selectDeviceIntent = new Intent(BluetoothTools.ACTION_SELECTED_DEVICE);
				selectDeviceIntent.putExtra(BluetoothTools.DEVICE, deviceListT.get(arg2));
				sendBroadcast(selectDeviceIntent);
				
			}
			
			
			
		});
		
		sendBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//������Ϣ
				if ("".equals(sendEditText.getText().toString().trim())) {
					Toast.makeText(ClientActivity.this, "���벻��Ϊ��", Toast.LENGTH_SHORT).show();
				} else {
					//������Ϣ
					TransmitBean data = new TransmitBean();
					data.setMsg(sendEditText.getText().toString());
					Intent sendDataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_SERVICE);
					sendDataIntent.putExtra(BluetoothTools.DATA, data);
					sendBroadcast(sendDataIntent);
				}
			}
			});
		
		startSearchBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//��ʼ����
				Intent startSearchIntent = new Intent(BluetoothTools.ACTION_START_DISCOVERY);
				sendBroadcast(startSearchIntent);
				
			}
		});
		
		selectDeviceBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//ѡ���һ���豸
				Intent selectDeviceIntent = new Intent(BluetoothTools.ACTION_SELECTED_DEVICE);
				selectDeviceIntent.putExtra(BluetoothTools.DEVICE, deviceList.get(0));
				sendBroadcast(selectDeviceIntent);
			}
		});
	}

	@Override
	protected void onStop() {
		//�رպ�̨Service
		Intent startService = new Intent(BluetoothTools.ACTION_STOP_SERVICE);
		sendBroadcast(startService);
		
		unregisterReceiver(broadcastReceiver);
		super.onStop();
	}
}
